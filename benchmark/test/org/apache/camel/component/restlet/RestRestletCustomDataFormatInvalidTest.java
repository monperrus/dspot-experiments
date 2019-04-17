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
package org.apache.camel.component.restlet;


import RestBindingMode.json;
import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;


public class RestRestletCustomDataFormatInvalidTest extends RestletTestSupport {
    @Test
    public void testCustom() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration().component("restlet").host("localhost").port(RestletTestSupport.portNum).bindingMode(json).jsonDataFormat("bla");
                // use the rest DSL to define the rest services
                rest("/users/").post("lives").type(UserPojo.class).outType(CountryPojo.class).route().choice().when().simple("${body.id} < 100").bean(new UserErrorService(), "idToLowError").otherwise().bean(new UserService(), "livesWhere");
            }
        });
        try {
            context.start();
            fail("Should have thrown exception");
        } catch (FailedToCreateRouteException e) {
            assertTrue(e.getCause().getMessage().contains("JsonDataFormat name: bla must not be an existing bean instance from the registry"));
        }
    }
}
