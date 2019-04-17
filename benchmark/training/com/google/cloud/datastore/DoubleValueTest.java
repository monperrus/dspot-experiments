/**
 * Copyright 2015 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.datastore;


import DoubleValue.Builder;
import org.junit.Assert;
import org.junit.Test;


public class DoubleValueTest {
    private static final Double CONTENT = 1.25;

    @Test
    public void testToBuilder() throws Exception {
        DoubleValue value = DoubleValue.of(DoubleValueTest.CONTENT);
        Assert.assertEquals(value, value.toBuilder().build());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testOf() throws Exception {
        DoubleValue value = DoubleValue.of(DoubleValueTest.CONTENT);
        Assert.assertEquals(DoubleValueTest.CONTENT, value.get());
        Assert.assertFalse(value.excludeFromIndexes());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testBuilder() throws Exception {
        DoubleValue.Builder builder = DoubleValue.newBuilder(DoubleValueTest.CONTENT);
        DoubleValue value = builder.setMeaning(1).setExcludeFromIndexes(true).build();
        Assert.assertEquals(DoubleValueTest.CONTENT, value.get());
        Assert.assertEquals(1, value.getMeaning());
        Assert.assertTrue(value.excludeFromIndexes());
    }
}
