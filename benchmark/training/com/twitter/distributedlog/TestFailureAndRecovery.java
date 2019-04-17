/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twitter.distributedlog;


import BKException.Code.NotEnoughBookiesException;
import com.twitter.distributedlog.exceptions.BKTransmitException;
import com.twitter.distributedlog.io.Abortables;
import com.twitter.distributedlog.util.FutureUtils;
import org.apache.bookkeeper.proto.BookieServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestFailureAndRecovery extends TestDistributedLogBase {
    static final Log LOG = LogFactory.getLog(TestFailureAndRecovery.class);

    @Test(timeout = 60000)
    public void testSimpleRecovery() throws Exception {
        DLMTestUtil.BKLogPartitionWriteHandlerAndClients bkdlmAndClients = createNewBKDLM(TestDistributedLogBase.conf, "distrlog-simplerecovery");
        BKLogSegmentWriter out = bkdlmAndClients.getWriteHandler().startLogSegment(1);
        long txid = 1;
        for (long i = 1; i <= 100; i++) {
            LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
            out.write(op);
            if ((i % 10) == 0) {
                FutureUtils.result(out.flushAndCommit());
            }
        }
        FutureUtils.result(out.flushAndCommit());
        Abortables.abort(out, false);
        FutureUtils.result(out.asyncClose());
        Assert.assertNull(zkc.exists(bkdlmAndClients.getWriteHandler().completedLedgerZNode(1, 100, out.getLogSegmentSequenceNumber()), false));
        Assert.assertNotNull(zkc.exists(bkdlmAndClients.getWriteHandler().inprogressZNode(out.getLogSegmentId(), 1, out.getLogSegmentSequenceNumber()), false));
        FutureUtils.result(bkdlmAndClients.getWriteHandler().recoverIncompleteLogSegments());
        Assert.assertNotNull(zkc.exists(bkdlmAndClients.getWriteHandler().completedLedgerZNode(1, 100, out.getLogSegmentSequenceNumber()), false));
        Assert.assertNull(zkc.exists(bkdlmAndClients.getWriteHandler().inprogressZNode(out.getLogSegmentId(), 1, out.getLogSegmentSequenceNumber()), false));
    }

    /**
     * Test that if enough bookies fail to prevent an ensemble,
     * writes the bookkeeper will fail. Test that when once again
     * an ensemble is available, it can continue to write.
     */
    @Test(timeout = 60000)
    public void testAllBookieFailure() throws Exception {
        BookieServer bookieToFail = TestDistributedLogBase.bkutil.newBookie();
        BookieServer replacementBookie = null;
        try {
            int ensembleSize = (TestDistributedLogBase.numBookies) + 1;
            Assert.assertEquals("Begin: New bookie didn't start", ensembleSize, TestDistributedLogBase.bkutil.checkBookiesUp(ensembleSize, 10));
            // ensure that the journal manager has to use all bookies,
            // so that a failure will fail the journal manager
            DistributedLogConfiguration conf = new DistributedLogConfiguration();
            conf.setEnsembleSize(ensembleSize);
            conf.setWriteQuorumSize(ensembleSize);
            conf.setAckQuorumSize(ensembleSize);
            long txid = 1;
            DLMTestUtil.BKLogPartitionWriteHandlerAndClients bkdlmAndClients = createNewBKDLM(conf, "distrlog-allbookiefailure");
            BKLogSegmentWriter out = bkdlmAndClients.getWriteHandler().startLogSegment(txid);
            for (long i = 1; i <= 3; i++) {
                LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
                out.write(op);
            }
            FutureUtils.result(out.flushAndCommit());
            bookieToFail.shutdown();
            Assert.assertEquals("New bookie didn't die", TestDistributedLogBase.numBookies, TestDistributedLogBase.bkutil.checkBookiesUp(TestDistributedLogBase.numBookies, 10));
            try {
                for (long i = 1; i <= 3; i++) {
                    LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
                    out.write(op);
                    txid++;
                }
                FutureUtils.result(out.flushAndCommit());
                Assert.fail("should not get to this stage");
            } catch (BKTransmitException bkte) {
                TestFailureAndRecovery.LOG.debug("Error writing to bookkeeper", bkte);
                Assert.assertEquals("Invalid exception message", NotEnoughBookiesException, bkte.getBKResultCode());
            }
            replacementBookie = TestDistributedLogBase.bkutil.newBookie();
            Assert.assertEquals("Replacement: New bookie didn't start", ((TestDistributedLogBase.numBookies) + 1), TestDistributedLogBase.bkutil.checkBookiesUp(((TestDistributedLogBase.numBookies) + 1), 10));
            out = bkdlmAndClients.getWriteHandler().startLogSegment(txid);
            for (long i = 1; i <= 3; i++) {
                LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
                out.write(op);
            }
            FutureUtils.result(out.flushAndCommit());
        } catch (Exception e) {
            TestFailureAndRecovery.LOG.error("Exception in test", e);
            throw e;
        } finally {
            if (replacementBookie != null) {
                replacementBookie.shutdown();
            }
            bookieToFail.shutdown();
            if ((TestDistributedLogBase.bkutil.checkBookiesUp(TestDistributedLogBase.numBookies, 30)) != (TestDistributedLogBase.numBookies)) {
                TestFailureAndRecovery.LOG.warn("Not all bookies from this test shut down, expect errors");
            }
        }
    }

    /**
     * Test that a BookKeeper JM can continue to work across the
     * failure of a bookie. This should be handled transparently
     * by bookkeeper.
     */
    @Test(timeout = 60000)
    public void testOneBookieFailure() throws Exception {
        BookieServer bookieToFail = TestDistributedLogBase.bkutil.newBookie();
        BookieServer replacementBookie = null;
        try {
            int ensembleSize = (TestDistributedLogBase.numBookies) + 1;
            Assert.assertEquals("New bookie didn't start", ensembleSize, TestDistributedLogBase.bkutil.checkBookiesUp(ensembleSize, 10));
            // ensure that the journal manager has to use all bookies,
            // so that a failure will fail the journal manager
            DistributedLogConfiguration conf = new DistributedLogConfiguration();
            conf.setEnsembleSize(ensembleSize);
            conf.setWriteQuorumSize(ensembleSize);
            conf.setAckQuorumSize(ensembleSize);
            long txid = 1;
            DLMTestUtil.BKLogPartitionWriteHandlerAndClients bkdlmAndClients = createNewBKDLM(conf, "distrlog-onebookiefailure");
            BKLogSegmentWriter out = bkdlmAndClients.getWriteHandler().startLogSegment(txid);
            for (long i = 1; i <= 3; i++) {
                LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
                out.write(op);
            }
            FutureUtils.result(out.flushAndCommit());
            replacementBookie = TestDistributedLogBase.bkutil.newBookie();
            Assert.assertEquals("replacement bookie didn't start", (ensembleSize + 1), TestDistributedLogBase.bkutil.checkBookiesUp((ensembleSize + 1), 10));
            bookieToFail.shutdown();
            Assert.assertEquals("New bookie didn't die", ensembleSize, TestDistributedLogBase.bkutil.checkBookiesUp(ensembleSize, 10));
            for (long i = 1; i <= 3; i++) {
                LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
                out.write(op);
            }
            FutureUtils.result(out.flushAndCommit());
        } catch (Exception e) {
            TestFailureAndRecovery.LOG.error("Exception in test", e);
            throw e;
        } finally {
            if (replacementBookie != null) {
                replacementBookie.shutdown();
            }
            bookieToFail.shutdown();
            if ((TestDistributedLogBase.bkutil.checkBookiesUp(TestDistributedLogBase.numBookies, 30)) != (TestDistributedLogBase.numBookies)) {
                TestFailureAndRecovery.LOG.warn("Not all bookies from this test shut down, expect errors");
            }
        }
    }

    @Test(timeout = 60000)
    public void testRecoveryEmptyLedger() throws Exception {
        DLMTestUtil.BKLogPartitionWriteHandlerAndClients bkdlmAndClients = createNewBKDLM(TestDistributedLogBase.conf, "distrlog-recovery-empty-ledger");
        BKLogSegmentWriter out = bkdlmAndClients.getWriteHandler().startLogSegment(1);
        long txid = 1;
        for (long i = 1; i <= 100; i++) {
            LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
            out.write(op);
            if ((i % 10) == 0) {
                FutureUtils.result(out.flushAndCommit());
            }
        }
        FutureUtils.result(out.flushAndCommit());
        FutureUtils.result(out.asyncClose());
        bkdlmAndClients.getWriteHandler().completeAndCloseLogSegment(out.getLogSegmentSequenceNumber(), out.getLogSegmentId(), 1, 100, 100);
        Assert.assertNotNull(zkc.exists(bkdlmAndClients.getWriteHandler().completedLedgerZNode(1, 100, out.getLogSegmentSequenceNumber()), false));
        BKLogSegmentWriter outEmpty = bkdlmAndClients.getWriteHandler().startLogSegment(101);
        Abortables.abort(outEmpty, false);
        Assert.assertNull(zkc.exists(bkdlmAndClients.getWriteHandler().completedLedgerZNode(101, 101, outEmpty.getLogSegmentSequenceNumber()), false));
        Assert.assertNotNull(zkc.exists(bkdlmAndClients.getWriteHandler().inprogressZNode(outEmpty.getLogSegmentId(), 101, outEmpty.getLogSegmentSequenceNumber()), false));
        FutureUtils.result(bkdlmAndClients.getWriteHandler().recoverIncompleteLogSegments());
        Assert.assertNull(zkc.exists(bkdlmAndClients.getWriteHandler().inprogressZNode(outEmpty.getLogSegmentId(), outEmpty.getLogSegmentSequenceNumber(), 101), false));
        Assert.assertNotNull(zkc.exists(bkdlmAndClients.getWriteHandler().completedLedgerZNode(101, 101, outEmpty.getLogSegmentSequenceNumber()), false));
    }

    @Test(timeout = 60000)
    public void testRecoveryAPI() throws Exception {
        DistributedLogManager dlm = createNewDLM(TestDistributedLogBase.conf, "distrlog-recovery-api");
        BKSyncLogWriter out = ((BKSyncLogWriter) (dlm.startLogSegmentNonPartitioned()));
        long txid = 1;
        for (long i = 1; i <= 100; i++) {
            LogRecord op = DLMTestUtil.getLogRecordInstance((txid++));
            out.write(op);
            if ((i % 10) == 0) {
                out.setReadyToFlush();
                out.flushAndSync();
            }
        }
        BKLogSegmentWriter perStreamLogWriter = out.getCachedLogWriter();
        out.setReadyToFlush();
        out.flushAndSync();
        out.abort();
        BKLogWriteHandler blplm1 = createWriteHandler(true);
        Assert.assertNull(zkc.exists(blplm1.completedLedgerZNode(1, 100, perStreamLogWriter.getLogSegmentSequenceNumber()), false));
        Assert.assertNotNull(zkc.exists(blplm1.inprogressZNode(perStreamLogWriter.getLogSegmentId(), 1, perStreamLogWriter.getLogSegmentSequenceNumber()), false));
        dlm.recover();
        Assert.assertNotNull(zkc.exists(blplm1.completedLedgerZNode(1, 100, perStreamLogWriter.getLogSegmentSequenceNumber()), false));
        Assert.assertNull(zkc.exists(blplm1.inprogressZNode(perStreamLogWriter.getLogSegmentId(), 1, perStreamLogWriter.getLogSegmentSequenceNumber()), false));
        FutureUtils.result(blplm1.asyncClose());
        Assert.assertEquals(100, dlm.getLogRecordCount());
        dlm.close();
    }
}
