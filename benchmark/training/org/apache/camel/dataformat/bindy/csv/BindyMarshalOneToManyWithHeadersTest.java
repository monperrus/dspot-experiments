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
package org.apache.camel.dataformat.bindy.csv;


import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.dataformat.bindy.model.simple.linkonetomany.Order;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration
public class BindyMarshalOneToManyWithHeadersTest extends AbstractJUnit4SpringContextTests {
    private static final String URI_MOCK_RESULT = "mock:result";

    private static final String URI_MOCK_ERROR = "mock:error";

    private static final String URI_DIRECT_START = "direct:start";

    private String expected;

    @Produce(uri = BindyMarshalOneToManyWithHeadersTest.URI_DIRECT_START)
    private ProducerTemplate template;

    @EndpointInject(uri = BindyMarshalOneToManyWithHeadersTest.URI_MOCK_RESULT)
    private MockEndpoint result;

    @Test
    @DirtiesContext
    public void testMarshallMessage() throws Exception {
        expected = "orderNumber,customerName,sku,quantity,unitPrice\r\n" + ("11111,Joe Blow,abc,1,3\r\n" + "11111,Joe Blow,cde,3,2\r\n");
        result.expectedBodiesReceived(expected);
        template.sendBody(generateModel());
        result.assertIsSatisfied();
    }

    public static class ContextConfig extends RouteBuilder {
        public void configure() {
            BindyCsvDataFormat camelDataFormat = new BindyCsvDataFormat(Order.class);
            camelDataFormat.setLocale("en");
            // default should errors go to mock:error
            errorHandler(deadLetterChannel(BindyMarshalOneToManyWithHeadersTest.URI_MOCK_ERROR).redeliveryDelay(0));
            onException(Exception.class).maximumRedeliveries(0).handled(true);
            from(BindyMarshalOneToManyWithHeadersTest.URI_DIRECT_START).marshal(camelDataFormat).to(BindyMarshalOneToManyWithHeadersTest.URI_MOCK_RESULT);
        }
    }
}
