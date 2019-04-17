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
package org.apache.camel.processor.aggregator;


import org.apache.camel.ContextTestSupport;
import org.junit.Test;


public class AggregateDslTest extends ContextTestSupport {
    @Test
    public void testAggregate() throws Exception {
        getMockEndpoint("mock:aggregated").expectedBodiesReceived("0,3", "1,4", "2,5");
        getMockEndpoint("mock:aggregated-supplier").expectedBodiesReceived("0,3,6", "1,4,7", "2,5,8");
        for (int i = 0; i < 9; i++) {
            template.sendBodyAndHeader("direct:start", i, "type", (i % 3));
            template.sendBodyAndHeader("direct:start-supplier", i, "type", (i % 3));
        }
        assertMockEndpointsSatisfied();
    }
}
