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
package org.apache.camel.processor.intercept;


import Exchange.INTERCEPTED_ENDPOINT;
import org.apache.camel.ContextTestSupport;
import org.junit.Test;


/**
 * Testing http://camel.apache.org/dsl.html
 */
public class InterceptFromUriWildcardTest extends ContextTestSupport {
    @Test
    public void testNoIntercept() throws Exception {
        getMockEndpoint("mock:intercept").expectedMessageCount(0);
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInterceptFoo() throws Exception {
        getMockEndpoint("mock:intercept").expectedMessageCount(1);
        getMockEndpoint("mock:intercept").expectedHeaderReceived(INTERCEPTED_ENDPOINT, "seda://foo");
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("seda:foo", "Hello World");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInterceptBar() throws Exception {
        getMockEndpoint("mock:intercept").expectedMessageCount(1);
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("seda:bar", "Hello World");
        assertMockEndpointsSatisfied();
    }
}
