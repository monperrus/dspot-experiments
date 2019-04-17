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
package org.apache.camel.component.seda;


import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.TestSupport;
import org.apache.camel.util.StopWatch;
import org.junit.Assert;
import org.junit.Test;


public class SedaInOutChainedTimeoutTest extends ContextTestSupport {
    @Test
    public void testSedaInOutChainedTimeout() throws Exception {
        // time timeout after 2 sec should trigger a immediately reply
        StopWatch watch = new StopWatch();
        try {
            template.requestBody("seda:a?timeout=5000", "Hello World");
            Assert.fail("Should have thrown an exception");
        } catch (CamelExecutionException e) {
            ExchangeTimedOutException cause = TestSupport.assertIsInstanceOf(ExchangeTimedOutException.class, e.getCause());
            Assert.assertEquals(2000, cause.getTimeout());
        }
        long delta = watch.taken();
        Assert.assertTrue(("Should be faster than 4000 millis, was: " + delta), (delta < 4000));
    }
}
