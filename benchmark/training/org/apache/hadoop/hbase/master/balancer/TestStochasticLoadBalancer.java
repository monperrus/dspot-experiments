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


import Cluster.Action;
import Size.Unit;
import Size.ZERO;
import StochasticLoadBalancer.CPRequestCostFunction;
import StochasticLoadBalancer.CostFromRegionLoadFunction;
import StochasticLoadBalancer.CostFunction;
import StochasticLoadBalancer.ReadRequestCostFunction;
import StochasticLoadBalancer.StoreFileCostFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterMetrics;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.RegionMetrics;
import org.apache.hadoop.hbase.ServerMetrics;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.master.MockNoopMasterServices;
import org.apache.hadoop.hbase.master.RegionPlan;
import org.apache.hadoop.hbase.master.balancer.BaseLoadBalancer.Cluster;
import org.apache.hadoop.hbase.testclassification.MasterTests;
import org.apache.hadoop.hbase.testclassification.MediumTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;


@Category({ MasterTests.class, MediumTests.class })
public class TestStochasticLoadBalancer extends BalancerTestBase {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestStochasticLoadBalancer.class);

    private static final String REGION_KEY = "testRegion";

    // Mapping of locality test -> expected locality
    private float[] expectedLocalities = new float[]{ 1.0F, 0.0F, 0.5F, 0.25F, 1.0F };

    /**
     * Data set for testLocalityCost:
     * [test][0][0] = mapping of server to number of regions it hosts
     * [test][region + 1][0] = server that region is hosted on
     * [test][region + 1][server + 1] = locality for region on server
     */
    private int[][][] clusterRegionLocationMocks = new int[][][]{ // Test 1: each region is entirely on server that hosts it
    new int[][]{ new int[]{ 2, 1, 1 }, new int[]{ 2, 0, 0, 100 }// region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    , new int[]{ 0, 100, 0, 0 }// region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    , new int[]{ 0, 100, 0, 0 }// region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    , new int[]{ 1, 0, 100, 0 }// region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
     }, // Test 2: each region is 0% local on the server that hosts it
    new int[][]{ new int[]{ 1, 2, 1 }, new int[]{ 0, 0, 0, 100 }// region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    , new int[]{ 1, 100, 0, 0 }// region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    , new int[]{ 1, 100, 0, 0 }// region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    , new int[]{ 2, 0, 100, 0 }// region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
     }, // Test 3: each region is 25% local on the server that hosts it (and 50% locality is possible)
    new int[][]{ new int[]{ 1, 2, 1 }, new int[]{ 0, 25, 0, 50 }// region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    , new int[]{ 1, 50, 25, 0 }// region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    , new int[]{ 1, 50, 25, 0 }// region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    , new int[]{ 2, 0, 50, 25 }// region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
     }, // Test 4: each region is 25% local on the server that hosts it (and 100% locality is possible)
    new int[][]{ new int[]{ 1, 2, 1 }, new int[]{ 0, 25, 0, 100 }// region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    , new int[]{ 1, 100, 25, 0 }// region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    , new int[]{ 1, 100, 25, 0 }// region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    , new int[]{ 2, 0, 100, 25 }// region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
     }, // Test 5: each region is 75% local on the server that hosts it (and 75% locality is possible everywhere)
    new int[][]{ new int[]{ 1, 2, 1 }, new int[]{ 0, 75, 75, 75 }// region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    // region 0 is hosted and entirely local on server 2
    , new int[]{ 1, 75, 75, 75 }// region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    // region 1 is hosted and entirely on server 0
    , new int[]{ 1, 75, 75, 75 }// region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    // region 2 is hosted and entirely on server 0
    , new int[]{ 2, 75, 75, 75 }// region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
    // region 1 is hosted and entirely on server 1
     } };

    @Test
    public void testCPRequestCost() {
        // in order to pass needsBalance judgement
        BalancerTestBase.conf.setFloat("hbase.master.balancer.stochastic.cpRequestCost", 10000.0F);
        BalancerTestBase.loadBalancer.setConf(BalancerTestBase.conf);
        // mock cluster State
        Map<ServerName, List<RegionInfo>> clusterState = new HashMap<ServerName, List<RegionInfo>>();
        ServerName serverA = randomServer(3).getServerName();
        ServerName serverB = randomServer(3).getServerName();
        ServerName serverC = randomServer(3).getServerName();
        List<RegionInfo> regionsOnServerA = randomRegions(3);
        List<RegionInfo> regionsOnServerB = randomRegions(3);
        List<RegionInfo> regionsOnServerC = randomRegions(3);
        clusterState.put(serverA, regionsOnServerA);
        clusterState.put(serverB, regionsOnServerB);
        clusterState.put(serverC, regionsOnServerC);
        // mock ClusterMetrics
        Map<ServerName, ServerMetrics> serverMetricsMap = new TreeMap<>();
        serverMetricsMap.put(serverA, mockServerMetricsWithCpRequests(serverA, regionsOnServerA, 0));
        serverMetricsMap.put(serverB, mockServerMetricsWithCpRequests(serverB, regionsOnServerB, 0));
        serverMetricsMap.put(serverC, mockServerMetricsWithCpRequests(serverC, regionsOnServerC, 0));
        ClusterMetrics clusterStatus = Mockito.mock(ClusterMetrics.class);
        Mockito.when(clusterStatus.getLiveServerMetrics()).thenReturn(serverMetricsMap);
        BalancerTestBase.loadBalancer.setClusterMetrics(clusterStatus);
        // CPRequestCostFunction are Rate based, So doing setClusterMetrics again
        // this time, regions on serverA with more cpRequestCount load
        // serverA : 1000,1000,1000
        // serverB : 0,0,0
        // serverC : 0,0,0
        // so should move two regions from serverA to serverB & serverC
        serverMetricsMap = new TreeMap();
        serverMetricsMap.put(serverA, mockServerMetricsWithCpRequests(serverA, regionsOnServerA, 1000));
        serverMetricsMap.put(serverB, mockServerMetricsWithCpRequests(serverB, regionsOnServerB, 0));
        serverMetricsMap.put(serverC, mockServerMetricsWithCpRequests(serverC, regionsOnServerC, 0));
        clusterStatus = Mockito.mock(ClusterMetrics.class);
        Mockito.when(clusterStatus.getLiveServerMetrics()).thenReturn(serverMetricsMap);
        BalancerTestBase.loadBalancer.setClusterMetrics(clusterStatus);
        List<RegionPlan> plans = BalancerTestBase.loadBalancer.balanceCluster(clusterState);
        Set<RegionInfo> regionsMoveFromServerA = new HashSet<>();
        Set<ServerName> targetServers = new HashSet<>();
        for (RegionPlan plan : plans) {
            if (plan.getSource().equals(serverA)) {
                regionsMoveFromServerA.add(plan.getRegionInfo());
                targetServers.add(plan.getDestination());
            }
        }
        // should move 2 regions from serverA, one moves to serverB, the other moves to serverC
        Assert.assertEquals(2, regionsMoveFromServerA.size());
        Assert.assertEquals(2, targetServers.size());
        Assert.assertTrue(regionsOnServerA.containsAll(regionsMoveFromServerA));
        // reset config
        BalancerTestBase.conf.setFloat("hbase.master.balancer.stochastic.cpRequestCost", 5.0F);
        BalancerTestBase.loadBalancer.setConf(BalancerTestBase.conf);
    }

    @Test
    public void testKeepRegionLoad() throws Exception {
        ServerName sn = ServerName.valueOf("test:8080", 100);
        int numClusterStatusToAdd = 20000;
        for (int i = 0; i < numClusterStatusToAdd; i++) {
            ServerMetrics sl = Mockito.mock(ServerMetrics.class);
            RegionMetrics rl = Mockito.mock(RegionMetrics.class);
            Mockito.when(rl.getReadRequestCount()).thenReturn(0L);
            Mockito.when(rl.getCpRequestCount()).thenReturn(0L);
            Mockito.when(rl.getWriteRequestCount()).thenReturn(0L);
            Mockito.when(rl.getMemStoreSize()).thenReturn(ZERO);
            Mockito.when(rl.getStoreFileSize()).thenReturn(new org.apache.hadoop.hbase.Size(i, Unit.MEGABYTE));
            Map<byte[], RegionMetrics> regionLoadMap = new TreeMap(Bytes.BYTES_COMPARATOR);
            regionLoadMap.put(Bytes.toBytes(TestStochasticLoadBalancer.REGION_KEY), rl);
            Mockito.when(sl.getRegionMetrics()).thenReturn(regionLoadMap);
            ClusterMetrics clusterStatus = Mockito.mock(ClusterMetrics.class);
            Map<ServerName, ServerMetrics> serverMetricsMap = new TreeMap<>();
            serverMetricsMap.put(sn, sl);
            Mockito.when(clusterStatus.getLiveServerMetrics()).thenReturn(serverMetricsMap);
            // when(clusterStatus.getLoad(sn)).thenReturn(sl);
            BalancerTestBase.loadBalancer.setClusterMetrics(clusterStatus);
        }
        String regionNameAsString = RegionInfo.getRegionNameAsString(Bytes.toBytes(TestStochasticLoadBalancer.REGION_KEY));
        Assert.assertTrue(((TestStochasticLoadBalancer.loadBalancer.loads.get(regionNameAsString)) != null));
        Assert.assertTrue(((TestStochasticLoadBalancer.loadBalancer.loads.get(regionNameAsString).size()) == 15));
        Queue<BalancerRegionLoad> loads = TestStochasticLoadBalancer.loadBalancer.loads.get(regionNameAsString);
        int i = 0;
        while ((loads.size()) > 0) {
            BalancerRegionLoad rl = loads.remove();
            Assert.assertEquals((i + (numClusterStatusToAdd - 15)), rl.getStorefileSizeMB());
            i++;
        } 
    }

    @Test
    public void testNeedBalance() {
        float minCost = BalancerTestBase.conf.getFloat("hbase.master.balancer.stochastic.minCostNeedBalance", 0.05F);
        BalancerTestBase.conf.setFloat("hbase.master.balancer.stochastic.minCostNeedBalance", 1.0F);
        BalancerTestBase.loadBalancer.setConf(BalancerTestBase.conf);
        for (int[] mockCluster : clusterStateMocks) {
            Map<ServerName, List<RegionInfo>> servers = mockClusterServers(mockCluster);
            List<RegionPlan> plans = BalancerTestBase.loadBalancer.balanceCluster(servers);
            Assert.assertNull(plans);
        }
        // reset config
        BalancerTestBase.conf.setFloat("hbase.master.balancer.stochastic.minCostNeedBalance", minCost);
        BalancerTestBase.loadBalancer.setConf(BalancerTestBase.conf);
    }

    @Test
    public void testLocalityCost() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        MockNoopMasterServices master = new MockNoopMasterServices();
        StochasticLoadBalancer.CostFunction costFunction = new org.apache.hadoop.hbase.master.balancer.StochasticLoadBalancer.ServerLocalityCostFunction(conf, master);
        for (int test = 0; test < (clusterRegionLocationMocks.length); test++) {
            int[][] clusterRegionLocations = clusterRegionLocationMocks[test];
            TestStochasticLoadBalancer.MockCluster cluster = new TestStochasticLoadBalancer.MockCluster(clusterRegionLocations);
            costFunction.init(cluster);
            double cost = costFunction.cost();
            double expected = 1 - (expectedLocalities[test]);
            Assert.assertEquals(expected, cost, 0.001);
        }
    }

    @Test
    public void testMoveCost() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        StochasticLoadBalancer.CostFunction costFunction = new StochasticLoadBalancer.MoveCostFunction(conf);
        for (int[] mockCluster : clusterStateMocks) {
            BaseLoadBalancer.Cluster cluster = mockCluster(mockCluster);
            costFunction.init(cluster);
            double cost = costFunction.cost();
            Assert.assertEquals(0.0F, cost, 0.001);
            // cluster region number is smaller than maxMoves=600
            cluster.setNumRegions(200);
            cluster.setNumMovedRegions(10);
            cost = costFunction.cost();
            Assert.assertEquals(0.05F, cost, 0.001);
            cluster.setNumMovedRegions(100);
            cost = costFunction.cost();
            Assert.assertEquals(0.5F, cost, 0.001);
            cluster.setNumMovedRegions(200);
            cost = costFunction.cost();
            Assert.assertEquals(1.0F, cost, 0.001);
            // cluster region number is bigger than maxMoves=2500
            cluster.setNumRegions(10000);
            cluster.setNumMovedRegions(250);
            cost = costFunction.cost();
            Assert.assertEquals(0.1F, cost, 0.001);
            cluster.setNumMovedRegions(1250);
            cost = costFunction.cost();
            Assert.assertEquals(0.5F, cost, 0.001);
            cluster.setNumMovedRegions(2500);
            cost = costFunction.cost();
            Assert.assertEquals(1.0F, cost, 0.01);
        }
    }

    @Test
    public void testSkewCost() {
        Configuration conf = HBaseConfiguration.create();
        StochasticLoadBalancer.CostFunction costFunction = new StochasticLoadBalancer.RegionCountSkewCostFunction(conf);
        for (int[] mockCluster : clusterStateMocks) {
            costFunction.init(mockCluster(mockCluster));
            double cost = costFunction.cost();
            Assert.assertTrue((cost >= 0));
            Assert.assertTrue((cost <= 1.01));
        }
        costFunction.init(mockCluster(new int[]{ 0, 0, 0, 0, 1 }));
        Assert.assertEquals(0, costFunction.cost(), 0.01);
        costFunction.init(mockCluster(new int[]{ 0, 0, 0, 1, 1 }));
        Assert.assertEquals(0, costFunction.cost(), 0.01);
        costFunction.init(mockCluster(new int[]{ 0, 0, 1, 1, 1 }));
        Assert.assertEquals(0, costFunction.cost(), 0.01);
        costFunction.init(mockCluster(new int[]{ 0, 1, 1, 1, 1 }));
        Assert.assertEquals(0, costFunction.cost(), 0.01);
        costFunction.init(mockCluster(new int[]{ 1, 1, 1, 1, 1 }));
        Assert.assertEquals(0, costFunction.cost(), 0.01);
        costFunction.init(mockCluster(new int[]{ 10000, 0, 0, 0, 0 }));
        Assert.assertEquals(1, costFunction.cost(), 0.01);
    }

    @Test
    public void testCostAfterUndoAction() {
        final int runs = 10;
        BalancerTestBase.loadBalancer.setConf(BalancerTestBase.conf);
        for (int[] mockCluster : clusterStateMocks) {
            BaseLoadBalancer.Cluster cluster = mockCluster(mockCluster);
            BalancerTestBase.loadBalancer.initCosts(cluster);
            for (int i = 0; i != runs; ++i) {
                final double expectedCost = BalancerTestBase.loadBalancer.computeCost(cluster, Double.MAX_VALUE);
                Cluster.Action action = BalancerTestBase.loadBalancer.nextAction(cluster);
                cluster.doAction(action);
                BalancerTestBase.loadBalancer.updateCostsWithAction(cluster, action);
                Cluster.Action undoAction = action.undoAction();
                cluster.doAction(undoAction);
                BalancerTestBase.loadBalancer.updateCostsWithAction(cluster, undoAction);
                final double actualCost = BalancerTestBase.loadBalancer.computeCost(cluster, Double.MAX_VALUE);
                Assert.assertEquals(expectedCost, actualCost, 0);
            }
        }
    }

    @Test
    public void testTableSkewCost() {
        Configuration conf = HBaseConfiguration.create();
        StochasticLoadBalancer.CostFunction costFunction = new StochasticLoadBalancer.TableSkewCostFunction(conf);
        for (int[] mockCluster : clusterStateMocks) {
            BaseLoadBalancer.Cluster cluster = mockCluster(mockCluster);
            costFunction.init(cluster);
            double cost = costFunction.cost();
            Assert.assertTrue((cost >= 0));
            Assert.assertTrue((cost <= 1.01));
        }
    }

    @Test
    public void testRegionLoadCost() {
        List<BalancerRegionLoad> regionLoads = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            BalancerRegionLoad regionLoad = Mockito.mock(BalancerRegionLoad.class);
            Mockito.when(regionLoad.getReadRequestsCount()).thenReturn(new Long(i));
            Mockito.when(regionLoad.getCpRequestsCount()).thenReturn(new Long(i));
            Mockito.when(regionLoad.getStorefileSizeMB()).thenReturn(i);
            regionLoads.add(regionLoad);
        }
        Configuration conf = HBaseConfiguration.create();
        StochasticLoadBalancer.ReadRequestCostFunction readCostFunction = new StochasticLoadBalancer.ReadRequestCostFunction(conf);
        double rateResult = readCostFunction.getRegionLoadCost(regionLoads);
        // read requests are treated as a rate so the average rate here is simply 1
        Assert.assertEquals(1, rateResult, 0.01);
        StochasticLoadBalancer.CPRequestCostFunction cpCostFunction = new StochasticLoadBalancer.CPRequestCostFunction(conf);
        rateResult = cpCostFunction.getRegionLoadCost(regionLoads);
        // coprocessor requests are treated as a rate so the average rate here is simply 1
        Assert.assertEquals(1, rateResult, 0.01);
        StochasticLoadBalancer.StoreFileCostFunction storeFileCostFunction = new StochasticLoadBalancer.StoreFileCostFunction(conf);
        double result = storeFileCostFunction.getRegionLoadCost(regionLoads);
        // storefile size cost is simply an average of it's value over time
        Assert.assertEquals(2.5, result, 0.01);
    }

    @Test
    public void testCostFromArray() {
        Configuration conf = HBaseConfiguration.create();
        StochasticLoadBalancer.CostFromRegionLoadFunction costFunction = new StochasticLoadBalancer.MemStoreSizeCostFunction(conf);
        costFunction.init(mockCluster(new int[]{ 0, 0, 0, 0, 1 }));
        double[] statOne = new double[100];
        for (int i = 0; i < 100; i++) {
            statOne[i] = 10;
        }
        Assert.assertEquals(0, costFunction.costFromArray(statOne), 0.01);
        double[] statTwo = new double[101];
        for (int i = 0; i < 100; i++) {
            statTwo[i] = 0;
        }
        statTwo[100] = 100;
        Assert.assertEquals(1, costFunction.costFromArray(statTwo), 0.01);
        double[] statThree = new double[200];
        for (int i = 0; i < 100; i++) {
            statThree[i] = 0;
            statThree[(i + 100)] = 100;
        }
        Assert.assertEquals(0.5, costFunction.costFromArray(statThree), 0.01);
    }

    @Test
    public void testLosingRs() throws Exception {
        int numNodes = 3;
        int numRegions = 20;
        int numRegionsPerServer = 3;// all servers except one

        int replication = 1;
        int numTables = 2;
        Map<ServerName, List<RegionInfo>> serverMap = createServerMap(numNodes, numRegions, numRegionsPerServer, replication, numTables);
        List<ServerAndLoad> list = convertToList(serverMap);
        List<RegionPlan> plans = BalancerTestBase.loadBalancer.balanceCluster(serverMap);
        Assert.assertNotNull(plans);
        // Apply the plan to the mock cluster.
        List<ServerAndLoad> balancedCluster = reconcile(list, plans, serverMap);
        assertClusterAsBalanced(balancedCluster);
        ServerName sn = serverMap.keySet().toArray(new ServerName[serverMap.size()])[0];
        ServerName deadSn = ServerName.valueOf(sn.getHostname(), sn.getPort(), ((sn.getStartcode()) - 100));
        serverMap.put(deadSn, new ArrayList(0));
        plans = BalancerTestBase.loadBalancer.balanceCluster(serverMap);
        Assert.assertNull(plans);
    }

    // This mock allows us to test the LocalityCostFunction
    private class MockCluster extends BaseLoadBalancer.Cluster {
        private int[][] localities = null;// [region][server] = percent of blocks


        public MockCluster(int[][] regions) {
            // regions[0] is an array where index = serverIndex an value = number of regions
            super(mockClusterServers(regions[0], 1), null, null, null);
            localities = new int[(regions.length) - 1][];
            for (int i = 1; i < (regions.length); i++) {
                int regionIndex = i - 1;
                localities[regionIndex] = new int[(regions[i].length) - 1];
                regionIndexToServerIndex[regionIndex] = regions[i][0];
                for (int j = 1; j < (regions[i].length); j++) {
                    int serverIndex = j - 1;
                    localities[regionIndex][serverIndex] = ((regions[i][j]) > 100) ? (regions[i][j]) % 100 : regions[i][j];
                }
            }
        }

        @Override
        float getLocalityOfRegion(int region, int server) {
            // convert the locality percentage to a fraction
            return (localities[region][server]) / 100.0F;
        }

        @Override
        public int getRegionSizeMB(int region) {
            return 1;
        }
    }
}
