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
package org.apache.camel.component.jgroups;


import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


/**
 * Test for shared channel in JGroups endpoint
 */
public class JGroupsSharedChannelTest extends CamelTestSupport {
    private static final String JGROUPS_SHARED_CHANNEL = "jgroups:sharedChannel";

    private static final String DIRECT_PRODUCER = "direct:producer";

    private static final String MOCK_CONSUMER = "mock:consumer";

    private static final String PRODUCER_ROUTE = "producerRoute";

    private static final String CONSUMER_ROUTE = "consumerRoute";

    private static final String TEST_MESSAGE = "Test Message";

    @Test
    public void testStopStartProducer() throws Exception {
        context().getRouteController().stopRoute(JGroupsSharedChannelTest.PRODUCER_ROUTE);
        context().getRouteController().startRoute(JGroupsSharedChannelTest.PRODUCER_ROUTE);
        testSendReceive();
    }

    @Test
    public void testStopStartConsumer() throws Exception {
        context().getRouteController().stopRoute(JGroupsSharedChannelTest.CONSUMER_ROUTE);
        template().sendBody(JGroupsSharedChannelTest.DIRECT_PRODUCER, JGroupsSharedChannelTest.TEST_MESSAGE);
        context().getRouteController().startRoute(JGroupsSharedChannelTest.CONSUMER_ROUTE);
        testSendReceive();
    }
}
