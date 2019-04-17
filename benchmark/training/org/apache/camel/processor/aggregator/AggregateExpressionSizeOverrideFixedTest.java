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


import Exchange.AGGREGATED_COMPLETED_BY;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.ContextTestSupport;
import org.junit.Test;


public class AggregateExpressionSizeOverrideFixedTest extends ContextTestSupport {
    @Test
    public void testAggregateExpressionSize() throws Exception {
        getMockEndpoint("mock:aggregated").expectedBodiesReceived("A+B+C");
        getMockEndpoint("mock:aggregated").expectedPropertyReceived(AGGREGATED_COMPLETED_BY, "size");
        Map<String, Object> headers = new HashMap<>();
        headers.put("id", 123);
        headers.put("mySize", 3);
        template.sendBodyAndHeaders("direct:start", "A", headers);
        template.sendBodyAndHeaders("direct:start", "B", headers);
        template.sendBodyAndHeaders("direct:start", "C", headers);
        assertMockEndpointsSatisfied();
    }
}
