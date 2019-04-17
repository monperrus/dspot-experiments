/**
 * Copyright 2016 LinkedIn Corp. All rights reserved.
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
 */
package com.github.ambry.router;


import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.github.ambry.clustermap.MockDataNodeId;
import com.github.ambry.clustermap.MockPartitionId;
import com.github.ambry.clustermap.ReplicaId;
import com.github.ambry.network.Port;
import com.github.ambry.network.PortType;
import com.github.ambry.utils.MockTime;
import com.github.ambry.utils.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Unit test for the following operation trackers
 * 1. {@link SimpleOperationTracker}
 * 2. {@link AdaptiveOperationTracker}
 *
 * The status of an operation is represented as in the following format:
 *
 * local unsent count-local inflight count-local succeeded count-local failed count;
 * remote unsent count-remote inflight count-remote succeeded count-remote failed count
 *
 * For example: 3-0-0-0; 9-0-0-0
 *
 * The number of replicas for most tests is 12.
 */
@RunWith(Parameterized.class)
public class OperationTrackerTest {
    private static final int PORT = 6666;

    private static final String SIMPLE_OP_TRACKER = "Simple";

    private static final String ADAPTIVE_OP_TRACKER = "Adaptive";

    private static final double QUANTILE = 0.9;

    private final String operationTrackerType;

    private List<MockDataNodeId> datanodes;

    private MockPartitionId mockPartition;

    private String localDcName;

    private String originatingDcName;

    private final LinkedList<ReplicaId> inflightReplicas = new LinkedList<>();

    private final Set<ReplicaId> repetitionTracker = new HashSet<>();

    // for AdaptiveOperationTracker
    private final Time time = new MockTime();

    private final MetricRegistry registry = new MetricRegistry();

    private final Histogram localColoTracker = registry.histogram("LocalColoTracker");

    private final Histogram crossColoTracker = registry.histogram("CrossColoTracker");

    private final Counter pastDueCounter = registry.counter("PastDueCounter");

    /**
     *
     *
     * @param operationTrackerType
     * 		the type of {@link OperationTracker} that needs to be used in tests
     */
    public OperationTrackerTest(String operationTrackerType) {
        this.operationTrackerType = operationTrackerType;
    }

    /**
     * crossColoEnabled = false, successTarget = 2, parallelism = 3.
     *
     * <p/>
     * 1. Get 3 local replicas to send request (and send requests);
     * 2. 2 replicas succeeds.
     * 3. Operation succeeds.
     * 4. 1 local fails.
     * 5. Operation remains succeeded.
     */
    @Test
    public void localSucceedTest() {
        initialize();
        OperationTracker ot = getOperationTracker(false, 2, 3, true, Integer.MAX_VALUE);
        // 3-0-0-0; 9-0-0-0
        Assert.assertFalse("Operation should not have been done.", ot.isDone());
        sendRequests(ot, 3, false);
        // 0-3-0-0; 9-0-0-0
        Assert.assertFalse("Operation should not have been done.", ot.isDone());
        for (int i = 0; i < 2; i++) {
            ot.onResponse(inflightReplicas.poll(), true);
        }
        // 0-1-2-0; 9-0-0-0
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
        ot.onResponse(inflightReplicas.poll(), false);
        // 0-0-2-1; 9-0-0-0
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * crossColoEnabled = false, successTarget = 2, parallelism = 3.
     *
     * <p/>
     * 1. Get 3 local replicas to send request (and send requests);
     * 2. 1 local replicas succeeded, 2 failed.
     * 3. Operation fails.
     */
    @Test
    public void localFailTest() {
        initialize();
        OperationTracker ot = getOperationTracker(false, 2, 3, true, Integer.MAX_VALUE);
        // 3-0-0-0; 9-0-0-0
        Assert.assertFalse("Operation should not have been done.", ot.isDone());
        sendRequests(ot, 3, false);
        // 0-3-0-0; 9-0-0-0
        for (int i = 0; i < 2; i++) {
            ot.onResponse(inflightReplicas.poll(), false);
        }
        Assert.assertFalse("Operation should not have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
        // 0-1-0-2; 9-0-0-0
        ot.onResponse(inflightReplicas.poll(), true);
        // 0-0-1-2; 9-0-0-0
        Assert.assertFalse("Operation should not have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * crossColoEnabled = true, successTarget = 1, parallelism = 2.
     *
     * <p/>
     * 1. Get 2 local replicas to send request (and send requests);
     * 2. 1 fails? 1 pending.
     * 3. Get 1 more local replicas to send request (and send requests);
     * 4. 1 succeeds.
     * 5. Operation succeeds.
     */
    @Test
    public void localSucceedWithDifferentParameterTest() {
        initialize();
        OperationTracker ot = getOperationTracker(true, 1, 2, true, Integer.MAX_VALUE);
        // 3-0-0-0; 9-0-0-0
        sendRequests(ot, 2, false);
        // 1-2-0-0; 9-0-0-0
        ot.onResponse(inflightReplicas.poll(), false);
        // 1-1-0-1; 9-0-0-0
        Assert.assertFalse("Operation should not have been done.", ot.isDone());
        sendRequests(ot, 1, false);
        // 0-2-0-1; 9-0-0-0
        Assert.assertFalse("Operation should not be done", ot.isDone());
        ot.onResponse(inflightReplicas.poll(), true);
        // 0-1-1-1; 9-0-0-0
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * crossColoEnabled = true, successTarget = 1, parallelism = 2.
     *
     * <p/>
     * 1. Get 2 local replicas to send request (and send requests);
     * 2. 1 local replica fails, 1 pending.
     * 3. Get 1 more local replicas to send request (and send requests);
     * 4. 2 local replica fails.
     * 5. Get 1 remote replica from each Dc to send request (and send requests);
     * 6. All fails.
     * 7. Get 1 remote replica from each DC to send request (and send requests);
     * 8. 1 fails, 2 pending.
     * 9. Get 1 remote replica from each DC to send request (and send requests);
     * 10. 2 fails.
     * 11. 1 succeeds.
     * 12. Operation succeeds.
     */
    @Test
    public void remoteReplicaTest() {
        initialize();
        OperationTracker ot = getOperationTracker(true, 1, 2, true, Integer.MAX_VALUE);
        // 3-0-0-0; 9-0-0-0
        sendRequests(ot, 2, false);
        // 1-2-0-0; 9-0-0-0
        ReplicaId id = inflightReplicas.poll();
        Assert.assertEquals("First request should have been to local DC", localDcName, id.getDataNodeId().getDatacenterName());
        ot.onResponse(id, false);
        // 1-1-0-1; 9-0-0-0
        Assert.assertFalse("Operation should not be done", ot.isDone());
        sendRequests(ot, 1, false);
        // 0-2-0-1; 9-0-0-0
        id = inflightReplicas.poll();
        Assert.assertEquals("Second request should have been to local DC", localDcName, id.getDataNodeId().getDatacenterName());
        ot.onResponse(id, false);
        id = inflightReplicas.poll();
        Assert.assertEquals("Third request should have been to local DC", localDcName, id.getDataNodeId().getDatacenterName());
        ot.onResponse(id, false);
        // 0-0-0-3; 9-0-0-0
        Assert.assertFalse("Operation should not be done", ot.isDone());
        sendRequests(ot, 2, false);
        // 0-0-0-3; 7-2-0-0
        Assert.assertFalse("Operation should not be done", ot.isDone());
        for (int i = 0; i < 2; i++) {
            ot.onResponse(inflightReplicas.poll(), false);
        }
        // 0-0-0-3; 7-0-0-2
        Assert.assertFalse("Operation should not be done", ot.isDone());
        sendRequests(ot, 2, false);
        // 0-0-0-3; 5-2-0-2
        ot.onResponse(inflightReplicas.poll(), false);
        Assert.assertFalse("Operation should not be done", ot.isDone());
        // 0-0-0-3; 5-1-0-3
        sendRequests(ot, 1, false);
        // 0-0-0-3; 4-1-0-3
        ot.onResponse(inflightReplicas.poll(), true);
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * crossColoEnabled = true, successTarget = 12, parallelism = 3.
     *
     * This test may be meaningful for DELETE operation.
     *
     * <p/>
     * 1. Get 3 local replicas to send request (and send requests);
     * 2. 3 succeeded.
     * 3. Operation succeeded.
     */
    @Test
    public void fullSuccessTargetTest() {
        initialize();
        OperationTracker ot = getOperationTracker(true, 12, 3, true, Integer.MAX_VALUE);
        while (!(ot.hasSucceeded())) {
            sendRequests(ot, 3, false);
            for (int i = 0; i < 3; i++) {
                ot.onResponse(inflightReplicas.poll(), true);
            }
        } 
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * crossColoEnabled = true, successTarget = 1, parallelism = 2.
     * Only 4 local replicas
     *
     * 1. Get 1st local replica to send request (and sent);
     * 2. Get 2nd local replica to send request (and failed to send);
     * 3. Get 3rd local replica to send request (and sent);
     * 4. Receive 2 failed responses from the 1st and 3rd replicas;
     * 5. Get again 2nd local replica to send request (and sent);
     * 6. Get 4th local replica to send request (and failed to send);
     * 7. Receive 1 failed responses from the 2nd replicas;
     * 8. Get again 4th local replica to send request (and sent);
     * 9. Receive 1 successful response from the 4th replica;
     * 10. Operation succeeds.
     */
    @Test
    public void useReplicaNotSucceededSendTest() {
        int replicaCount = 4;
        List<Port> portList = Collections.singletonList(new Port(OperationTrackerTest.PORT, PortType.PLAINTEXT));
        List<String> mountPaths = Collections.singletonList("mockMountPath");
        datanodes = Collections.singletonList(new MockDataNodeId(portList, mountPaths, "dc-0"));
        mockPartition = new MockPartitionId();
        populateReplicaList(replicaCount);
        localDcName = datanodes.get(0).getDatacenterName();
        OperationTracker ot = getOperationTracker(true, 1, 2, true, Integer.MAX_VALUE);
        sendRequests(ot, 2, true);
        ot.onResponse(inflightReplicas.poll(), false);
        ot.onResponse(inflightReplicas.poll(), false);
        Assert.assertFalse("Operation should not be done", ot.isDone());
        sendRequests(ot, 1, true);
        ot.onResponse(inflightReplicas.poll(), false);
        Assert.assertFalse("Operation should not be done", ot.isDone());
        sendRequests(ot, 1, false);
        ot.onResponse(inflightReplicas.poll(), true);
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * Test to ensure that replicas that are down are also returned by the operation tracker, but they are
     * ordered after the healthy replicas.
     */
    @Test
    public void downReplicasOrderingTest() {
        List<Port> portList = Collections.singletonList(new Port(OperationTrackerTest.PORT, PortType.PLAINTEXT));
        List<String> mountPaths = Collections.singletonList("mockMountPath");
        datanodes = new ArrayList();
        datanodes.add(new MockDataNodeId(portList, mountPaths, "dc-0"));
        datanodes.add(new MockDataNodeId(portList, mountPaths, "dc-1"));
        mockPartition = new MockPartitionId();
        int replicaCount = 6;
        populateReplicaList(replicaCount);
        // Test scenarios with various number of replicas down
        for (int i = 0; i < replicaCount; i++) {
            testReplicaDown(replicaCount, i);
        }
    }

    /**
     * Test to ensure all replicas are candidates if originatingDcName is unknown.
     */
    @Test
    public void replicasOrderingTestOriginatingUnknown() {
        initialize();
        originatingDcName = null;
        OperationTracker ot = getOperationTracker(true, 3, 3, false, 6);
        sendRequests(ot, 3, false);
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, false);
        }
        sendRequests(ot, 3, false);
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, false);
        }
        Assert.assertEquals("Should have 0 replica in flight.", 0, inflightReplicas.size());
        Assert.assertFalse("Operation should have not succeeded", ot.hasSucceeded());
        Assert.assertFalse("Operation should have not been done", ot.isDone());
        sendRequests(ot, 3, false);
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, true);
        }
        Assert.assertEquals("Should have 0 replica in flight.", 0, inflightReplicas.size());
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * Test to ensure that replicas in originating DC are first priority when originating DC is local DC.
     */
    @Test
    public void replicasOrderingTestOriginatingIsLocal() {
        initialize();
        originatingDcName = localDcName;
        OperationTracker ot = getOperationTracker(true, 3, 3, false, 6);
        sendRequests(ot, 3, false);
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, true);
            Assert.assertEquals("Should be originating DC", originatingDcName, replica.getDataNodeId().getDatacenterName());
        }
        Assert.assertEquals("Should have 0 replica in flight.", 0, inflightReplicas.size());
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * Test to ensure that replicas in originating DC are right after local DC replicas.
     */
    @Test
    public void replicasOrderingTestOriginatingNotLocal() {
        initialize();
        originatingDcName = datanodes.get(((datanodes.size()) - 1)).getDatacenterName();
        OperationTracker ot = getOperationTracker(true, 3, 6, false, 6);
        sendRequests(ot, 6, false);
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, false);// fail first 3 requests to local

            Assert.assertEquals("Should be local DC", localDcName, replica.getDataNodeId().getDatacenterName());
        }
        Assert.assertFalse("Operation should have not succeeded", ot.hasSucceeded());
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, true);
            Assert.assertEquals("Should be originating DC", originatingDcName, replica.getDataNodeId().getDatacenterName());
        }
        Assert.assertEquals("Should have 0 replica in flight.", 0, inflightReplicas.size());
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * Test to ensure that replicas are exactly 6 when includeNonOriginatingDcReplicas is false.
     * 3 local replicas in the beginning and 3 originating replicas afterwards.
     */
    @Test
    public void replicasOrderTestOriginatingDcOnly() {
        initialize();
        originatingDcName = datanodes.get(((datanodes.size()) - 1)).getDatacenterName();
        OperationTracker ot = getOperationTracker(true, 3, 9, false, 6);
        sendRequests(ot, 6, false);
        Assert.assertEquals("Should have 6 replicas", 6, inflightReplicas.size());
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, false);// fail first 3 requests to local replicas

            Assert.assertEquals("Should be local DC", localDcName, replica.getDataNodeId().getDatacenterName());
        }
        Assert.assertFalse("Operation should have not succeeded", ot.hasSucceeded());
        for (int i = 0; i < 3; i++) {
            ReplicaId replica = inflightReplicas.poll();
            ot.onResponse(replica, true);
            Assert.assertEquals("Should be originating DC", originatingDcName, replica.getDataNodeId().getDatacenterName());
        }
        Assert.assertEquals("Should have 0 replica in flight.", 0, inflightReplicas.size());
        Assert.assertTrue("Operation should have succeeded", ot.hasSucceeded());
        Assert.assertTrue("Operation should be done", ot.isDone());
    }

    /**
     * Tests the case when the success target > number of replicas.
     */
    @Test
    public void notEnoughReplicasToMeetTargetTest() {
        initialize();
        try {
            getOperationTracker(true, 13, 3, true, Integer.MAX_VALUE);
            Assert.fail("Should have failed to construct tracker because success target > replica count");
        } catch (IllegalArgumentException e) {
            // expected. Nothing to do.
        }
    }

    /**
     * Tests the case when parallelism < 1
     */
    @Test
    public void incorrectParallelismTest() {
        initialize();
        for (int parallelism : Arrays.asList(0, (-1))) {
            try {
                getOperationTracker(true, 13, 0, true, Integer.MAX_VALUE);
                Assert.fail(("Should have failed to construct tracker because parallelism is " + parallelism));
            } catch (IllegalArgumentException e) {
                // expected. Nothing to do.
            }
        }
    }
}
