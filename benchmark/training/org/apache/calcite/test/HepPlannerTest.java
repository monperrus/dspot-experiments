/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.test;


import FilterToCalcRule.INSTANCE;
import HepMatchOrder.ARBITRARY;
import HepMatchOrder.BOTTOM_UP;
import HepMatchOrder.DEPTH_FIRST;
import HepMatchOrder.TOP_DOWN;
import HepProgram.MATCH_UNTIL_FIXPOINT;
import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptListener;
import org.apache.calcite.plan.RelOptMaterialization;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.logical.LogicalIntersect;
import org.apache.calcite.rel.logical.LogicalUnion;
import org.apache.calcite.rel.rules.CoerceInputsRule;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


/**
 * HepPlannerTest is a unit test for {@link HepPlanner}. See
 * {@link RelOptRulesTest} for an explanation of how to add tests; the tests in
 * this class are targeted at exercising the planner, and use specific rules for
 * convenience only, whereas the tests in that class are targeted at exercising
 * specific rules, and use the planner for convenience only. Hence the split.
 */
public class HepPlannerTest extends RelOptTestBase {
    // ~ Static fields/initializers ---------------------------------------------
    private static final String UNION_TREE = "(select name from dept union select ename from emp)" + " union (select ename from bonus)";

    private static final String COMPLEX_UNION_TREE = "select * from (\n" + ((((((((((((((((((((("  select ENAME, 50011895 as cat_id, \'1\' as cat_name, 1 as require_free_postage, 0 as require_15return, 0 as require_48hour,1 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50011895 union all\n" + "  select ENAME, 50013023 as cat_id, \'2\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013023 union all\n") + "  select ENAME, 50013032 as cat_id, \'3\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013032 union all\n") + "  select ENAME, 50013024 as cat_id, \'4\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013024 union all\n") + "  select ENAME, 50004204 as cat_id, \'5\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50004204 union all\n") + "  select ENAME, 50013043 as cat_id, \'6\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013043 union all\n") + "  select ENAME, 290903 as cat_id, \'7\' as cat_name, 1 as require_free_postage, 0 as require_15return, 0 as require_48hour,1 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 290903 union all\n") + "  select ENAME, 50008261 as cat_id, \'8\' as cat_name, 1 as require_free_postage, 0 as require_15return, 0 as require_48hour,1 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50008261 union all\n") + "  select ENAME, 124478013 as cat_id, \'9\' as cat_name, 0 as require_free_postage, 0 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 124478013 union all\n") + "  select ENAME, 124472005 as cat_id, \'10\' as cat_name, 0 as require_free_postage, 0 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 124472005 union all\n") + "  select ENAME, 50013475 as cat_id, \'11\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013475 union all\n") + "  select ENAME, 50018263 as cat_id, \'12\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50018263 union all\n") + "  select ENAME, 50013498 as cat_id, \'13\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50013498 union all\n") + "  select ENAME, 350511 as cat_id, \'14\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 350511 union all\n") + "  select ENAME, 50019790 as cat_id, \'15\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50019790 union all\n") + "  select ENAME, 50015382 as cat_id, \'16\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50015382 union all\n") + "  select ENAME, 350503 as cat_id, \'17\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 350503 union all\n") + "  select ENAME, 350401 as cat_id, \'18\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 350401 union all\n") + "  select ENAME, 50015560 as cat_id, \'19\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50015560 union all\n") + "  select ENAME, 122658003 as cat_id, \'20\' as cat_name, 0 as require_free_postage, 1 as require_15return, 1 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 122658003 union all\n") + "  select ENAME, 50022371 as cat_id, \'100\' as cat_name, 0 as require_free_postage, 0 as require_15return, 0 as require_48hour,0 as require_insurance from emp where EMPNO = 20171216 and MGR = 0 and ENAME = \'Y\' and SAL = 50022371\n") + ") a");

    @Test
    public void testRuleClass() throws Exception {
        // Verify that an entire class of rules can be applied.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addRuleClass(CoerceInputsRule.class);
        HepPlanner planner = new HepPlanner(programBuilder.build());
        planner.addRule(new CoerceInputsRule(LogicalUnion.class, false, RelFactories.LOGICAL_BUILDER));
        planner.addRule(new CoerceInputsRule(LogicalIntersect.class, false, RelFactories.LOGICAL_BUILDER));
        checkPlanning(planner, ("(select name from dept union select ename from emp)" + " intersect (select fname from customer.contact)"));
    }

    @Test
    public void testRuleDescription() throws Exception {
        // Verify that a rule can be applied via its description.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addRuleByDescription("FilterToCalcRule");
        HepPlanner planner = new HepPlanner(programBuilder.build());
        planner.addRule(INSTANCE);
        checkPlanning(planner, "select name from sales.dept where deptno=12");
    }

    /**
     * Ensures {@link org.apache.calcite.rel.AbstractRelNode} digest does not include
     * full digest tree.
     */
    @Test
    public void relDigestLength() {
        HepProgramBuilder programBuilder = HepProgram.builder();
        HepPlanner planner = new HepPlanner(programBuilder.build());
        StringBuilder sb = new StringBuilder();
        final int n = 10;
        sb.append("select * from (");
        sb.append("select name from sales.dept");
        for (int i = 0; i < n; i++) {
            sb.append(" union all select name from sales.dept");
        }
        sb.append(")");
        RelRoot root = tester.convertSqlToRel(sb.toString());
        planner.setRoot(root.rel);
        RelNode best = planner.findBestExp();
        // Good digest should look like rel#66:LogicalProject(input=rel#64:LogicalUnion)
        // Bad digest includes full tree like rel#66:LogicalProject(input=rel#64:LogicalUnion(...))
        // So the assertion is to ensure digest includes LogicalUnion exactly once
        assertIncludesExactlyOnce("best.getDescription()", best.getDescription(), "LogicalUnion");
        assertIncludesExactlyOnce("best.getDigest()", best.getDigest(), "LogicalUnion");
    }

    @Test
    public void testMatchLimitOneTopDown() throws Exception {
        // Verify that only the top union gets rewritten.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addMatchOrder(TOP_DOWN);
        programBuilder.addMatchLimit(1);
        programBuilder.addRuleInstance(UnionToDistinctRule.INSTANCE);
        checkPlanning(programBuilder.build(), HepPlannerTest.UNION_TREE);
    }

    @Test
    public void testMatchLimitOneBottomUp() throws Exception {
        // Verify that only the bottom union gets rewritten.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addMatchLimit(1);
        programBuilder.addMatchOrder(BOTTOM_UP);
        programBuilder.addRuleInstance(UnionToDistinctRule.INSTANCE);
        checkPlanning(programBuilder.build(), HepPlannerTest.UNION_TREE);
    }

    @Test
    public void testMatchUntilFixpoint() throws Exception {
        // Verify that both unions get rewritten.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addMatchLimit(MATCH_UNTIL_FIXPOINT);
        programBuilder.addRuleInstance(UnionToDistinctRule.INSTANCE);
        checkPlanning(programBuilder.build(), HepPlannerTest.UNION_TREE);
    }

    @Test
    public void testReplaceCommonSubexpression() throws Exception {
        // Note that here it may look like the rule is firing
        // twice, but actually it's only firing once on the
        // common sub-expression.  The purpose of this test
        // is to make sure the planner can deal with
        // rewriting something used as a common sub-expression
        // twice by the same parent (the join in this case).
        checkPlanning(ProjectRemoveRule.INSTANCE, ("select d1.deptno from (select * from dept) d1," + " (select * from dept) d2"));
    }

    /**
     * Tests that if two relational expressions are equivalent, the planner
     * notices, and only applies the rule once.
     */
    @Test
    public void testCommonSubExpression() {
        // In the following,
        // (select 1 from dept where abs(-1)=20)
        // occurs twice, but it's a common sub-expression, so the rule should only
        // apply once.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addRuleInstance(INSTANCE);
        final HepPlannerTest.HepTestListener listener = new HepPlannerTest.HepTestListener(0);
        HepPlanner planner = new HepPlanner(programBuilder.build());
        planner.addListener(listener);
        final String sql = "(select 1 from dept where abs(-1)=20)\n" + ("union all\n" + "(select 1 from dept where abs(-1)=20)");
        planner.setRoot(tester.convertSqlToRel(sql).rel);
        RelNode bestRel = planner.findBestExp();
        Assert.assertThat(bestRel.getInput(0).equals(bestRel.getInput(1)), CoreMatchers.is(true));
        Assert.assertThat(((listener.getApplyTimes()) == 1), CoreMatchers.is(true));
    }

    @Test
    public void testSubprogram() throws Exception {
        // Verify that subprogram gets re-executed until fixpoint.
        // In this case, the first time through we limit it to generate
        // only one calc; the second time through it will generate
        // a second calc, and then merge them.
        HepProgramBuilder subprogramBuilder = HepProgram.builder();
        subprogramBuilder.addMatchOrder(TOP_DOWN);
        subprogramBuilder.addMatchLimit(1);
        subprogramBuilder.addRuleInstance(ProjectToCalcRule.INSTANCE);
        subprogramBuilder.addRuleInstance(CalcMergeRule.INSTANCE);
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addSubprogram(subprogramBuilder.build());
        checkPlanning(programBuilder.build(), "select upper(ename) from (select lower(ename) as ename from emp)");
    }

    @Test
    public void testGroup() throws Exception {
        // Verify simultaneous application of a group of rules.
        // Intentionally add them in the wrong order to make sure
        // that order doesn't matter within the group.
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addGroupBegin();
        programBuilder.addRuleInstance(CalcMergeRule.INSTANCE);
        programBuilder.addRuleInstance(ProjectToCalcRule.INSTANCE);
        programBuilder.addRuleInstance(INSTANCE);
        programBuilder.addGroupEnd();
        checkPlanning(programBuilder.build(), "select upper(name) from dept where deptno=20");
    }

    @Test
    public void testGC() throws Exception {
        HepProgramBuilder programBuilder = HepProgram.builder();
        programBuilder.addMatchOrder(TOP_DOWN);
        programBuilder.addRuleInstance(CalcMergeRule.INSTANCE);
        programBuilder.addRuleInstance(ProjectToCalcRule.INSTANCE);
        programBuilder.addRuleInstance(INSTANCE);
        HepPlanner planner = new HepPlanner(programBuilder.build());
        planner.setRoot(tester.convertSqlToRel("select upper(name) from dept where deptno=20").rel);
        planner.findBestExp();
        // Reuse of HepPlanner (should trigger GC).
        planner.setRoot(tester.convertSqlToRel("select upper(name) from dept where deptno=20").rel);
        planner.findBestExp();
    }

    @Test
    public void testRuleApplyCount() {
        final long applyTimes1 = checkRuleApplyCount(ARBITRARY);
        Assert.assertThat(applyTimes1, CoreMatchers.is(316L));
        final long applyTimes2 = checkRuleApplyCount(DEPTH_FIRST);
        Assert.assertThat(applyTimes2, CoreMatchers.is(87L));
    }

    @Test
    public void testMaterialization() throws Exception {
        HepPlanner planner = new HepPlanner(HepProgram.builder().build());
        RelNode tableRel = tester.convertSqlToRel("select * from dept").rel;
        RelNode queryRel = tableRel;
        RelOptMaterialization mat1 = new RelOptMaterialization(tableRel, queryRel, null, ImmutableList.of("default", "mv"));
        planner.addMaterialization(mat1);
        Assert.assertEquals(planner.getMaterializations().size(), 1);
        Assert.assertEquals(planner.getMaterializations().get(0), mat1);
        planner.clear();
        Assert.assertEquals(planner.getMaterializations().size(), 0);
    }

    /**
     * Listener for HepPlannerTest; counts how many times rules fire.
     */
    private class HepTestListener implements RelOptListener {
        private long applyTimes;

        HepTestListener(long applyTimes) {
            this.applyTimes = applyTimes;
        }

        long getApplyTimes() {
            return applyTimes;
        }

        @Override
        public void relEquivalenceFound(RelEquivalenceEvent event) {
        }

        @Override
        public void ruleAttempted(RuleAttemptedEvent event) {
            if (event.isBefore()) {
                ++(applyTimes);
            }
        }

        @Override
        public void ruleProductionSucceeded(RuleProductionEvent event) {
        }

        @Override
        public void relDiscarded(RelDiscardedEvent event) {
        }

        @Override
        public void relChosen(RelChosenEvent event) {
        }
    }
}

/**
 * End HepPlannerTest.java
 */