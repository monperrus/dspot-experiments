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
package org.assertj.core.api;


import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Assertions#assertThat(int)}</code>.
 *
 * @author Alex Ruiz
 */
public class Assertions_assertThat_with_primitive_int_Test {
    @Test
    public void should_create_Assert() {
        AbstractIntegerAssert<?> assertions = Assertions.assertThat(0);
        Assertions.assertThat(assertions).isNotNull();
    }

    @Test
    public void should_pass_actual() {
        AbstractIntegerAssert<?> assertions = Assertions.assertThat(8);
        Assertions.assertThat(assertions.actual).isEqualTo(new Integer(8));
    }

    @Test
    public void should_pass_when_expected_long_equals_actual_int() {
        Assertions.assertThat(123).isEqualTo(123L);
    }

    @Test
    public void should_pass_if_expected_long_is_Integer_MAX_and_actual_is_too() {
        int actual = Integer.MAX_VALUE;
        long expected = Integer.MAX_VALUE;
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void should_fail_if_expected_long_is_Integer_MAX_plus_one() {
        int actual = Integer.MAX_VALUE;
        long expected = (Integer.MAX_VALUE) + 1;
        Assertions.assertThatThrownBy(() -> assertThat(actual).isEqualTo(expected));
    }

    @Test
    public void should_fail_if_expected_long_is_Integer_MIN_minus_one() {
        int actual = Integer.MIN_VALUE;
        long expected = (Integer.MIN_VALUE) - 1;
        Assertions.assertThatThrownBy(() -> assertThat(actual).isEqualTo(expected));
    }

    @Test
    public void should_fail_if_expected_long_is_different_from_actual_int() {
        Assertions.assertThatThrownBy(() -> assertThat(123).isEqualTo(456L));
    }

    @Test
    public void should_pass_if_expected_long_is_Integer_MIN_and_actual_is_too() {
        int actual = Integer.MIN_VALUE;
        long expected = Integer.MIN_VALUE;
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
