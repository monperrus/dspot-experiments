/**
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
package io.helidon.microprofile.metrics;


import MediaType.TEXT_PLAIN_TYPE;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;


/**
 * Class HelloWorldTest.
 *
 * @author Santiago Pericas-Geertsen
 */
public class HelloWorldTest extends MetricsMpServiceTest {
    @Test
    public void testMetrics() {
        IntStream.range(0, 5).forEach(( i) -> MetricsMpServiceTest.client.target(baseUri()).path("helloworld").request().accept(TEXT_PLAIN_TYPE).get(String.class));
        MatcherAssert.assertThat(MetricsMpServiceTest.getCounter("helloCounter").getCount(), CoreMatchers.is(5L));
    }
}
