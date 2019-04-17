package com.alibaba.druid.bvt.sql.schemaStat;


import JdbcConstants.MYSQL;
import TableStat.Relationship;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import java.util.Set;
import junit.framework.TestCase;


public class SchemaStatTest14 extends TestCase {
    public void test_schemaStat() throws Exception {
        String sql = "delete r from t_res r where id=1 ";
        SQLStatement stmt = SQLUtils.parseStatements(sql, MYSQL).get(0);
        System.out.println(stmt);
        SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(MYSQL);
        stmt.accept(statVisitor);
        Set<TableStat.Relationship> relationships = statVisitor.getRelationships();
        for (TableStat.Relationship relationship : relationships) {
            System.out.println(relationship);// table1.id = table2.id

        }
        System.out.println(("Tables : " + (statVisitor.getTables())));
        System.out.println(statVisitor.getColumns());
        // System.out.println(statVisitor.getGroupByColumns()); // group by
        System.out.println(("relationships : " + (statVisitor.getRelationships())));// group by

        System.out.println(statVisitor.getConditions());
        TestCase.assertEquals(0, relationships.size());
        TestCase.assertEquals(1, statVisitor.getColumns().size());
        TestCase.assertEquals(1, statVisitor.getConditions().size());
        TestCase.assertEquals(0, statVisitor.getFunctions().size());
        TestCase.assertTrue(statVisitor.containsTable("t_res"));
        TestCase.assertTrue(statVisitor.containsColumn("t_res", "id"));
    }
}
