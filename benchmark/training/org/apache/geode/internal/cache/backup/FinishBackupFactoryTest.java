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
package org.apache.geode.internal.cache.backup;


import java.util.Set;
import org.apache.geode.distributed.internal.DistributionManager;
import org.apache.geode.distributed.internal.membership.InternalDistributedMember;
import org.apache.geode.internal.cache.InternalCache;
import org.junit.Test;


public class FinishBackupFactoryTest {
    private FinishBackupFactory finishBackupFactory;

    private BackupResultCollector resultCollector;

    private DistributionManager dm;

    private InternalDistributedMember sender;

    private Set<InternalDistributedMember> recipients;

    private InternalDistributedMember member;

    private InternalCache cache;

    @Test
    public void createReplyProcessorReturnsBackupReplyProcessor() throws Exception {
        assertThat(finishBackupFactory.createReplyProcessor(resultCollector, dm, recipients)).isInstanceOf(BackupReplyProcessor.class);
    }

    @Test
    public void createRequestReturnsFinishBackupRequest() throws Exception {
        assertThat(finishBackupFactory.createRequest(sender, recipients, 1)).isInstanceOf(FinishBackupRequest.class);
    }

    @Test
    public void createFinishBackupReturnsFinishBackup() throws Exception {
        assertThat(finishBackupFactory.createFinishBackup(cache)).isInstanceOf(FinishBackup.class);
    }

    @Test
    public void createBackupResponseReturnsBackupResponse() {
        assertThat(finishBackupFactory.createBackupResponse(member, null)).isInstanceOf(BackupResponse.class);
    }
}
