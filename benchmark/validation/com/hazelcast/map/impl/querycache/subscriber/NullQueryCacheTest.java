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
package com.hazelcast.map.impl.querycache.subscriber;


import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class NullQueryCacheTest extends HazelcastTestSupport {
    @Test
    public void testGet() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.get(1));
    }

    @Test
    public void testContainsKey() throws Exception {
        Assert.assertFalse(NullQueryCache.NULL_QUERY_CACHE.containsKey(1));
    }

    @Test
    public void testContainsValue() throws Exception {
        Assert.assertFalse(NullQueryCache.NULL_QUERY_CACHE.containsValue(1));
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(NullQueryCache.NULL_QUERY_CACHE.isEmpty());
    }

    @Test
    public void testSize() throws Exception {
        Assert.assertEquals(0, NullQueryCache.NULL_QUERY_CACHE.size());
    }

    @Test
    public void testGetAll() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.getAll(null));
    }

    @Test
    public void testKeySet() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.keySet());
    }

    @Test
    public void testEntrySet() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.entrySet());
    }

    @Test
    public void testValues() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.values());
    }

    @Test
    public void testGetName() throws Exception {
        Assert.assertNull(NullQueryCache.NULL_QUERY_CACHE.getName());
    }
}
