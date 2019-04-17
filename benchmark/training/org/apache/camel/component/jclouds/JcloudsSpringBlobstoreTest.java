/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jclouds;


import JcloudsConstants.BLOB_NAME_LIST;
import JcloudsConstants.CONTAINER_NAME;
import JcloudsConstants.OPERATION;
import JcloudsConstants.REMOVE_BLOBS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;


public class JcloudsSpringBlobstoreTest extends CamelSpringTestSupport {
    @EndpointInject(uri = "mock:result-foo")
    protected MockEndpoint resultFoo;

    @EndpointInject(uri = "mock:result-bar")
    protected MockEndpoint resultBar;

    @Test
    public void testBlobStorePut() throws InterruptedException {
        resultFoo.expectedMessageCount(1);
        template.sendBody("direct:start", "Some message");
        resultFoo.assertIsSatisfied();
    }

    @Test
    public void testBlobStoreGet() throws InterruptedException {
        resultFoo.expectedMessageCount(1);
        template.sendBody("direct:start", "Some message");
        resultFoo.assertIsSatisfied();
    }

    @Test
    public void testProduceWithUrlParametes() throws InterruptedException {
        resultBar.expectedMessageCount(1);
        template.sendBody("direct:start-with-url-parameters", "Some message");
        resultBar.assertIsSatisfied();
    }

    @Test
    public void testBlobStoreCount() throws InterruptedException {
        Long count = template.requestBody("direct:count", "Some message", Long.class);
        assertEquals(new Long(1), count);
    }

    @Test
    public void testBlobStoreRemove() throws InterruptedException {
        Long count = template.requestBody("direct:remove", "Some message", Long.class);
        assertEquals(new Long(0), count);
    }

    @Test
    public void testBlobStoreClear() throws InterruptedException {
        Long count = template.requestBody("direct:clear", "Some message", Long.class);
        assertEquals(new Long(0), count);
    }

    @Test
    public void testBlobStoreDelete() throws InterruptedException {
        Boolean result = template.requestBody("direct:delete", "Some message", Boolean.class);
        assertEquals(false, result);
    }

    @Test
    public void testBlobStoreContainerExists() throws InterruptedException {
        Boolean result = template.requestBody("direct:exists", "Some message", Boolean.class);
        assertEquals(true, result);
    }

    @Test
    public void testBlobStoreRemoveBlobs() throws InterruptedException {
        Boolean result = template.requestBody("direct:exists", "Some message", Boolean.class);
        assertEquals(true, result);
        List blobsToRemove = new ArrayList<>();
        blobsToRemove.add("testName");
        Map<String, Object> headers = new HashMap<>();
        headers.put(OPERATION, REMOVE_BLOBS);
        headers.put(CONTAINER_NAME, "foo");
        headers.put(BLOB_NAME_LIST, blobsToRemove);
        template.sendBodyAndHeaders("direct:remove-blobs", null, headers);
        Long count = template.requestBody("direct:count-after-remove-blobs", null, Long.class);
        assertEquals(new Long(0), count);
    }
}
