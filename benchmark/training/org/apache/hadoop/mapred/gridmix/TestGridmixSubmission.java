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
package org.apache.hadoop.mapred.gridmix;


import ExitUtil.ExitException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.test.GenericTestUtils;
import org.apache.hadoop.tools.rumen.JobStoryProducer;
import org.apache.hadoop.util.ExitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static GridmixJobSubmissionPolicy.REPLAY;
import static GridmixJobSubmissionPolicy.STRESS;


public class TestGridmixSubmission extends CommonJobTest {
    private static File inSpace = new File((((((("src" + (File.separator)) + "test") + (File.separator)) + "resources") + (File.separator)) + "data"));

    static {
        GenericTestUtils.setLogLevel(LoggerFactory.getLogger("org.apache.hadoop.mapred.gridmix"), Level.DEBUG);
    }

    /**
     * Tests the reading of traces in GridMix3. These traces are generated by
     * Rumen and are in the JSON format. The traces can optionally be compressed
     * and uncompressed traces can also be passed to GridMix3 via its standard
     * input stream. The testing is effected via JUnit assertions.
     *
     * @throws Exception
     * 		if there was an error.
     */
    @Test(timeout = 20000)
    public void testTraceReader() throws Exception {
        Configuration conf = new Configuration();
        FileSystem lfs = FileSystem.getLocal(conf);
        Path rootInputDir = new Path(System.getProperty("src.test.data"));
        rootInputDir = rootInputDir.makeQualified(lfs.getUri(), lfs.getWorkingDirectory());
        Path rootTempDir = new Path(System.getProperty("test.build.data", System.getProperty("java.io.tmpdir")), "testTraceReader");
        rootTempDir = rootTempDir.makeQualified(lfs.getUri(), lfs.getWorkingDirectory());
        Path inputFile = new Path(rootInputDir, "wordcount.json.gz");
        Path tempFile = new Path(rootTempDir, "gridmix3-wc.json");
        InputStream origStdIn = System.in;
        InputStream tmpIs = null;
        try {
            CommonJobTest.DebugGridmix dgm = new CommonJobTest.DebugGridmix();
            JobStoryProducer jsp = dgm.createJobStoryProducer(inputFile.toString(), conf);
            CommonJobTest.LOG.info("Verifying JobStory from compressed trace...");
            verifyWordCountJobStory(jsp.getNextJob());
            expandGzippedTrace(lfs, inputFile, tempFile);
            jsp = dgm.createJobStoryProducer(tempFile.toString(), conf);
            CommonJobTest.LOG.info("Verifying JobStory from uncompressed trace...");
            verifyWordCountJobStory(jsp.getNextJob());
            tmpIs = lfs.open(tempFile);
            System.setIn(tmpIs);
            CommonJobTest.LOG.info("Verifying JobStory from trace in standard input...");
            jsp = dgm.createJobStoryProducer("-", conf);
            verifyWordCountJobStory(jsp.getNextJob());
        } finally {
            System.setIn(origStdIn);
            if (tmpIs != null) {
                tmpIs.close();
            }
            lfs.delete(rootTempDir, true);
        }
    }

    @Test(timeout = 500000)
    public void testReplaySubmit() throws Exception {
        CommonJobTest.policy = REPLAY;
        CommonJobTest.LOG.info((" Replay started at " + (System.currentTimeMillis())));
        doSubmission(null, false);
        CommonJobTest.LOG.info((" Replay ended at " + (System.currentTimeMillis())));
    }

    @Test(timeout = 500000)
    public void testStressSubmit() throws Exception {
        CommonJobTest.policy = STRESS;
        CommonJobTest.LOG.info((" Stress started at " + (System.currentTimeMillis())));
        doSubmission(null, false);
        CommonJobTest.LOG.info((" Stress ended at " + (System.currentTimeMillis())));
    }

    // test empty request should be hint message
    @Test(timeout = 100000)
    public void testMain() throws Exception {
        SecurityManager securityManager = System.getSecurityManager();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(bytes);
        final PrintStream oldOut = System.out;
        System.setErr(out);
        ExitUtil.disableSystemExit();
        try {
            String[] argv = new String[0];
            main(argv);
        } catch (ExitUtil e) {
            assertExceptionContains(ExitUtil.EXIT_EXCEPTION_MESSAGE, e);
            ExitUtil.resetFirstExitException();
        } finally {
            System.setErr(oldOut);
            System.setSecurityManager(securityManager);
        }
        String print = bytes.toString();
        // should be printed tip in std error stream
        Assert.assertTrue(print.contains("Usage: gridmix [-generate <MiB>] [-users URI] [-Dname=value ...] <iopath> <trace>"));
        Assert.assertTrue(print.contains("e.g. gridmix -generate 100m foo -"));
    }
}
