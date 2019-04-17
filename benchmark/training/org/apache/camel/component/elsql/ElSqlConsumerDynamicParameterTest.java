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
package org.apache.camel.component.elsql;


import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;


/**
 *
 */
public class ElSqlConsumerDynamicParameterTest extends CamelTestSupport {
    private EmbeddedDatabase db;

    private ElSqlConsumerDynamicParameterTest.MyIdGenerator idGenerator = new ElSqlConsumerDynamicParameterTest.MyIdGenerator();

    @Test
    public void testDynamicConsume() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(3);
        context.getRouteController().startRoute("foo");
        assertMockEndpointsSatisfied();
        List<Exchange> exchanges = mock.getReceivedExchanges();
        assertEquals(1, exchanges.get(0).getIn().getBody(Map.class).get("ID"));
        assertEquals("Camel", exchanges.get(0).getIn().getBody(Map.class).get("PROJECT"));
        assertEquals(2, exchanges.get(1).getIn().getBody(Map.class).get("ID"));
        assertEquals("AMQ", exchanges.get(1).getIn().getBody(Map.class).get("PROJECT"));
        assertEquals(3, exchanges.get(2).getIn().getBody(Map.class).get("ID"));
        assertEquals("Linux", exchanges.get(2).getIn().getBody(Map.class).get("PROJECT"));
        // and the bean id should be > 1
        assertTrue("Id counter should be > 1", ((idGenerator.getId()) > 1));
    }

    public static class MyIdGenerator {
        private int id = 1;

        public int nextId() {
            // spring will call this twice, one for initializing query and 2nd for actual value
            (id)++;
            return (id) / 2;
        }

        public int getId() {
            return id;
        }
    }
}
