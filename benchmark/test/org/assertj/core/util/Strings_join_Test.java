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
package org.assertj.core.util;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Strings#join(String...)}</code>.
 *
 * @author Alex Ruiz
 */
public class Strings_join_Test {
    @Test
    public void should_throw_error_if_delimiter_is_null() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> Strings.join(null, "Uno", "Dos").with(null));
    }

    @Test
    public void should_return_empty_String_if_array_to_join_is_null() {
        Assertions.assertThat(Strings.join(((String[]) (null))).with("|")).isEmpty();
    }

    @Test
    public void should_join_using_delimiter() {
        Assertions.assertThat(Strings.join("Luke", "Leia", "Han").with("|")).isEqualTo("Luke|Leia|Han");
    }

    @Test
    public void should_join_using_delimiter_and_escape() {
        Assertions.assertThat(Strings.join("Luke", "Leia", "Han").with("|", "'")).isEqualTo("'Luke'|'Leia'|'Han'");
    }

    @Test
    public void should_join_using_iterable_delimiter_and_escape() {
        Assertions.assertThat(Strings.join(Lists.newArrayList("Luke", "Leia", "Han")).with("|", "'")).isEqualTo("'Luke'|'Leia'|'Han'");
    }
}
