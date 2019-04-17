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
package org.apache.camel.issues;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.TestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


public class SplitWithCustomAggregationStrategyTest extends ContextTestSupport {
    @Test
    public void testSplitWithCustomAggregatorStrategy() throws Exception {
        int files = 10;
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(files);
        // no duplicates should be received
        mock.expectsNoDuplicates(TestSupport.body());
        for (int i = 0; i < files; i++) {
            template.sendBody("direct:start", "");
        }
        assertMockEndpointsSatisfied();
    }
}
