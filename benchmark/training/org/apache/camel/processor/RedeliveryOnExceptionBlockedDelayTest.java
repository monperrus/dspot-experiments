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
package org.apache.camel.processor;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RedeliveryOnExceptionBlockedDelayTest extends ContextTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(RedeliveryOnExceptionBlockedDelayTest.class);

    private static volatile int attempt;

    @Test
    public void testRedelivery() throws Exception {
        MockEndpoint before = getMockEndpoint("mock:result");
        before.expectedBodiesReceived("Hello World", "Hello Camel");
        // we use blocked redelivery delay so the messages arrive in the same order
        MockEndpoint result = getMockEndpoint("mock:result");
        result.expectedBodiesReceived("Hello World", "Hello Camel");
        template.sendBody("seda:start", "World");
        template.sendBody("seda:start", "Camel");
        assertMockEndpointsSatisfied();
    }
}
