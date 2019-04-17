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
package org.apache.camel.component.jcr;


import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


public class JcrGetNodeByIdTest extends JcrRouteTestSupport {
    public static final String CONTENT = "content is here";

    public static final Boolean APPROVED = true;

    private String identifier;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @Test
    public void testJcrProducer() throws Exception {
        result.expectedMessageCount(1);
        result.expectedHeaderReceived("my.contents.property", JcrGetNodeByIdTest.CONTENT);
        result.expectedHeaderReceived("content.approved", JcrGetNodeByIdTest.APPROVED);
        Exchange exchange = createExchangeWithBody(identifier);
        template.send("direct:a", exchange);
        assertMockEndpointsSatisfied();
    }
}
