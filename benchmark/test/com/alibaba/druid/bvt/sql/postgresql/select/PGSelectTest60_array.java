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
package com.alibaba.druid.bvt.sql.postgresql.select;


import SQLUtils.DEFAULT_LCASE_FORMAT_OPTION;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import java.util.List;
import junit.framework.TestCase;


public class PGSelectTest60_array extends TestCase {
    private final String dbType = JdbcConstants.POSTGRESQL;

    public void test_0() throws Exception {
        String sql = "SELECT COL1 from TABLE where NID = ANY(ARRAY[1,2,3])";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
        SQLStatement stmt = stmtList.get(0);
        TestCase.assertEquals(("SELECT COL1\n" + ("FROM TABLE\n" + "WHERE NID = ANY(ARRAY[1, 2, 3])")), SQLUtils.toPGString(stmt));
        TestCase.assertEquals(("select COL1\n" + ("from TABLE\n" + "where NID = ANY(ARRAY[1, 2, 3])")), SQLUtils.toPGString(stmt, DEFAULT_LCASE_FORMAT_OPTION));
        TestCase.assertEquals(1, stmtList.size());
        SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(dbType);
        stmt.accept(visitor);
        // System.out.println("Tables : " + visitor.getTables());
        // System.out.println("fields : " + visitor.getColumns());
        // System.out.println("coditions : " + visitor.getConditions());
        TestCase.assertEquals(2, visitor.getColumns().size());
        TestCase.assertEquals(1, visitor.getTables().size());
    }
}
