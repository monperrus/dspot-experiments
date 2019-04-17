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
package org.apache.hadoop.hbase.regionserver;


import HConstants.HBASE_CLIENT_RETRIES_NUMBER;
import HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD;
import KeyValue.LOWESTKEY;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FilterFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MiniHBaseCluster;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.fs.HFileSystem;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFileContext;
import org.apache.hadoop.hbase.io.hfile.HFileContextBuilder;
import org.apache.hadoop.hbase.io.hfile.HFileScanner;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.testclassification.RegionServerTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static BloomType.NONE;


/**
 * Test cases that ensure that file system level errors are bubbled up
 * appropriately to clients, rather than swallowed.
 */
@Category({ RegionServerTests.class, MediumTests.class })
public class TestFSErrorsExposed {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestFSErrorsExposed.class);

    private static final Logger LOG = LoggerFactory.getLogger(TestFSErrorsExposed.class);

    HBaseTestingUtility util = new HBaseTestingUtility();

    @Rule
    public TestName name = new TestName();

    /**
     * Injects errors into the pread calls of an on-disk file, and makes
     * sure those bubble up to the HFile scanner
     */
    @Test
    public void testHFileScannerThrowsErrors() throws IOException {
        Path hfilePath = new Path(new Path(getDataTestDir("internalScannerExposesErrors"), "regionname"), "familyname");
        HFileSystem hfs = ((HFileSystem) (util.getTestFileSystem()));
        TestFSErrorsExposed.FaultyFileSystem faultyfs = new TestFSErrorsExposed.FaultyFileSystem(hfs.getBackingFs());
        FileSystem fs = new HFileSystem(faultyfs);
        CacheConfig cacheConf = new CacheConfig(util.getConfiguration());
        HFileContext meta = new HFileContextBuilder().withBlockSize((2 * 1024)).build();
        StoreFileWriter writer = new StoreFileWriter.Builder(util.getConfiguration(), cacheConf, hfs).withOutputDir(hfilePath).withFileContext(meta).build();
        TestHStoreFile.writeStoreFile(writer, Bytes.toBytes("cf"), Bytes.toBytes("qual"));
        HStoreFile sf = new HStoreFile(fs, writer.getPath(), util.getConfiguration(), cacheConf, NONE, true);
        sf.initReader();
        StoreFileReader reader = sf.getReader();
        HFileScanner scanner = reader.getScanner(false, true);
        TestFSErrorsExposed.FaultyInputStream inStream = faultyfs.inStreams.get(0).get();
        Assert.assertNotNull(inStream);
        scanner.seekTo();
        // Do at least one successful read
        Assert.assertTrue(scanner.next());
        faultyfs.startFaults();
        try {
            int scanned = 0;
            while (scanner.next()) {
                scanned++;
            } 
            Assert.fail("Scanner didn't throw after faults injected");
        } catch (IOException ioe) {
            TestFSErrorsExposed.LOG.info("Got expected exception", ioe);
            Assert.assertTrue(ioe.getMessage().contains("Fault"));
        }
        reader.close(true);// end of test so evictOnClose

    }

    /**
     * Injects errors into the pread calls of an on-disk file, and makes
     * sure those bubble up to the StoreFileScanner
     */
    @Test
    public void testStoreFileScannerThrowsErrors() throws IOException {
        Path hfilePath = new Path(new Path(getDataTestDir("internalScannerExposesErrors"), "regionname"), "familyname");
        HFileSystem hfs = ((HFileSystem) (util.getTestFileSystem()));
        TestFSErrorsExposed.FaultyFileSystem faultyfs = new TestFSErrorsExposed.FaultyFileSystem(hfs.getBackingFs());
        HFileSystem fs = new HFileSystem(faultyfs);
        CacheConfig cacheConf = new CacheConfig(util.getConfiguration());
        HFileContext meta = new HFileContextBuilder().withBlockSize((2 * 1024)).build();
        StoreFileWriter writer = new StoreFileWriter.Builder(util.getConfiguration(), cacheConf, hfs).withOutputDir(hfilePath).withFileContext(meta).build();
        TestHStoreFile.writeStoreFile(writer, Bytes.toBytes("cf"), Bytes.toBytes("qual"));
        HStoreFile sf = new HStoreFile(fs, writer.getPath(), util.getConfiguration(), cacheConf, NONE, true);
        List<StoreFileScanner> scanners = // 0 is passed as readpoint because this test operates on HStoreFile directly
        StoreFileScanner.getScannersForStoreFiles(Collections.singletonList(sf), false, true, false, false, 0);
        KeyValueScanner scanner = scanners.get(0);
        TestFSErrorsExposed.FaultyInputStream inStream = faultyfs.inStreams.get(0).get();
        Assert.assertNotNull(inStream);
        scanner.seek(LOWESTKEY);
        // Do at least one successful read
        Assert.assertNotNull(scanner.next());
        faultyfs.startFaults();
        try {
            int scanned = 0;
            while ((scanner.next()) != null) {
                scanned++;
            } 
            Assert.fail("Scanner didn't throw after faults injected");
        } catch (IOException ioe) {
            TestFSErrorsExposed.LOG.info("Got expected exception", ioe);
            Assert.assertTrue(ioe.getMessage().contains("Could not iterate"));
        }
        scanner.close();
    }

    /**
     * Cluster test which starts a region server with a region, then
     * removes the data from HDFS underneath it, and ensures that
     * errors are bubbled to the client.
     */
    @Test
    public void testFullSystemBubblesFSErrors() throws Exception {
        // We won't have an error if the datanode is not there if we use short circuit
        // it's a known 'feature'.
        Assume.assumeTrue((!(util.isReadShortCircuitOn())));
        try {
            // Make it fail faster.
            util.getConfiguration().setInt(HBASE_CLIENT_RETRIES_NUMBER, 1);
            util.getConfiguration().setInt(HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD, 90000);
            util.getConfiguration().setInt("hbase.lease.recovery.timeout", 10000);
            util.getConfiguration().setInt("hbase.lease.recovery.dfs.timeout", 1000);
            util.startMiniCluster(1);
            final TableName tableName = TableName.valueOf(name.getMethodName());
            byte[] fam = Bytes.toBytes("fam");
            Admin admin = util.getAdmin();
            HTableDescriptor desc = new HTableDescriptor(tableName);
            desc.addFamily(new HColumnDescriptor(fam).setMaxVersions(1).setBlockCacheEnabled(false));
            admin.createTable(desc);
            // Make a new Configuration so it makes a new connection that has the
            // above configuration on it; else we use the old one w/ 10 as default.
            try (Table table = util.getConnection().getTable(tableName)) {
                // Load some data
                util.loadTable(table, fam, false);
                util.flush();
                util.countRows(table);
                // Kill the DFS cluster
                util.getDFSCluster().shutdownDataNodes();
                try {
                    util.countRows(table);
                    Assert.fail("Did not fail to count after removing data");
                } catch (Exception e) {
                    TestFSErrorsExposed.LOG.info("Got expected error", e);
                    Assert.assertTrue(e.getMessage().contains("Could not seek"));
                }
            }
            // Restart data nodes so that HBase can shut down cleanly.
            util.getDFSCluster().restartDataNodes();
        } finally {
            MiniHBaseCluster cluster = util.getMiniHBaseCluster();
            if (cluster != null)
                cluster.killAll();

            util.shutdownMiniCluster();
        }
    }

    static class FaultyFileSystem extends FilterFileSystem {
        List<SoftReference<TestFSErrorsExposed.FaultyInputStream>> inStreams = new ArrayList<>();

        public FaultyFileSystem(FileSystem testFileSystem) {
            super(testFileSystem);
        }

        @Override
        public FSDataInputStream open(Path p, int bufferSize) throws IOException {
            FSDataInputStream orig = fs.open(p, bufferSize);
            TestFSErrorsExposed.FaultyInputStream faulty = new TestFSErrorsExposed.FaultyInputStream(orig);
            inStreams.add(new SoftReference<>(faulty));
            return faulty;
        }

        /**
         * Starts to simulate faults on all streams opened so far
         */
        public void startFaults() {
            for (SoftReference<TestFSErrorsExposed.FaultyInputStream> is : inStreams) {
                is.get().startFaults();
            }
        }
    }

    static class FaultyInputStream extends FSDataInputStream {
        boolean faultsStarted = false;

        public FaultyInputStream(InputStream in) throws IOException {
            super(in);
        }

        public void startFaults() {
            faultsStarted = true;
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length) throws IOException {
            injectFault();
            return ((org.apache.hadoop.fs.PositionedReadable) (in)).read(position, buffer, offset, length);
        }

        private void injectFault() throws IOException {
            if (faultsStarted) {
                throw new IOException("Fault injected");
            }
        }
    }
}
