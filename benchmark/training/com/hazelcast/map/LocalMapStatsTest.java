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
package com.hazelcast.map;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.spi.properties.GroupProperty;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import com.hazelcast.util.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class LocalMapStatsTest extends HazelcastTestSupport {
    static final int OPERATION_COUNT = 10;

    static final int DEFAULT_PARTITION_COUNT = Integer.valueOf(GroupProperty.PARTITION_COUNT.getDefaultValue());

    HazelcastInstance instance;

    private String mapName = "mapName";

    @Test
    public void testHitsGenerated() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.get(i);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(100, localMapStats.getHits());
    }

    @Test
    public void testPutAndHitsGenerated() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.get(i);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(100, localMapStats.getPutOperationCount());
        Assert.assertEquals(100, localMapStats.getHits());
    }

    @Test
    public void testPutAsync() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.putAsync(i, i);
        }
        final LocalMapStats localMapStats = geMapStats();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertEquals(100, localMapStats.getPutOperationCount());
            }
        });
    }

    @Test
    public void testGetAndHitsGenerated() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.get(i);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(100, localMapStats.getGetOperationCount());
        Assert.assertEquals(100, localMapStats.getHits());
    }

    @Test
    public void testPutAllGenerated() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            Map<Integer, Integer> putMap = new HashMap<Integer, Integer>(2);
            putMap.put(i, i);
            putMap.put((100 + i), (100 + i));
            map.putAll(putMap);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(200, localMapStats.getPutOperationCount());
    }

    @Test
    public void testGetAllGenerated() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 200; i++) {
            map.put(i, i);
        }
        for (int i = 0; i < 100; i++) {
            Set<Integer> keys = new HashSet<Integer>();
            keys.add(i);
            keys.add((100 + i));
            map.getAll(keys);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(200, localMapStats.getGetOperationCount());
    }

    @Test
    public void testGetAsyncAndHitsGenerated() throws Exception {
        final IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.getAsync(i).get();
        }
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                final LocalMapStats localMapStats = geMapStats();
                Assert.assertEquals(100, localMapStats.getGetOperationCount());
                Assert.assertEquals(100, localMapStats.getHits());
            }
        });
    }

    @Test
    public void testDelete() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.delete(i);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(100, localMapStats.getRemoveOperationCount());
    }

    @Test
    public void testRemove() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.remove(i);
        }
        LocalMapStats localMapStats = geMapStats();
        Assert.assertEquals(100, localMapStats.getRemoveOperationCount());
    }

    @Test
    public void testRemoveAsync() throws Exception {
        IMap<Integer, Integer> map = getMap();
        for (int i = 0; i < 100; i++) {
            map.put(i, i);
            map.removeAsync(i);
        }
        final LocalMapStats localMapStats = geMapStats();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertEquals(100, localMapStats.getRemoveOperationCount());
            }
        });
    }

    @Test
    public void testHitsGenerated_updatedConcurrently() throws Exception {
        final IMap<Integer, Integer> map = getMap();
        final int actionCount = 100;
        for (int i = 0; i < actionCount; i++) {
            map.put(i, i);
            map.get(i);
        }
        final LocalMapStats localMapStats = geMapStats();
        final long initialHits = localMapStats.getHits();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < actionCount; i++) {
                    map.get(i);
                }
                geMapStats();// causes the local stats object to update

            }
        }).start();
        Assert.assertEquals(actionCount, initialHits);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertEquals((actionCount * 2), localMapStats.getHits());
            }
        });
    }

    @Test
    public void testLastAccessTime() throws InterruptedException {
        final long startTime = Clock.currentTimeMillis();
        IMap<String, String> map = getMap();
        String key = "key";
        map.put(key, "value");
        map.get(key);
        long lastAccessTime = geMapStats().getLastAccessTime();
        Assert.assertTrue((lastAccessTime >= startTime));
        Thread.sleep(5);
        map.put(key, "value2");
        long lastAccessTime2 = geMapStats().getLastAccessTime();
        Assert.assertTrue((lastAccessTime2 > lastAccessTime));
    }

    @Test
    public void testLastAccessTime_updatedConcurrently() throws InterruptedException {
        final long startTime = Clock.currentTimeMillis();
        final IMap<String, String> map = getMap();
        final String key = "key";
        map.put(key, "value");
        map.put(key, "value");
        final LocalMapStats localMapStats = geMapStats();
        final long lastUpdateTime = localMapStats.getLastUpdateTime();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HazelcastTestSupport.sleepAtLeastMillis(1);
                map.put(key, "value2");
                geMapStats();// causes the local stats object to update

            }
        }).start();
        Assert.assertTrue((lastUpdateTime >= startTime));
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertTrue(((localMapStats.getLastUpdateTime()) > lastUpdateTime));
            }
        });
    }

    @Test
    public void testEvictAll() throws Exception {
        IMap<String, String> map = getMap();
        map.put("key", "value");
        map.evictAll();
        final long heapCost = geMapStats().getHeapCost();
        Assert.assertEquals(0L, heapCost);
    }

    @Test
    public void testOtherOperationCount_containsKey() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.containsKey(i);
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(LocalMapStatsTest.OPERATION_COUNT, stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_entrySet() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.entrySet();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(LocalMapStatsTest.OPERATION_COUNT, stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_keySet() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.keySet();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(LocalMapStatsTest.OPERATION_COUNT, stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_values() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.values();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(LocalMapStatsTest.OPERATION_COUNT, stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_clear() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.clear();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(LocalMapStatsTest.OPERATION_COUNT, stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_containsValue() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.containsValue(1);
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(((LocalMapStatsTest.OPERATION_COUNT) * (LocalMapStatsTest.DEFAULT_PARTITION_COUNT)), stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_isEmpty() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.isEmpty();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(((LocalMapStatsTest.OPERATION_COUNT) * (LocalMapStatsTest.DEFAULT_PARTITION_COUNT)), stats.getOtherOperationCount());
    }

    @Test
    public void testOtherOperationCount_size() {
        Map map = getMap();
        for (int i = 0; i < (LocalMapStatsTest.OPERATION_COUNT); i++) {
            map.size();
        }
        LocalMapStats stats = geMapStats();
        Assert.assertEquals(((LocalMapStatsTest.OPERATION_COUNT) * (LocalMapStatsTest.DEFAULT_PARTITION_COUNT)), stats.getOtherOperationCount());
    }
}
