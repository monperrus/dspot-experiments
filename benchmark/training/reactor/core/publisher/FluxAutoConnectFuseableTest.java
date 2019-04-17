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


import Scannable.Attr.CAPACITY;
import Scannable.Attr.PARENT;
import Scannable.Attr.PREFETCH;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.test.MockUtils;


public class FluxAutoConnectFuseableTest {
    @Test
    public void scanMain() {
        @SuppressWarnings("unchecked")
        ConnectableFlux<String> source = Mockito.mock(MockUtils.TestScannableConnectableFlux.class);
        Mockito.when(source.getPrefetch()).thenReturn(888);
        FluxAutoConnectFuseable<String> test = new FluxAutoConnectFuseable(source, 123, ( d) -> {
        });
        assertThat(test.scan(PARENT)).isSameAs(source);
        assertThat(test.scan(PREFETCH)).isEqualTo(888);
        assertThat(test.scan(CAPACITY)).isEqualTo(123);
    }
}
