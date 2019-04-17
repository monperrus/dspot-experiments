package com.alibaba.druid.bvt.sql.mysql.param;


import JdbcConstants.MYSQL;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.ParameterizedOutputVisitorUtils;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.util.JdbcConstants;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;


/**
 * Created by wenshao on 16/8/23.
 */
public class MySqlParameterizedOutputVisitorTest_33 extends TestCase {
    public void test_for_parameterize() throws Exception {
        final String dbType = JdbcConstants.MYSQL;
        String sql = "select * from t where id = 1 or id = 2";
        String psql = ParameterizedOutputVisitorUtils.parameterize(sql, dbType);
        TestCase.assertEquals(("SELECT *\n" + ("FROM t\n" + "WHERE id = ?")), psql);
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = SQLUtils.createOutputVisitor(out, MYSQL);
        List<Object> parameters = new ArrayList<Object>();
        visitor.setParameterized(true);
        visitor.setParameterizedMergeInList(true);
        visitor.setParameters(parameters);
        visitor.setExportTables(true);
        /* visitor.setPrettyFormat(false); */
        SQLStatement stmt = stmtList.get(0);
        stmt.accept(visitor);
        // System.out.println(parameters);
        TestCase.assertEquals(1, parameters.size());
        // SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(psql, dbType);
        // List<SQLStatement> stmtList = parser.parseStatementList();
        SQLStatement pstmt = SQLUtils.parseStatements(psql, dbType).get(0);
        StringBuilder buf = new StringBuilder();
        SQLASTOutputVisitor visitor1 = SQLUtils.createOutputVisitor(buf, dbType);
        visitor1.addTableMapping("udata", "udata_0888");
        visitor1.setInputParameters(visitor.getParameters());
        pstmt.accept(visitor1);
        TestCase.assertEquals(("SELECT *\n" + ("FROM t\n" + "WHERE id IN (1, 2)")), buf.toString());
    }
}
