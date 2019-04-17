package com.alibaba.json.bvt.kotlin;


import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;


/**
 * Created by wenshao on 06/08/2017.
 */
public class ClassWithTripleTest extends TestCase {
    public void test_user() throws Exception {
        ClassWithTripleTest.ExtClassLoader classLoader = new ClassWithTripleTest.ExtClassLoader();
        Class clazz = classLoader.loadClass("ClassWithTriple");
        String json = "{\"name\":{\"first\":\"wen\",\"second\":\"shaojin\",\"third\":99}}";
        Object obj = JSON.parseObject(json, clazz);
        TestCase.assertEquals("{\"age\":0,\"name\":{\"first\":\"wen\",\"second\":\"shaojin\",\"third\":\"99\"}}", JSON.toJSONString(obj));
    }

    public static class ExtClassLoader extends ClassLoader {
        public ExtClassLoader() throws IOException {
            super(Thread.currentThread().getContextClassLoader());
            {
                byte[] bytes;
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("kotlin/ClassWithTriple.clazz");
                bytes = IOUtils.toByteArray(is);
                is.close();
                super.defineClass("ClassWithTriple", bytes, 0, bytes.length);
            }
        }
    }
}
