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
package com.hazelcast.internal.cluster.impl;


import GroupProperty.HEARTBEAT_INTERVAL_SECONDS;
import GroupProperty.MAX_JOIN_SECONDS;
import GroupProperty.MAX_NO_HEARTBEAT_SECONDS;
import GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.instance.Node;
import com.hazelcast.instance.NodeContext;
import com.hazelcast.instance.StaticMemberNodeContext;
import com.hazelcast.internal.cluster.MemberInfo;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.Connection;
import com.hazelcast.nio.ConnectionListener;
import com.hazelcast.nio.EndpointManager;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationService;
import com.hazelcast.spi.PostJoinAwareService;
import com.hazelcast.spi.PreJoinAwareService;
import com.hazelcast.spi.impl.NodeEngineImpl;
import com.hazelcast.spi.properties.GroupProperty;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.PacketFiltersUtil;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import com.hazelcast.util.UuidUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class MembershipUpdateTest extends HazelcastTestSupport {
    private TestHazelcastInstanceFactory factory;

    @Test
    public void sequential_member_join() {
        HazelcastInstance[] instances = new HazelcastInstance[4];
        for (int i = 0; i < (instances.length); i++) {
            instances[i] = factory.newHazelcastInstance();
        }
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(instances.length, instance);
        }
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(instances[0]);
        // version = number of started members
        Assert.assertEquals(instances.length, referenceMemberMap.getVersion());
        for (HazelcastInstance instance : instances) {
            MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
        }
    }

    @Test
    public void parallel_member_join() {
        final AtomicReferenceArray<HazelcastInstance> instances = new AtomicReferenceArray<HazelcastInstance>(4);
        for (int i = 0; i < (instances.length()); i++) {
            final int ix = i;
            HazelcastTestSupport.spawn(new Runnable() {
                @Override
                public void run() {
                    instances.set(ix, factory.newHazelcastInstance());
                }
            });
        }
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < (instances.length()); i++) {
                    HazelcastInstance instance = instances.get(i);
                    Assert.assertNotNull(instance);
                    HazelcastTestSupport.assertClusterSize(instances.length(), instance);
                }
            }
        });
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(instances.get(0));
        // version = number of started members
        Assert.assertEquals(instances.length(), referenceMemberMap.getVersion());
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
        }
    }

    @Test
    public void parallel_member_join_whenPostJoinOperationPresent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Config config = getConfigWithService(new MembershipUpdateTest.PostJoinAwareServiceImpl(latch), MembershipUpdateTest.PostJoinAwareServiceImpl.SERVICE_NAME);
        final AtomicReferenceArray<HazelcastInstance> instances = new AtomicReferenceArray<HazelcastInstance>(6);
        for (int i = 0; i < (instances.length()); i++) {
            final int ix = i;
            HazelcastTestSupport.spawn(new Runnable() {
                @Override
                public void run() {
                    instances.set(ix, factory.newHazelcastInstance(config));
                }
            });
        }
        // just a random latency
        HazelcastTestSupport.sleepSeconds(3);
        latch.countDown();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < (instances.length()); i++) {
                    HazelcastInstance instance = instances.get(i);
                    Assert.assertNotNull(instance);
                    HazelcastTestSupport.assertClusterSize(instances.length(), instance);
                }
            }
        });
    }

    @Test
    public void parallel_member_join_whenPreJoinOperationPresent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        MembershipUpdateTest.PreJoinAwareServiceImpl service = new MembershipUpdateTest.PreJoinAwareServiceImpl(latch);
        final Config config = getConfigWithService(service, MembershipUpdateTest.PreJoinAwareServiceImpl.SERVICE_NAME);
        final AtomicReferenceArray<HazelcastInstance> instances = new AtomicReferenceArray<HazelcastInstance>(6);
        for (int i = 0; i < (instances.length()); i++) {
            final int ix = i;
            HazelcastTestSupport.spawn(new Runnable() {
                @Override
                public void run() {
                    instances.set(ix, factory.newHazelcastInstance(config));
                }
            });
        }
        HazelcastTestSupport.sleepSeconds(3);
        latch.countDown();
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < (instances.length()); i++) {
                    HazelcastInstance instance = instances.get(i);
                    Assert.assertNotNull(instance);
                    HazelcastTestSupport.assertClusterSize(instances.length(), instance);
                }
            }
        });
    }

    @Test
    public void sequential_member_join_and_removal() {
        HazelcastInstance[] instances = new HazelcastInstance[4];
        for (int i = 0; i < (instances.length); i++) {
            instances[i] = factory.newHazelcastInstance();
        }
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(instances.length, instance);
        }
        instances[((instances.length) - 1)].shutdown();
        for (int i = 0; i < ((instances.length) - 1); i++) {
            HazelcastInstance instance = instances[i];
            HazelcastTestSupport.assertClusterSizeEventually(((instances.length) - 1), instance);
        }
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(instances[0]);
        // version = number of started members + 1 removal
        Assert.assertEquals(((instances.length) + 1), referenceMemberMap.getVersion());
        for (int i = 0; i < ((instances.length) - 1); i++) {
            HazelcastInstance instance = instances[i];
            MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
        }
    }

    @Test
    public void sequential_member_join_and_restart() {
        HazelcastInstance[] instances = new HazelcastInstance[3];
        for (int i = 0; i < (instances.length); i++) {
            instances[i] = factory.newHazelcastInstance();
        }
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(instances.length, instance);
        }
        instances[((instances.length) - 1)].shutdown();
        instances[((instances.length) - 1)] = factory.newHazelcastInstance();
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(instances.length, instance);
        }
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(instances[0]);
        // version = number of started members + 1 removal + 1 start
        Assert.assertEquals(((instances.length) + 2), referenceMemberMap.getVersion());
        for (HazelcastInstance instance : instances) {
            MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
        }
    }

    @Test
    public void parallel_member_join_and_removal() {
        final AtomicReferenceArray<HazelcastInstance> instances = new AtomicReferenceArray<HazelcastInstance>(4);
        for (int i = 0; i < (instances.length()); i++) {
            final int ix = i;
            HazelcastTestSupport.spawn(new Runnable() {
                @Override
                public void run() {
                    instances.set(ix, factory.newHazelcastInstance());
                }
            });
        }
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < (instances.length()); i++) {
                    HazelcastInstance instance = instances.get(i);
                    Assert.assertNotNull(instance);
                    HazelcastTestSupport.assertClusterSize(instances.length(), instance);
                }
            }
        });
        for (int i = 0; i < (instances.length()); i++) {
            if (HazelcastTestSupport.getNode(instances.get(i)).isMaster()) {
                continue;
            }
            instances.getAndSet(i, null).shutdown();
            break;
        }
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            if (instance != null) {
                HazelcastTestSupport.assertClusterSizeEventually(((instances.length()) - 1), instance);
            }
        }
        HazelcastInstance master = null;
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            if ((instance != null) && (HazelcastTestSupport.getNode(instance).isMaster())) {
                master = instance;
                break;
            }
        }
        Assert.assertNotNull(master);
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(master);
        // version = number of started members + 1 removal
        Assert.assertEquals(((instances.length()) + 1), referenceMemberMap.getVersion());
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            if (instance != null) {
                MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
                MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
            }
        }
    }

    @Test
    public void parallel_member_join_and_restart() {
        final AtomicReferenceArray<HazelcastInstance> instances = new AtomicReferenceArray<HazelcastInstance>(3);
        for (int i = 0; i < (instances.length()); i++) {
            final int ix = i;
            HazelcastTestSupport.spawn(new Runnable() {
                @Override
                public void run() {
                    instances.set(ix, factory.newHazelcastInstance());
                }
            });
        }
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                for (int i = 0; i < (instances.length()); i++) {
                    HazelcastInstance instance = instances.get(i);
                    Assert.assertNotNull(instance);
                    HazelcastTestSupport.assertClusterSize(instances.length(), instance);
                }
            }
        });
        for (int i = 0; i < (instances.length()); i++) {
            if (HazelcastTestSupport.getNode(instances.get(i)).isMaster()) {
                continue;
            }
            instances.get(i).shutdown();
            instances.set(i, factory.newHazelcastInstance());
            break;
        }
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            HazelcastTestSupport.assertClusterSizeEventually(instances.length(), instance);
        }
        HazelcastInstance master = null;
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            if (HazelcastTestSupport.getNode(instances.get(i)).isMaster()) {
                master = instance;
                break;
            }
        }
        Assert.assertNotNull(master);
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(master);
        // version = number of started members + 1 removal + 1 start
        Assert.assertEquals(((instances.length()) + 2), referenceMemberMap.getVersion());
        for (int i = 0; i < (instances.length()); i++) {
            HazelcastInstance instance = instances.get(i);
            MemberMap memberMap = MembershipUpdateTest.getMemberMap(instance);
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, memberMap);
        }
    }

    @Test
    public void memberListsConverge_whenMemberUpdateMissed() {
        Config config = new Config();
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(2, hz1, hz2);
        PacketFiltersUtil.rejectOperationsFrom(hz1, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(3, hz1, hz3);
        HazelcastTestSupport.assertClusterSize(2, hz2);
        PacketFiltersUtil.resetPacketFiltersFrom(hz1);
        ClusterServiceImpl clusterService = ((ClusterServiceImpl) (HazelcastTestSupport.getClusterService(hz1)));
        clusterService.getMembershipManager().sendMemberListToMember(HazelcastTestSupport.getAddress(hz2));
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(hz1);
        MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(hz2));
        MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(hz3));
    }

    @Test
    public void memberListsConverge_whenMemberUpdateMissed_withPeriodicUpdates() {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), "5");
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(2, hz1, hz2);
        PacketFiltersUtil.rejectOperationsFrom(hz1, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(3, hz1, hz3);
        HazelcastTestSupport.assertClusterSize(2, hz2);
        PacketFiltersUtil.resetPacketFiltersFrom(hz1);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(hz1);
        MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(hz2));
        MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(hz3));
    }

    @Test
    public void memberListsConverge_whenMembershipUpdatesSent_outOfOrder() {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), "1");
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        PacketFiltersUtil.delayOperationsFrom(hz1, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastInstance hz4 = factory.newHazelcastInstance(config);
        HazelcastInstance hz5 = factory.newHazelcastInstance(config);
        HazelcastInstance[] instances = new HazelcastInstance[]{ hz1, hz2, hz3, hz4, hz5 };
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(5, instance);
        }
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(hz1);
        for (HazelcastInstance instance : instances) {
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(instance));
        }
    }

    @Test
    public void memberListsConverge_whenFinalizeJoinAndMembershipUpdatesSent_outOfOrder() {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), "1");
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        PacketFiltersUtil.delayOperationsFrom(hz1, ClusterDataSerializerHook.F_ID, Arrays.asList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE, ClusterDataSerializerHook.FINALIZE_JOIN));
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastInstance hz4 = factory.newHazelcastInstance(config);
        HazelcastInstance hz5 = factory.newHazelcastInstance(config);
        HazelcastInstance[] instances = new HazelcastInstance[]{ hz1, hz2, hz3, hz4, hz5 };
        for (HazelcastInstance instance : instances) {
            HazelcastTestSupport.assertClusterSizeEventually(5, instance);
        }
        MemberMap referenceMemberMap = MembershipUpdateTest.getMemberMap(hz1);
        for (HazelcastInstance instance : instances) {
            MembershipUpdateTest.assertMemberViewsAreSame(referenceMemberMap, MembershipUpdateTest.getMemberMap(instance));
        }
    }

    @Test
    public void memberListsConverge_whenExistingMemberMissesMemberRemove_withPeriodicUpdates() {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), "1");
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(3, hz1, hz3);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        PacketFiltersUtil.dropOperationsBetween(hz1, hz3, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        hz2.getLifecycleService().terminate();
        HazelcastTestSupport.assertClusterSizeEventually(2, hz1);
        HazelcastTestSupport.assertClusterSize(3, hz3);
        PacketFiltersUtil.resetPacketFiltersFrom(hz1);
        HazelcastTestSupport.assertClusterSizeEventually(2, hz3);
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz3));
    }

    @Test
    public void memberListsConverge_whenExistingMemberMissesMemberRemove_afterNewMemberJoins() {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), String.valueOf(Integer.MAX_VALUE));
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSize(3, hz1, hz3);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        PacketFiltersUtil.dropOperationsBetween(hz1, hz3, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        hz2.getLifecycleService().terminate();
        HazelcastTestSupport.assertClusterSizeEventually(2, hz1);
        HazelcastTestSupport.assertClusterSize(3, hz3);
        PacketFiltersUtil.resetPacketFiltersFrom(hz1);
        HazelcastInstance hz4 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz3);
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz3));
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz4));
    }

    @Test
    public void memberReceives_memberUpdateNotContainingItself() throws Exception {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), String.valueOf(Integer.MAX_VALUE));
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        Node node = HazelcastTestSupport.getNode(hz1);
        ClusterServiceImpl clusterService = node.getClusterService();
        MembershipManager membershipManager = clusterService.getMembershipManager();
        MembersView membersView = MembersView.createNew(((membershipManager.getMemberListVersion()) + 1), Arrays.asList(membershipManager.getMember(HazelcastTestSupport.getAddress(hz1)), membershipManager.getMember(HazelcastTestSupport.getAddress(hz2))));
        Operation memberUpdate = new com.hazelcast.internal.cluster.impl.operations.MembersUpdateOp(membershipManager.getMember(HazelcastTestSupport.getAddress(hz3)).getUuid(), membersView, clusterService.getClusterTime(), null, true);
        memberUpdate.setCallerUuid(node.getThisUuid());
        Future<Object> future = node.getNodeEngine().getOperationService().invokeOnTarget(null, memberUpdate, HazelcastTestSupport.getAddress(hz3));
        try {
            future.get();
            Assert.fail("Membership update should fail!");
        } catch (ExecutionException e) {
            Assert.assertTrue(((e.getCause()) instanceof IllegalArgumentException));
        }
    }

    @Test
    public void memberReceives_memberUpdateFromInvalidMaster() throws Exception {
        Config config = new Config();
        config.setProperty(GroupProperty.MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), String.valueOf(Integer.MAX_VALUE));
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
        Node node = HazelcastTestSupport.getNode(hz1);
        ClusterServiceImpl clusterService = node.getClusterService();
        MembershipManager membershipManager = clusterService.getMembershipManager();
        MemberInfo newMemberInfo = new MemberInfo(new Address("127.0.0.1", 6000), UuidUtil.newUnsecureUuidString(), Collections.<String, Object>emptyMap(), node.getVersion());
        MembersView membersView = MembersView.cloneAdding(membershipManager.getMembersView(), Collections.singleton(newMemberInfo));
        Operation memberUpdate = new com.hazelcast.internal.cluster.impl.operations.MembersUpdateOp(membershipManager.getMember(HazelcastTestSupport.getAddress(hz3)).getUuid(), membersView, clusterService.getClusterTime(), null, true);
        NodeEngineImpl nonMasterNodeEngine = HazelcastTestSupport.getNodeEngineImpl(hz2);
        memberUpdate.setCallerUuid(nonMasterNodeEngine.getNode().getThisUuid());
        Future<Object> future = nonMasterNodeEngine.getOperationService().invokeOnTarget(null, memberUpdate, HazelcastTestSupport.getAddress(hz3));
        future.get();
        // member update should not be applied
        HazelcastTestSupport.assertClusterSize(3, hz1, hz2, hz3);
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz2));
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz3));
    }

    @Test
    public void memberListOrder_shouldBeSame_whenMemberRestartedWithSameIdentity() {
        Config configMaster = new Config();
        configMaster.setProperty(MEMBER_LIST_PUBLISH_INTERVAL_SECONDS.getName(), "5");
        // Needed only on master to prevent accepting stale join requests.
        // See ClusterJoinManager#checkRecentlyJoinedMemberUuidBeforeJoin(target, uuid)
        configMaster.setProperty(MAX_JOIN_SECONDS.getName(), "5");
        final HazelcastInstance hz1 = factory.newHazelcastInstance(configMaster);
        final HazelcastInstance hz2 = factory.newHazelcastInstance();
        HazelcastInstance hz3 = factory.newHazelcastInstance();
        HazelcastInstance hz4 = factory.newHazelcastInstance();
        HazelcastTestSupport.assertClusterSizeEventually(4, hz2, hz3);
        PacketFiltersUtil.rejectOperationsBetween(hz1, hz2, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.MEMBER_INFO_UPDATE));
        final MemberImpl member3 = HazelcastTestSupport.getNode(hz3).getLocalMember();
        hz3.getLifecycleService().terminate();
        HazelcastTestSupport.assertClusterSizeEventually(3, hz1, hz4);
        HazelcastTestSupport.assertClusterSize(4, hz2);
        hz3 = HazelcastInstanceFactory.newHazelcastInstance(TestHazelcastInstanceFactory.initOrCreateConfig(new Config()), HazelcastTestSupport.randomName(), new StaticMemberNodeContext(factory, member3));
        HazelcastTestSupport.assertClusterSizeEventually(4, hz1, hz4);
        PacketFiltersUtil.resetPacketFiltersFrom(hz1);
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz3));
        MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz4));
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                MembershipUpdateTest.assertMemberViewsAreSame(MembershipUpdateTest.getMemberMap(hz1), MembershipUpdateTest.getMemberMap(hz2));
            }
        });
    }

    @Test
    public void shouldNotProcessStaleJoinRequest() {
        HazelcastInstance hz1 = factory.newHazelcastInstance();
        HazelcastInstance hz2 = factory.newHazelcastInstance();
        JoinRequest staleJoinReq = HazelcastTestSupport.getNode(hz2).createJoinRequest(true);
        hz2.shutdown();
        HazelcastTestSupport.assertClusterSizeEventually(1, hz1);
        ClusterServiceImpl clusterService = ((ClusterServiceImpl) (HazelcastTestSupport.getClusterService(hz1)));
        clusterService.getClusterJoinManager().handleJoinRequest(staleJoinReq, null);
        HazelcastTestSupport.assertClusterSize(1, hz1);
    }

    @Test
    public void memberJoinsEventually_whenMemberRestartedWithSameUuid_butMasterDoesNotNoticeItsLeave() throws Exception {
        Config configMaster = new Config();
        // Needed only on master to prevent accepting stale join requests.
        // See ClusterJoinManager#checkRecentlyJoinedMemberUuidBeforeJoin(target, uuid)
        configMaster.setProperty(MAX_JOIN_SECONDS.getName(), "5");
        final HazelcastInstance hz1 = factory.newHazelcastInstance(configMaster);
        final HazelcastInstance hz2 = factory.newHazelcastInstance();
        final HazelcastInstance hz3 = factory.newHazelcastInstance();
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2, hz3);
        final MemberImpl member3 = HazelcastTestSupport.getNode(hz3).getLocalMember();
        // A new member is restarted with member3's UUID.
        // Then after some time, member3 is terminated.
        // This is to emulate the case, member3 is restarted with preserving its UUID (using hot-restart),
        // but master does not realize its leave in time.
        // When master realizes, member3 is terminated,
        // new member should eventually join the cluster.
        Future<HazelcastInstance> future = HazelcastTestSupport.spawn(new Callable<HazelcastInstance>() {
            @Override
            public HazelcastInstance call() {
                NodeContext nodeContext = new StaticMemberNodeContext(factory, member3.getUuid(), factory.nextAddress());
                return HazelcastInstanceFactory.newHazelcastInstance(TestHazelcastInstanceFactory.initOrCreateConfig(new Config()), HazelcastTestSupport.randomName(), nodeContext);
            }
        });
        HazelcastTestSupport.spawn(new Runnable() {
            @Override
            public void run() {
                HazelcastTestSupport.sleepSeconds(5);
                hz3.getLifecycleService().terminate();
            }
        });
        HazelcastInstance hz4 = future.get();
        HazelcastTestSupport.assertClusterSize(3, hz1, hz4);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz2);
    }

    // On a joining member assert that no operations are executed before pre join operations execution is completed.
    @Test
    public void noOperationExecuted_beforePreJoinOpIsDone() {
        CountDownLatch latch = new CountDownLatch(1);
        MembershipUpdateTest.PreJoinAwareServiceImpl service = new MembershipUpdateTest.PreJoinAwareServiceImpl(latch);
        final Config config = getConfigWithService(service, MembershipUpdateTest.PreJoinAwareServiceImpl.SERVICE_NAME);
        HazelcastInstance instance1 = factory.newHazelcastInstance(config);
        final Address instance2Address = factory.nextAddress();
        final OperationService operationService = HazelcastTestSupport.getNode(instance1).getNodeEngine().getOperationService();
        // send operations from master to joining member. The master has already added the joining member to its member list
        // while the FinalizeJoinOp is being executed on joining member, so it might send operations to the joining member.
        Future sendOpsFromMaster = HazelcastTestSupport.spawn(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        MembershipUpdateTest.ExecutionTrackerOp op = new MembershipUpdateTest.ExecutionTrackerOp();
                        operationService.send(op, instance2Address);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    LockSupport.parkNanos(1);
                } 
            }
        });
        final AtomicReference<HazelcastInstance> instanceReference = new AtomicReference<HazelcastInstance>(null);
        HazelcastTestSupport.spawn(new Runnable() {
            @Override
            public void run() {
                instanceReference.set(factory.newHazelcastInstance(instance2Address, config));
            }
        });
        HazelcastTestSupport.sleepSeconds(10);
        // on latch countdown, the pre-join op completes
        latch.countDown();
        HazelcastTestSupport.sleepSeconds(5);
        sendOpsFromMaster.cancel(true);
        Assert.assertFalse(service.otherOpExecutedBeforePreJoin.get());
    }

    @Test
    public void joiningMemberShouldShutdown_whenExceptionDeserializingPreJoinOp() {
        Config config = getConfigWithService(new MembershipUpdateTest.FailingPreJoinOpService(), MembershipUpdateTest.FailingPreJoinOpService.SERVICE_NAME);
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        // joining member fails while deserializing pre-join op and should shutdown
        try {
            factory.newHazelcastInstance(config);
            Assert.fail("Second HazelcastInstance should not have started");
        } catch (IllegalStateException e) {
            // expected
        }
        HazelcastTestSupport.assertClusterSize(1, hz1);
    }

    @Test
    public void joiningMemberShouldShutdown_whenExceptionDeserializingPostJoinOp() {
        Config config = getConfigWithService(new MembershipUpdateTest.FailingPostJoinOpService(), MembershipUpdateTest.FailingPostJoinOpService.SERVICE_NAME);
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        // joining member fails while deserializing post-join op and should shutdown
        try {
            factory.newHazelcastInstance(config);
            Assert.fail("Second HazelcastInstance should not have started");
        } catch (IllegalStateException e) {
            // expected
        }
        HazelcastTestSupport.assertClusterSize(1, hz1);
    }

    @Test
    public void connectionsToTerminatedMember_shouldBeClosed() {
        HazelcastInstance hz1 = factory.newHazelcastInstance();
        HazelcastInstance hz2 = factory.newHazelcastInstance();
        HazelcastInstance hz3 = factory.newHazelcastInstance();
        HazelcastTestSupport.assertClusterSizeEventually(3, hz1, hz2, hz3);
        final Address target = HazelcastTestSupport.getAddress(hz2);
        hz2.getLifecycleService().terminate();
        HazelcastTestSupport.assertClusterSizeEventually(2, hz1, hz3);
        Assert.assertNull(HazelcastTestSupport.getEndpointManager(hz1).getConnection(target));
        final EndpointManager cm3 = HazelcastTestSupport.getEndpointManager(hz3);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                Assert.assertNull(cm3.getConnection(target));
            }
        });
    }

    @Test
    public void connectionsToRemovedMember_shouldBeClosed() {
        Config config = new Config().setProperty(MAX_NO_HEARTBEAT_SECONDS.getName(), "10").setProperty(HEARTBEAT_INTERVAL_SECONDS.getName(), "1");
        HazelcastInstance hz1 = factory.newHazelcastInstance(config);
        HazelcastInstance hz2 = factory.newHazelcastInstance(config);
        HazelcastInstance hz3 = factory.newHazelcastInstance(config);
        HazelcastTestSupport.assertClusterSizeEventually(3, hz1, hz2, hz3);
        final Address target = HazelcastTestSupport.getAddress(hz3);
        MembershipUpdateTest.ConnectionRemovedListener connListener1 = new MembershipUpdateTest.ConnectionRemovedListener(target);
        HazelcastTestSupport.getEndpointManager(hz1).addConnectionListener(connListener1);
        MembershipUpdateTest.ConnectionRemovedListener connListener2 = new MembershipUpdateTest.ConnectionRemovedListener(target);
        HazelcastTestSupport.getEndpointManager(hz2).addConnectionListener(connListener2);
        PacketFiltersUtil.dropOperationsBetween(hz3, hz1, ClusterDataSerializerHook.F_ID, Collections.singletonList(ClusterDataSerializerHook.HEARTBEAT));
        HazelcastTestSupport.assertClusterSizeEventually(2, hz1, hz2);
        connListener1.assertConnectionRemoved();
        connListener2.assertConnectionRemoved();
    }

    private static class PostJoinAwareServiceImpl implements PostJoinAwareService {
        static final String SERVICE_NAME = "post-join-service";

        final CountDownLatch latch;

        private PostJoinAwareServiceImpl(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Operation getPostJoinOperation() {
            return new MembershipUpdateTest.TimeConsumingPostJoinOperation();
        }
    }

    private static class TimeConsumingPostJoinOperation extends Operation {
        @Override
        public void run() throws Exception {
            MembershipUpdateTest.PostJoinAwareServiceImpl service = getService();
            service.latch.await();
        }

        @Override
        public String getServiceName() {
            return MembershipUpdateTest.PostJoinAwareServiceImpl.SERVICE_NAME;
        }
    }

    private static class PreJoinAwareServiceImpl implements PreJoinAwareService {
        static final String SERVICE_NAME = "pre-join-service";

        final CountDownLatch latch;

        final AtomicBoolean preJoinOpExecutionCompleted = new AtomicBoolean();

        final AtomicBoolean otherOpExecutedBeforePreJoin = new AtomicBoolean();

        private PreJoinAwareServiceImpl(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public Operation getPreJoinOperation() {
            return new MembershipUpdateTest.TimeConsumingPreJoinOperation();
        }
    }

    private static class TimeConsumingPreJoinOperation extends Operation {
        @Override
        public void run() throws Exception {
            MembershipUpdateTest.PreJoinAwareServiceImpl service = getService();
            service.latch.await();
            service.preJoinOpExecutionCompleted.set(true);
        }

        @Override
        public String getServiceName() {
            return MembershipUpdateTest.PreJoinAwareServiceImpl.SERVICE_NAME;
        }
    }

    private static class ExecutionTrackerOp extends Operation {
        @Override
        public void run() throws Exception {
            MembershipUpdateTest.PreJoinAwareServiceImpl preJoinAwareService = getService();
            if (!(preJoinAwareService.preJoinOpExecutionCompleted.get())) {
                preJoinAwareService.otherOpExecutedBeforePreJoin.set(true);
            }
        }

        @Override
        public boolean returnsResponse() {
            return false;
        }

        @Override
        public String getServiceName() {
            return MembershipUpdateTest.PreJoinAwareServiceImpl.SERVICE_NAME;
        }
    }

    private static class FailingPreJoinOpService implements PreJoinAwareService {
        static final String SERVICE_NAME = "failing-pre-join-service";

        @Override
        public Operation getPreJoinOperation() {
            return new MembershipUpdateTest.FailsDeserializationOperation();
        }
    }

    private static class FailingPostJoinOpService implements PostJoinAwareService {
        static final String SERVICE_NAME = "failing-post-join-service";

        @Override
        public Operation getPostJoinOperation() {
            return new MembershipUpdateTest.FailsDeserializationOperation();
        }
    }

    public static class FailsDeserializationOperation extends Operation {
        @Override
        public void run() throws Exception {
        }

        @Override
        protected void readInternal(ObjectDataInput in) throws IOException {
            throw new RuntimeException("This operation always fails during deserialization");
        }
    }

    private static class ConnectionRemovedListener implements ConnectionListener {
        private final Address endpoint;

        private final CountDownLatch latch = new CountDownLatch(1);

        ConnectionRemovedListener(Address endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void connectionAdded(Connection connection) {
        }

        @Override
        public void connectionRemoved(Connection connection) {
            if (endpoint.equals(connection.getEndPoint())) {
                latch.countDown();
            }
        }

        void assertConnectionRemoved() {
            HazelcastTestSupport.assertOpenEventually(latch);
        }
    }
}
