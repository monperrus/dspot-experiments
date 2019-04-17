/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.netty.examples.tcp.interceptors.simple;


import io.reactivex.netty.examples.ExamplesTestUtil;
import java.util.Queue;
import org.hamcrest.MatcherAssert;
import org.junit.Test;


public class SimpleInterceptorTest {
    @Test(timeout = 60000)
    public void testSimpleInterceptor() throws Exception {
        Queue<String> output = ExamplesTestUtil.runClientInMockedEnvironment(InterceptingClient.class);
        MatcherAssert.assertThat("Unexpected number of messages echoed", output, hasSize(1));
        MatcherAssert.assertThat("Unexpected number of messages echoed", output, contains("echo => Hello World!"));
    }
}
