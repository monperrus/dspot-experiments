/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.test;


import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


/**
 * Tests the {@link OverridePropertyRule} with multiple instances and a Hazelcast specific runner.
 */
@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class OverridePropertyRuleTest {
    @Rule
    public OverridePropertyRule overridePropertyRule = OverridePropertyRule.set("hazelcast.custom.system.property", "5");

    @Rule
    public OverridePropertyRule overridePreferIpv4Rule = OverridePropertyRule.set("java.net.preferIPv4Stack", "true");

    @Test
    public void testNonExistingProperty() {
        Assert.assertNull(System.getProperty("notExists"));
    }

    @Test
    public void testCustomSystemProperty() {
        Assert.assertEquals("5", System.getProperty("hazelcast.custom.system.property"));
    }

    @Test
    public void testHazelcastProperty() {
        Assert.assertEquals("true", System.getProperty("java.net.preferIPv4Stack"));
    }

    @Test
    public void testHazelcastPropertyWithGetBoolean() {
        Assert.assertTrue(Boolean.getBoolean("java.net.preferIPv4Stack"));
    }
}
