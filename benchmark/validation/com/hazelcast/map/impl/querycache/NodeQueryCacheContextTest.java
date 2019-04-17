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
package com.hazelcast.map.impl.querycache;


import com.hazelcast.map.impl.querycache.subscriber.operation.MadePublishableOperationFactory;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class NodeQueryCacheContextTest extends HazelcastTestSupport {
    private QueryCacheContext context;

    private int partitionCount;

    @Test(expected = UnsupportedOperationException.class)
    public void testDestroy() {
        context.destroy();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokerWrapper_invokeOnAllPartitions() throws Exception {
        MadePublishableOperationFactory factory = new MadePublishableOperationFactory("mapName", "cacheId");
        Map<Integer, Object> result = ((Map<Integer, Object>) (context.getInvokerWrapper().invokeOnAllPartitions(factory)));
        Assert.assertEquals(partitionCount, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvokerWrapper_invokeOnAllPartitions_whenRequestOfWrongType_thenThrowException() throws Exception {
        context.getInvokerWrapper().invokeOnAllPartitions(new Object());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvokerWrapper_invoke() {
        context.getInvokerWrapper().invoke(null);
    }

    @Test
    public void testGetQueryCacheScheduler() {
        QueryCacheScheduler scheduler = context.getQueryCacheScheduler();
        Assert.assertNotNull(scheduler);
        final NodeQueryCacheContextTest.QuerySchedulerTask task = new NodeQueryCacheContextTest.QuerySchedulerTask();
        scheduler.execute(task);
        final NodeQueryCacheContextTest.QuerySchedulerRepetitionTask repetitionTask = new NodeQueryCacheContextTest.QuerySchedulerRepetitionTask();
        scheduler.scheduleWithRepetition(repetitionTask, 1);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertTrue(task.executed);
                Assert.assertTrue(((repetitionTask.counter.get()) > 1));
            }
        });
        scheduler.shutdown();
    }

    public static class QuerySchedulerTask implements Runnable {
        public volatile boolean executed;

        @Override
        public void run() {
            executed = true;
        }
    }

    public static class QuerySchedulerRepetitionTask implements Runnable {
        public final AtomicInteger counter = new AtomicInteger();

        @Override
        public void run() {
            counter.incrementAndGet();
        }
    }
}
