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
package org.apache.camel.processor.aggregator;


import org.apache.camel.AggregationStrategy;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.TestSupport;
import org.junit.Assert;
import org.junit.Test;


public class AggregateRepositoryReturnNullTest extends ContextTestSupport {
    @Test
    public void testAggregateRepositoryReturnNull() throws Exception {
        try {
            template.sendBodyAndHeader("direct:start", "Hello World", "id", 123);
            Assert.fail("Should throw exception");
        } catch (CamelExecutionException e) {
            TestSupport.assertIsInstanceOf(CamelExchangeException.class, e.getCause());
            Assert.assertTrue(e.getCause().getMessage().startsWith("AggregationStrategy"));
            Assert.assertTrue(e.getCause().getMessage().contains("returned null which is not allowed"));
        }
    }

    private static class MyNullAggregationStrategy implements AggregationStrategy {
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            // on purpose
            return null;
        }
    }
}
