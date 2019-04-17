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
package com.hazelcast.internal.util.counters;


import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class MwCounterTest {
    private MwCounter counter;

    @Test
    public void inc() {
        counter.inc();
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void inc_withAmount() {
        counter.inc(10);
        Assert.assertEquals(10, counter.get());
        counter.inc(0);
        Assert.assertEquals(10, counter.get());
    }

    @Test
    public void test_toString() {
        String s = counter.toString();
        Assert.assertEquals("Counter{value=0}", s);
    }
}
