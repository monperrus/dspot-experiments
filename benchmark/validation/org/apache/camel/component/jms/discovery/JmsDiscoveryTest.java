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
package org.apache.camel.component.jms.discovery;


import java.util.HashMap;
import java.util.Map;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class JmsDiscoveryTest extends CamelTestSupport {
    protected MyRegistry myRegistry = new MyRegistry();

    @Test
    public void testDiscovery() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.setResultWaitTime(5000);
        // force shutdown after 5 seconds as otherwise the bean will keep generating a new input
        context.getShutdownStrategy().setTimeout(5);
        assertMockEndpointsSatisfied();
        // sleep a little
        Thread.sleep(1000);
        Map<String, Map<?, ?>> map = new HashMap<>(myRegistry.getServices());
        assertTrue(("There should be 1 or more, was: " + (map.size())), ((map.size()) >= 1));
    }
}
