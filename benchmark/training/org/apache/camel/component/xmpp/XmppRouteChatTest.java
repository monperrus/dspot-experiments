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
package org.apache.camel.component.xmpp;


import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;


@Ignore("This test is flaky on CI server")
public class XmppRouteChatTest extends CamelTestSupport {
    protected MockEndpoint consumerEndpoint;

    protected MockEndpoint producerEndpoint;

    protected String body1 = "the first message";

    protected String body2 = "the second message";

    private EmbeddedXmppTestServer embeddedXmppTestServer;

    @Test
    public void testXmppChat() throws Exception {
        consumerEndpoint = context.getEndpoint("mock:out1", MockEndpoint.class);
        producerEndpoint = context.getEndpoint("mock:out2", MockEndpoint.class);
        consumerEndpoint.expectedBodiesReceived(body1, body2);
        producerEndpoint.expectedBodiesReceived(body1, body2);
        // will send chat messages to the consumer
        template.sendBody("direct:toConsumer", body1);
        Thread.sleep(50);
        template.sendBody("direct:toConsumer", body2);
        template.sendBody("direct:toProducer", body1);
        Thread.sleep(50);
        template.sendBody("direct:toProducer", body2);
        consumerEndpoint.assertIsSatisfied();
        producerEndpoint.assertIsSatisfied();
    }
}
