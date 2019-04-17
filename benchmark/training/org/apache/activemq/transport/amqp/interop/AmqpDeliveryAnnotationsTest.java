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
package org.apache.activemq.transport.amqp.interop;


import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpClientTestSupport;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpReceiver;
import org.apache.activemq.transport.amqp.client.AmqpSender;
import org.apache.activemq.transport.amqp.client.AmqpSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Test around the handling of Deliver Annotations in messages sent and received.
 */
@RunWith(Parameterized.class)
public class AmqpDeliveryAnnotationsTest extends AmqpClientTestSupport {
    private final String DELIVERY_ANNOTATION_NAME = "TEST-DELIVERY-ANNOTATION";

    private final String transformer;

    public AmqpDeliveryAnnotationsTest(String transformer) {
        this.transformer = transformer;
    }

    @Test(timeout = 60000)
    public void testDeliveryAnnotationsStrippedFromIncoming() throws Exception {
        AmqpClient client = createAmqpClient();
        AmqpConnection connection = trackConnection(client.connect());
        AmqpSession session = connection.createSession();
        AmqpSender sender = session.createSender(("queue://" + (getTestName())));
        AmqpReceiver receiver = session.createReceiver(("queue://" + (getTestName())));
        AmqpMessage message = new AmqpMessage();
        message.setText("Test-Message");
        message.setDeliveryAnnotation(DELIVERY_ANNOTATION_NAME, getTestName());
        sender.send(message);
        receiver.flow(1);
        QueueViewMBean queue = getProxyToQueue(getTestName());
        Assert.assertEquals(1, queue.getQueueSize());
        AmqpMessage received = receiver.receive();// 5, TimeUnit.SECONDS);

        Assert.assertNotNull(received);
        Assert.assertNull(received.getDeliveryAnnotation(DELIVERY_ANNOTATION_NAME));
        sender.close();
        connection.close();
    }
}
