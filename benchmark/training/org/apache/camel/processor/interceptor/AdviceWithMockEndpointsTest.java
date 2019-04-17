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
package org.apache.camel.processor.interceptor;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.reifier.RouteReifier;
import org.junit.Assert;
import org.junit.Test;


// end::route[]
// END SNIPPET: route
public class AdviceWithMockEndpointsTest extends ContextTestSupport {
    @Test
    public void testNoAdvised() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
    }

    // START SNIPPET: e1
    // tag::e1[]
    @Test
    public void testAdvisedMockEndpoints() throws Exception {
        // advice the first route using the inlined AdviceWith route builder
        // which has extended capabilities than the regular route builder
        RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                // mock all endpoints
                mockEndpoints();
            }
        });
        getMockEndpoint("mock:direct:start").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:direct:foo").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:log:foo").expectedBodiesReceived("Bye World");
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
        // additional test to ensure correct endpoints in registry
        Assert.assertNotNull(context.hasEndpoint("direct:start"));
        Assert.assertNotNull(context.hasEndpoint("direct:foo"));
        Assert.assertNotNull(context.hasEndpoint("log:foo"));
        Assert.assertNotNull(context.hasEndpoint("mock:result"));
        // all the endpoints was mocked
        Assert.assertNotNull(context.hasEndpoint("mock:direct:start"));
        Assert.assertNotNull(context.hasEndpoint("mock:direct:foo"));
        Assert.assertNotNull(context.hasEndpoint("mock:log:foo"));
    }

    // end::e1[]
    // END SNIPPET: e1
    // START SNIPPET: e2
    // tag::e2[]
    @Test
    public void testAdvisedMockEndpointsWithPattern() throws Exception {
        // advice the first route using the inlined AdviceWith route builder
        // which has extended capabilities than the regular route builder
        RouteReifier.adviceWith(context.getRouteDefinitions().get(0), context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                // mock only log endpoints
                mockEndpoints("log*");
            }
        });
        // now we can refer to log:foo as a mock and set our expectations
        getMockEndpoint("mock:log:foo").expectedBodiesReceived("Bye World");
        getMockEndpoint("mock:result").expectedBodiesReceived("Bye World");
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
        // additional test to ensure correct endpoints in registry
        Assert.assertNotNull(context.hasEndpoint("direct:start"));
        Assert.assertNotNull(context.hasEndpoint("direct:foo"));
        Assert.assertNotNull(context.hasEndpoint("log:foo"));
        Assert.assertNotNull(context.hasEndpoint("mock:result"));
        // only the log:foo endpoint was mocked
        Assert.assertNotNull(context.hasEndpoint("mock:log:foo"));
        Assert.assertNull(context.hasEndpoint("mock:direct:start"));
        Assert.assertNull(context.hasEndpoint("mock:direct:foo"));
    }
}
