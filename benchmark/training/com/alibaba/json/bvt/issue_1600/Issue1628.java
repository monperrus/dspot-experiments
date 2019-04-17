package com.alibaba.json.bvt.issue_1600;


import SerializeConfig.globalInstance;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;


public class Issue1628 extends TestCase {
    public void test_toJSONBytes() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SimplePropertyPreFilter("a"));
        TestCase.assertEquals("{\"a\":1001}", new String(bytes));
    }

    public void test_toJSONBytes_1() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, new SerializeFilter[]{ new SimplePropertyPreFilter("a") });
        TestCase.assertEquals("{\"a\":1001}", new String(bytes));
    }

    public void test_toJSONBytes_2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", 1001);
        map.put("b", 2002);
        byte[] bytes = JSON.toJSONBytes(map, globalInstance, new SimplePropertyPreFilter("a"));
        TestCase.assertEquals("{\"a\":1001}", new String(bytes));
    }
}
