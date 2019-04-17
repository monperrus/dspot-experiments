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
package org.assertj.core.api.float_;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.FloatAssertBaseTest;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link FloatAssert#isNotZero()}</code>.
 *
 * @author Alex Ruiz
 */
public class FloatAssert_isNotZero_Test extends FloatAssertBaseTest {
    @Test
    public void should_pass_with_Float_negative_zero() {
        // GIVEN
        final Float negativeZero = -0.0F;
        // THEN
        Assertions.assertThat(negativeZero).isNotZero();
    }

    @Test
    public void should_fail_with_primitive_negative_zero() {
        // GIVEN
        final float negativeZero = -0.0F;
        try {
            // WHEN
            Assertions.assertThat(negativeZero).isNotZero();
        } catch (AssertionError e) {
            // THEN
            Assertions.assertThat(e).hasMessage(String.format("%nExpecting:%n <-0.0f>%nnot to be equal to:%n <0.0>%n"));
            return;
        }
        failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_with_primitive_positive_zero() {
        // GIVEN
        final float positiveZero = 0.0F;
        try {
            // WHEN
            Assertions.assertThat(positiveZero).isNotZero();
        } catch (AssertionError e) {
            // THEN
            Assertions.assertThat(e).hasMessage(String.format("%nExpecting:%n <0.0f>%nnot to be equal to:%n <0.0>%n"));
            return;
        }
        failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_with_Float_positive_zero() {
        // GIVEN
        final Float positiveZero = 0.0F;
        try {
            // WHEN
            Assertions.assertThat(positiveZero).isNotZero();
        } catch (AssertionError e) {
            // THEN
            Assertions.assertThat(e).hasMessage(String.format("%nExpecting:%n <0.0f>%nnot to be equal to:%n <0.0f>%n"));
            return;
        }
        failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
