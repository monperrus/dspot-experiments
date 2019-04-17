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
package com.hazelcast.util;


import SampleableConcurrentHashMap.SamplingEntry;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class SampleableConcurrentHashMapTest extends HazelcastTestSupport {
    private static final int ENTRY_COUNT = 100;

    private static final int SAMPLE_COUNT = 15;

    private static final int SPARSE_MAP_CAPACITY = 128;

    private static final int SPARSE_MAP_ENTRY_COUNT = 6;

    private static final int SPARSE_MAP_SAMPLE_COUNT = 6;

    private static final int COUNT = 10;

    private SampleableConcurrentHashMap<Integer, Integer> map;

    @Test
    public void test_getRandomSamples() {
        testSampling(SampleableConcurrentHashMapTest.ENTRY_COUNT, SampleableConcurrentHashMapTest.ENTRY_COUNT, SampleableConcurrentHashMapTest.SAMPLE_COUNT);
    }

    @Test
    public void test_getRandomSamples_whenMapIsSparselyPopulated() {
        testSampling(SampleableConcurrentHashMapTest.SPARSE_MAP_CAPACITY, SampleableConcurrentHashMapTest.SPARSE_MAP_ENTRY_COUNT, SampleableConcurrentHashMapTest.SPARSE_MAP_SAMPLE_COUNT);
    }

    @Test
    public void test_applyIfAbsent() throws Throwable {
        final SampleableConcurrentHashMap<String, String> map = new SampleableConcurrentHashMap<String, String>(10);
        Assert.assertEquals(map.applyIfAbsent("key", new com.hazelcast.core.IFunction<String, String>() {
            @Override
            public String apply(String input) {
                return "value";
            }
        }), "value");
        final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
        final CountDownLatch latch = new CountDownLatch(SampleableConcurrentHashMapTest.COUNT);
        for (int i = 0; i < (SampleableConcurrentHashMapTest.COUNT); i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Assert.assertEquals(map.applyIfAbsent("key", new com.hazelcast.core.IFunction<String, String>() {
                            @Override
                            public String apply(String input) {
                                return "value1";
                            }
                        }), "value");
                    } catch (Throwable e) {
                        error.set(e);
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }
        latch.await(20, TimeUnit.SECONDS);
        if ((error.get()) != null) {
            throw error.get();
        }
        map.clear();
        map.applyIfAbsent("key", new com.hazelcast.core.IFunction<String, String>() {
            @Override
            public String apply(String input) {
                return null;
            }
        });
        Assert.assertEquals(map.size(), 0);
    }

    @Test
    public void test_getRandomSamples_whenSampleCountIsGreaterThenCapacity() {
        final int entryCount = 10;
        final int sampleCount = 100;
        map = new SampleableConcurrentHashMap<Integer, Integer>(entryCount);
        // put single entry
        map.put(1, 1);
        Iterable<SamplingEntry<Integer, Integer>> samples = map.getRandomSamples(sampleCount);
        Iterator<SamplingEntry<Integer, Integer>> iterator = samples.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNotNull(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getRandomSamples_whenSampleCountIsNegative() {
        map = new SampleableConcurrentHashMap<Integer, Integer>(10);
        map.getRandomSamples((-1));
    }

    @Test
    public void testIteratorContract() {
        final int entryCount = 100;
        final int sampleCount = 30;
        map = new SampleableConcurrentHashMap<Integer, Integer>(100);
        for (int i = 0; i < entryCount; i++) {
            map.put(i, i);
        }
        Iterable<SamplingEntry<Integer, Integer>> samples = map.getRandomSamples(sampleCount);
        Iterator<SamplingEntry<Integer, Integer>> iterator = samples.iterator();
        // hasNext should not consume the items
        for (int i = 0; i < (entryCount * 2); i++) {
            Assert.assertTrue(iterator.hasNext());
        }
        Set<Integer> set = new HashSet<Integer>();
        // should return unique samples
        for (int i = 0; i < sampleCount; i++) {
            set.add(iterator.next().key);
        }
        Assert.assertEquals(30, set.size());
        Assert.assertFalse(iterator.hasNext());
    }
}
