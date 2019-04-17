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
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AsyncEndpointRedeliveryErrorHandlerNonBlockedDelay2Test extends ContextTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncEndpointRedeliveryErrorHandlerNonBlockedDelay2Test.class);

    private static volatile int attempt;

    private static String beforeThreadName;

    private static String afterThreadName;

    @Test
    public void testRedelivery() throws Exception {
        MockEndpoint before = getMockEndpoint("mock:result");
        before.expectedBodiesReceived("World");
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedBodiesReceived("Hello Camel");
        template.sendBody("seda:start", "World");
        assertMockEndpointsSatisfied();
        Assert.assertFalse("Should use different threads", AsyncEndpointRedeliveryErrorHandlerNonBlockedDelay2Test.beforeThreadName.equalsIgnoreCase(AsyncEndpointRedeliveryErrorHandlerNonBlockedDelay2Test.afterThreadName));
    }
}
