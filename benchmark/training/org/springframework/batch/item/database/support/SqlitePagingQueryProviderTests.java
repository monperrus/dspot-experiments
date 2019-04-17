/**
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.item.database.support;


import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Thomas Risberg
 * @author Michael Minella
 * @author Luke Taylor
 */
public class SqlitePagingQueryProviderTests extends AbstractSqlPagingQueryProviderTests {
    public SqlitePagingQueryProviderTests() {
        pagingQueryProvider = new MySqlPagingQueryProvider();
    }

    @Test
    @Override
    public void testGenerateFirstPageQuery() {
        String sql = "SELECT id, name, age FROM foo WHERE bar = 1 ORDER BY id ASC LIMIT 100";
        String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
        Assert.assertEquals(sql, s);
    }

    @Test
    @Override
    public void testGenerateRemainingPagesQuery() {
        String sql = "SELECT id, name, age FROM foo WHERE (bar = 1) AND ((id > ?)) ORDER BY id ASC LIMIT 100";
        String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
        Assert.assertEquals(sql, s);
    }

    @Test
    @Override
    public void testGenerateJumpToItemQuery() {
        String sql = "SELECT id FROM foo WHERE bar = 1 ORDER BY id ASC LIMIT 99, 1";
        String s = pagingQueryProvider.generateJumpToItemQuery(145, pageSize);
        Assert.assertEquals(sql, s);
    }

    @Test
    @Override
    public void testGenerateJumpToItemQueryForFirstPage() {
        String sql = "SELECT id FROM foo WHERE bar = 1 ORDER BY id ASC LIMIT 0, 1";
        String s = pagingQueryProvider.generateJumpToItemQuery(45, pageSize);
        Assert.assertEquals(sql, s);
    }

    @Override
    @Test
    public void testGenerateFirstPageQueryWithGroupBy() {
        pagingQueryProvider.setGroupClause("dep");
        String sql = "SELECT id, name, age FROM foo WHERE bar = 1 GROUP BY dep ORDER BY id ASC LIMIT 100";
        String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
        Assert.assertEquals(sql, s);
    }

    @Override
    @Test
    public void testGenerateRemainingPagesQueryWithGroupBy() {
        pagingQueryProvider.setGroupClause("dep");
        String sql = "SELECT *  FROM (SELECT id, name, age FROM foo WHERE bar = 1 GROUP BY dep) AS MAIN_QRY WHERE ((id > ?)) ORDER BY id ASC LIMIT 100";
        String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
        Assert.assertEquals(sql, s);
    }

    @Override
    @Test
    public void testGenerateJumpToItemQueryWithGroupBy() {
        pagingQueryProvider.setGroupClause("dep");
        String sql = "SELECT id FROM foo WHERE bar = 1 GROUP BY dep ORDER BY id ASC LIMIT 99, 1";
        String s = pagingQueryProvider.generateJumpToItemQuery(145, pageSize);
        Assert.assertEquals(sql, s);
    }

    @Override
    @Test
    public void testGenerateJumpToItemQueryForFirstPageWithGroupBy() {
        pagingQueryProvider.setGroupClause("dep");
        String sql = "SELECT id FROM foo WHERE bar = 1 GROUP BY dep ORDER BY id ASC LIMIT 0, 1";
        String s = pagingQueryProvider.generateJumpToItemQuery(45, pageSize);
        Assert.assertEquals(sql, s);
    }
}
