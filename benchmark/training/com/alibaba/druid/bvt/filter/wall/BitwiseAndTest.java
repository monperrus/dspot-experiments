package com.alibaba.druid.bvt.filter.wall;


import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallUtils;
import junit.framework.TestCase;
import org.junit.Assert;


public class BitwiseAndTest extends TestCase {
    public void test_true() throws Exception {
        Assert.assertTrue(// 
        WallUtils.isValidateMySql("SELECT * from t where (id = 1) & 2"));// 

    }

    public void test_false() throws Exception {
        WallConfig config = new WallConfig();
        config.setConditionOpBitwseAllow(false);
        Assert.assertFalse(// 
        WallUtils.isValidateMySql("SELECT * from t where (id = 1) & 2", config));// 

    }
}
