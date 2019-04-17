/**
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.publisher;


import Scannable.Attr.PARENT;
import Scannable.Attr.PREFETCH;
import org.junit.Test;
import reactor.test.StepVerifier;


public class ParallelMapTest {
    @Test
    public void parallelism() {
        ParallelFlux<Integer> source = Flux.range(1, 4).parallel(3);
        ParallelMap<Integer, String> test = new ParallelMap(source, ( i) -> "" + i);
        assertThat(test.parallelism()).isEqualTo(source.parallelism()).isEqualTo(3);
    }

    @Test
    public void scanOperator() {
        ParallelFlux<Integer> source = Flux.range(1, 4).parallel(3);
        ParallelMap<Integer, String> test = new ParallelMap(source, ( i) -> "" + i);
        assertThat(test.scan(PARENT)).isSameAs(source);
        assertThat(test.scan(PREFETCH)).isEqualTo((-1));
    }

    @Test
    public void conditional() {
        Flux<Integer> source = Flux.range(1, 1000);
        for (int i = 1; i < 33; i++) {
            Flux<Integer> result = ParallelFlux.from(source, i).map(( v) -> v + 1).filter(( t) -> true).sequential();
            StepVerifier.create(result).expectNextCount(1000).verifyComplete();
        }
    }
}
