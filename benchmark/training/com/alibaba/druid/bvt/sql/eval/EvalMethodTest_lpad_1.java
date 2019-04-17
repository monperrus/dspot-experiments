package com.alibaba.druid.bvt.sql.eval;


import JdbcConstants.MYSQL;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import junit.framework.TestCase;
import org.junit.Assert;


public class EvalMethodTest_lpad_1 extends TestCase {
    public void test_method() throws Exception {
        Assert.assertEquals("h", SQLEvalVisitorUtils.evalExpr(MYSQL, "LPAD('hi',1,'??')"));
    }
}
