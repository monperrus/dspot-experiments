package us.codecraft.webmagic.utils;


import java.util.List;
import org.junit.Test;
import us.codecraft.webmagic.Request;


/**
 *
 *
 * @author code4crafter@gmail.com
Date: 2017/6/5
Time: ??5:08
 */
public class RequestUtilsTest {
    @Test
    public void test_generate_range() throws Exception {
        List<Request> requests = RequestUtils.from("http://angularjs.cn/api/article/latest?p=[1-3]&s=20");
        assertThat(requests).containsExactly(new Request("http://angularjs.cn/api/article/latest?p=1&s=20"), new Request("http://angularjs.cn/api/article/latest?p=2&s=20"), new Request("http://angularjs.cn/api/article/latest?p=3&s=20"));
    }

    @Test
    public void test_generate_range_when_invalid_number() throws Exception {
        List<Request> requests = RequestUtils.from("http://angularjs.cn/api/article/latest?p=[10-3]&s=20");
        assertThat(requests).isEmpty();
    }
}
