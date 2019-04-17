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
package org.apache.camel.component.netty4;


import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;


public class NettySSLTest extends BaseNettyTest {
    @Test
    public void testSSLInOutWithNettyConsumer() throws Exception {
        // ibm jdks dont have sun security algorithms
        if (isJavaVendor("ibm")) {
            return;
        }
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                // needClientAuth=true so we can get the client certificate details
                from("netty4:tcp://localhost:{{port}}?sync=true&ssl=true&passphrase=changeit&keyStoreResource=#ksf&trustStoreResource=#tsf&needClientAuth=true").process(new Processor() {
                    public void process(org.apache.camel.Exchange exchange) throws Exception {
                        javax.net.ssl.SSLSession session = exchange.getIn().getHeader(NettyConstants.NETTY_SSL_SESSION, javax.net.ssl.SSLSession.class);
                        if (session != null) {
                            javax.security.cert.X509Certificate cert = session.getPeerCertificateChain()[0];
                            java.security.Principal principal = cert.getSubjectDN();
                            log.info("Client Cert SubjectDN: {}", principal.getName());
                            exchange.getOut().setBody("When You Go Home, Tell Them Of Us And Say, For Your Tomorrow, We Gave Our Today.");
                        } else {
                            exchange.getOut().setBody("Cannot start conversion without SSLSession");
                        }
                    }
                });
            }
        });
        context.start();
        String response = template.requestBody("netty4:tcp://localhost:{{port}}?sync=true&ssl=true&passphrase=changeit&keyStoreResource=#ksf&trustStoreResource=#tsf", "Epitaph in Kohima, India marking the WWII Battle of Kohima and Imphal, Burma Campaign - Attributed to John Maxwell Edmonds", String.class);
        assertEquals("When You Go Home, Tell Them Of Us And Say, For Your Tomorrow, We Gave Our Today.", response);
    }
}
