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
package org.apache.camel.component.aws.s3.integration;


import ExchangePattern.InOnly;
import ExchangePattern.InOut;
import S3Constants.KEY;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;


@Ignore("Must be manually tested. Provide your own accessKey and secretKey!")
public class S3ComponentIntegrationTest extends CamelTestSupport {
    @EndpointInject(uri = "direct:start")
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint result;

    @Test
    public void sendInOnly() throws Exception {
        result.expectedMessageCount(2);
        Exchange exchange1 = template.send("direct:start", InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(KEY, "CamelUnitTest1");
                exchange.getIn().setBody("This is my bucket content.");
            }
        });
        Exchange exchange2 = template.send("direct:start", InOnly, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(KEY, "CamelUnitTest2");
                exchange.getIn().setBody("This is my bucket content.");
            }
        });
        assertMockEndpointsSatisfied();
        assertResultExchange(result.getExchanges().get(0));
        assertResultExchange(result.getExchanges().get(1));
        assertResponseMessage(exchange1.getIn());
        assertResponseMessage(exchange2.getIn());
    }

    @Test
    public void sendInOut() throws Exception {
        result.expectedMessageCount(1);
        Exchange exchange = template.send("direct:start", InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(KEY, "CamelUnitTest");
                exchange.getIn().setBody("This is my bucket content.");
            }
        });
        assertMockEndpointsSatisfied();
        assertResultExchange(result.getExchanges().get(0));
        assertResponseMessage(exchange.getOut());
    }
}
