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
package org.example.test;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;


/**
 * This test has to be in a package other than org.assertj because otherwise the
 * line number information will be removed by the assertj filtering of internal lines.
 * {@link org.assertj.core.util.Throwables#removeAssertJRelatedElementsFromStackTrace}
 */
public class AutoClosableSoftAssertionsLineNumberTest {
    @Test
    public void should_print_line_numbers_of_failed_assertions() {
        AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions();
        softly.assertThat(1).isLessThan(0).isLessThan(1);
        // WHEN
        AssertionError error = Assertions.catchThrowableOfType(() -> softly.close(), AssertionError.class);
        // THEN
        Assertions.assertThat(error).hasMessageContaining(String.format(("%n" + (((("Expecting:%n" + " <1>%n") + "to be less than:%n") + " <0> %n") + "at AutoClosableSoftAssertionsLineNumberTest.should_print_line_numbers_of_failed_assertions(AutoClosableSoftAssertionsLineNumberTest.java:33)%n")))).hasMessageContaining(String.format(("%n" + (((("Expecting:%n" + " <1>%n") + "to be less than:%n") + " <1> %n") + "at AutoClosableSoftAssertionsLineNumberTest.should_print_line_numbers_of_failed_assertions(AutoClosableSoftAssertionsLineNumberTest.java:34)"))));
    }
}
