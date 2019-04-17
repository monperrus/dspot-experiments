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


import Exchange.FILE_NAME;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


/**
 *
 */
public class SplitGroupMultiXmlTokenTest extends ContextTestSupport {
    @Test
    public void testTokenXMLPairGroup() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedMessageCount(3);
        mock.message(0).body().isEqualTo("<group><order id=\"1\" xmlns=\"http:acme.com\">Camel in Action</order><order id=\"2\" xmlns=\"http:acme.com\">ActiveMQ in Action</order></group>");
        mock.message(1).body().isEqualTo("<group><order id=\"3\" xmlns=\"http:acme.com\">Spring in Action</order><order id=\"4\" xmlns=\"http:acme.com\">Scala in Action</order></group>");
        mock.message(2).body().isEqualTo("<group><order id=\"5\" xmlns=\"http:acme.com\">Groovy in Action</order></group>");
        String body = createBody();
        template.sendBodyAndHeader("file:target/data/pair", body, FILE_NAME, "orders.xml");
        assertMockEndpointsSatisfied();
    }
}
