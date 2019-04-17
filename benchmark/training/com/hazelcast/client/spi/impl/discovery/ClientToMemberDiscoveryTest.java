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
package com.hazelcast.client.spi.impl.discovery;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class ClientToMemberDiscoveryTest extends HazelcastTestSupport {
    public final TestHazelcastFactory factory = new TestHazelcastFactory();

    Config serverConfig;

    ClientConfig clientConfig;

    HazelcastInstance instance1;

    HazelcastInstance instance2;

    @Test
    public void clientTest() {
        instance1 = factory.newHazelcastInstance(serverConfig);
        instance2 = factory.newHazelcastInstance(serverConfig);
        HazelcastInstance client = factory.newHazelcastClient(clientConfig);
        assertClusterSizeEventually(2, client);
    }
}
