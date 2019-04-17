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
import org.junit.Test;


/**
 *
 */
public class MulticastNoOnPrepareTest extends ContextTestSupport {
    @Test
    public void testMulticastNoOnPrepare() throws Exception {
        getMockEndpoint("mock:a").expectedMessageCount(1);
        getMockEndpoint("mock:a").message(0).body(String.class).isEqualTo("1 Tony the Tiger");
        getMockEndpoint("mock:b").expectedMessageCount(1);
        // adjustment of processor a causes effects in processor b as its not a deep clone
        getMockEndpoint("mock:b").message(0).body(String.class).isEqualTo("1 Tony the Tiger");
        template.sendBody("direct:start", new Animal(1, "Tiger"));
        assertMockEndpointsSatisfied();
    }
}
