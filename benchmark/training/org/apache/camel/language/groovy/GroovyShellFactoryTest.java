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
package org.apache.camel.language.groovy;


import groovy.lang.GroovyShell;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.SimpleRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;


public class GroovyShellFactoryTest extends CamelTestSupport {
    @Test
    public void testExpressionReturnsTheCorrectValue() {
        // Given
        GroovyShellFactory groovyShellFactory = Mockito.mock(GroovyShellFactory.class);
        BDDMockito.given(groovyShellFactory.createGroovyShell(ArgumentMatchers.any(Exchange.class))).willReturn(new GroovyShell());
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("groovyShellFactory", groovyShellFactory);
        CamelContext camelContext = new org.apache.camel.impl.DefaultCamelContext(registry);
        // When
        assertExpression(GroovyLanguage.groovy("exchange.in.body"), new org.apache.camel.support.DefaultExchange(camelContext), null);
        // Then
        Mockito.verify(groovyShellFactory).createGroovyShell(ArgumentMatchers.any(Exchange.class));
    }
}
