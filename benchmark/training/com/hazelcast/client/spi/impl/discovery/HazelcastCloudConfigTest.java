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


import ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN;
import HazelcastCloudDiscovery.CLOUD_URL_BASE_PROPERTY;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.ClientTestSupport;
import com.hazelcast.spi.properties.HazelcastProperties;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class HazelcastCloudConfigTest extends ClientTestSupport {
    @Test
    public void testCustomCloudUrlEndpoint() {
        ClientConfig config = new ClientConfig();
        String token = "TOKEN";
        config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), token);
        config.setProperty(CLOUD_URL_BASE_PROPERTY.getName(), "https://dev.hazelcast.cloud");
        HazelcastProperties hazelcastProperties = new HazelcastProperties(config.getProperties());
        String cloudUrlBase = hazelcastProperties.getString(CLOUD_URL_BASE_PROPERTY);
        String urlEndpoint = HazelcastCloudDiscovery.createUrlEndpoint(cloudUrlBase, token);
        Assert.assertEquals("https://dev.hazelcast.cloud/cluster/discovery?token=TOKEN", urlEndpoint);
    }

    @Test
    public void testDefaultCloudUrlEndpoint() {
        ClientConfig config = new ClientConfig();
        String token = "TOKEN";
        config.setProperty(HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), token);
        HazelcastProperties hazelcastProperties = new HazelcastProperties(config.getProperties());
        String cloudUrlBase = hazelcastProperties.getString(CLOUD_URL_BASE_PROPERTY);
        String urlEndpoint = HazelcastCloudDiscovery.createUrlEndpoint(cloudUrlBase, token);
        Assert.assertEquals("https://coordinator.hazelcast.cloud/cluster/discovery?token=TOKEN", urlEndpoint);
    }
}
