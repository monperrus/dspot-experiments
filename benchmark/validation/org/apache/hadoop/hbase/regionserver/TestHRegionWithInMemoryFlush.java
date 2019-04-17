/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.regionserver;


import MutableSegment.DEEP_OVERHEAD;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hadoop.hbase.testclassification.VerySlowRegionServerTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.wal.WALFactory;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * A test similar to TestHRegion, but with in-memory flush families.
 * Also checks wal truncation after in-memory compaction.
 */
@Category({ VerySlowRegionServerTests.class, LargeTests.class })
public class TestHRegionWithInMemoryFlush extends TestHRegion {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestHRegionWithInMemoryFlush.class);

    /**
     * A test case of HBASE-21041
     *
     * @throws Exception
     * 		Exception
     */
    @Override
    @Test
    public void testFlushAndMemstoreSizeCounting() throws Exception {
        byte[] family = Bytes.toBytes("family");
        this.region = initHRegion(tableName, method, TestHRegion.CONF, family);
        final WALFactory wals = new WALFactory(TestHRegion.CONF, method);
        int count = 0;
        try {
            for (byte[] row : HBaseTestingUtility.ROWS) {
                Put put = new Put(row);
                put.addColumn(family, family, row);
                region.put(put);
                // In memory flush every 1000 puts
                if (((count++) % 1000) == 0) {
                    flushInMemory();
                }
            }
            region.flush(true);
            // After flush, data size should be zero
            Assert.assertEquals(0, region.getMemStoreDataSize());
            // After flush, a new active mutable segment is created, so the heap size
            // should equal to MutableSegment.DEEP_OVERHEAD
            Assert.assertEquals(DEEP_OVERHEAD, region.getMemStoreHeapSize());
            // After flush, offheap size should be zero
            Assert.assertEquals(0, region.getMemStoreOffHeapSize());
        } finally {
            HBaseTestingUtility.closeRegionAndWAL(this.region);
            this.region = null;
            wals.close();
        }
    }
}
