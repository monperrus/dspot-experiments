/**
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.method.annotation;


import java.util.Collections;
import java.util.Map;
import javax.servlet.http.Part;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.test.MockHttpServletRequest;
import org.springframework.mock.web.test.MockHttpServletResponse;
import org.springframework.mock.web.test.MockMultipartFile;
import org.springframework.mock.web.test.MockMultipartHttpServletRequest;
import org.springframework.mock.web.test.MockPart;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.MvcAnnotationPredicates;
import org.springframework.web.method.ResolvableMethod;
import org.springframework.web.multipart.MultipartFile;


/**
 * Test fixture with {@link RequestParamMapMethodArgumentResolver}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 */
public class RequestParamMapMethodArgumentResolverTests {
    private RequestParamMapMethodArgumentResolver resolver = new RequestParamMapMethodArgumentResolver();

    private MockHttpServletRequest request = new MockHttpServletRequest();

    private NativeWebRequest webRequest = new ServletWebRequest(request, new MockHttpServletResponse());

    private ResolvableMethod testMethod = ResolvableMethod.on(getClass()).named("handle").build();

    @Test
    public void supportsParameter() {
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(Map.class, String.class, String.class);
        Assert.assertTrue(resolver.supportsParameter(param));
        param = this.testMethod.annotPresent(RequestParam.class).arg(MultiValueMap.class, String.class, String.class);
        Assert.assertTrue(resolver.supportsParameter(param));
        param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().name("name")).arg(Map.class, String.class, String.class);
        Assert.assertFalse(resolver.supportsParameter(param));
        param = this.testMethod.annotNotPresent(RequestParam.class).arg(Map.class, String.class, String.class);
        Assert.assertFalse(resolver.supportsParameter(param));
    }

    @Test
    public void resolveMapOfString() throws Exception {
        String name = "foo";
        String value = "bar";
        request.addParameter(name, value);
        Map<String, String> expected = Collections.singletonMap(name, value);
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(Map.class, String.class, String.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof Map));
        Assert.assertEquals("Invalid result", expected, result);
    }

    @Test
    public void resolveMultiValueMapOfString() throws Exception {
        String name = "foo";
        String value1 = "bar";
        String value2 = "baz";
        request.addParameter(name, value1, value2);
        MultiValueMap<String, String> expected = new org.springframework.util.LinkedMultiValueMap(1);
        expected.add(name, value1);
        expected.add(name, value2);
        MethodParameter param = this.testMethod.annotPresent(RequestParam.class).arg(MultiValueMap.class, String.class, String.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof MultiValueMap));
        Assert.assertEquals("Invalid result", expected, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resolveMapOfMultipartFile() throws Exception {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        MultipartFile expected1 = new MockMultipartFile("mfile", "Hello World".getBytes());
        MultipartFile expected2 = new MockMultipartFile("other", "Hello World 3".getBytes());
        request.addFile(expected1);
        request.addFile(expected2);
        webRequest = new ServletWebRequest(request);
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(Map.class, String.class, MultipartFile.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof Map));
        Map<String, MultipartFile> resultMap = ((Map<String, MultipartFile>) (result));
        Assert.assertEquals(2, resultMap.size());
        Assert.assertEquals(expected1, resultMap.get("mfile"));
        Assert.assertEquals(expected2, resultMap.get("other"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resolveMultiValueMapOfMultipartFile() throws Exception {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        MultipartFile expected1 = new MockMultipartFile("mfilelist", "Hello World 1".getBytes());
        MultipartFile expected2 = new MockMultipartFile("mfilelist", "Hello World 2".getBytes());
        MultipartFile expected3 = new MockMultipartFile("other", "Hello World 3".getBytes());
        request.addFile(expected1);
        request.addFile(expected2);
        request.addFile(expected3);
        webRequest = new ServletWebRequest(request);
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(MultiValueMap.class, String.class, MultipartFile.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof MultiValueMap));
        MultiValueMap<String, MultipartFile> resultMap = ((MultiValueMap<String, MultipartFile>) (result));
        Assert.assertEquals(2, resultMap.size());
        Assert.assertEquals(2, resultMap.get("mfilelist").size());
        Assert.assertEquals(expected1, resultMap.get("mfilelist").get(0));
        Assert.assertEquals(expected2, resultMap.get("mfilelist").get(1));
        Assert.assertEquals(1, resultMap.get("other").size());
        Assert.assertEquals(expected3, resultMap.get("other").get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resolveMapOfPart() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data");
        Part expected1 = new MockPart("mfile", "Hello World".getBytes());
        Part expected2 = new MockPart("other", "Hello World 3".getBytes());
        request.addPart(expected1);
        request.addPart(expected2);
        webRequest = new ServletWebRequest(request);
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(Map.class, String.class, Part.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof Map));
        Map<String, Part> resultMap = ((Map<String, Part>) (result));
        Assert.assertEquals(2, resultMap.size());
        Assert.assertEquals(expected1, resultMap.get("mfile"));
        Assert.assertEquals(expected2, resultMap.get("other"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void resolveMultiValueMapOfPart() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("multipart/form-data");
        Part expected1 = new MockPart("mfilelist", "Hello World 1".getBytes());
        Part expected2 = new MockPart("mfilelist", "Hello World 2".getBytes());
        Part expected3 = new MockPart("other", "Hello World 3".getBytes());
        request.addPart(expected1);
        request.addPart(expected2);
        request.addPart(expected3);
        webRequest = new ServletWebRequest(request);
        MethodParameter param = this.testMethod.annot(MvcAnnotationPredicates.requestParam().noName()).arg(MultiValueMap.class, String.class, Part.class);
        Object result = resolver.resolveArgument(param, null, webRequest, null);
        Assert.assertTrue((result instanceof MultiValueMap));
        MultiValueMap<String, Part> resultMap = ((MultiValueMap<String, Part>) (result));
        Assert.assertEquals(2, resultMap.size());
        Assert.assertEquals(2, resultMap.get("mfilelist").size());
        Assert.assertEquals(expected1, resultMap.get("mfilelist").get(0));
        Assert.assertEquals(expected2, resultMap.get("mfilelist").get(1));
        Assert.assertEquals(1, resultMap.get("other").size());
        Assert.assertEquals(expected3, resultMap.get("other").get(0));
    }
}
