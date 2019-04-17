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
package com.hazelcast.flakeidgen.impl;


import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class AutoBatcherTest {
    private static final int VALIDITY = 10000;

    private AutoBatcher batcher = new AutoBatcher(3, AutoBatcherTest.VALIDITY, new AutoBatcher.IdBatchSupplier() {
        int base;

        @Override
        public IdBatch newIdBatch(int batchSize) {
            try {
                return new IdBatch(base, 1, batchSize);
            } finally {
                base += batchSize;
            }
        }
    });

    @Test
    public void when_validButUsedAll_then_fetchNew() {
        Assert.assertEquals(0, batcher.newId());
        Assert.assertEquals(1, batcher.newId());
        Assert.assertEquals(2, batcher.newId());
        Assert.assertEquals(3, batcher.newId());
    }

    @Test
    public void when_notValid_then_fetchNew() throws Exception {
        Assert.assertEquals(0, batcher.newId());
        Thread.sleep(AutoBatcherTest.VALIDITY);
        Assert.assertEquals(3, batcher.newId());
    }

    @Test
    public void concurrencySmokeTest() throws Exception {
        Set<Long> ids = FlakeIdConcurrencyTestUtil.concurrentlyGenerateIds(new com.hazelcast.util.function.Supplier<Long>() {
            @Override
            public Long get() {
                return batcher.newId();
            }
        });
        for (int i = 0; i < ((FlakeIdConcurrencyTestUtil.NUM_THREADS) * (FlakeIdConcurrencyTestUtil.IDS_IN_THREAD)); i++) {
            Assert.assertTrue(("Missing ID: " + i), ids.contains(((long) (i))));
        }
    }
}
