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
package org.apache.camel.component.undertow;


import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/SpringTest.xml" })
public class UndertowHttpsSpringTest {
    private Integer port;

    @Produce
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:input")
    private MockEndpoint mockEndpoint;

    @Test
    public void testSSLConsumer() throws Exception {
        mockEndpoint.expectedBodiesReceived("Hello World");
        String out = template.requestBody((("undertow:https://localhost:" + (port)) + "/spring?sslContextParameters=#sslClient"), "Hello World", String.class);
        Assert.assertEquals("Bye World", out);
        mockEndpoint.assertIsSatisfied();
    }
}
