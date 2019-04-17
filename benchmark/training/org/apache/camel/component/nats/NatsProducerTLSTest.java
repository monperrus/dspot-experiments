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
package org.apache.camel.component.nats;


import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;


/* The keystore used in this test it's from https://github.com/nats-io/jnats/tree/master/src/test/resources, in particular
the tls_1222.conf file. Running this test will require use the server-cert.pem and server-key.pem in your gnatsd running instance.
 */
@Ignore("Require a running Nats server")
public class NatsProducerTLSTest extends CamelTestSupport {
    @Test
    public void sendTest() throws Exception {
        template.sendBody("direct:send", "pippo");
    }
}
