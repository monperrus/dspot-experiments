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
package io.crate.integrationtests;


import ESIntegTestCase.ClusterScope;
import io.crate.testing.TestingHelpers;
import org.hamcrest.Matchers;
import org.junit.Test;


@ClusterScope(minNumDataNodes = 1)
public class UnionIntegrationTest extends SQLTransportIntegrationTest {
    @Test
    public void testUnionAllSimpleSelect() {
        execute(("select * from unnest([1, 2, 3], ['1', '2', '3']) " + ("union all " + "select * from unnest([4, 5, 6], ['4', '5', '6'])")));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ 1L, "1" }, new Object[]{ 2L, "2" }, new Object[]{ 3L, "3" }, new Object[]{ 4L, "4" }, new Object[]{ 5L, "5" }, new Object[]{ 6L, "6" }));
    }

    @Test
    public void testUnionAllSelf() {
        execute(("select id from t1 " + ("union all " + "select id from t1")));
        assertThat(response.rows(), // same results twice
        Matchers.arrayContainingInAnyOrder(new Object[]{ 1 }, new Object[]{ 42 }, new Object[]{ 1000 }, new Object[]{ 1 }, new Object[]{ 42 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnionAll2Tables() {
        execute(("select id from t1 " + ("union all " + "select id from t2 ")));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ 1 }, new Object[]{ 11 }, new Object[]{ 42 }, new Object[]{ 43 }, new Object[]{ 1000 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnionAll3Tables() {
        execute(("select id from t1 " + ((("union all " + "select id from t2 ") + "union all ") + "select id from t3 where arr is null")));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ 1 }, new Object[]{ 11 }, new Object[]{ 42 }, new Object[]{ 43 }, new Object[]{ 44 }, new Object[]{ 111 }, new Object[]{ 1000 }, new Object[]{ 1000 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnion2TablesWithOrderBy() {
        execute(("select id from t1 " + (("union all " + "select id from t2 ") + "order by id")));
        assertThat(response.rows(), Matchers.arrayContaining(new Object[]{ 1 }, new Object[]{ 11 }, new Object[]{ 42 }, new Object[]{ 43 }, new Object[]{ 1000 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnionAll3TablesWithOrderBy() {
        execute(("select id from t1 " + ((((("union all " + "select id from t2 ") + "union all ") + "select id from t3 ") + "where arr is null ") + "order by id")));
        assertThat(response.rows(), Matchers.arrayContaining(new Object[]{ 1 }, new Object[]{ 11 }, new Object[]{ 42 }, new Object[]{ 43 }, new Object[]{ 44 }, new Object[]{ 111 }, new Object[]{ 1000 }, new Object[]{ 1000 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnionAllWith1SubSelect() {
        execute(("select * from (select text from t1 order by text limit 2) a " + ("union all " + "select text from t2 ")));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ "magic number" }, new Object[]{ "magic number" }, new Object[]{ "text" }, new Object[]{ "text" }, new Object[]{ "text2" }));
    }

    @Test
    public void testUnionAllWith1SubSelectOrderBy() {
        execute(("select * from (select id, text from t1 order by text limit 2) a " + (("union all " + "select id, text from t2 ") + "order by text, id")));
        assertThat(response.rows(), Matchers.arrayContaining(new Object[]{ 42, "magic number" }, new Object[]{ 43, "magic number" }, new Object[]{ 1, "text" }, new Object[]{ 11, "text" }, new Object[]{ 1000, "text2" }));
    }

    @Test
    public void testUnionAllWith2SubSelect() {
        execute(("select * from (select text from t1 order by text limit 2) a " + (("union all " + "select * from (select text from t2 order by text limit 1) b ") + "order by text ")));
        assertThat(response.rows(), Matchers.arrayContaining(new Object[]{ "magic number" }, new Object[]{ "magic number" }, new Object[]{ "text" }));
    }

    /**
     * The left and right side of the Union could utilize a fetch operation
     * (no ORDER BY specified). Fetch operations are currently not supported
     * in Union.
     */
    @Test
    public void testUnionAllNoFetching() {
        execute(("select * from (select text from t1 limit 1) a " + (("union all " + "select * from (select text from t2 limit 1) b ") + "order by text ")));
        assertThat(response.rows().length, Matchers.is(2));
    }

    @Test
    public void testUnionAllWithSystemTable() {
        execute(("select name from sys.nodes " + ("union all " + "select text from t2")));
        int numResults = (clusterService().state().nodes().getSize()) + 3;
        assertThat(response.rows().length, Matchers.is(numResults));
    }

    @Test
    public void testUnionAllSubselectJoins() {
        execute(("select * from (select t1.id from t1 join t2 on t1.id = t2.id) a " + (("union all " + "select * from (select t2.id from t1 join t2 on t1.text = t2.text) b ") + "order by id")));
        assertThat(response.rows(), Matchers.arrayContaining(new Object[]{ 11 }, new Object[]{ 43 }, new Object[]{ 1000 }));
    }

    @Test
    public void testUnionAllArrayAndObjectColumns() {
        execute(("select * from (select t1.id, t1.text, t3.arr, t3.obj from t1 join t3 on arr is not null) a " + (("union all " + "select id, text, [1,2], {custom = true} from t3 where arr is not null ") + "order by id")));
        assertThat(TestingHelpers.printedTable(response.rows()), Matchers.is(("1| text| [1, 2, 3]| {temperature=42}\n" + (("42| magic number| [1, 2, 3]| {temperature=42}\n" + "1000| text1| [1, 2, 3]| {temperature=42}\n") + "NULL| NULL| [1, 2]| {custom=true}\n"))));
    }

    @Test
    public void testUnionAllWithScalarSubqueries() {
        execute(("select * from (select count(*) from t1) a " + ("union all " + "select id::long from t2")));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ 3L }, new Object[]{ 11L }, new Object[]{ 43L }, new Object[]{ 1000L }));
    }

    @Test
    public void testUnionAllAsSubquery() {
        execute(("select t2.id from (select * from t1 union all select * from t2) a " + "join t2 on a.id = t2.id"));
        assertThat(response.rows(), Matchers.arrayContainingInAnyOrder(new Object[]{ 11 }, new Object[]{ 43 }, new Object[]{ 1000 }, new Object[]{ 1000 }));
    }
}
