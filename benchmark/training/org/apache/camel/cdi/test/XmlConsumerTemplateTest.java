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
package org.apache.camel.cdi.test;


import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ImportResource;
import org.apache.camel.cdi.Uri;
import org.hamcrest.Matchers;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
@ImportResource("imported-context.xml")
public class XmlConsumerTemplateTest {
    @Inject
    @Uri("direct:inbound")
    private ProducerTemplate inbound;

    @Named
    @Inject
    private ConsumerTemplate consumer;

    @Test
    public void verifyConsumerTemplate() {
        Assert.assertThat("Consumer template Camel context is incorrect!", consumer.getCamelContext().getName(), Matchers.is(Matchers.equalTo("test")));
        Assert.assertThat("Consumer template cache size is incorrect!", consumer.getMaximumCacheSize(), Matchers.is(Matchers.equalTo(100)));
    }

    @Test
    public void sendMessageToInbound() {
        inbound.sendBody("seda:foo", "message");
        String body = consumer.receiveBody("seda:foo", TimeUnit.SECONDS.toMillis(1L), String.class);
        Assert.assertThat("Body is incorrect!", body, Matchers.is(Matchers.equalTo("message")));
    }
}
