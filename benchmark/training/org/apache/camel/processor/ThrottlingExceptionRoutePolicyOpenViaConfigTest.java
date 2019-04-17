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
import org.apache.camel.throttling.ThrottlingExceptionRoutePolicy;
import org.junit.Test;


public class ThrottlingExceptionRoutePolicyOpenViaConfigTest extends ContextTestSupport {
    private String url = "seda:foo?concurrentConsumers=20";

    private MockEndpoint result;

    private int size = 5;

    private ThrottlingExceptionRoutePolicy policy;

    @Test
    public void testThrottlingRoutePolicyStartWithAlwaysOpenOffThenToggle() throws Exception {
        // send first set of messages
        // should go through b/c circuit is closed
        for (int i = 0; i < (size); i++) {
            template.sendBody(url, ("MessageRound1 " + i));
            Thread.sleep(3);
        }
        result.expectedMessageCount(size);
        result.setResultWaitTime(1000);
        assertMockEndpointsSatisfied();
        // set keepOpen to true
        policy.setKeepOpen(true);
        // trigger opening circuit
        // by sending another message
        template.sendBody(url, "MessageTrigger");
        // give time for circuit to open
        Thread.sleep(500);
        // send next set of messages
        // should NOT go through b/c circuit is open
        for (int i = 0; i < (size); i++) {
            template.sendBody(url, ("MessageRound2 " + i));
            Thread.sleep(3);
        }
        // gives time for policy half open check to run every second
        // and should not close b/c keepOpen is true
        Thread.sleep(500);
        result.expectedMessageCount(((size) + 1));
        result.setResultWaitTime(1000);
        assertMockEndpointsSatisfied();
        // set keepOpen to false
        policy.setKeepOpen(false);
        // gives time for policy half open check to run every second
        // and it should close b/c keepOpen is false
        result.expectedMessageCount((((size) * 2) + 1));
        result.setResultWaitTime(1000);
        assertMockEndpointsSatisfied();
    }
}
