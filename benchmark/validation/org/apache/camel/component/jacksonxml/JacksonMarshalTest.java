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
package org.apache.camel.component.jacksonxml;


import java.util.HashMap;
import java.util.Map;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;


public class JacksonMarshalTest extends CamelTestSupport {
    @Test
    public void testMarshalAndUnmarshalMap() throws Exception {
        Map<String, Object> in = new HashMap<>();
        in.put("name", "Camel");
        MockEndpoint mock = getMockEndpoint("mock:reverse");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(Map.class);
        mock.message(0).body().isEqualTo(in);
        Object marshalled = template.requestBody("direct:in", in);
        String marshalledAsString = context.getTypeConverter().convertTo(String.class, marshalled);
        assertEquals("<HashMap><name>Camel</name></HashMap>", marshalledAsString);
        template.sendBody("direct:back", marshalled);
        mock.assertIsSatisfied();
    }

    @Test
    public void testMarshalAndUnmarshalMapWithPrettyPrint() throws Exception {
        Map<String, Object> in = new HashMap<>();
        in.put("name", "Camel");
        MockEndpoint mock = getMockEndpoint("mock:reverse");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(Map.class);
        mock.message(0).body().isEqualTo(in);
        Object marshalled = template.requestBody("direct:inPretty", in);
        String marshalledAsString = context.getTypeConverter().convertTo(String.class, marshalled);
        String expected = "<HashMap>\n  <name>Camel</name>\n</HashMap>\n";
        assertEquals(expected, marshalledAsString);
        template.sendBody("direct:backPretty", marshalled);
        mock.assertIsSatisfied();
    }

    @Test
    public void testMarshalAndUnmarshalPojo() throws Exception {
        TestPojo in = new TestPojo();
        in.setName("Camel");
        MockEndpoint mock = getMockEndpoint("mock:reversePojo");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(TestPojo.class);
        mock.message(0).body().isEqualTo(in);
        Object marshalled = template.requestBody("direct:inPojo", in);
        String marshalledAsString = context.getTypeConverter().convertTo(String.class, marshalled);
        assertEquals("<TestPojo><name>Camel</name></TestPojo>", marshalledAsString);
        template.sendBody("direct:backPojo", marshalled);
        mock.assertIsSatisfied();
    }
}
