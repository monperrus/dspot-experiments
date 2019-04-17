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
package org.assertj.core.error.future;


import java.util.concurrent.CompletableFuture;
import org.assertj.core.api.Assertions;
import org.assertj.core.internal.TestDescription;
import org.junit.jupiter.api.Test;


public class ShouldNotHaveCompletedExceptionally_create_Test {
    @Test
    public void should_create_error_message() {
        String error = ShouldNotBeCompletedExceptionally.shouldNotHaveCompletedExceptionally(new CompletableFuture()).create(new TestDescription("TEST"));
        Assertions.assertThat(error).isEqualTo(String.format(("[TEST] %n" + (("Expecting%n" + "  <CompletableFuture[Incomplete]>%n") + "to not be completed exceptionally.%n%s")), Warning.WARNING));
    }
}
