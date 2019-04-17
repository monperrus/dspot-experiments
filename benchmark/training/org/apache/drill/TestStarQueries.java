/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill;


import TypeProtos.MinorType.INT;
import TypeProtos.MinorType.VARCHAR;
import java.time.LocalDate;
import org.apache.drill.categories.PlannerTest;
import org.apache.drill.categories.SqlTest;
import org.apache.drill.categories.UnlikelyTest;
import org.apache.drill.common.exceptions.UserException;
import org.apache.drill.exec.record.BatchSchema;
import org.apache.drill.exec.record.metadata.SchemaBuilder;
import org.apache.drill.test.BaseTestQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Category({ SqlTest.class, PlannerTest.class })
public class TestStarQueries extends BaseTestQuery {
    static final Logger logger = LoggerFactory.getLogger(TestStarQueries.class);

    // see DRILL-2021
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarCommaSameColumnRepeated() throws Exception {
        BaseTestQuery.testBuilder().sqlQuery("select n_name, *, n_name, n_name from cp.`tpch/nation.parquet`").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarCommaSameColumnRepeated/q1.tsv").baselineTypes(VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR, VARCHAR).baselineColumns("n_name", "n_nationkey", "n_name0", "n_regionkey", "n_comment", "n_name00", "n_name1").build().run();
        BaseTestQuery.testBuilder().sqlQuery("select n_name, *, n_name, n_name from cp.`tpch/nation.parquet` limit 2").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarCommaSameColumnRepeated/q2.tsv").baselineTypes(VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR, VARCHAR).baselineColumns("n_name", "n_nationkey", "n_name0", "n_regionkey", "n_comment", "n_name00", "n_name1").build().run();
        BaseTestQuery.testBuilder().sqlQuery("select *, n_name, *, n_name, n_name from cp.`tpch/nation.parquet`").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarCommaSameColumnRepeated/q3.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "n_name0", "n_nationkey0", "n_name1", "n_regionkey0", "n_comment0", "n_name00", "n_name10").build().run();
        BaseTestQuery.testBuilder().sqlQuery("select *, n_name, *, n_name, n_name from cp.`tpch/nation.parquet` limit 2").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarCommaSameColumnRepeated/q4.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "n_name0", "n_nationkey0", "n_name1", "n_regionkey0", "n_comment0", "n_name00", "n_name10").build().run();
    }

    // see DRILL-1979
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarMultipleStarsRegularColumnAsAlias() throws Exception {
        BaseTestQuery.testBuilder().sqlQuery("select *, n_name as extra, *, n_name as extra from cp.`tpch/nation.parquet`").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarMultipleStarsRegularColumnAsAlias/q1.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "extra", "n_nationkey0", "n_name0", "n_regionkey0", "n_comment0", "extra0").build().run();
        BaseTestQuery.testBuilder().sqlQuery("select *, n_name as extra, *, n_name as extra from cp.`tpch/nation.parquet` limit 2").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarMultipleStarsRegularColumnAsAlias/q2.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "extra", "n_nationkey0", "n_name0", "n_regionkey0", "n_comment0", "extra0").build().run();
    }

    // see DRILL-1828
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarMultipleStars() throws Exception {
        BaseTestQuery.testBuilder().sqlQuery("select *, *, n_name from cp.`tpch/nation.parquet`").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarMultipleStars/q1.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "n_nationkey0", "n_name0", "n_regionkey0", "n_comment0", "n_name1").build().run();
        BaseTestQuery.testBuilder().sqlQuery("select *, *, n_name from cp.`tpch/nation.parquet` limit 2").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarMultipleStars/q2.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, INT, VARCHAR, INT, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "n_nationkey0", "n_name0", "n_regionkey0", "n_comment0", "n_name1").build().run();
    }

    // see DRILL-1825
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarWithAdditionalColumnLimit() throws Exception {
        BaseTestQuery.testBuilder().sqlQuery("select *, n_nationkey, *, n_name from cp.`tpch/nation.parquet` limit 2").ordered().csvBaselineFile("testframework/testStarQueries/testSelStarWithAdditionalColumnLimit/q1.tsv").baselineTypes(INT, VARCHAR, INT, VARCHAR, INT, INT, VARCHAR, INT, VARCHAR, VARCHAR).baselineColumns("n_nationkey", "n_name", "n_regionkey", "n_comment", "n_nationkey0", "n_nationkey1", "n_name0", "n_regionkey0", "n_comment0", "n_name1").build().run();
    }

    @Test
    public void testSelStarOrderBy() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery(" select * from cp.`employee.json` order by last_name").sqlBaselineQuery((" select employee_id, full_name,first_name,last_name,position_id,position_title,store_id," + ((" department_id,birth_date,hire_date,salary,supervisor_id,education_level,marital_status,gender,management_role " + " from cp.`employee.json` ") + " order by last_name "))).build().run();
    }

    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarOrderByLimit() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery(" select * from cp.`employee.json` order by last_name limit 2").sqlBaselineQuery((" select employee_id, full_name,first_name,last_name,position_id,position_title,store_id," + ((" department_id,birth_date,hire_date,salary,supervisor_id,education_level,marital_status,gender,management_role " + " from cp.`employee.json` ") + " order by last_name limit 2"))).build().run();
    }

    @Test
    public void testSelStarPlusRegCol() throws Exception {
        BaseTestQuery.testBuilder().unOrdered().sqlQuery("select *, n_nationkey as key2 from cp.`tpch/nation.parquet` order by n_name limit 2").sqlBaselineQuery("select n_comment, n_name, n_nationkey, n_regionkey, n_nationkey as key2 from cp.`tpch/nation.parquet` order by n_name limit 2").build().run();
    }

    @Test
    public void testSelStarWhereOrderBy() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select * from cp.`employee.json` where first_name = 'James' order by last_name").sqlBaselineQuery(("select employee_id, full_name,first_name,last_name,position_id,position_title,store_id," + ((" department_id,birth_date,hire_date,salary,supervisor_id,education_level,marital_status,gender,management_role " + " from cp.`employee.json` ") + " where first_name = 'James' order by last_name"))).build().run();
    }

    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarJoin() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select * from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").sqlBaselineQuery("select n.n_nationkey, n.n_name,n.n_regionkey,n.n_comment,r.r_regionkey,r.r_name, r.r_comment from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").build().run();
    }

    @Test
    public void testSelLeftStarJoin() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select n.* from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").sqlBaselineQuery("select n.n_nationkey, n.n_name, n.n_regionkey, n.n_comment from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").build().run();
    }

    @Test
    public void testSelRightStarJoin() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select r.* from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").sqlBaselineQuery("select r.r_regionkey, r.r_name, r.r_comment from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").build().run();
    }

    @Test
    public void testSelStarRegColConstJoin() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select *, n.n_nationkey as n_nationkey0, 1 + 2 as constant from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").sqlBaselineQuery((" select n.n_nationkey, n.n_name, n.n_regionkey, n.n_comment, r.r_regionkey, r.r_name, r.r_comment, " + (((" n.n_nationkey as n_nationkey0, 1 + 2 as constant " + " from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r ") + " where n.n_regionkey = r.r_regionkey ") + " order by n.n_name"))).build().run();
    }

    @Test
    public void testSelStarBothSideJoin() throws Exception {
        BaseTestQuery.testBuilder().unOrdered().sqlQuery("select n.*, r.* from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey").sqlBaselineQuery("select n.n_nationkey,n.n_name,n.n_regionkey,n.n_comment,r.r_regionkey,r.r_name,r.r_comment from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey order by n.n_name").build().run();
    }

    @Test
    public void testSelStarJoinSameColName() throws Exception {
        BaseTestQuery.testBuilder().unOrdered().sqlQuery("select * from cp.`tpch/nation.parquet` n1, cp.`tpch/nation.parquet` n2 where n1.n_nationkey = n2.n_nationkey").sqlBaselineQuery(("select n1.n_nationkey,n1.n_name,n1.n_regionkey,n1.n_comment,n2.n_nationkey,n2.n_name,n2.n_regionkey, n2.n_comment " + "from cp.`tpch/nation.parquet` n1, cp.`tpch/nation.parquet` n2 where n1.n_nationkey = n2.n_nationkey")).build().run();
    }

    // DRILL-1293
    @Test
    @Category(UnlikelyTest.class)
    public void testStarView1() throws Exception {
        BaseTestQuery.test("use dfs.tmp");
        BaseTestQuery.test("create view vt1 as select * from cp.`tpch/region.parquet` r, cp.`tpch/nation.parquet` n where r.r_regionkey = n.n_regionkey");
        try {
            BaseTestQuery.test("select * from vt1");
        } finally {
            BaseTestQuery.test("drop view vt1");
        }
    }

    // select star for a SchemaTable.
    @Test
    public void testSelStarSubQSchemaTable() throws Exception {
        BaseTestQuery.test("select name, kind, accessibleScopes from (select * from sys.options);");
    }

    // Join a select star of SchemaTable, with a select star of Schema-less table.
    @Test
    public void testSelStarJoinSchemaWithSchemaLess() throws Exception {
        String query = "select t1.name, t1.kind, t2.n_nationkey from " + (("(select * from sys.options) t1 " + "join (select * from cp.`tpch/nation.parquet`) t2 ") + "on t1.name = t2.n_name");
        try {
            BaseTestQuery.alterSession("planner.enable_broadcast_join", false);
            BaseTestQuery.test(query);
        } finally {
            BaseTestQuery.resetSessionOption("planner.enable_broadcast_join");
        }
        BaseTestQuery.test(query);
    }

    // see DRILL-1811
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarDifferentColumnOrder() throws Exception {
        BaseTestQuery.test("select first_name, * from cp.`employee.json`;");
        BaseTestQuery.test("select *, first_name, *, last_name from cp.`employee.json`;");
    }

    // Should get "At line 1, column 8: Column 'n_nationkey' is ambiguous"
    @Test(expected = UserException.class)
    public void testSelStarAmbiguousJoin() throws Exception {
        try {
            BaseTestQuery.test("select x.n_nationkey, x.n_name, x.n_regionkey, x.r_name from (select * from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r where n.n_regionkey = r.r_regionkey) x ");
        } catch (UserException e) {
            TestStarQueries.logger.info(("***** Test resulted in expected failure: " + (e.getMessage())));
            throw e;
        }
    }

    @Test
    public void testSelStarSubQJson2() throws Exception {
        BaseTestQuery.test("select v.first_name from (select * from cp.`employee.json`) v limit 2");
    }

    // Select * in SubQuery,  View  or CTE (With clause)
    // Select * in SubQuery : regular columns appear in select clause, where, group by, order by.
    @Test
    public void testSelStarSubQPrefix() throws Exception {
        BaseTestQuery.test("select t.n_nationkey, t.n_name, t.n_regionkey from (select * from cp.`tpch/nation.parquet`) t where t.n_regionkey > 1 order by t.n_name");
        BaseTestQuery.test("select n.n_regionkey, count(*) as cnt from ( select * from ( select * from cp.`tpch/nation.parquet`) t where t.n_nationkey < 10 ) n where n.n_nationkey >1 group by n.n_regionkey order by n.n_regionkey ; ");
        BaseTestQuery.test("select t.n_regionkey, count(*) as cnt from (select * from cp.`tpch/nation.parquet`) t where t.n_nationkey > 1 group by t.n_regionkey order by t.n_regionkey;");
    }

    // Select * in SubQuery : regular columns appear in select clause, where, group by, order by.
    @Test
    public void testSelStarSubQNoPrefix() throws Exception {
        BaseTestQuery.test("select n_nationkey, n_name, n_regionkey from (select * from cp.`tpch/nation.parquet`)  where n_regionkey > 1 order by n_name");
        BaseTestQuery.test("select n_regionkey, count(*) as cnt from ( select * from ( select * from cp.`tpch/nation.parquet`)  where n_nationkey < 10 ) where n_nationkey >1 group by n_regionkey order by n_regionkey ; ");
        BaseTestQuery.test("select n_regionkey, count(*) as cnt from (select * from cp.`tpch/nation.parquet`) t where n_nationkey > 1 group by n_regionkey order by n_regionkey;");
    }

    // join two SubQuery, each having select * : regular columns appear in the select , where and on clause, group by, order by.
    @Test
    @Category(UnlikelyTest.class)
    public void testSelStarSubQJoin() throws Exception {
        // select clause, where.
        BaseTestQuery.test((" select n.n_nationkey, n.n_name, n.n_regionkey, r.r_name \n" + ((" from (select * from cp.`tpch/nation.parquet`) n, \n" + "      (select * from cp.`tpch/region.parquet`) r \n") + " where n.n_regionkey = r.r_regionkey ")));
        // select clause, where, group by, order by
        BaseTestQuery.test((" select n.n_regionkey, count(*) as cnt \n" + ((((" from (select * from cp.`tpch/nation.parquet`) n  \n" + "    , (select * from cp.`tpch/region.parquet`) r  \n") + " where n.n_regionkey = r.r_regionkey and n.n_nationkey > 10 \n") + " group by n.n_regionkey \n") + " order by n.n_regionkey; ")));
        // Outer query use select *. Join condition in where clause.
        BaseTestQuery.test((" select *  \n" + ((" from (select * from cp.`tpch/nation.parquet`) n \n" + "    , (select * from cp.`tpch/region.parquet`) r \n") + " where n.n_regionkey = r.r_regionkey ")));
        // Outer query use select *. Join condition in on clause.
        BaseTestQuery.test((" select *  \n" + ((" from (select * from cp.`tpch/nation.parquet`) n \n" + "    join (select * from cp.`tpch/region.parquet`) r \n") + " on n.n_regionkey = r.r_regionkey ")));
    }

    @Test
    public void testSelectStartSubQueryJoinWithWhereClause() throws Exception {
        // select clause, where, on, group by, order by.
        BaseTestQuery.test((" select n.n_regionkey, count(*) as cnt \n" + (((((" from   (select * from cp.`tpch/nation.parquet`) n  \n" + "   join (select * from cp.`tpch/region.parquet`) r  \n") + " on n.n_regionkey = r.r_regionkey \n") + " where n.n_nationkey > 10 \n") + " group by n.n_regionkey \n") + " order by n.n_regionkey; ")));
    }

    // DRILL-595 : Select * in CTE WithClause : regular columns appear in select clause, where, group by, order by.
    @Test
    public void testDRILL_595WithClause() throws Exception {
        BaseTestQuery.test((" with x as (select * from cp.`region.json`) \n" + (" select x.region_id, x.sales_city \n" + " from x where x.region_id > 10 limit 5;")));
        BaseTestQuery.test((" with x as (select * from cp.`region.json`) \n" + (" select region_id, sales_city \n" + " from x where region_id > 10 limit 5;")));
        BaseTestQuery.test((" with x as (select * from cp.`tpch/nation.parquet`) \n" + ((((" select x.n_regionkey, count(*) as cnt \n" + " from x \n") + " where x.n_nationkey > 5 \n") + " group by x.n_regionkey \n") + " order by cnt limit 5; ")));
    }

    // DRILL-595 : Join two CTE, each having select * : regular columns appear in the select , where and on clause, group by, order by.
    @Test
    @Category(UnlikelyTest.class)
    public void testDRILL_595WithClauseJoin() throws Exception {
        BaseTestQuery.test(("with n as (select * from cp.`tpch/nation.parquet`), \n " + ((("     r as (select * from cp.`tpch/region.parquet`) \n" + "select n.n_nationkey, n.n_name, n.n_regionkey, r.r_name \n") + "from  n, r \n") + "where n.n_regionkey = r.r_regionkey ;")));
        BaseTestQuery.test(("with n as (select * from cp.`tpch/nation.parquet`), \n " + ((((("     r as (select * from cp.`tpch/region.parquet`) \n" + "select n.n_regionkey, count(*) as cnt \n") + "from  n, r \n") + "where n.n_regionkey = r.r_regionkey  and n.n_nationkey > 5 \n") + "group by n.n_regionkey \n") + "order by cnt;")));
    }

    // DRILL-1889
    @Test
    @Category(UnlikelyTest.class)
    public void testStarWithOtherExpression() throws Exception {
        BaseTestQuery.testBuilder().ordered().sqlQuery("select *  from cp.`tpch/nation.parquet` order by substr(n_name, 2, 5) limit 3").sqlBaselineQuery("select n_comment, n_name, n_nationkey, n_regionkey from cp.`tpch/nation.parquet` order by substr(n_name, 2, 5) limit 3 ").build().run();
        BaseTestQuery.testBuilder().ordered().sqlQuery("select *, n_nationkey + 5 as myexpr from cp.`tpch/nation.parquet` limit 3").sqlBaselineQuery("select n_comment, n_name, n_nationkey, n_regionkey, n_nationkey + 5 as myexpr from cp.`tpch/nation.parquet` order by n_nationkey limit 3").build().run();
        BaseTestQuery.testBuilder().ordered().sqlQuery("select *  from cp.`tpch/nation.parquet` where n_nationkey + 5 > 10 limit 3").sqlBaselineQuery("select n_comment, n_name, n_nationkey, n_regionkey  from cp.`tpch/nation.parquet` where n_nationkey + 5 > 10 order by n_nationkey limit 3").build().run();
    }

    // DRILL-1500
    @Test
    @Category(UnlikelyTest.class)
    public void testStarPartitionFilterOrderBy() throws Exception {
        LocalDate mydate = LocalDate.parse("1994-01-20");
        BaseTestQuery.testBuilder().sqlQuery("select * from dfs.`multilevel/parquet` where dir0=1994 and dir1='Q1' order by dir0 limit 1").ordered().baselineColumns("dir0", "dir1", "o_clerk", "o_comment", "o_custkey", "o_orderdate", "o_orderkey", "o_orderpriority", "o_orderstatus", "o_shippriority", "o_totalprice").baselineValues("1994", "Q1", "Clerk#000000743", "y pending requests integrate", 1292, mydate, 66, "5-LOW", "F", 0, 104190.66).build().run();
    }

    // DRILL-2069
    @Test
    @Category(UnlikelyTest.class)
    public void testStarInSubquery() throws Exception {
        BaseTestQuery.testBuilder().unOrdered().sqlQuery("select * from cp.`tpch/nation.parquet` where n_regionkey in (select r_regionkey from cp.`tpch/region.parquet`)").sqlBaselineQuery("select n_nationkey, n_name, n_regionkey, n_comment from cp.`tpch/nation.parquet` where n_regionkey in (select r_regionkey from cp.`tpch/region.parquet`)").build().run();
        // multiple columns in "IN" subquery predicates.
        BaseTestQuery.testBuilder().unOrdered().sqlQuery("select * from cp.`tpch/nation.parquet` where (n_nationkey, n_name) in ( select n_nationkey, n_name from cp.`tpch/nation.parquet`)").sqlBaselineQuery("select n_nationkey, n_name, n_regionkey, n_comment from cp.`tpch/nation.parquet` where (n_nationkey, n_name) in ( select n_nationkey, n_name from cp.`tpch/nation.parquet`)").build().run();
        // Multiple in subquery predicates.
        BaseTestQuery.testBuilder().unOrdered().sqlQuery(("select * from cp.`tpch/nation.parquet` " + ("where n_regionkey in ( select r_regionkey from cp.`tpch/region.parquet`) and " + "      n_name in (select n_name from cp.`tpch/nation.parquet`)"))).sqlBaselineQuery(("select n_nationkey, n_name, n_regionkey, n_comment from cp.`tpch/nation.parquet` " + ("where n_regionkey in ( select r_regionkey from cp.`tpch/region.parquet`) and " + "      n_name in (select n_name from cp.`tpch/nation.parquet`)"))).build().run();
        // Both the out QB and SUBQ are join.
        BaseTestQuery.testBuilder().unOrdered().sqlQuery(("select * from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r " + (((("where n.n_regionkey = r.r_regionkey and " + "       (n.n_nationkey, n.n_name) in ") + "          ( select n2.n_nationkey, n2.n_name ") + "            from cp.`tpch/nation.parquet` n2, cp.`tpch/region.parquet` r2 ") + "            where n2.n_regionkey = r2.r_regionkey)"))).sqlBaselineQuery(("select n.n_nationkey, n.n_name, n.n_regionkey, n.n_comment, r.r_regionkey, r.r_name, r.r_comment " + ((((("from cp.`tpch/nation.parquet` n, cp.`tpch/region.parquet` r " + "where n.n_regionkey = r.r_regionkey and ") + "       (n.n_nationkey, n.n_name) in ") + "          ( select n2.n_nationkey, n2.n_name ") + "            from cp.`tpch/nation.parquet` n2, cp.`tpch/region.parquet` r2 ") + "            where n2.n_regionkey = r2.r_regionkey)"))).build().run();
    }

    // DRILL-2802
    @Test
    @Category(UnlikelyTest.class)
    public void testSelectPartitionColumnOnly() throws Exception {
        final String[] expectedPlan1 = new String[]{ ".*Project.*dir0=\\[\\$0\\]" };
        final String[] excludedPlan1 = new String[]{  };
        PlanTestBase.testPlanMatchingPatterns("select dir0 from dfs.`multilevel/parquet` limit 1", expectedPlan1, excludedPlan1);
        final String[] expectedPlan2 = new String[]{ ".*Project.*dir0=\\[\\$0\\], dir1=\\[\\$1\\]" };
        final String[] excludedPlan2 = new String[]{  };
        PlanTestBase.testPlanMatchingPatterns("select dir0, dir1 from dfs.`multilevel/parquet` limit 1", expectedPlan2, excludedPlan2);
    }

    // DRILL-2053 : column name is case-insensitive when join a CTE with a regluar table.
    @Test
    @Category(UnlikelyTest.class)
    public void testCaseSenJoinCTEWithRegTab() throws Exception {
        final String query1 = "with a as ( select * from cp.`tpch/nation.parquet` ) select * from a, cp.`tpch/region.parquet` b where a.N_REGIONKEY = b.R_REGIONKEY";
        int actualRecordCount = BaseTestQuery.testSql(query1);
        int expectedRecordCount = 25;
        Assert.assertEquals(String.format("Received unexpected number of rows in output for query:\n%s\n expected=%d, received=%s", query1, expectedRecordCount, actualRecordCount), expectedRecordCount, actualRecordCount);
        final String query2 = "with a as ( select * from cp.`tpch/nation.parquet` ) select * from a, cp.`tpch/region.parquet` b where a.n_regionkey = b.r_regionkey";
        actualRecordCount = BaseTestQuery.testSql(query2);
        expectedRecordCount = 25;
        Assert.assertEquals(String.format("Received unexpected number of rows in output for query:\n%s\n expected=%d, received=%s", query2, expectedRecordCount, actualRecordCount), expectedRecordCount, actualRecordCount);
    }

    // DRILL-5845
    @Test
    public void testSchemaForStarOrderByLimit() throws Exception {
        final String query = "select * from cp.`tpch/nation.parquet` order by n_name limit 1";
        final BatchSchema expectedSchema = new SchemaBuilder().add("n_nationkey", INT).add("n_name", VARCHAR).add("n_regionkey", INT).add("n_comment", VARCHAR).build();
        BaseTestQuery.testBuilder().sqlQuery(query).schemaBaseLine(expectedSchema).build().run();
    }

    // DRILL-5822
    @Test
    public void testSchemaForParallelizedStarOrderBy() throws Exception {
        final String query = "select * from cp.`tpch/region.parquet` order by r_name";
        final BatchSchema expectedSchema = new SchemaBuilder().add("r_regionkey", INT).add("r_name", VARCHAR).add("r_comment", VARCHAR).build();
        BaseTestQuery.testBuilder().sqlQuery(query).optionSettingQueriesForTestQuery("alter session set `planner.slice_target`=1").schemaBaseLine(expectedSchema).build().run();
    }
}
