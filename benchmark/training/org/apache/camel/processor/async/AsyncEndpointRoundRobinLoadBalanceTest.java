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
package org.apache.camel.processor.async;


import org.apache.camel.ContextTestSupport;
import org.junit.Assert;
import org.junit.Test;


public class AsyncEndpointRoundRobinLoadBalanceTest extends ContextTestSupport {
    private static String beforeThreadName;

    private static String afterThreadName;

    @Test
    public void testAsyncEndpoint() throws Exception {
        getMockEndpoint("mock:before").expectedBodiesReceived("Hello Camel", "Hello World");
        getMockEndpoint("mock:after").expectedBodiesReceived("Bye Camel", "Bye World");
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye Camel", "Bye World");
        String reply = template.requestBody("direct:start", "Hello Camel", String.class);
        Assert.assertEquals("Bye Camel", reply);
        reply = template.requestBody("direct:start", "Hello World", String.class);
        Assert.assertEquals("Bye World", reply);
        assertMockEndpointsSatisfied();
        Assert.assertFalse("Should use different threads", AsyncEndpointRoundRobinLoadBalanceTest.beforeThreadName.equalsIgnoreCase(AsyncEndpointRoundRobinLoadBalanceTest.afterThreadName));
    }
}
