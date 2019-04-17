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
package org.apache.camel.dataformat.bindy.fix;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.kvp.BindyKeyValuePairDataFormat;
import org.apache.camel.dataformat.bindy.model.fix.simple.Order;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration
public class BindySimpleKeyValuePairMarshallTest extends AbstractJUnit4SpringContextTests {
    private List<Map<String, Object>> models = new ArrayList<>();

    private String result = "1=BE.CHM.001\u00018=FIX 4.1\u00019=20\u000110=220\u000111=CHM0001-01\u000122=4\u000134=1\u000135=0\u000148=BE0001245678\u000149=INVMGR\u000154=1\u000156=BRKR\u000158=this is a camel - bindy test\u0001\r\n";

    @Produce(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint resultEndpoint;

    @Test
    public void testMarshallMessage() throws Exception {
        resultEndpoint.expectedBodiesReceived(result);
        template.sendBody(generateModel());
        resultEndpoint.assertIsSatisfied();
    }

    public static class ContextConfig extends RouteBuilder {
        BindyKeyValuePairDataFormat camelDataFormat = new BindyKeyValuePairDataFormat(Order.class);

        public void configure() {
            from("direct:start").marshal(camelDataFormat).to("mock:result");
        }
    }
}
