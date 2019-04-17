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


import ServiceStatus.Started;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;
import org.junit.Test;


public class ManagedRouteRestartTest extends ManagementTestSupport {
    private ManagedRouteRestartTest.MyRoutePolicy myRoutePolicy = new ManagedRouteRestartTest.MyRoutePolicy();

    @Test
    public void testRestartRoute() throws Exception {
        // JMX tests dont work well on AIX CI servers (hangs them)
        if (isPlatform("aix")) {
            return;
        }
        assertEquals(1, myRoutePolicy.getStart());
        assertEquals(0, myRoutePolicy.getStop());
        // fire a message to get it running
        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();
        MBeanServer mbeanServer = getMBeanServer();
        Set<ObjectName> set = mbeanServer.queryNames(new ObjectName("*:type=routes,*"), null);
        assertEquals(1, set.size());
        ObjectName on = set.iterator().next();
        boolean registered = mbeanServer.isRegistered(on);
        assertEquals("Should be registered", true, registered);
        String uri = ((String) (mbeanServer.getAttribute(on, "EndpointUri")));
        // the route has this starting endpoint uri
        assertEquals("direct://start", uri);
        // should be started
        String state = ((String) (mbeanServer.getAttribute(on, "State")));
        assertEquals("Should be started", Started.name(), state);
        String uptime = ((String) (mbeanServer.getAttribute(on, "Uptime")));
        assertNotNull(uptime);
        log.info("Uptime: {}", uptime);
        long uptimeMillis = ((Long) (mbeanServer.getAttribute(on, "UptimeMillis")));
        assertTrue((uptimeMillis > 0));
        mbeanServer.invoke(on, "restart", null, null);
        registered = mbeanServer.isRegistered(on);
        assertEquals("Should be registered", true, registered);
        // should be started
        state = ((String) (mbeanServer.getAttribute(on, "State")));
        assertEquals("Should be started", Started.name(), state);
        assertEquals(2, myRoutePolicy.getStart());
        assertEquals(1, myRoutePolicy.getStop());
    }

    private final class MyRoutePolicy extends RoutePolicySupport {
        private int start;

        private int stop;

        @Override
        public void onStart(Route route) {
            (start)++;
        }

        @Override
        public void onStop(Route route) {
            (stop)++;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }
    }
}
