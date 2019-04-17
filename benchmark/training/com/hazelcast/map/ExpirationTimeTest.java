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


import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class ExpirationTimeTest extends HazelcastTestSupport {
    private static final long ONE_MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);

    @Test
    public void testExpirationTime_withTTL() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expectedExpirationTime = creationTime + (ExpirationTimeTest.ONE_MINUTE_IN_MILLIS);
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withTTL_withShorterMaxIdle() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS, 10, TimeUnit.SECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withShorterTTL_andMaxIdle() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, 10, TimeUnit.SECONDS, 20, TimeUnit.SECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withZeroTTL() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, 0, TimeUnit.MILLISECONDS);
        Assert.assertEquals(Long.MAX_VALUE, ExpirationTimeTest.getExpirationTime(map, 1));
    }

    @Test
    public void testExpirationTime_withNegativeTTL() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, (-1), TimeUnit.MILLISECONDS);
        Assert.assertEquals(Long.MAX_VALUE, ExpirationTimeTest.getExpirationTime(map, 1));
    }

    @Test
    public void testExpirationTime_withTTL_andMapConfigTTL() {
        IMap<Integer, Integer> map = createMapWithTTLSeconds(5553152);
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expectedExpirationTime = creationTime + (ExpirationTimeTest.ONE_MINUTE_IN_MILLIS);
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withZeroTTL_andMapConfigTTL() {
        IMap<Integer, Integer> map = createMapWithTTLSeconds(((int) (TimeUnit.MINUTES.toSeconds(1))));
        map.put(1, 1, 0, TimeUnit.MILLISECONDS);
        Assert.assertEquals(Long.MAX_VALUE, ExpirationTimeTest.getExpirationTime(map, 1));
    }

    @Test
    public void testExpirationTime_withNegativeTTL_andMapConfigTTL() {
        IMap<Integer, Integer> map = createMapWithTTLSeconds(((int) (TimeUnit.MINUTES.toSeconds(1))));
        map.put(1, 1, (-1), TimeUnit.MILLISECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expectedExpirationTime = creationTime + (ExpirationTimeTest.ONE_MINUTE_IN_MILLIS);
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withTTL_afterMultipleUpdates() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS);
        HazelcastTestSupport.sleepMillis(1);
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS);
        HazelcastTestSupport.sleepMillis(1);
        map.put(1, 1, ExpirationTimeTest.ONE_MINUTE_IN_MILLIS, TimeUnit.MILLISECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long lastUpdateTime = entryView.getLastUpdateTime();
        long expectedExpirationTime = lastUpdateTime + (ExpirationTimeTest.ONE_MINUTE_IN_MILLIS);
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_withMaxIdleTime() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expirationTime = entryView.getExpirationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, expirationTime);
    }

    @Test
    public void testExpirationTime_withMaxIdleTime_withEntryCustomMaxIdle() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(20);
        map.put(1, 1, (-1), TimeUnit.MILLISECONDS, 10, TimeUnit.SECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expirationTime = entryView.getExpirationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, expirationTime);
    }

    @Test
    public void testExpirationTime_withMaxIdleTime_withEntryCustomMaxIdleGreaterThanConfig() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1, (-1), TimeUnit.MILLISECONDS, 2, TimeUnit.MINUTES);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expirationTime = entryView.getExpirationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.MINUTES.toMillis(2));
        Assert.assertEquals(expectedExpirationTime, expirationTime);
    }

    @Test
    public void testExpirationTime_withNegativeMaxIdleTime() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1, (-1), TimeUnit.MILLISECONDS, (-1), TimeUnit.MILLISECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expirationTime = entryView.getExpirationTime();
        // negative max idle means use value from map config
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, expirationTime);
    }

    @Test
    public void testExpirationTime_withMaxIdleTime_afterMultipleAccesses() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1);
        HazelcastTestSupport.sleepMillis(999);
        map.get(1);
        HazelcastTestSupport.sleepMillis(23);
        Assert.assertTrue(map.containsKey(1));
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long lastAccessTime = entryView.getLastAccessTime();
        long expectedExpirationTime = lastAccessTime + (TimeUnit.SECONDS.toMillis(10));
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_whenMaxIdleTime_isSmallerThan_TTL() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1, 100, TimeUnit.SECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long lastAccessTime = entryView.getLastAccessTime();
        long delayToExpiration = lastAccessTime + (TimeUnit.SECONDS.toMillis(10));
        // lastAccessTime is zero after put, we can find expiration by this calculation
        long expectedExpirationTime = delayToExpiration + (entryView.getCreationTime());
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void testExpirationTime_whenMaxIdleTime_isBiggerThan_TTL() {
        IMap<Integer, Integer> map = createMapWithMaxIdleSeconds(10);
        map.put(1, 1, 5, TimeUnit.SECONDS);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long creationTime = entryView.getCreationTime();
        long expirationTime = entryView.getExpirationTime();
        long expectedExpirationTime = creationTime + (TimeUnit.SECONDS.toMillis(5));
        Assert.assertEquals(expectedExpirationTime, expirationTime);
    }

    @Test
    public void testLastAccessTime_isZero_afterFirstPut() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        Assert.assertEquals(0L, entryView.getLastAccessTime());
    }

    @Test
    public void testExpirationTime_calculated_against_lastUpdateTime_after_PutWithNoTTL() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, 1, TimeUnit.MINUTES);
        HazelcastTestSupport.sleepMillis(1);
        map.put(1, 1);
        EntryView<Integer, Integer> entryView = map.getEntryView(1);
        long expectedExpirationTime = (entryView.getLastUpdateTime()) + (TimeUnit.MINUTES.toMillis(1));
        Assert.assertEquals(expectedExpirationTime, entryView.getExpirationTime());
    }

    @Test
    public void replace_shifts_expiration_time_when_succeeded() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, 100, TimeUnit.SECONDS);
        long expirationTimeAfterPut = ExpirationTimeTest.getExpirationTime(map, 1);
        HazelcastTestSupport.sleepAtLeastMillis(1000);
        map.replace(1, 1, 2);
        long expirationTimeAfterReplace = ExpirationTimeTest.getExpirationTime(map, 1);
        Assert.assertTrue((expirationTimeAfterReplace > expirationTimeAfterPut));
    }

    @Test
    public void replace_does_not_shift_expiration_time_when_failed() {
        IMap<Integer, Integer> map = createMap();
        map.put(1, 1, 100, TimeUnit.SECONDS);
        long expirationTimeAfterPut = ExpirationTimeTest.getExpirationTime(map, 1);
        HazelcastTestSupport.sleepAtLeastMillis(3);
        int wrongOldValue = -1;
        map.replace(1, wrongOldValue, 2);
        long expirationTimeAfterReplace = ExpirationTimeTest.getExpirationTime(map, 1);
        Assert.assertEquals(expirationTimeAfterReplace, expirationTimeAfterPut);
    }

    @Test
    public void last_access_time_updated_on_primary_when_read_backup_data_enabled() {
        String mapName = "test";
        Config config = getConfig();
        MapConfig mapConfig = config.getMapConfig(mapName);
        mapConfig.setBackupCount(0);
        mapConfig.setAsyncBackupCount(0);
        mapConfig.setReadBackupData(true);
        mapConfig.setMaxIdleSeconds(20);
        mapConfig.setInMemoryFormat(inMemoryFormat());
        IMap map = createHazelcastInstance(config).getMap(mapName);
        map.put(1, 1);
        long lastAccessTimeBefore = map.getEntryView(1).getLastAccessTime();
        HazelcastTestSupport.sleepAtLeastMillis(10);
        map.get(1);
        HazelcastTestSupport.sleepAtLeastMillis(10);
        map.get(1);
        long lastAccessTimeAfter = map.getEntryView(1).getLastAccessTime();
        String msg = String.format(("lastAccessTimeBefore:%d," + " lastAccessTimeAfter:%d"), lastAccessTimeBefore, lastAccessTimeAfter);
        Assert.assertTrue(msg, (lastAccessTimeAfter > lastAccessTimeBefore));
    }
}
