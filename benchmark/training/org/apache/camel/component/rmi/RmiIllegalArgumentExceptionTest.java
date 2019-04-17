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
package org.apache.camel.component.rmi;


import java.rmi.RemoteException;
import org.apache.camel.CamelExecutionException;
import org.junit.Test;


public class RmiIllegalArgumentExceptionTest extends RmiRouteTestSupport {
    private boolean created;

    @Test
    public void tesIllegal() throws Exception {
        if (classPathHasSpaces()) {
            return;
        }
        getMockEndpoint("mock:result").expectedMessageCount(0);
        try {
            template.sendBody("direct:echo", "Hello World");
            fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            assertIsInstanceOf(RemoteException.class, e.getCause());
            // wrapped far down
            IllegalArgumentException iae = assertIsInstanceOf(IllegalArgumentException.class, e.getCause().getCause().getCause().getCause());
            assertEquals("Illegal", iae.getMessage());
        }
        assertMockEndpointsSatisfied();
    }
}
