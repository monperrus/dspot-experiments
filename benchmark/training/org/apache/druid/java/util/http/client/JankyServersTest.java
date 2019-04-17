/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.java.util.http.client;


import com.google.common.util.concurrent.ListenableFuture;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLContext;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.java.util.common.lifecycle.Lifecycle;
import org.apache.druid.java.util.http.client.response.StatusResponseHandler;
import org.apache.druid.java.util.http.client.response.StatusResponseHolder;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests with a bunch of goofy not-actually-http servers.
 */
public class JankyServersTest {
    static ExecutorService exec;

    static ServerSocket silentServerSocket;

    static ServerSocket echoServerSocket;

    static ServerSocket closingServerSocket;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testHttpSilentServerWithGlobalTimeout() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().withReadTimeout(new Duration(100)).build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> future = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("http://localhost:%d/", JankyServersTest.silentServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            Throwable e = null;
            try {
                future.get();
            } catch (ExecutionException e1) {
                e = e1.getCause();
            }
            Assert.assertTrue("ReadTimeoutException thrown by 'get'", (e instanceof ReadTimeoutException));
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpSilentServerWithRequestTimeout() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().withReadTimeout(new Duration((86400L * 365))).build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> future = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("http://localhost:%d/", JankyServersTest.silentServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8), new Duration(100L));
            Throwable e = null;
            try {
                future.get();
            } catch (ExecutionException e1) {
                e = e1.getCause();
            }
            Assert.assertTrue("ReadTimeoutException thrown by 'get'", (e instanceof ReadTimeoutException));
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpsSilentServer() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().withSslContext(SSLContext.getDefault()).withSslHandshakeTimeout(new Duration(100)).build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> response = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("https://localhost:%d/", JankyServersTest.silentServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            Throwable e = null;
            try {
                response.get();
            } catch (ExecutionException e1) {
                e = e1.getCause();
            }
            Assert.assertTrue("ChannelException thrown by 'get'", (e instanceof ChannelException));
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpConnectionClosingServer() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> response = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("http://localhost:%d/", JankyServersTest.closingServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            Throwable e = null;
            try {
                response.get();
            } catch (ExecutionException e1) {
                e = e1.getCause();
                e1.printStackTrace();
            }
            Assert.assertTrue("ChannelException thrown by 'get'", isChannelClosedException(e));
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpsConnectionClosingServer() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().withSslContext(SSLContext.getDefault()).build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> response = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("https://localhost:%d/", JankyServersTest.closingServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            Throwable e = null;
            try {
                response.get();
            } catch (ExecutionException e1) {
                e = e1.getCause();
                e1.printStackTrace();
            }
            Assert.assertTrue("ChannelException thrown by 'get'", isChannelClosedException(e));
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpEchoServer() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> response = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("http://localhost:%d/", JankyServersTest.echoServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            expectedException.expect(ExecutionException.class);
            expectedException.expectMessage("java.lang.IllegalArgumentException: invalid version format: GET");
            response.get();
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    public void testHttpsEchoServer() throws Throwable {
        final Lifecycle lifecycle = new Lifecycle();
        try {
            final HttpClientConfig config = HttpClientConfig.builder().withSslContext(SSLContext.getDefault()).build();
            final HttpClient client = HttpClientInit.createClient(config, lifecycle);
            final ListenableFuture<StatusResponseHolder> response = client.go(new Request(HttpMethod.GET, new URL(StringUtils.format("https://localhost:%d/", JankyServersTest.echoServerSocket.getLocalPort()))), new StatusResponseHandler(StandardCharsets.UTF_8));
            expectedException.expect(ExecutionException.class);
            expectedException.expectMessage("org.jboss.netty.channel.ChannelException: Faulty channel in resource pool");
            response.get();
        } finally {
            lifecycle.stop();
        }
    }
}
