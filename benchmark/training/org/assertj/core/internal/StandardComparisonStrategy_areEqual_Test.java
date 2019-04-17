/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2019 the original author or authors.
 */
package org.assertj.core.internal;


import org.junit.jupiter.api.Test;


/**
 * Tests for {@link StandardComparisonStrategy#areEqual(Object, Object)}.<br>
 * Conceptually the same as {@link Objects#areEqual(Object, Object)} but I don't know how to verify/test that
 * {@link StandardComparisonStrategy#areEqual(Object, Object)} simply calls {@link Objects#areEqual(Object, Object)}
 *
 * @author Joel Costigliola
 */
public class StandardComparisonStrategy_areEqual_Test {
    private static StandardComparisonStrategy standardComparisonStrategy = StandardComparisonStrategy.instance();

    @Test
    public void should_return_true_if_both_Objects_are_null_with_verify() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(null, null)).isTrue();
    }

    @Test
    public void should_return_true_if_both_Objects_are_null() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(null, null)).isTrue();
    }

    @Test
    public void should_return_true_if_Objects_are_equal() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual("Yoda", "Yoda")).isTrue();
    }

    @Test
    public void should_return_are_not_equal_if_first_Object_is_null_and_second_is_not() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(null, "Yoda")).isFalse();
    }

    @Test
    public void should_return_are_not_equal_if_second_Object_is_null_and_first_is_not() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual("Yoda", null)).isFalse();
    }

    @Test
    public void should_return_are_not_equal_if_Objects_are_not_equal() {
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual("Yoda", 2)).isFalse();
    }

    @Test
    public void should_return_true_if_arrays_of_Objects_are_equal() {
        Object[] a1 = new Object[]{ "Luke", "Yoda", "Leia" };
        Object[] a2 = new Object[]{ "Luke", "Yoda", "Leia" };
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(a1, a2)).isTrue();
    }

    @Test
    public void should_return_true_if_arrays_of_primitives_are_equal() {
        int[] a1 = new int[]{ 6, 8, 10 };
        int[] a2 = new int[]{ 6, 8, 10 };
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(a1, a2)).isTrue();
    }

    @Test
    public void should_return_false_if_arrays_of_Objects_are_not_equal() {
        Object[] a1 = new Object[]{ "Luke", "Yoda", "Leia" };
        Object[] a2 = new Object[0];
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(a1, a2)).isFalse();
    }

    @Test
    public void should_return_false_if_arrays_of_primitives_are_not_equal() {
        int[] a1 = new int[]{ 6, 8, 10 };
        boolean[] a2 = new boolean[]{ true };
        assertThat(StandardComparisonStrategy_areEqual_Test.standardComparisonStrategy.areEqual(a1, a2)).isFalse();
    }
}
