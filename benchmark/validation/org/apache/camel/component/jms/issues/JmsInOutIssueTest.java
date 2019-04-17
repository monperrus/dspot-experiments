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
package org.apache.camel.component.jms.issues;


import ExchangePattern.InOut;
import java.util.concurrent.Future;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class JmsInOutIssueTest extends CamelTestSupport {
    @Test
    public void testInOutWithRequestBody() throws Exception {
        String reply = template.requestBody("activemq:queue:in", "Hello World", String.class);
        assertEquals("Bye World", reply);
    }

    @Test
    public void testInOutTwoTimes() throws Exception {
        String reply = template.requestBody("activemq:queue:in", "Hello World", String.class);
        assertEquals("Bye World", reply);
        reply = template.requestBody("activemq:queue:in", "Hello Camel", String.class);
        assertEquals("Bye World", reply);
    }

    @Test
    public void testInOutWithAsyncRequestBody() throws Exception {
        Future<String> reply = template.asyncRequestBody("activemq:queue:in", "Hello World", String.class);
        assertEquals("Bye World", reply.get());
    }

    @Test
    public void testInOutWithSendExchange() throws Exception {
        Exchange out = template.send("activemq:queue:in", InOut, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody("Hello World");
            }
        });
        assertEquals("Bye World", out.getOut().getBody());
    }

    @Test
    public void testInOutWithAsyncSendExchange() throws Exception {
        Future<Exchange> out = template.asyncSend("activemq:queue:in", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(InOut);
                exchange.getIn().setBody("Hello World");
            }
        });
        assertEquals("Bye World", out.get().getOut().getBody());
    }
}
