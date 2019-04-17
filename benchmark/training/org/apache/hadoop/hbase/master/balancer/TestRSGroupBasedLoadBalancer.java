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
package org.apache.hadoop.hbase.master.balancer;


import LoadBalancer.BOGUS_SERVER_NAME;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.RegionInfoBuilder;
import org.apache.hadoop.hbase.master.RegionPlan;
import org.apache.hadoop.hbase.net.Address;
import org.apache.hadoop.hbase.rsgroup.RSGroupBasedLoadBalancer;
import org.apache.hadoop.hbase.rsgroup.RSGroupInfo;
import org.apache.hadoop.hbase.testclassification.LargeTests;
import org.apache.hbase.thirdparty.com.google.common.collect.ArrayListMultimap;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test RSGroupBasedLoadBalancer with SimpleLoadBalancer as internal balancer
 */
@Category(LargeTests.class)
public class TestRSGroupBasedLoadBalancer extends RSGroupableBalancerTestBase {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestRSGroupBasedLoadBalancer.class);

    private static final Logger LOG = LoggerFactory.getLogger(TestRSGroupBasedLoadBalancer.class);

    private static RSGroupBasedLoadBalancer loadBalancer;

    /**
     * Test the load balancing algorithm.
     *
     * Invariant is that all servers of the group should be hosting either floor(average) or
     * ceiling(average)
     */
    @Test
    public void testBalanceCluster() throws Exception {
        Map<ServerName, List<RegionInfo>> servers = mockClusterServers();
        ArrayListMultimap<String, ServerAndLoad> list = convertToGroupBasedMap(servers);
        TestRSGroupBasedLoadBalancer.LOG.info(("Mock Cluster :  " + (printStats(list))));
        List<RegionPlan> plans = TestRSGroupBasedLoadBalancer.loadBalancer.balanceCluster(servers);
        ArrayListMultimap<String, ServerAndLoad> balancedCluster = reconcile(list, plans);
        TestRSGroupBasedLoadBalancer.LOG.info(("Mock Balance : " + (printStats(balancedCluster))));
        assertClusterAsBalanced(balancedCluster);
    }

    /**
     * Tests the bulk assignment used during cluster startup.
     *
     * Round-robin. Should yield a balanced cluster so same invariant as the
     * load balancer holds, all servers holding either floor(avg) or
     * ceiling(avg).
     */
    @Test
    public void testBulkAssignment() throws Exception {
        List<RegionInfo> regions = randomRegions(25);
        Map<ServerName, List<RegionInfo>> assignments = TestRSGroupBasedLoadBalancer.loadBalancer.roundRobinAssignment(regions, RSGroupableBalancerTestBase.servers);
        // test empty region/servers scenario
        // this should not throw an NPE
        TestRSGroupBasedLoadBalancer.loadBalancer.roundRobinAssignment(regions, Collections.emptyList());
        // test regular scenario
        Assert.assertTrue(((assignments.keySet().size()) == (RSGroupableBalancerTestBase.servers.size())));
        for (ServerName sn : assignments.keySet()) {
            List<RegionInfo> regionAssigned = assignments.get(sn);
            for (RegionInfo region : regionAssigned) {
                TableName tableName = region.getTable();
                String groupName = RSGroupableBalancerTestBase.getMockedGroupInfoManager().getRSGroupOfTable(tableName);
                Assert.assertTrue(StringUtils.isNotEmpty(groupName));
                RSGroupInfo gInfo = RSGroupableBalancerTestBase.getMockedGroupInfoManager().getRSGroup(groupName);
                Assert.assertTrue("Region is not correctly assigned to group servers.", gInfo.containsServer(sn.getAddress()));
            }
        }
        ArrayListMultimap<String, ServerAndLoad> loadMap = convertToGroupBasedMap(assignments);
        assertClusterAsBalanced(loadMap);
    }

    @Test
    public void testGetMisplacedRegions() throws Exception {
        // Test case where region is not considered misplaced if RSGroupInfo cannot be determined
        Map<RegionInfo, ServerName> inputForTest = new HashMap<>();
        RegionInfo ri = RegionInfoBuilder.newBuilder(RSGroupableBalancerTestBase.table0).setStartKey(new byte[16]).setEndKey(new byte[16]).setSplit(false).setRegionId(((RSGroupableBalancerTestBase.regionId)++)).build();
        inputForTest.put(ri, RSGroupableBalancerTestBase.servers.iterator().next());
        Set<RegionInfo> misplacedRegions = TestRSGroupBasedLoadBalancer.loadBalancer.getMisplacedRegions(inputForTest);
        Assert.assertFalse(misplacedRegions.contains(ri));
    }

    /**
     * Test the cluster startup bulk assignment which attempts to retain assignment info.
     */
    @Test
    public void testRetainAssignment() throws Exception {
        // Test simple case where all same servers are there
        Map<ServerName, List<RegionInfo>> currentAssignments = mockClusterServers();
        Map<RegionInfo, ServerName> inputForTest = new HashMap<>();
        for (ServerName sn : currentAssignments.keySet()) {
            for (RegionInfo region : currentAssignments.get(sn)) {
                inputForTest.put(region, sn);
            }
        }
        // verify region->null server assignment is handled
        inputForTest.put(randomRegions(1).get(0), null);
        Map<ServerName, List<RegionInfo>> newAssignment = TestRSGroupBasedLoadBalancer.loadBalancer.retainAssignment(inputForTest, RSGroupableBalancerTestBase.servers);
        assertRetainedAssignment(inputForTest, RSGroupableBalancerTestBase.servers, newAssignment);
    }

    /**
     * Test BOGUS_SERVER_NAME among groups do not overwrite each other.
     */
    @Test
    public void testRoundRobinAssignment() throws Exception {
        List<ServerName> onlineServers = new ArrayList<ServerName>(RSGroupableBalancerTestBase.servers.size());
        onlineServers.addAll(RSGroupableBalancerTestBase.servers);
        List<RegionInfo> regions = randomRegions(25);
        int bogusRegion = 0;
        for (RegionInfo region : regions) {
            String group = RSGroupableBalancerTestBase.tableMap.get(region.getTable());
            if (("dg3".equals(group)) || ("dg4".equals(group))) {
                bogusRegion++;
            }
        }
        Set<Address> offlineServers = new HashSet<Address>();
        offlineServers.addAll(RSGroupableBalancerTestBase.groupMap.get("dg3").getServers());
        offlineServers.addAll(RSGroupableBalancerTestBase.groupMap.get("dg4").getServers());
        for (Iterator<ServerName> it = onlineServers.iterator(); it.hasNext();) {
            ServerName server = it.next();
            Address address = server.getAddress();
            if (offlineServers.contains(address)) {
                it.remove();
            }
        }
        Map<ServerName, List<RegionInfo>> assignments = TestRSGroupBasedLoadBalancer.loadBalancer.roundRobinAssignment(regions, onlineServers);
        Assert.assertEquals(bogusRegion, assignments.get(BOGUS_SERVER_NAME).size());
    }
}
