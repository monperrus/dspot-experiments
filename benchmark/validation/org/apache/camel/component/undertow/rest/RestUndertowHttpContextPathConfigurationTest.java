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
package org.apache.camel.component.undertow.rest;


import org.apache.camel.component.undertow.BaseUndertowTest;
import org.junit.Test;


public class RestUndertowHttpContextPathConfigurationTest extends BaseUndertowTest {
    @Test
    public void testProducerGet() throws Exception {
        String out = template.requestBody("undertow:http://localhost:{{port}}/rest/users/123", null, String.class);
        assertEquals("123;Donald Duck", out);
        out = template.requestBody("undertow:http://localhost:{{port}}/rest/users/list", null, String.class);
        assertEquals("123;Donald Duck\n456;John Doe", out);
    }
}
