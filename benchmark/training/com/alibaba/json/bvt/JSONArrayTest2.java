package com.alibaba.json.bvt;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import junit.framework.TestCase;
import org.junit.Assert;


public class JSONArrayTest2 extends TestCase {
    public void test_0() throws Exception {
        long time = System.currentTimeMillis();
        JSONArray array = new JSONArray();
        array.add(null);
        array.add(1);
        array.add(time);
        Assert.assertEquals(0, array.getByteValue(0));
        Assert.assertEquals(0, array.getShortValue(0));
        Assert.assertTrue((0.0F == (array.getFloatValue(0))));
        Assert.assertTrue((0.0 == (array.getDoubleValue(0))));
        Assert.assertEquals(new BigInteger("1"), array.getBigInteger(1));
        Assert.assertEquals("1", array.getString(1));
        Assert.assertEquals(new Date(time), array.getDate(2));
        Assert.assertEquals(new java.sql.Date(time), array.getSqlDate(2));
        Assert.assertEquals(new Timestamp(time), array.getTimestamp(2));
        JSONArray array2 = ((JSONArray) (array.clone()));
        Assert.assertEquals(0, array2.getByteValue(0));
        Assert.assertEquals(0, array2.getShortValue(0));
        Assert.assertTrue((0.0F == (array2.getFloatValue(0))));
        Assert.assertTrue((0.0 == (array2.getDoubleValue(0))));
        Assert.assertEquals(new BigInteger("1"), array2.getBigInteger(1));
        Assert.assertEquals("1", array2.getString(1));
        Assert.assertEquals(new Date(time), array2.getDate(2));
        Assert.assertEquals(new java.sql.Date(time), array2.getSqlDate(2));
        Assert.assertEquals(new Timestamp(time), array2.getTimestamp(2));
        Assert.assertEquals(array2.size(), array2.size());
        JSON.parseArray("//arplatform.alicdn.com/images/874/1504512065305.png");
        JSON.parseArray("//arplatform.alicdn.com/images/874/1504512065305.png");
    }
}
