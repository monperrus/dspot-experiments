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
package io.crate.expression.scalar.string;


import DataTypes.STRING;
import io.crate.expression.scalar.AbstractScalarFunctionsTest;
import io.crate.expression.symbol.Literal;
import io.crate.testing.SymbolMatchers;
import org.junit.Test;


public class ReplaceFunctionTest extends AbstractScalarFunctionsTest {
    @Test
    public void testNormalizeReplaceFunc() throws Exception {
        assertNormalize("replace('Crate', 'C', 'D')", SymbolMatchers.isLiteral("Drate"));
    }

    @Test
    public void testEvaluateReplaceFunc() throws Exception {
        assertEvaluate("replace(name, 'Crate', 'ba')", "foobarbequebaz bar", Literal.of(STRING, "fooCraterbequebaz bar"));
    }

    @Test
    public void testEvaluateReplaceFuncWhenEmptyString() throws Exception {
        assertEvaluate("replace(name, 'Crate', 'ba')", "", Literal.of(STRING, ""));
    }

    @Test
    public void testEvaluateReplaceFuncWhenEmptyReplacement() throws Exception {
        assertEvaluate("replace(name, 'Crate', '')", "barfoo", Literal.of(STRING, "barCratefoo"));
    }

    @Test
    public void testReplaceWithNullArgumentResultsInNull() {
        assertEvaluate("replace('foo', null, 'x')", null);
        assertEvaluate("replace(null, 'y', 'x')", null);
        assertEvaluate("replace('foo', 'y', null)", null);
    }
}
