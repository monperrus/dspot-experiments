package com.alibaba.json.bvt.issue_1200;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import junit.framework.TestCase;


/**
 * Created by kimmking on 09/06/2017.
 */
public class Issue1254 extends TestCase {
    public void test_for_issue() throws Exception {
        Issue1254.A a = new Issue1254.A();
        a._parentId = "001";
        String test = JSON.toJSONString(a);
        System.out.println(test);
        TestCase.assertEquals("{\"_parentId\":\"001\"}", test);
        Issue1254.B b = new Issue1254.B();
        b.set_parentId("001");
        String testB = JSON.toJSONString(b);
        System.out.println(testB);
        TestCase.assertEquals("{\"_parentId\":\"001\"}", testB);
    }

    public static class A {
        public String _parentId;
    }

    public static class B {
        @JSONField(name = "_parentId")
        private String _parentId;

        public String get_parentId() {
            return _parentId;
        }

        public void set_parentId(String _parentId) {
            this._parentId = _parentId;
        }
    }
}
