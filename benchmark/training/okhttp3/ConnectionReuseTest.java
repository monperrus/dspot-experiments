/**
 * Copyright (C) 2015 Square, Inc.
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
package okhttp3;


import SocketPolicy.DISCONNECT_AFTER_REQUEST;
import SocketPolicy.DISCONNECT_AT_END;
import SocketPolicy.SHUTDOWN_OUTPUT_AT_END;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import okhttp3.internal.Util;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;


public final class ConnectionReuseTest {
    @Rule
    public final TestRule timeout = new Timeout(30000);

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Rule
    public final OkHttpClientTestRule clientTestRule = new OkHttpClientTestRule();

    private HandshakeCertificates handshakeCertificates = localhost();

    private OkHttpClient client = clientTestRule.client;

    @Test
    public void connectionsAreReused() throws Exception {
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        assertConnectionReused(request, request);
    }

    @Test
    public void connectionsAreReusedWithHttp2() throws Exception {
        enableHttp2();
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        assertConnectionReused(request, request);
    }

    @Test
    public void connectionsAreNotReusedWithRequestConnectionClose() throws Exception {
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request requestA = new Request.Builder().url(server.url("/")).header("Connection", "close").build();
        Request requestB = new Request.Builder().url(server.url("/")).build();
        assertConnectionNotReused(requestA, requestB);
    }

    @Test
    public void connectionsAreNotReusedWithResponseConnectionClose() throws Exception {
        server.enqueue(new MockResponse().addHeader("Connection", "close").setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request requestA = new Request.Builder().url(server.url("/")).build();
        Request requestB = new Request.Builder().url(server.url("/")).build();
        assertConnectionNotReused(requestA, requestB);
    }

    @Test
    public void connectionsAreNotReusedWithUnknownLengthResponseBody() throws Exception {
        server.enqueue(new MockResponse().setBody("a").setSocketPolicy(DISCONNECT_AT_END).clearHeaders());
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        assertConnectionNotReused(request, request);
    }

    @Test
    public void connectionsAreNotReusedIfPoolIsSizeZero() throws Exception {
        client = client.newBuilder().connectionPool(new ConnectionPool(0, 5, TimeUnit.SECONDS)).build();
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        assertConnectionNotReused(request, request);
    }

    @Test
    public void connectionsReusedWithRedirectEvenIfPoolIsSizeZero() throws Exception {
        client = client.newBuilder().connectionPool(new ConnectionPool(0, 5, TimeUnit.SECONDS)).build();
        server.enqueue(new MockResponse().setResponseCode(301).addHeader("Location: /b").setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals("b", response.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(1, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void connectionsNotReusedWithRedirectIfDiscardingResponseIsSlow() throws Exception {
        client = client.newBuilder().connectionPool(new ConnectionPool(0, 5, TimeUnit.SECONDS)).build();
        server.enqueue(new MockResponse().setResponseCode(301).addHeader("Location: /b").setBodyDelay(1, TimeUnit.SECONDS).setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals("b", response.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void silentRetryWhenIdempotentRequestFailsOnReusedConnection() throws Exception {
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        Response responseA = client.newCall(request).execute();
        Assert.assertEquals("a", responseA.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Response responseB = client.newCall(request).execute();
        Assert.assertEquals("b", responseB.body().string());
        Assert.assertEquals(1, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void staleConnectionNotReusedForNonIdempotentRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("a").setSocketPolicy(SHUTDOWN_OUTPUT_AT_END));
        server.enqueue(new MockResponse().setBody("b"));
        Request requestA = new Request.Builder().url(server.url("/")).build();
        Response responseA = client.newCall(requestA).execute();
        Assert.assertEquals("a", responseA.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        // Give the socket a chance to become stale.
        Thread.sleep(250);
        Request requestB = new Request.Builder().url(server.url("/")).post(RequestBody.create(MediaType.get("text/plain"), "b")).build();
        Response responseB = client.newCall(requestB).execute();
        Assert.assertEquals("b", responseB.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void http2ConnectionsAreSharedBeforeResponseIsConsumed() throws Exception {
        enableHttp2();
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response1 = client.newCall(request).execute();
        Response response2 = client.newCall(request).execute();
        response1.body().string();// Discard the response body.

        response2.body().string();// Discard the response body.

        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(1, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void connectionsAreEvicted() throws Exception {
        server.enqueue(new MockResponse().setBody("a"));
        server.enqueue(new MockResponse().setBody("b"));
        client = client.newBuilder().connectionPool(new ConnectionPool(5, 250, TimeUnit.MILLISECONDS)).build();
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response1 = client.newCall(request).execute();
        Assert.assertEquals("a", response1.body().string());
        // Give the thread pool a chance to evict.
        Thread.sleep(500);
        Response response2 = client.newCall(request).execute();
        Assert.assertEquals("b", response2.body().string());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
    }

    @Test
    public void connectionsAreNotReusedIfSslSocketFactoryChanges() throws Exception {
        enableHttps();
        server.enqueue(new MockResponse());
        server.enqueue(new MockResponse());
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response = client.newCall(request).execute();
        response.body().close();
        // This client shares a connection pool but has a different SSL socket factory.
        HandshakeCertificates handshakeCertificates2 = new HandshakeCertificates.Builder().build();
        OkHttpClient anotherClient = client.newBuilder().sslSocketFactory(handshakeCertificates2.sslSocketFactory(), handshakeCertificates2.trustManager()).build();
        // This client fails to connect because the new SSL socket factory refuses.
        try {
            anotherClient.newCall(request).execute();
            Assert.fail();
        } catch (SSLException expected) {
        }
    }

    @Test
    public void connectionsAreNotReusedIfHostnameVerifierChanges() throws Exception {
        enableHttps();
        server.enqueue(new MockResponse());
        server.enqueue(new MockResponse());
        Request request = new Request.Builder().url(server.url("/")).build();
        Response response1 = client.newCall(request).execute();
        response1.body().close();
        // This client shares a connection pool but has a different SSL socket factory.
        OkHttpClient anotherClient = client.newBuilder().hostnameVerifier(new RecordingHostnameVerifier()).build();
        Response response2 = anotherClient.newCall(request).execute();
        response2.body().close();
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
    }

    /**
     * Regression test for an edge case where closing response body in the HTTP engine doesn't release
     * the corresponding stream allocation. This test keeps those response bodies alive and reads
     * them after the redirect has completed. This forces a connection to not be reused where it would
     * be otherwise.
     *
     * <p>This test leaks a response body by not closing it.
     *
     * https://github.com/square/okhttp/issues/2409
     */
    @Test
    public void connectionsAreNotReusedIfNetworkInterceptorInterferes() throws Exception {
        List<Response> responsesNotClosed = new ArrayList<>();
        client = // Since this test knowingly leaks a connection, avoid using the default shared connection
        // pool, which should remain clean for subsequent tests.
        client.newBuilder().connectionPool(new ConnectionPool()).addNetworkInterceptor(( chain) -> {
            Response response = chain.proceed(chain.request());
            responsesNotClosed.add(response);
            return response.newBuilder().body(ResponseBody.create(null, "unrelated response body!")).build();
        }).build();
        server.enqueue(new MockResponse().setResponseCode(301).addHeader("Location: /b").setBody("/a has moved!"));
        server.enqueue(new MockResponse().setBody("/b is here"));
        Request request = new Request.Builder().url(server.url("/")).build();
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            Assert.assertEquals("unrelated response body!", response.body().string());
        }
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());
        Assert.assertEquals(0, server.takeRequest().getSequenceNumber());// No connection reuse.

        for (Response response : responsesNotClosed) {
            Util.closeQuietly(response);
        }
    }
}
