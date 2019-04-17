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
package org.apache.camel.test.blueprint;


import org.junit.Test;


// START SNIPPET: example
// tag::example[]
// to use camel-test-blueprint, then extend the CamelBlueprintTestSupport class,
// and add your unit tests methods as shown below.
public class DebugBlueprintTest extends CamelBlueprintTestSupport {
    private boolean debugBeforeMethodCalled;

    private boolean debugAfterMethodCalled;

    // here we have regular JUnit @Test method
    @Test
    public void testRoute() throws Exception {
        // set mock expectations
        getMockEndpoint("mock:a").expectedMessageCount(1);
        // send a message
        template.sendBody("direct:start", "World");
        // assert mocks
        assertMockEndpointsSatisfied();
        // assert on the debugBefore/debugAfter methods below being called as we've enabled the debugger
        assertTrue(debugBeforeMethodCalled);
        assertTrue(debugAfterMethodCalled);
    }
}

/**
 * end::example[]
 */
/**
 * END SNIPPET: example
 */