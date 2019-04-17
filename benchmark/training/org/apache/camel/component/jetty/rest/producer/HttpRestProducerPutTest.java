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
package org.apache.camel.component.jetty.rest.producer;


import org.apache.camel.component.jetty.BaseJettyTest;
import org.junit.Test;


public class HttpRestProducerPutTest extends BaseJettyTest {
    @Test
    public void testHttpProducerPut() throws Exception {
        getMockEndpoint("mock:input").expectedMessageCount(1);
        fluentTemplate.withBody("Donald Duck").withHeader("id", "123").to("direct:start").send();
        assertMockEndpointsSatisfied();
    }
}
