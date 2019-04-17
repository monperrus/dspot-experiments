/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl;


import org.apache.camel.ContextTestSupport;
import org.junit.Assert;
import org.junit.Test;


public class EndpointRegistryKeepRouteEndpointsTest extends ContextTestSupport {
    @Test
    public void testEndpointRegistryKeepRouteEndpoints() throws Exception {
        Assert.assertTrue(((context.hasEndpoint("direct://start")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://foo")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://bar")) != null));
        Assert.assertTrue(((context.hasEndpoint("mock://result")) != null));
        // we dont have this endpoint yet
        Assert.assertFalse(((context.hasEndpoint("mock://unknown0")) != null));
        for (int i = 0; i < 50; i++) {
            template.sendBody(("mock:unknown" + i), ("Hello " + i));
        }
        // the eviction is async so force cleanup
        context.getEndpointRegistry().cleanUp();
        // endpoints from routes is always kept in the cache
        Assert.assertTrue(((context.hasEndpoint("direct://start")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://foo")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://bar")) != null));
        Assert.assertTrue(((context.hasEndpoint("mock://result")) != null));
        // and the dynamic cache only keeps 20 dynamic endpoints
        int count = 0;
        for (int i = 0; i < 50; i++) {
            String uri = "mock://unknown" + i;
            if ((context.hasEndpoint(uri)) != null) {
                count++;
                // and it should be dynamic
                Assert.assertTrue(context.getEndpointRegistry().isDynamic(uri));
            }
        }
        Assert.assertEquals("Should only be 20 dynamic endpoints in the cache", 20, count);
        // we should have 4 static, 20 dynamic and 24 in total
        Assert.assertEquals(4, context.getEndpointRegistry().staticSize());
        Assert.assertTrue(context.getEndpointRegistry().isStatic("direct://start"));
        Assert.assertEquals(20, context.getEndpointRegistry().dynamicSize());
        Assert.assertEquals(24, context.getEndpointRegistry().size());
        // and we can browse all 24
        Assert.assertEquals(24, context.getEndpoints().size());
        // and if we purge only the dynamic is removed
        context.getEndpointRegistry().purge();
        Assert.assertEquals(4, context.getEndpointRegistry().staticSize());
        Assert.assertEquals(0, context.getEndpointRegistry().dynamicSize());
        Assert.assertEquals(4, context.getEndpointRegistry().size());
        // endpoints from routes is always kept in the cache
        Assert.assertTrue(((context.hasEndpoint("direct://start")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://foo")) != null));
        Assert.assertTrue(((context.hasEndpoint("log://bar")) != null));
        Assert.assertTrue(((context.hasEndpoint("mock://result")) != null));
    }
}
