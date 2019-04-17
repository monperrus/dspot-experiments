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
package org.apache.camel.component.jetty.jettyproducer;


import org.apache.camel.component.jetty.BaseJettyTest;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.junit.Test;


public class JettyHttpProducerSimulate404ErrorTest extends BaseJettyTest {
    private String url = ("jetty://http://127.0.0.1:" + (BaseJettyTest.getPort())) + "/bar";

    @Test
    public void test404() throws Exception {
        // give Jetty time to startup properly
        Thread.sleep(1000);
        try {
            template.sendBody(url, null);
            fail("Should have thrown exception");
        } catch (Exception e) {
            HttpOperationFailedException cause = assertIsInstanceOf(HttpOperationFailedException.class, e.getCause());
            assertEquals(404, cause.getStatusCode());
            assertEquals((("http://127.0.0.1:" + (BaseJettyTest.getPort())) + "/bar"), cause.getUri());
            assertEquals("Page not found", cause.getResponseBody());
            assertNotNull(cause.getResponseHeaders());
        }
    }
}
