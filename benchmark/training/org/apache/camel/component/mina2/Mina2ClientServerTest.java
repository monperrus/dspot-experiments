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
package org.apache.camel.component.mina2;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.junit.Test;


// END SNIPPET: e2
public class Mina2ClientServerTest extends BaseMina2Test {
    @Test
    public void testSendToServer() throws InterruptedException {
        // START SNIPPET: e3
        String out = ((String) (template.requestBody(String.format("mina2:tcp://localhost:%1$s?textline=true", getPort()), "Chad")));
        assertEquals("Hello Chad", out);
        // END SNIPPET: e3
    }

    // START SNIPPET: e2
    private static class MyServerProcessor implements Processor {
        public void process(Exchange exchange) throws Exception {
            // get the input from the IN body
            String name = exchange.getIn().getBody(String.class);
            // send back a response on the OUT body
            exchange.getOut().setBody(("Hello " + name));
        }
    }
}
