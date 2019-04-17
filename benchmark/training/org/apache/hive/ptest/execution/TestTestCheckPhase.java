/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hive.ptest.execution;


import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;


public class TestTestCheckPhase extends AbstractTestPhase {
    private TestCheckPhase phase;

    @Test
    public void testNoTests() throws Exception {
        URL url = this.getClass().getResource("/HIVE-9377.1.patch");
        File patchFile = new File(url.getFile());
        Set<String> addedTests = new HashSet<String>();
        phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
        phase.execute();
        Assert.assertEquals(addedTests.size(), 0);
    }

    @Test
    public void testJavaTests() throws Exception {
        URL url = this.getClass().getResource("/HIVE-10761.6.patch");
        File patchFile = new File(url.getFile());
        Set<String> addedTests = new HashSet<String>();
        phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
        phase.execute();
        Assert.assertEquals(addedTests.size(), 3);
        Assert.assertTrue(addedTests.contains("TestCodahaleMetrics.java"));
        Assert.assertTrue(addedTests.contains("TestMetaStoreMetrics.java"));
        Assert.assertTrue(addedTests.contains("TestLegacyMetrics.java"));
    }

    @Test
    public void testQTests() throws Exception {
        URL url = this.getClass().getResource("/HIVE-11271.4.patch");
        File patchFile = new File(url.getFile());
        Set<String> addedTests = new HashSet<String>();
        phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
        phase.execute();
        Assert.assertEquals(addedTests.size(), 1);
        Assert.assertTrue(addedTests.contains("unionall_unbalancedppd.q"));
    }

    @Test
    public void testRemoveTest() throws Exception {
        URL url = this.getClass().getResource("/remove-test.patch");
        File patchFile = new File(url.getFile());
        Set<String> addedTests = new HashSet<String>();
        phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
        phase.execute();
        Assert.assertEquals(addedTests.size(), 0);
    }

    @Test
    public void testSamePatchMultipleTimes() throws Exception {
        int executions = 0;
        try {
            URL url = this.getClass().getResource("/HIVE-19077.1.patch");
            File patchFile = new File(url.getFile());
            Set<String> addedTests = new HashSet<String>();
            phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
            phase.execute();
            executions++;
            phase = new TestCheckPhase(hostExecutors, localCommandFactory, templateDefaults, url.toString(), patchFile, logger, addedTests);
            phase.execute();
            executions++;
            Assert.fail("Should've thrown exception");
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains(("HIVE-19077.1.patch was found in seen patch url's cache " + "and a test was probably run already on it. Aborting...")));
        }
        Assert.assertEquals(1, executions);
    }
}
