/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.internal.processors.cache.persistence.file;


import org.apache.ignite.IgniteCache;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Checks if Direct IO can be set up if no persistent store is configured
 */
public class IgniteNativeIoWithNoPersistenceTest extends GridCommonAbstractTest {
    /**
     * Checks simple launch with native IO.
     *
     * @throws Exception
     * 		if failed
     */
    @Test
    public void testDirectIoHandlesNoPersistentGrid() throws Exception {
        IgniteEx ignite = startGrid(0);
        ignite.active(true);
        IgniteCache<Object, Object> cache = ignite.getOrCreateCache("cache");
        for (int i = 0; i < 100; i++)
            cache.put(i, valueForKey(i));

        stopAllGrids();
    }
}
