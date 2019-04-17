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
package org.apache.camel.processor;


import org.junit.Assert;
import org.junit.Test;


public class RedeliveryPolicyDelayPatternTest extends Assert {
    private RedeliveryPolicy policy = new RedeliveryPolicy();

    @Test
    public void testDelayPattern() throws Exception {
        policy.setDelayPattern("3:1000;5:3000;10:5000;20:10000");
        Assert.assertEquals(0, policy.calculateRedeliveryDelay(0, 0));
        Assert.assertEquals(0, policy.calculateRedeliveryDelay(0, 1));
        Assert.assertEquals(0, policy.calculateRedeliveryDelay(0, 2));
        Assert.assertEquals(1000, policy.calculateRedeliveryDelay(0, 3));
        Assert.assertEquals(1000, policy.calculateRedeliveryDelay(0, 4));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 5));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 5));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 6));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 7));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 8));
        Assert.assertEquals(3000, policy.calculateRedeliveryDelay(0, 9));
        Assert.assertEquals(5000, policy.calculateRedeliveryDelay(0, 10));
        Assert.assertEquals(5000, policy.calculateRedeliveryDelay(0, 11));
        Assert.assertEquals(5000, policy.calculateRedeliveryDelay(0, 15));
        Assert.assertEquals(5000, policy.calculateRedeliveryDelay(0, 19));
        Assert.assertEquals(10000, policy.calculateRedeliveryDelay(0, 20));
        Assert.assertEquals(10000, policy.calculateRedeliveryDelay(0, 21));
        Assert.assertEquals(10000, policy.calculateRedeliveryDelay(0, 25));
        Assert.assertEquals(10000, policy.calculateRedeliveryDelay(0, 50));
        Assert.assertEquals(10000, policy.calculateRedeliveryDelay(0, 100));
    }
}
