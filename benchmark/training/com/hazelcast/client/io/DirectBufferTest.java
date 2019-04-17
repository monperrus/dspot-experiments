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
package com.hazelcast.client.io;


import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.HazelcastParametersRunnerFactory;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * A test that verifies that the Client can deal with various permutations of direct-buffers.
 */
@Category(QuickTest.class)
@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(HazelcastParametersRunnerFactory.class)
public class DirectBufferTest extends HazelcastTestSupport {
    @Parameterized.Parameter(0)
    public boolean memberDirectBuffer;

    @Parameterized.Parameter(1)
    public boolean clientDirectBuffer;

    private HazelcastInstance client;

    private HazelcastInstance server;

    @Test
    public void test() {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.setProperty(SOCKET_BUFFER_DIRECT.getName(), ("" + (memberDirectBuffer)));
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setProperty(SOCKET_CLIENT_BUFFER_DIRECT.getName(), ("" + (clientDirectBuffer)));
        server = Hazelcast.newHazelcastInstance(config);
        client = HazelcastClient.newHazelcastClient(clientConfig);
        List<byte[]> values = new LinkedList<byte[]>();
        IMap<Integer, byte[]> map = client.getMap("foo");
        for (int k = 0; k < 24; k++) {
            byte[] value = DirectBufferTest.randomByteArray(((int) (Math.pow(2, k))));
            values.add(value);
            map.put(k, value);
        }
        for (int k = 0; k < (values.size()); k++) {
            byte[] expected = values.get(k);
            Assert.assertArrayEquals(expected, map.get(k));
        }
    }
}
