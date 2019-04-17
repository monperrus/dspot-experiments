/**
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */
package io.crate.execution.dsl.projection;


import io.crate.expression.symbol.Literal;
import io.crate.test.integration.CrateUnitTest;
import io.crate.types.DataTypes;
import java.util.Collections;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.hamcrest.Matchers;
import org.junit.Test;


public class OrderedTopNProjectionTest extends CrateUnitTest {
    @Test
    public void testStreaming() throws Exception {
        Projection p = new OrderedTopNProjection(10, 20, Collections.singletonList(Literal.of("foobar")), Collections.singletonList(new io.crate.expression.symbol.InputColumn(0, DataTypes.STRING)), new boolean[]{ true }, new Boolean[]{ null });
        BytesStreamOutput out = new BytesStreamOutput();
        Projection.toStream(p, out);
        StreamInput in = out.bytes().streamInput();
        Projection projection = Projection.fromStream(in);
        assertThat(projection, Matchers.equalTo(p));
    }
}
