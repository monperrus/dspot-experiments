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
package org.apache.camel.management;


import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


public class ManagedRouteDumpRouteAsXmlPlaceholderTest extends ManagementTestSupport {
    @Test
    public void testDumpAsXml() throws Exception {
        // JMX tests dont work well on AIX CI servers (hangs them)
        if (isPlatform("aix")) {
            return;
        }
        MBeanServer mbeanServer = getMBeanServer();
        ObjectName on = ManagedRouteDumpRouteAsXmlPlaceholderTest.getRouteObjectName(mbeanServer);
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
        // should be started
        String routeId = ((String) (mbeanServer.getAttribute(on, "RouteId")));
        assertEquals("myRoute", routeId);
        String xml = ((String) (mbeanServer.invoke(on, "dumpRouteAsXml", new Object[]{ true }, new String[]{ "boolean" })));
        assertNotNull(xml);
        log.info(xml);
        assertTrue(xml.contains("direct:start"));
        assertTrue(xml.contains("route"));
        assertTrue(xml.contains("myRoute"));
        assertTrue(xml.contains("mock:result"));
    }
}
