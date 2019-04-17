/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.management.internal.cli.commands;


import Result.Status.ERROR;
import SizeExportLogsFunction.Args;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.internal.cache.InternalCacheForClientAccess;
import org.apache.geode.management.ManagementException;
import org.apache.geode.management.internal.cli.result.CommandResult;
import org.apache.geode.management.internal.cli.util.BytesToString;
import org.apache.geode.test.junit.categories.GfshTest;
import org.apache.geode.test.junit.categories.LoggingTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;


@Category({ GfshTest.class, LoggingTest.class })
public class ExportLogsCommandTest {
    @Test
    public void parseSize_sizeWithUnit_shouldReturnSize() {
        assertThat(ExportLogsCommand.parseSize("1000m")).isEqualTo(1000);
    }

    @Test
    public void parseSize_sizeWithoutUnit_shouldReturnSize() {
        assertThat(ExportLogsCommand.parseSize("1000")).isEqualTo(1000);
    }

    @Test
    public void parseByteMultiplier_sizeWithoutUnit_shouldReturnDefaultUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000")).isEqualTo(ExportLogsCommand.MEGABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_k_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000k")).isEqualTo(ExportLogsCommand.KILOBYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_m_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000m")).isEqualTo(ExportLogsCommand.MEGABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_g_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000g")).isEqualTo(ExportLogsCommand.GIGABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_t_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000t")).isEqualTo(ExportLogsCommand.TERABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_K_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000K")).isEqualTo(ExportLogsCommand.KILOBYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_M_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000M")).isEqualTo(ExportLogsCommand.MEGABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_G_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000G")).isEqualTo(ExportLogsCommand.GIGABYTE);
    }

    @Test
    public void parseByteMultiplier_sizeWith_T_shouldReturnUnit() {
        assertThat(ExportLogsCommand.parseByteMultiplier("1000T")).isEqualTo(ExportLogsCommand.TERABYTE);
    }

    @Test
    public void parseByteMultiplier_illegalUnit_shouldThrow() {
        assertThatThrownBy(() -> ExportLogsCommand.parseByteMultiplier("1000q")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseSize_garbage_shouldThrow() {
        assertThatThrownBy(() -> ExportLogsCommand.parseSize("bizbap")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseByteMultiplier_garbage_shouldThrow() {
        assertThatThrownBy(() -> ExportLogsCommand.parseByteMultiplier("bizbap")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseSizeLimit_sizeWithoutUnit_shouldReturnMegabytesSize() {
        ExportLogsCommand exportCmd = new ExportLogsCommand();
        assertThat(exportCmd.parseFileSizeLimit("1000")).isEqualTo((1000 * (ExportLogsCommand.MEGABYTE)));
    }

    @Test
    public void parseSizeLimit_sizeWith_K_shouldReturnKilobytesSize() {
        ExportLogsCommand exportCmd = new ExportLogsCommand();
        assertThat(exportCmd.parseFileSizeLimit("1000k")).isEqualTo((1000 * (ExportLogsCommand.KILOBYTE)));
    }

    @Test
    public void parseSizeLimit_sizeWith_M_shouldReturnMegabytesSize() {
        ExportLogsCommand exportCmd = new ExportLogsCommand();
        assertThat(exportCmd.parseFileSizeLimit("1000m")).isEqualTo((1000 * (ExportLogsCommand.MEGABYTE)));
    }

    @Test
    public void parseSizeLimit_sizeWith_G_shouldReturnMegabytesSize() {
        ExportLogsCommand exportCmd = new ExportLogsCommand();
        assertThat(exportCmd.parseFileSizeLimit("1000g")).isEqualTo((1000 * (ExportLogsCommand.GIGABYTE)));
    }

    @Test
    public void parseSizeLimit_sizeWith_T_shouldReturnMegabytesSize() {
        ExportLogsCommand exportCmd = new ExportLogsCommand();
        assertThat(exportCmd.parseFileSizeLimit("1000t")).isEqualTo((1000 * (ExportLogsCommand.TERABYTE)));
    }

    @Test
    public void testTotalEstimateSizeExceedsLocatorAvailableDisk() throws Exception {
        final InternalCache mockCache = Mockito.mock(InternalCache.class);
        final InternalCacheForClientAccess mockCacheFilter = Mockito.mock(InternalCacheForClientAccess.class);
        Mockito.when(mockCache.getCacheForProcessingClientRequests()).thenReturn(mockCacheFilter);
        final ExportLogsCommand realCmd = new ExportLogsCommand();
        ExportLogsCommand spyCmd = Mockito.spy(realCmd);
        String start = null;
        String end = null;
        String logLevel = null;
        boolean onlyLogLevel = false;
        boolean logsOnly = false;
        boolean statsOnly = false;
        InternalDistributedMember member1 = new InternalDistributedMember("member1", 12345);
        InternalDistributedMember member2 = new InternalDistributedMember("member2", 98765);
        member1.getNetMember().setName("member1");
        member2.getNetMember().setName("member2");
        Set<DistributedMember> testMembers = new HashSet<>();
        testMembers.add(member1);
        testMembers.add(member2);
        ResultCollector testResults1 = new ExportLogsCommandTest.CustomCollector();
        testResults1.addResult(member1, (75 * (ExportLogsCommand.MEGABYTE)));
        ResultCollector testResults2 = new ExportLogsCommandTest.CustomCollector();
        testResults2.addResult(member2, (60 * (ExportLogsCommand.MEGABYTE)));
        Mockito.doReturn(mockCache).when(spyCmd).getCache();
        Mockito.doReturn(testMembers).when(spyCmd).getMembersIncludingLocators(null, null);
        Mockito.doReturn(testResults1).when(spyCmd).estimateLogSize(Matchers.any(Args.class), ArgumentMatchers.eq(member1));
        Mockito.doReturn(testResults2).when(spyCmd).estimateLogSize(Matchers.any(Args.class), ArgumentMatchers.eq(member2));
        Mockito.doReturn((10 * (ExportLogsCommand.MEGABYTE))).when(spyCmd).getLocalDiskAvailable();
        CommandResult res = ((CommandResult) (spyCmd.exportLogs("working dir", null, null, logLevel, onlyLogLevel, false, start, end, logsOnly, statsOnly, "125m")));
        assertThat(res.getStatus()).isEqualTo(ERROR);
        assertThat(res.toJson()).contains("Estimated logs size will exceed the available disk space on the locator");
    }

    @Test
    public void testTotalEstimateSizeExceedsUserSpecifiedValue() throws Exception {
        final InternalCache mockCache = Mockito.mock(InternalCache.class);
        final InternalCacheForClientAccess mockCacheFilter = Mockito.mock(InternalCacheForClientAccess.class);
        Mockito.when(mockCache.getCacheForProcessingClientRequests()).thenReturn(mockCacheFilter);
        final ExportLogsCommand realCmd = new ExportLogsCommand();
        ExportLogsCommand spyCmd = Mockito.spy(realCmd);
        String start = null;
        String end = null;
        String logLevel = null;
        boolean onlyLogLevel = false;
        boolean logsOnly = false;
        boolean statsOnly = false;
        InternalDistributedMember member1 = new InternalDistributedMember("member1", 12345);
        InternalDistributedMember member2 = new InternalDistributedMember("member2", 98765);
        member1.getNetMember().setName("member1");
        member2.getNetMember().setName("member2");
        Set<DistributedMember> testMembers = new HashSet<>();
        testMembers.add(member1);
        testMembers.add(member2);
        ResultCollector testResults1 = new ExportLogsCommandTest.CustomCollector();
        testResults1.addResult(member1, (75 * (ExportLogsCommand.MEGABYTE)));
        ResultCollector testResults2 = new ExportLogsCommandTest.CustomCollector();
        testResults2.addResult(member2, (60 * (ExportLogsCommand.MEGABYTE)));
        Mockito.doReturn(mockCache).when(spyCmd).getCache();
        Mockito.doReturn(testMembers).when(spyCmd).getMembersIncludingLocators(null, null);
        Mockito.doReturn(testResults1).when(spyCmd).estimateLogSize(Matchers.any(Args.class), ArgumentMatchers.eq(member1));
        Mockito.doReturn(testResults2).when(spyCmd).estimateLogSize(Matchers.any(Args.class), ArgumentMatchers.eq(member2));
        Mockito.doReturn(ExportLogsCommand.GIGABYTE).when(spyCmd).getLocalDiskAvailable();
        CommandResult res = ((CommandResult) (spyCmd.exportLogs("working dir", null, null, logLevel, onlyLogLevel, false, start, end, logsOnly, statsOnly, "125m")));
        assertThat(res.getStatus()).isEqualTo(ERROR);
        assertThat(res.toJson()).contains("Estimated exported logs expanded file size = 141557760, file-size-limit = 131072000");
    }

    @Test
    public void estimateLogSizeExceedsServerDisk() throws Exception {
        final InternalCache mockCache = Mockito.mock(InternalCache.class);
        final InternalCacheForClientAccess mockCacheFilter = Mockito.mock(InternalCacheForClientAccess.class);
        Mockito.when(mockCache.getCacheForProcessingClientRequests()).thenReturn(mockCacheFilter);
        final ExportLogsCommand realCmd = new ExportLogsCommand();
        ExportLogsCommand spyCmd = Mockito.spy(realCmd);
        String start = null;
        String end = null;
        String logLevel = null;
        boolean onlyLogLevel = false;
        boolean logsOnly = false;
        boolean statsOnly = false;
        InternalDistributedMember member1 = new InternalDistributedMember("member1", 12345);
        member1.getNetMember().setName("member1");
        Set<DistributedMember> testMembers = new HashSet<>();
        testMembers.add(member1);
        BytesToString bytesToString = new BytesToString();
        ResultCollector testResults1 = new ExportLogsCommandTest.CustomCollector();
        StringBuilder sb = new StringBuilder().append("Estimated disk space required (").append(bytesToString.of(ExportLogsCommand.GIGABYTE)).append(") to consolidate logs on member ").append(member1.getName()).append(" will exceed available disk space (").append(bytesToString.of((500 * (ExportLogsCommand.MEGABYTE)))).append(")");
        testResults1.addResult(member1, new ManagementException(sb.toString()));
        Mockito.doReturn(mockCache).when(spyCmd).getCache();
        Mockito.doReturn(testMembers).when(spyCmd).getMembersIncludingLocators(null, null);
        Mockito.doReturn(testResults1).when(spyCmd).estimateLogSize(Matchers.any(Args.class), ArgumentMatchers.eq(member1));
        CommandResult res = ((CommandResult) (spyCmd.exportLogs("working dir", null, null, logLevel, onlyLogLevel, false, start, end, logsOnly, statsOnly, "125m")));
        assertThat(res.getStatus()).isEqualTo(ERROR);
        assertThat(res.toJson()).contains("Estimated disk space required (1 GB) to consolidate logs on member member1 will exceed available disk space (500 MB)");
    }

    private static class CustomCollector implements ResultCollector<Object, List<Object>> {
        private final ArrayList<Object> results = new ArrayList<>();

        @Override
        public List<Object> getResult() throws FunctionException {
            return results;
        }

        @Override
        public List<Object> getResult(final long timeout, final TimeUnit unit) throws FunctionException {
            return results;
        }

        @Override
        public void addResult(final DistributedMember memberID, final Object resultOfSingleExecution) {
            results.add(resultOfSingleExecution);
        }

        @Override
        public void endResults() {
        }

        @Override
        public void clearResults() {
            results.clear();
        }
    }
}
