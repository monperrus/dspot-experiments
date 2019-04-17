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
package com.hazelcast.spi.impl.operationexecutor.impl;


import com.hazelcast.spi.Operation;
import com.hazelcast.spi.impl.PartitionSpecificRunnable;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class OperationExecutorImpl_RunOrExecuteTest extends OperationExecutorImpl_AbstractTest {
    @Test(expected = NullPointerException.class)
    public void whenNullOperation() {
        initExecutor();
        executor.runOrExecute(null);
    }

    // ============= generic operations ==============================
    @Test
    public void whenGenericOperation_andCallingFromUserThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        Operation operation = new OperationExecutorImpl_RunOrExecuteTest.ThreadCapturingOperation(executingThread);
        executor.runOrExecute(operation);
        Assert.assertSame(Thread.currentThread(), executingThread.get());
    }

    @Test
    public void whenGenericOperation_andCallingFromPartitionThread_thenExecuteOnPartitionThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = new OperationExecutorImpl_RunOrExecuteTest.ThreadCapturingOperation(executingThread);
        final PartitionSpecificCallable<Thread> task = new PartitionSpecificCallable<Thread>(0) {
            @Override
            public Thread call() {
                executor.runOrExecute(operation);
                return Thread.currentThread();
            }
        };
        executor.execute(task);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertSame(task.getResult(), executingThread.get());
            }
        });
    }

    @Test
    public void whenGenericOperation_andCallingFromGenericThread_thenExecuteOnGenericThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = new OperationExecutorImpl_RunOrExecuteTest.ThreadCapturingOperation(executingThread);
        final PartitionSpecificCallable<Thread> task = new PartitionSpecificCallable<Thread>(Operation.GENERIC_PARTITION_ID) {
            @Override
            public Thread call() {
                executor.runOrExecute(operation);
                return Thread.currentThread();
            }
        };
        executor.execute(task);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertSame(task.getResult(), executingThread.get());
            }
        });
    }

    @Test
    public void whenGenericOperation_andCallingFromOperationHostileThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = new OperationExecutorImpl_RunOrExecuteTest.ThreadCapturingOperation(executingThread);
        OperationExecutorImpl_AbstractTest.DummyOperationHostileThread thread = new OperationExecutorImpl_AbstractTest.DummyOperationHostileThread(new Runnable() {
            @Override
            public void run() {
                executor.runOrExecute(operation);
            }
        });
        thread.start();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                HazelcastTestSupport.assertInstanceOf(GenericOperationThread.class, executingThread.get());
            }
        });
    }

    // ===================== partition specific operations ========================
    @Test
    public void whenPartitionOperation_andCallingFromUserThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = setPartitionId(0);
        executor.runOrExecute(operation);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                HazelcastTestSupport.assertInstanceOf(PartitionOperationThread.class, executingThread.get());
            }
        });
    }

    @Test
    public void whenPartitionOperation_andCallingFromGenericThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = setPartitionId(0);
        executor.execute(new PartitionSpecificRunnable() {
            @Override
            public int getPartitionId() {
                return -1;
            }

            @Override
            public void run() {
                executor.runOrExecute(operation);
            }
        });
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                HazelcastTestSupport.assertInstanceOf(PartitionOperationThread.class, executingThread.get());
            }
        });
    }

    @Test
    public void whenPartitionOperation_andCallingFromPartitionOperationThread_andCorrectPartition() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = setPartitionId(0);
        final PartitionSpecificCallable<Thread> task = new PartitionSpecificCallable<Thread>(operation.getPartitionId()) {
            @Override
            public Thread call() {
                executor.runOrExecute(operation);
                return Thread.currentThread();
            }
        };
        executor.execute(task);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertSame(task.getResult(), executingThread.get());
            }
        });
    }

    @Test
    public void whenPartitionOperation_andCallingFromPartitionOperationThread_andWrongPartition() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = setPartitionId(0);
        final PartitionSpecificCallable<Thread> task = new PartitionSpecificCallable<Thread>(((operation.getPartitionId()) + 1)) {
            @Override
            public Thread call() {
                executor.runOrExecute(operation);
                return Thread.currentThread();
            }
        };
        executor.execute(task);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                HazelcastTestSupport.assertInstanceOf(PartitionOperationThread.class, executingThread.get());
                Assert.assertNotSame(task.getResult(), executingThread.get());
            }
        });
    }

    @Test
    public void whenPartitionOperation_andCallingFromOperationHostileThread() {
        initExecutor();
        final AtomicReference<Thread> executingThread = new AtomicReference<Thread>();
        final Operation operation = setPartitionId(0);
        OperationExecutorImpl_AbstractTest.DummyOperationHostileThread thread = new OperationExecutorImpl_AbstractTest.DummyOperationHostileThread(new Runnable() {
            @Override
            public void run() {
                executor.runOrExecute(operation);
            }
        });
        thread.start();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                HazelcastTestSupport.assertInstanceOf(PartitionOperationThread.class, executingThread.get());
            }
        });
    }

    private static class ThreadCapturingOperation extends Operation {
        private final AtomicReference<Thread> executingThread;

        ThreadCapturingOperation(AtomicReference<Thread> executingThread) {
            this.executingThread = executingThread;
        }

        @Override
        public void run() throws Exception {
            executingThread.set(Thread.currentThread());
        }
    }
}
