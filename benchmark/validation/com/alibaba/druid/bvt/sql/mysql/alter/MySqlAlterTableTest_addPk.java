/**
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.druid.bvt.sql.mysql.alter;


import JdbcConstants.MYSQL;
import SQLUtils.DEFAULT_LCASE_FORMAT_OPTION;
import Token.EOF;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import junit.framework.TestCase;


public class MySqlAlterTableTest_addPk extends TestCase {
    public void test_alter_first() throws Exception {
        String sql = "ALTER TABLE test.table ADD PRIMARY KEY test_pk(id);";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(EOF);
        SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(MYSQL);
        stmt.accept(visitor);
        System.out.println(("Tables : " + (visitor.getTables())));
        System.out.println(("fields : " + (visitor.getColumns())));
        // System.out.println("coditions : " + visitor.getConditions());
        // System.out.println("orderBy : " + visitor.getOrderByColumns());
        TestCase.assertEquals(("ALTER TABLE test.table\n" + "\tADD CONSTRAINT test_pk PRIMARY KEY (id);"), SQLUtils.toMySqlString(stmt));
        TestCase.assertEquals(("alter table test.table\n" + "\tadd constraint test_pk primary key (id);"), SQLUtils.toMySqlString(stmt, DEFAULT_LCASE_FORMAT_OPTION));
        TestCase.assertEquals(1, visitor.getTables().size());
        TestCase.assertEquals(1, visitor.getColumns().size());
        TableStat tableStat = visitor.getTableStat("test.table");
        TestCase.assertNotNull(tableStat);
        TestCase.assertEquals(1, tableStat.getAlterCount());
        TestCase.assertEquals(1, tableStat.getCreateIndexCount());
    }
}
