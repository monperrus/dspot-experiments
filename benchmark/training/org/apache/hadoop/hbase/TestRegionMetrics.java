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
package org.apache.hadoop.hbase;


import Option.LIVE_SERVERS;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.testclassification.MiscTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Category({ MiscTests.class, MediumTests.class })
public class TestRegionMetrics {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestRegionMetrics.class);

    private static final Logger LOG = LoggerFactory.getLogger(TestRegionMetrics.class);

    private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();

    private static Admin admin;

    private static final TableName TABLE_1 = TableName.valueOf("table_1");

    private static final TableName TABLE_2 = TableName.valueOf("table_2");

    private static final TableName TABLE_3 = TableName.valueOf("table_3");

    private static final TableName[] tables = new TableName[]{ TestRegionMetrics.TABLE_1, TestRegionMetrics.TABLE_2, TestRegionMetrics.TABLE_3 };

    private static final int MSG_INTERVAL = 500;// ms


    @Test
    public void testRegionMetrics() throws Exception {
        // Check if regions match with the RegionMetrics from the server
        for (ServerName serverName : TestRegionMetrics.admin.getClusterMetrics(EnumSet.of(LIVE_SERVERS)).getLiveServerMetrics().keySet()) {
            List<RegionInfo> regions = TestRegionMetrics.admin.getRegions(serverName);
            Collection<RegionMetrics> regionMetricsList = TestRegionMetrics.admin.getRegionMetrics(serverName);
            checkRegionsAndRegionMetrics(regions, regionMetricsList);
        }
        // Check if regionMetrics matches the table's regions and nothing is missed
        for (TableName table : new TableName[]{ TestRegionMetrics.TABLE_1, TestRegionMetrics.TABLE_2, TestRegionMetrics.TABLE_3 }) {
            List<RegionInfo> tableRegions = TestRegionMetrics.admin.getRegions(table);
            List<RegionMetrics> regionMetrics = new ArrayList<>();
            for (ServerName serverName : TestRegionMetrics.admin.getClusterMetrics(EnumSet.of(LIVE_SERVERS)).getLiveServerMetrics().keySet()) {
                regionMetrics.addAll(TestRegionMetrics.admin.getRegionMetrics(serverName, table));
            }
            checkRegionsAndRegionMetrics(tableRegions, regionMetrics);
        }
        // Just wait here. If this fixes the test, come back and do a better job.
        // Would have to redo the below so can wait on cluster status changing.
        // Admin#getClusterMetrics retrieves data from HMaster. Admin#getRegionMetrics, by contrast,
        // get the data from RS. Hence, it will fail if we do the assert check before RS has done
        // the report.
        TimeUnit.MILLISECONDS.sleep((3 * (TestRegionMetrics.MSG_INTERVAL)));
        // Check RegionMetrics matches the RegionMetrics from ClusterMetrics
        for (Map.Entry<ServerName, ServerMetrics> entry : TestRegionMetrics.admin.getClusterMetrics(EnumSet.of(LIVE_SERVERS)).getLiveServerMetrics().entrySet()) {
            ServerName serverName = entry.getKey();
            ServerMetrics serverMetrics = entry.getValue();
            List<RegionMetrics> regionMetrics = TestRegionMetrics.admin.getRegionMetrics(serverName);
            TestRegionMetrics.LOG.debug(((("serverName=" + serverName) + ", getRegionLoads=") + (serverMetrics.getRegionMetrics().keySet().stream().map(( r) -> Bytes.toString(r)).collect(Collectors.toList()))));
            TestRegionMetrics.LOG.debug(((("serverName=" + serverName) + ", regionLoads=") + (regionMetrics.stream().map(( r) -> Bytes.toString(r.getRegionName())).collect(Collectors.toList()))));
            Assert.assertEquals(serverMetrics.getRegionMetrics().size(), regionMetrics.size());
        }
    }
}
