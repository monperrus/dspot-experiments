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
package org.apache.camel.spring.interceptor;


import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


/**
 * Unit test to demonstrate the transactional client pattern.
 */
public class TransactionalClientDataSourceAsyncTest extends TransactionalClientDataSourceTest {
    @Test
    public void testTransactionRollback() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:error");
        mock.expectedMessageCount(1);
        try {
            template.sendBody("direct:fail", "Hello World");
            fail("Should have thrown exception");
        } catch (RuntimeCamelException e) {
            // expected as we fail
            assertIsInstanceOf(RuntimeCamelException.class, e.getCause());
            assertTrue(((e.getCause().getCause()) instanceof IllegalArgumentException));
            assertEquals("We don't have Donkeys, only Camels", e.getCause().getCause().getMessage());
        }
        assertMockEndpointsSatisfied();
        int count = jdbc.queryForObject("select count(*) from books", Integer.class);
        assertEquals("Number of books", 1, count);
    }
}
