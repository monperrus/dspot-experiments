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
package org.apache.camel.issues;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


public class NeilSplitterTest extends ContextTestSupport {
    protected Endpoint startEndpoint;

    protected MockEndpoint resultEndpoint;

    class CatFight {
        String name;

        String[] cats;

        public String[] getCats() {
            return cats;
        }

        public void setCats(String[] cats) {
            this.cats = cats;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testCustomExpression() throws Exception {
        resultEndpoint.expectedBodiesReceived("Ginger", "Mr Boots");
        template.send("direct:custom", new Processor() {
            public void process(Exchange exchange) {
                Message in = exchange.getIn();
                NeilSplitterTest.CatFight catFight = new NeilSplitterTest.CatFight();
                catFight.setName("blueydart");
                catFight.setCats(new String[]{ "Ginger", "Mr Boots" });
                in.setBody(catFight);
                in.setHeader("foo", "bar");
            }
        });
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    public void testXPathExpression() throws Exception {
        resultEndpoint.expectedBodiesReceived("<b>Ginger</b>", "<b>Mr Boots</b>");
        template.send("direct:xpath", new Processor() {
            public void process(Exchange exchange) {
                Message in = exchange.getIn();
                in.setBody("<a><b>Ginger</b><b>Mr Boots</b></a> ");
                in.setHeader("foo", "bar");
            }
        });
        resultEndpoint.assertIsSatisfied();
    }
}
