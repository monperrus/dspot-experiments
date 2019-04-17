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
package io.crate.execution.engine.join;


import JoinType.CROSS;
import JoinType.INNER;
import JoinType.LEFT;
import JoinType.SEMI;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.crate.analyze.relations.AnalyzedRelation;
import io.crate.analyze.relations.JoinPair;
import io.crate.expression.symbol.Symbol;
import io.crate.sql.tree.QualifiedName;
import io.crate.test.integration.CrateUnitTest;
import io.crate.testing.SqlExpressions;
import io.crate.testing.T3;
import io.crate.testing.TestingHelpers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;


public class JoinOperationsTest extends CrateUnitTest {
    private static final Map<QualifiedName, AnalyzedRelation> sources = ImmutableMap.of(new QualifiedName(T3.T1_INFO.ident().name()), T3.TR_1, new QualifiedName(T3.T2_INFO.ident().name()), T3.TR_2, new QualifiedName(T3.T3_INFO.ident().name()), T3.TR_3);

    private static final SqlExpressions expressions = new SqlExpressions(JoinOperationsTest.sources);

    @Test
    public void testImplicitToExplicit_NoRemainingWhereQuery_NoConversion() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, INNER, asSymbol("t1.a = t2.b")));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, Collections.emptyMap());
        assertThat(newJoinPairs, Matchers.contains(JoinPair.of(T3.T1, T3.T2, INNER, asSymbol("t1.a = t2.b"))));
    }

    @Test
    public void testImplicitToExplicit_QueryDoesNotInvolveTwoRelations_NoConversion() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, INNER, asSymbol("t1.a = t2.b")));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2, T3.T3), asSymbol("t1.x = t2.y + t3.z"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("(doc.t1.a = doc.t2.b)"));
        assertThat(joinPair.joinType(), Matchers.is(INNER));
        assertThat(remainingQueries.size(), Matchers.is(1));
    }

    @Test
    public void testImplicitToExplicit_InnerJoinPairWithConditionAlreadyExists() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, INNER, asSymbol("t1.a = t2.b")));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2), asSymbol("t1.x = t2.y"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("((doc.t1.a = doc.t2.b) AND (doc.t1.x = doc.t2.y))"));
        assertThat(joinPair.joinType(), Matchers.is(INNER));
        assertThat(remainingQueries.isEmpty(), Matchers.is(true));
    }

    @Test
    public void testImplicitToExplicit_CrossJoinPairAlreadyExists() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, CROSS, null));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2), asSymbol("t1.x = t2.y"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("(doc.t1.x = doc.t2.y)"));
        assertThat(joinPair.joinType(), Matchers.is(INNER));
        assertThat(remainingQueries.isEmpty(), Matchers.is(true));
    }

    @Test
    public void testImplicitToExplicit_JoinPairDoesNotExist() {
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2), asSymbol("t1.x = t2.y"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(Collections.emptyList(), remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("(doc.t1.x = doc.t2.y)"));
        assertThat(joinPair.joinType(), Matchers.is(INNER));
        assertThat(remainingQueries.isEmpty(), Matchers.is(true));
    }

    @Test
    public void testImplicitToExplicit_OuterJoinPairExists_NoConversion() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, LEFT, asSymbol("t1.a = t2.b")));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2), asSymbol("t1.x = t2.y"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("(doc.t1.a = doc.t2.b)"));
        assertThat(joinPair.joinType(), Matchers.is(LEFT));
        assertThat(remainingQueries.size(), Matchers.is(1));
    }

    @Test
    public void testImplicitToExplicit_SemiJoinPairExists_NoConversion() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, SEMI, asSymbol("t1.a = t2.b")));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T1, T3.T2), asSymbol("t1.x = t2.y"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        assertThat(newJoinPairs.size(), Matchers.is(1));
        JoinPair joinPair = newJoinPairs.get(0);
        assertThat(joinPair.condition(), TestingHelpers.isSQL("(doc.t1.a = doc.t2.b)"));
        assertThat(joinPair.joinType(), Matchers.is(SEMI));
        assertThat(remainingQueries.size(), Matchers.is(1));
    }

    @Test
    public void testImplicitToExplicit_OrderOfPairsRemains() {
        List<JoinPair> joinPairs = new ArrayList<>();
        joinPairs.add(JoinPair.of(T3.T1, T3.T2, INNER, asSymbol("t1.a = t2.b")));
        joinPairs.add(JoinPair.of(T3.T2, T3.T3, INNER, asSymbol("t2.y = t3.z")));
        Map<Set<QualifiedName>, Symbol> remainingQueries = new HashMap<>();
        remainingQueries.put(Sets.newHashSet(T3.T2, T3.T3), asSymbol("t2.b = t3.c"));
        List<JoinPair> newJoinPairs = JoinOperations.convertImplicitJoinConditionsToJoinPairs(joinPairs, remainingQueries);
        for (int i = 0; i < (joinPairs.size()); i++) {
            JoinPair oldPairAtPos = joinPairs.get(i);
            JoinPair newPairAtPos = newJoinPairs.get(i);
            assertThat(oldPairAtPos.left(), Matchers.is(newPairAtPos.left()));
            assertThat(oldPairAtPos.right(), Matchers.is(newPairAtPos.right()));
        }
    }
}
