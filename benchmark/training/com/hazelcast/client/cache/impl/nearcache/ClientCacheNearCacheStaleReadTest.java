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
package com.hazelcast.client.cache.impl.nearcache;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.SlowTest;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.Cache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


/**
 * Tests that the Near Cache doesn't lose invalidations.
 * <p>
 * Issue: https://github.com/hazelcast/hazelcast/issues/4671
 * <p>
 * Thanks Lukas Blunschi for the original test (https://github.com/lukasblu).
 */
@RunWith(HazelcastParallelClassRunner.class)
@Category({ SlowTest.class, ParallelTest.class })
public class ClientCacheNearCacheStaleReadTest extends HazelcastTestSupport {
    private static final int NUM_GETTERS = 7;

    private static final int MAX_RUNTIME = 30;

    private static final String KEY = "key123";

    private static final ILogger LOGGER = Logger.getLogger(ClientCacheNearCacheStaleReadTest.class);

    private AtomicInteger valuePut = new AtomicInteger(0);

    private AtomicBoolean stop = new AtomicBoolean(false);

    private AtomicInteger assertionViolationCount = new AtomicInteger(0);

    private AtomicBoolean failed = new AtomicBoolean(false);

    private HazelcastInstance member;

    private HazelcastInstance client;

    private Cache<String, String> cache;

    @Test
    public void testNoLostInvalidationsEventually() {
        testNoLostInvalidationsStrict(false);
    }

    @Test
    public void testNoLostInvalidationsStrict() {
        testNoLostInvalidationsStrict(true);
    }

    private class PutRunnable implements Runnable {
        @Override
        public void run() {
            ClientCacheNearCacheStaleReadTest.LOGGER.info(((Thread.currentThread().getName()) + " started."));
            int i = 0;
            while (!(stop.get())) {
                i++;
                // put new value and update last state
                // note: the value in the map/Near Cache is *always* larger or equal to valuePut
                // assertion: valueMap >= valuePut
                cache.put(ClientCacheNearCacheStaleReadTest.KEY, String.valueOf(i));
                valuePut.set(i);
                // check if we see our last update
                String valueMapStr = cache.get(ClientCacheNearCacheStaleReadTest.KEY);
                if (valueMapStr == null) {
                    continue;
                }
                int valueMap = Integer.parseInt(valueMapStr);
                if (valueMap != i) {
                    assertionViolationCount.incrementAndGet();
                    ClientCacheNearCacheStaleReadTest.LOGGER.warning((((("Assertion violated! (valueMap = " + valueMap) + ", i = ") + i) + ")"));
                    // sleep to ensure Near Cache invalidation is really lost
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        ClientCacheNearCacheStaleReadTest.LOGGER.warning(("Interrupted: " + (e.getMessage())));
                    }
                    // test again and stop if really lost
                    valueMapStr = cache.get(ClientCacheNearCacheStaleReadTest.KEY);
                    valueMap = Integer.parseInt(valueMapStr);
                    if (valueMap != i) {
                        ClientCacheNearCacheStaleReadTest.LOGGER.warning((((("Near Cache invalidation lost! (valueMap = " + valueMap) + ", i = ") + i) + ")"));
                        failed.set(true);
                        stop.set(true);
                    }
                }
            } 
            ClientCacheNearCacheStaleReadTest.LOGGER.info(((((Thread.currentThread().getName()) + " performed ") + i) + " operations."));
        }
    }

    private class GetRunnable implements Runnable {
        @Override
        public void run() {
            ClientCacheNearCacheStaleReadTest.LOGGER.info(((Thread.currentThread().getName()) + " started."));
            int n = 0;
            while (!(stop.get())) {
                n++;
                // blindly get the value (to trigger the issue) and parse the value (to get some CPU load)
                String valueMapStr = cache.get(ClientCacheNearCacheStaleReadTest.KEY);
                int i = Integer.parseInt(valueMapStr);
                Assert.assertEquals(("" + i), valueMapStr);
            } 
            ClientCacheNearCacheStaleReadTest.LOGGER.info(((((Thread.currentThread().getName()) + " performed ") + n) + " operations."));
        }
    }
}
