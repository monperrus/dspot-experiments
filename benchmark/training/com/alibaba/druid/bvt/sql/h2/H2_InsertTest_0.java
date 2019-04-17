package com.alibaba.druid.bvt.sql.h2;


import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.h2.parser.H2StatementParser;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author machunxiao
 * @unknown 2018-08-02
 */
public class H2_InsertTest_0 {
    @Test
    public void test_insertSet() {
        String sql = "insert into tb1 set name='n1',age=12,date='1990-11-11 12:12:12'";
        H2StatementParser parser = new H2StatementParser(sql);
        List<SQLStatement> sqlStatements = parser.parseStatementList();
        Assert.assertEquals(1, sqlStatements.size());
    }
}
