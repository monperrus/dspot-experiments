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


import org.apache.camel.CamelExecutionException;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


/**
 * Unit test to demonstrate the transactional client pattern.
 */
public class TransactionalClientDataSourceWithOnExceptionHandledAndRollbackTest extends TransactionalClientDataSourceTest {
    @Test
    public void testTransactionRollback() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:error");
        mock.expectedMessageCount(1);
        try {
            template.requestBody("direct:fail", "Hello World", String.class);
            fail("Should have thrown exception");
        } catch (CamelExecutionException e) {
            // expected
        }
        assertMockEndpointsSatisfied();
        int count = jdbc.queryForObject("select count(*) from books", Integer.class);
        assertEquals("Number of books", 1, count);
    }
}
