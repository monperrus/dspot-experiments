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
package com.hazelcast.client.connection.nio;


import ClientProperty.SHUFFLE_MEMBER_LIST;
import LifecycleEvent.LifecycleState.CLIENT_CONNECTED;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.ClientTestSupport;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.nio.Address;
import com.hazelcast.test.HazelcastParametersRunnerFactory;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(HazelcastParametersRunnerFactory.class)
@Category({ QuickTest.class, ParallelTest.class })
public class ConnectMemberListOrderTest extends ClientTestSupport {
    private TestHazelcastFactory factory = new TestHazelcastFactory();

    @Parameterized.Parameter
    public String shuffleMemberList;

    @Test
    public void testPossibleMemberAddressesAfterDisconnection() throws Exception {
        HazelcastInstance instance = newHazelcastInstance();
        ClientConfig config = new ClientConfig();
        config.setProperty(SHUFFLE_MEMBER_LIST.getName(), shuffleMemberList);
        final CountDownLatch connectedBack = new CountDownLatch(2);
        config.addListenerConfig(new ListenerConfig(new LifecycleListener() {
            @Override
            public void stateChanged(LifecycleEvent event) {
                if (CLIENT_CONNECTED.equals(event.getState())) {
                    connectedBack.countDown();
                }
            }
        }));
        HazelcastInstance client = factory.newHazelcastClient(config);
        factory.newHazelcastInstance();
        factory.newHazelcastInstance();
        makeSureConnectedToServers(client, 3);
        Address lastConnectedMemberAddress = new Address(((InetSocketAddress) (instance.getLocalEndpoint().getSocketAddress())));
        instance.shutdown();
        assertOpenEventually(connectedBack);
        Collection<Address> possibleMemberAddresses = getPossibleMemberAddresses(client);
        // make sure last known member list is used. otherwise it returns 2
        Assert.assertEquals(3, possibleMemberAddresses.size());
        // make sure previous owner is not first one to tried in next owner connection selection
        Assert.assertNotEquals(((("possibleMemberAddresses : " + possibleMemberAddresses) + ", lastConnectedMemberAddress ") + lastConnectedMemberAddress), possibleMemberAddresses.iterator().next(), lastConnectedMemberAddress);
    }
}
