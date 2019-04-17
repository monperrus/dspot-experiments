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
package org.apache.camel.issues;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.seda.SedaEndpoint;
import org.junit.Assert;
import org.junit.Test;


public class ConsumerTemplateSedaQueueIssueTest extends ContextTestSupport {
    @Test
    public void testConsumerTemplateSedaQueue() throws Exception {
        template.sendBody("direct:start", "A");
        template.sendBody("direct:start", "B");
        template.sendBody("direct:start", "C");
        template.sendBody("direct:start", "D");
        template.sendBody("direct:start", "E");
        SedaEndpoint seda = context.getEndpoint("seda:foo", SedaEndpoint.class);
        Assert.assertEquals(5, seda.getCurrentQueueSize());
        String body = consumer.receiveBody(seda, 1000, String.class);
        Assert.assertEquals("A", body);
        Assert.assertEquals(4, seda.getCurrentQueueSize());
        body = consumer.receiveBody(seda, 1000, String.class);
        Assert.assertEquals("B", body);
        Assert.assertEquals(3, seda.getCurrentQueueSize());
        body = consumer.receiveBody(seda, 1000, String.class);
        Assert.assertEquals("C", body);
        Assert.assertEquals(2, seda.getCurrentQueueSize());
        body = consumer.receiveBody(seda, 1000, String.class);
        Assert.assertEquals("D", body);
        Assert.assertEquals(1, seda.getCurrentQueueSize());
        body = consumer.receiveBody(seda, 1000, String.class);
        Assert.assertEquals("E", body);
        Assert.assertEquals(0, seda.getCurrentQueueSize());
    }
}
