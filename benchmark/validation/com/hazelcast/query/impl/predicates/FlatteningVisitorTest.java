/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.query.impl.predicates;


import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.impl.Indexes;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class FlatteningVisitorTest {
    private FlatteningVisitor visitor;

    private Indexes indexes;

    @Test
    public void visitAndPredicate_whenHasInnerAndPredicate_thenFlattenIt() {
        // (a1 = 1 and (a2 = 2 and a3 = 3))  -->  (a1 = 1 and a2 = 2 and a3 = 3)
        Predicate a1 = Predicates.equal("a1", 1);
        Predicate a2 = Predicates.equal("a2", 2);
        Predicate a3 = Predicates.equal("a3", 3);
        AndPredicate innerAnd = ((AndPredicate) (Predicates.and(a2, a3)));
        AndPredicate outerAnd = ((AndPredicate) (Predicates.and(a1, innerAnd)));
        AndPredicate result = ((AndPredicate) (visitor.visit(outerAnd, indexes)));
        Predicate[] inners = result.predicates;
        Assert.assertEquals(3, inners.length);
    }

    @Test
    public void visitOrPredicate_whenHasInnerOrPredicate_thenFlattenIt() {
        // (a1 = 1 or (a2 = 2 or a3 = 3))  -->  (a1 = 1 or a2 = 2 or a3 = 3)
        Predicate a1 = Predicates.equal("a1", 1);
        Predicate a2 = Predicates.equal("a2", 2);
        Predicate a3 = Predicates.equal("a3", 3);
        OrPredicate innerOr = ((OrPredicate) (Predicates.or(a2, a3)));
        OrPredicate outerOr = ((OrPredicate) (Predicates.or(a1, innerOr)));
        OrPredicate result = ((OrPredicate) (visitor.visit(outerOr, indexes)));
        Predicate[] inners = result.predicates;
        Assert.assertEquals(3, inners.length);
    }

    @Test
    public void visitNotPredicate_whenContainsNegatablePredicate_thenFlattenIt() {
        // (not(equals(foo, 1)))  -->  (notEquals(foo, 1))
        Predicate negated = Mockito.mock(Predicate.class);
        NegatablePredicate negatablePredicate = Mockito.mock(NegatablePredicate.class, Mockito.withSettings().extraInterfaces(Predicate.class));
        Mockito.when(negatablePredicate.negate()).thenReturn(negated);
        NotPredicate outerPredicate = ((NotPredicate) (Predicates.not(((Predicate) (negatablePredicate)))));
        Predicate result = visitor.visit(outerPredicate, indexes);
        Assert.assertEquals(negated, result);
    }
}
