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
package org.apache.camel.component.sjms.batch;


import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class SjmsBatchEndpointTest extends CamelTestSupport {
    // Create one embedded broker instance for the entire test, as we aren't actually
    // going to send any messages to it; we just need it so that the ConnectionFactory
    // has something local to connect to.
    public static EmbeddedActiveMQBroker broker;

    @Test(expected = FailedToCreateRouteException.class)
    public void testProducerFailure() throws Exception {
        context.addRoutes(new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:in").to("sjms-batch:testQueue?aggregationStrategy=#unknown");
            }
        });
        context.start();
    }

    @Test(expected = FailedToCreateRouteException.class)
    public void testConsumerNegativePollDuration() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sjms-batch:in?aggregationStrategy=#aggStrategy&pollDuration=-1").to("mock:out");
            }
        });
        context.start();
    }

    @Test(expected = FailedToCreateRouteException.class)
    public void testConsumerNegativeConsumerCount() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sjms-batch:in?aggregationStrategy=#aggStrategy&consumerCount=-1").to("mock:out");
            }
        });
        context.start();
    }

    @Test(expected = FailedToCreateRouteException.class)
    public void testConsumerTopic() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("sjms-batch:topic:in?aggregationStrategy=#aggStrategy").to("mock:out");
            }
        });
        context.start();
    }
}
