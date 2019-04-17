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
package org.apache.camel.component.jetty.rest;


import org.apache.camel.component.jetty.BaseJettyTest;
import org.junit.Test;


public class RestJettyPojoInOutTest extends BaseJettyTest {
    @Test
    public void testJettyPojoInOut() throws Exception {
        String body = "{\"id\": 123, \"name\": \"Donald Duck\"}";
        String out = template.requestBody((("http://localhost:" + (BaseJettyTest.getPort())) + "/users/lives"), body, String.class);
        assertNotNull(out);
        assertEquals("{\"iso\":\"EN\",\"country\":\"England\"}", out);
    }

    @Test
    public void testJettyGetRequest() throws Exception {
        String out = template.requestBody((("http://localhost:" + (BaseJettyTest.getPort())) + "/users/lives"), null, String.class);
        assertNotNull(out);
        assertEquals("{\"iso\":\"EN\",\"country\":\"England\"}", out);
    }
}
