/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.internal.partition;


import com.hazelcast.config.Config;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.partition.impl.InternalPartitionServiceImpl;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.MigrationAwareService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationResponseHandler;
import com.hazelcast.spi.PartitionMigrationEvent;
import com.hazelcast.spi.PartitionReplicationEvent;
import com.hazelcast.spi.exception.RetryableHazelcastException;
import com.hazelcast.spi.partition.MigrationEndpoint;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class MigrationAwareServiceEventTest extends HazelcastTestSupport {
    private TestHazelcastInstanceFactory factory;

    @Test
    public void migrationCommitEvents_shouldBeEqual_onSource_and_onDestination() throws Exception {
        Config config = new Config();
        final MigrationAwareServiceEventTest.MigrationEventCounterService counter = new MigrationAwareServiceEventTest.MigrationEventCounterService();
        ServiceConfig serviceConfig = new ServiceConfig().setEnabled(true).setName("event-counter").setImplementation(counter);
        config.getServicesConfig().addServiceConfig(serviceConfig);
        final HazelcastInstance hz = factory.newHazelcastInstance(config);
        HazelcastTestSupport.warmUpPartitions(hz);
        final AssertTask assertTask = new AssertTask() {
            final InternalPartitionService partitionService = HazelcastTestSupport.getNode(hz).getPartitionService();

            @Override
            public void run() throws Exception {
                Assert.assertEquals(0, partitionService.getMigrationQueueSize());
                final int source = counter.sourceCommits.get();
                final int destination = counter.destinationCommits.get();
                Assert.assertEquals(source, destination);
            }
        };
        factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertTrueEventually(assertTask);
        factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertTrueEventually(assertTask);
    }

    @Test
    public void partitionIsMigratingFlag_shouldBeSet_until_serviceCommitRollback_isCompleted() throws Exception {
        MigrationAwareServiceEventTest.FailingOperationResponseHandler responseHandler = new MigrationAwareServiceEventTest.FailingOperationResponseHandler();
        HazelcastInstance hz = factory.newHazelcastInstance(newConfig(responseHandler));
        HazelcastTestSupport.warmUpPartitions(hz);
        HazelcastInstance[] instances = new HazelcastInstance[2];
        for (int i = 0; i < (instances.length); i++) {
            instances[i] = factory.newHazelcastInstance(newConfig(responseHandler));
        }
        HazelcastTestSupport.waitAllForSafeState(instances);
        for (HazelcastInstance instance : instances) {
            instance.getLifecycleService().terminate();
        }
        HazelcastTestSupport.waitAllForSafeState(hz);
        Assert.assertThat(responseHandler.failures, Matchers.empty());
    }

    private static class FailingOperationResponseHandler implements OperationResponseHandler {
        private final Queue<String> failures = new ConcurrentLinkedQueue<String>();

        @Override
        public void sendResponse(Operation operation, Object response) {
            assert operation instanceof MigrationAwareServiceEventTest.DummyPartitionAwareOperation : "Invalid operation: " + operation;
            NodeEngine nodeEngine = operation.getNodeEngine();
            if ((!(response instanceof RetryableHazelcastException)) && (nodeEngine.isRunning())) {
                MigrationAwareServiceEventTest.DummyPartitionAwareOperation op = ((MigrationAwareServiceEventTest.DummyPartitionAwareOperation) (operation));
                failures.add(((((((("Unexpected response: " + response) + ". Node: ") + (nodeEngine.getThisAddress())) + ", Event: ") + (op.event)) + ", Type: ") + (op.type)));
            }
        }
    }

    private static class MigrationCommitRollbackTestingService implements ManagedService , MigrationAwareService {
        private static final String NAME = MigrationAwareServiceEventTest.MigrationCommitRollbackTestingService.class.getSimpleName();

        private static final String TYPE_COMMIT = "COMMIT";

        private static final String TYPE_ROLLBACK = "ROLLBACK";

        private final MigrationAwareServiceEventTest.FailingOperationResponseHandler responseHandler;

        private volatile NodeEngine nodeEngine;

        MigrationCommitRollbackTestingService(MigrationAwareServiceEventTest.FailingOperationResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        public void init(NodeEngine nodeEngine, Properties properties) {
            this.nodeEngine = nodeEngine;
        }

        @Override
        public Operation prepareReplicationOperation(PartitionReplicationEvent event) {
            return null;
        }

        @Override
        public void beforeMigration(PartitionMigrationEvent event) {
        }

        @Override
        public void commitMigration(PartitionMigrationEvent event) {
            checkPartition(event, MigrationAwareServiceEventTest.MigrationCommitRollbackTestingService.TYPE_COMMIT);
        }

        @Override
        public void rollbackMigration(PartitionMigrationEvent event) {
            checkPartition(event, MigrationAwareServiceEventTest.MigrationCommitRollbackTestingService.TYPE_ROLLBACK);
        }

        private void checkPartition(PartitionMigrationEvent event, String type) {
            if (((event.getNewReplicaIndex()) != 0) && ((event.getCurrentReplicaIndex()) != 0)) {
                return;
            }
            checkPartitionMigrating(event, type);
            if ((event.getCurrentReplicaIndex()) != (-1)) {
                runPartitionOperation(event, type, event.getCurrentReplicaIndex());
            }
            if ((event.getNewReplicaIndex()) != (-1)) {
                runPartitionOperation(event, type, event.getNewReplicaIndex());
            }
        }

        private void runPartitionOperation(PartitionMigrationEvent event, String type, int replicaIndex) {
            MigrationAwareServiceEventTest.DummyPartitionAwareOperation op = new MigrationAwareServiceEventTest.DummyPartitionAwareOperation(event, type);
            op.setNodeEngine(nodeEngine).setPartitionId(event.getPartitionId()).setReplicaIndex(replicaIndex);
            setOperationResponseHandler(responseHandler);
            nodeEngine.getOperationService().run(op);
        }

        private void checkPartitionMigrating(PartitionMigrationEvent event, String type) {
            InternalPartitionServiceImpl partitionService = ((InternalPartitionServiceImpl) (nodeEngine.getPartitionService()));
            InternalPartition partition = partitionService.getPartition(event.getPartitionId());
            if ((!(partition.isMigrating())) && (nodeEngine.isRunning())) {
                responseHandler.failures.add(((((("Migrating flag is not set. Node: " + (nodeEngine.getThisAddress())) + ", Event: ") + event) + ", Type: ") + type));
            }
        }

        @Override
        public void reset() {
        }

        @Override
        public void shutdown(boolean terminate) {
        }
    }

    private static class DummyPartitionAwareOperation extends Operation {
        private final PartitionMigrationEvent event;

        private final String type;

        DummyPartitionAwareOperation(PartitionMigrationEvent event, String type) {
            this.event = event;
            this.type = type;
        }

        @Override
        public void run() throws Exception {
        }

        @Override
        public Object getResponse() {
            return Boolean.TRUE;
        }
    }

    private static class MigrationEventCounterService implements MigrationAwareService {
        final AtomicInteger sourceCommits = new AtomicInteger();

        final AtomicInteger destinationCommits = new AtomicInteger();

        @Override
        public Operation prepareReplicationOperation(PartitionReplicationEvent event) {
            return null;
        }

        @Override
        public void beforeMigration(PartitionMigrationEvent event) {
        }

        @Override
        public void commitMigration(PartitionMigrationEvent event) {
            // Only count ownership migrations.
            // For missing (new) backups there are also COPY migrations (call it just replication)
            // which don't have a source endpoint.
            if (((event.getCurrentReplicaIndex()) == 0) || ((event.getNewReplicaIndex()) == 0)) {
                if ((event.getMigrationEndpoint()) == (MigrationEndpoint.SOURCE)) {
                    sourceCommits.incrementAndGet();
                } else {
                    destinationCommits.incrementAndGet();
                }
            }
        }

        @Override
        public void rollbackMigration(PartitionMigrationEvent event) {
        }
    }
}
