/**
 * Copyright 2018 The gRPC Authors
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
package io.grpc.inprocess;


import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class InProcessSocketAddressTest {
    @Test
    public void equal() {
        new EqualsTester().addEqualityGroup(new InProcessSocketAddress("a"), new InProcessSocketAddress(new String("a"))).addEqualityGroup(new InProcessSocketAddress("z"), new InProcessSocketAddress("z")).addEqualityGroup(new InProcessSocketAddress(""), new InProcessSocketAddress("")).testEquals();
    }

    @Test
    public void hash() {
        assertThat(new InProcessSocketAddress("a").hashCode()).isEqualTo(new InProcessSocketAddress(new String("a")).hashCode());
    }
}
