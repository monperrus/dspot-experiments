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
import org.junit.Test;


/**
 *
 */
public class SplitTokenizerTest extends ContextTestSupport {
    @Test
    public void testSplitTokenizerA() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("Claus", "James", "Willem");
        template.sendBody("direct:a", "Claus,James,Willem");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerB() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("Claus", "James", "Willem");
        template.sendBodyAndHeader("direct:b", "Hello World", "myHeader", "Claus,James,Willem");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerC() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("Claus", "James", "Willem");
        template.sendBody("direct:c", "Claus James Willem");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerD() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("[Claus]", "[James]", "[Willem]");
        template.sendBody("direct:d", "[Claus][James][Willem]");
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerE() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("<person>Claus</person>", "<person>James</person>", "<person>Willem</person>");
        String xml = "<persons><person>Claus</person><person>James</person><person>Willem</person></persons>";
        template.sendBody("direct:e", xml);
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerEWithSlash() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        String xml = "<persons><person attr='/' /></persons>";
        mock.expectedBodiesReceived("<person attr='/' />");
        template.sendBody("direct:e", xml);
        mock.assertIsSatisfied();
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSplitTokenizerF() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:split");
        mock.expectedBodiesReceived("<person name=\"Claus\"/>", "<person>James</person>", "<person>Willem</person>");
        String xml = "<persons><person/><person name=\"Claus\"/><person>James</person><person>Willem</person></persons>";
        template.sendBody("direct:f", xml);
        assertMockEndpointsSatisfied();
    }
}
