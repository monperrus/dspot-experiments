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


import Exchange.ACCEPT_CONTENT_TYPE;
import Exchange.CONTENT_TYPE;
import Exchange.HTTP_METHOD;
import Exchange.HTTP_RESPONSE_CODE;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.undertow.BaseUndertowTest;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.VerbDefinition;
import org.junit.Test;


public class RestUndertowHttpPojoTypeTest extends BaseUndertowTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUndertowPojoTypeValidateModel() throws Exception {
        // Wasn't clear if there's a way to put this test into camel-core just to test the model
        // perhaps without starting the Camel Context?
        List<RestDefinition> restDefinitions = context().adapt(ModelCamelContext.class).getRestDefinitions();
        assertNotNull(restDefinitions);
        assertTrue(((restDefinitions.size()) > 0));
        RestDefinition restDefinition = restDefinitions.get(0);
        List<VerbDefinition> verbs = restDefinition.getVerbs();
        assertNotNull(verbs);
        Map<String, VerbDefinition> mapVerb = new TreeMap<>();
        verbs.forEach(( verb) -> mapVerb.put(verb.getId(), verb));
        assertEquals(UserPojo[].class.getCanonicalName(), mapVerb.get("getUsers").getOutType());
        assertEquals(UserPojo[].class.getCanonicalName(), mapVerb.get("getUsersList").getOutType());
        assertEquals(UserPojo.class.getCanonicalName(), mapVerb.get("getUser").getOutType());
        assertEquals(UserPojo.class.getCanonicalName(), mapVerb.get("putUser").getType());
        assertEquals(UserPojo[].class.getCanonicalName(), mapVerb.get("putUsers").getType());
        assertEquals(UserPojo[].class.getCanonicalName(), mapVerb.get("putUsersList").getType());
    }

    @Test
    public void testUndertowPojoTypeGetUsers() throws Exception {
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "GET");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
            }
        });
        assertNotNull(outExchange);
        assertEquals("application/json", outExchange.getOut().getHeader(CONTENT_TYPE));
        String out = outExchange.getOut().getBody(String.class);
        assertNotNull(out);
        UserPojo[] users = mapper.readValue(out, UserPojo[].class);
        assertEquals(2, users.length);
        assertEquals("Scott", users[0].getName());
        assertEquals("Claus", users[1].getName());
    }

    @Test
    public void testUndertowPojoTypePutUser() throws Exception {
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users/1", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                UserPojo user = new UserPojo();
                user.setId(1);
                user.setName("Scott");
                String body = mapper.writeValueAsString(user);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(200, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
    }

    @Test
    public void testUndertowPojoTypePutUserFail() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:putUser");
        mock.expectedMessageCount(0);
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users/1", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                CountryPojo country = new CountryPojo();
                country.setIso("US");
                country.setCountry("United States");
                String body = mapper.writeValueAsString(country);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(400, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testUndertowPojoTypePutUsers() throws Exception {
        UserPojo user1 = new UserPojo();
        user1.setId(1);
        user1.setName("Scott");
        UserPojo user2 = new UserPojo();
        user2.setId(2);
        user2.setName("Claus");
        final UserPojo[] users = new UserPojo[]{ user1, user2 };
        MockEndpoint mock = getMockEndpoint("mock:putUsers");
        mock.expectedMessageCount(1);
        mock.message(0).body(UserPojo[].class);
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                String body = mapper.writeValueAsString(users);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(200, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
        assertMockEndpointsSatisfied();
        Exchange exchange = mock.assertExchangeReceived(0);
        UserPojo[] receivedUsers = exchange.getIn().getBody(UserPojo[].class);
        assertEquals(2, receivedUsers.length);
        assertEquals(user1.getName(), receivedUsers[0].getName());
        assertEquals(user2.getName(), receivedUsers[1].getName());
    }

    @Test
    public void testUndertowPojoTypePutUsersFail() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:putUsers");
        mock.expectedMessageCount(0);
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                UserPojo user = new UserPojo();
                user.setId(1);
                user.setName("Scott");
                String body = mapper.writeValueAsString(user);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(400, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testUndertowPojoTypePutUsersList() throws Exception {
        UserPojo user1 = new UserPojo();
        user1.setId(1);
        user1.setName("Scott");
        UserPojo user2 = new UserPojo();
        user2.setId(2);
        user2.setName("Claus");
        final UserPojo[] users = new UserPojo[]{ user1, user2 };
        MockEndpoint mock = getMockEndpoint("mock:putUsersList");
        mock.expectedMessageCount(1);
        mock.message(0).body(UserPojo[].class);
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users/list", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                String body = mapper.writeValueAsString(users);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(200, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
        assertMockEndpointsSatisfied();
        Exchange exchange = mock.assertExchangeReceived(0);
        UserPojo[] receivedUsers = exchange.getIn().getBody(UserPojo[].class);
        assertEquals(2, receivedUsers.length);
        assertEquals(user1.getName(), receivedUsers[0].getName());
        assertEquals(user2.getName(), receivedUsers[1].getName());
    }

    @Test
    public void testUndertowPojoTypePutUsersListFail() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:putUsersList");
        mock.expectedMessageCount(0);
        Exchange outExchange = template.request("undertow:http://localhost:{{port}}/users/list", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(HTTP_METHOD, "PUT");
                exchange.getIn().setHeader(ACCEPT_CONTENT_TYPE, "application/json");
                exchange.getIn().setHeader(CONTENT_TYPE, "application/json");
                UserPojo user = new UserPojo();
                user.setId(1);
                user.setName("Scott");
                String body = mapper.writeValueAsString(user);
                exchange.getIn().setBody(body);
            }
        });
        assertNotNull(outExchange);
        assertEquals(400, outExchange.getOut().getHeader(HTTP_RESPONSE_CODE));
        assertMockEndpointsSatisfied();
    }
}
