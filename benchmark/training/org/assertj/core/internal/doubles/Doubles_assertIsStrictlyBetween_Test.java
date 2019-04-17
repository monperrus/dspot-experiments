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
package org.assertj.core.internal.doubles;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldBeBetween;
import org.assertj.core.internal.DoublesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Doubles#assertIsStrictlyBetween(AssertionInfo, Double, Double, Double)}</code>.
 *
 * @author William Delanoue
 */
public class Doubles_assertIsStrictlyBetween_Test extends DoublesBaseTest {
    private static final Double ZERO = 0.0;

    private static final Double ONE = 1.0;

    private static final Double TWO = 2.0;

    private static final Double TEN = 10.0;

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> doubles.assertIsStrictlyBetween(someInfo(), null, ZERO, ONE)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_start_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> doubles.assertIsStrictlyBetween(someInfo(), ONE, null, ONE));
    }

    @Test
    public void should_fail_if_end_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> doubles.assertIsStrictlyBetween(someInfo(), ONE, ZERO, null));
    }

    @Test
    public void should_pass_if_actual_is_in_range() {
        doubles.assertIsStrictlyBetween(TestData.someInfo(), Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.ZERO, Doubles_assertIsStrictlyBetween_Test.TEN);
    }

    @Test
    public void should_fail_if_actual_is_equal_to_range_start() {
        AssertionInfo info = TestData.someInfo();
        try {
            doubles.assertIsStrictlyBetween(info, Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.TEN);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeBetween.shouldBeBetween(Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.TEN, false, false));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_equal_to_range_end() {
        AssertionInfo info = TestData.someInfo();
        try {
            doubles.assertIsStrictlyBetween(info, Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.ZERO, Doubles_assertIsStrictlyBetween_Test.ONE);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeBetween.shouldBeBetween(Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.ZERO, Doubles_assertIsStrictlyBetween_Test.ONE, false, false));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_not_in_range_start() {
        AssertionInfo info = TestData.someInfo();
        try {
            doubles.assertIsStrictlyBetween(info, Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.TWO, Doubles_assertIsStrictlyBetween_Test.TEN);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeBetween.shouldBeBetween(Doubles_assertIsStrictlyBetween_Test.ONE, Doubles_assertIsStrictlyBetween_Test.TWO, Doubles_assertIsStrictlyBetween_Test.TEN, false, false));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_not_in_range_end() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> doubles.assertIsStrictlyBetween(someInfo(), ONE, ZERO, ZERO)).withMessage("The end value <0.0> must not be less than or equal to the start value <0.0>!");
    }
}
