/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.api.common.operators;


import Order.ASCENDING;
import Order.DESCENDING;
import org.junit.Assert;
import org.junit.Test;


public class OrderingTest {
    @Test
    public void testNewOrdering() {
        Ordering ordering = new Ordering();
        // add a field
        ordering.appendOrdering(3, Integer.class, ASCENDING);
        Assert.assertEquals(1, ordering.getNumberOfFields());
        // add a second field
        ordering.appendOrdering(1, Long.class, DESCENDING);
        Assert.assertEquals(2, ordering.getNumberOfFields());
        // duplicate field index does not change Ordering
        ordering.appendOrdering(1, String.class, ASCENDING);
        Assert.assertEquals(2, ordering.getNumberOfFields());
        // verify field positions, types, and orderings
        Assert.assertArrayEquals(new int[]{ 3, 1 }, ordering.getFieldPositions());
        Assert.assertArrayEquals(new Class[]{ Integer.class, Long.class }, ordering.getTypes());
        Assert.assertArrayEquals(new boolean[]{ true, false }, ordering.getFieldSortDirections());
    }
}
