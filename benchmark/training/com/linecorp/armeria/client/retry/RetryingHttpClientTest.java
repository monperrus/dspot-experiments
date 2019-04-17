/**
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.client.retry;


import HttpHeaderNames.RETRY_AFTER;
import HttpMethod.GET;
import HttpStatus.INTERNAL_SERVER_ERROR;
import HttpStatus.OK;
import HttpStatus.SERVICE_UNAVAILABLE;
import MediaType.PLAIN_TEXT_UTF_8;
import com.google.common.base.Stopwatch;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.ClientRequestContext;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.ResponseTimeoutException;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpObject;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpRequestWriter;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.util.EventLoopGroups;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.testing.internal.AnticipatedException;
import com.linecorp.armeria.testing.server.ServerRule;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.reactivestreams.Subscription;


public class RetryingHttpClientTest {
    // use different eventLoop from server's so that clients don't hang when the eventLoop in server hangs
    private static final ClientFactory clientFactory = new ClientFactoryBuilder().workerGroup(EventLoopGroups.newEventLoopGroup(2), true).build();

    private static final RetryStrategy retryAlways = ( ctx, cause) -> CompletableFuture.completedFuture(Backoff.fixed(500));

    private final AtomicInteger responseAbortServiceCallCounter = new AtomicInteger();

    private final AtomicInteger requestAbortServiceCallCounter = new AtomicInteger();

    private final AtomicInteger subscriberCancelServiceCallCounter = new AtomicInteger();

    @Rule
    public TestRule globalTimeout = new DisableOnDebug(new Timeout(10, TimeUnit.SECONDS));

    @Rule
    public final ServerRule server = new ServerRule() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.service("/retry-content", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    final int retryCount = reqCount.getAndIncrement();
                    if (retryCount != 0) {
                        assertThat(retryCount).isEqualTo(req.headers().getInt(RetryingClient.ARMERIA_RETRY_COUNT));
                    }
                    if (retryCount < 2) {
                        return HttpResponse.of("Need to retry");
                    } else {
                        return HttpResponse.of("Succeeded after retry");
                    }
                }
            });
            sb.service("/500-then-success", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((reqCount.getAndIncrement()) < 1) {
                        return HttpResponse.of(INTERNAL_SERVER_ERROR);
                    } else {
                        return HttpResponse.of("Succeeded after retry");
                    }
                }
            });
            sb.service("/503-then-success", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((reqCount.getAndIncrement()) < 1) {
                        return HttpResponse.of(SERVICE_UNAVAILABLE);
                    } else {
                        return HttpResponse.of("Succeeded after retry");
                    }
                }
            });
            sb.service("/retry-after-1-second", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((reqCount.getAndIncrement()) < 1) {
                        return HttpResponse.of(/* second */
                        HttpHeaders.of(SERVICE_UNAVAILABLE).setInt(RETRY_AFTER, 1));
                    } else {
                        return HttpResponse.of("Succeeded after retry");
                    }
                }
            });
            sb.service("/retry-after-with-http-date", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((reqCount.getAndIncrement()) < 1) {
                        return HttpResponse.of(HttpHeaders.of(SERVICE_UNAVAILABLE).setTimeMillis(RETRY_AFTER, ((Duration.ofSeconds(3).toMillis()) + (System.currentTimeMillis()))));
                    } else {
                        return HttpResponse.of(OK, PLAIN_TEXT_UTF_8, "Succeeded after retry");
                    }
                }
            });
            sb.service("/retry-after-one-year", new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    return HttpResponse.of(HttpHeaders.of(SERVICE_UNAVAILABLE).setTimeMillis(RETRY_AFTER, ((Duration.ofDays(365).toMillis()) + (System.currentTimeMillis()))));
                }
            });
            sb.service("/service-unavailable", new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    return HttpResponse.of(SERVICE_UNAVAILABLE);
                }
            });
            sb.service("/1sleep-then-success", new AbstractHttpService() {
                final AtomicInteger reqCount = new AtomicInteger();

                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((reqCount.getAndIncrement()) < 1) {
                        TimeUnit.MILLISECONDS.sleep(1000);
                        return HttpResponse.of(SERVICE_UNAVAILABLE);
                    } else {
                        return HttpResponse.of(OK, PLAIN_TEXT_UTF_8, "Succeeded after retry");
                    }
                }
            });
            sb.service("/post-ping-pong", new AbstractHttpService() {
                final AtomicInteger reqPostCount = new AtomicInteger();

                @Override
                protected HttpResponse doPost(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    return HttpResponse.from(req.aggregate().handle(( message, thrown) -> {
                        if ((reqPostCount.getAndIncrement()) < 1) {
                            return HttpResponse.of(HttpStatus.SERVICE_UNAVAILABLE);
                        } else {
                            return HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, message.contentUtf8());
                        }
                    }));
                }
            });
            sb.service("/response-abort", new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    responseAbortServiceCallCounter.incrementAndGet();
                    return HttpResponse.of(SERVICE_UNAVAILABLE);
                }
            });
            sb.service("/request-abort", new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    requestAbortServiceCallCounter.incrementAndGet();
                    return HttpResponse.of(SERVICE_UNAVAILABLE);
                }
            });
            sb.service("/subscriber-cancel", new AbstractHttpService() {
                @Override
                protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                    if ((subscriberCancelServiceCallCounter.getAndIncrement()) < 2) {
                        return HttpResponse.of(SERVICE_UNAVAILABLE);
                    } else {
                        return HttpResponse.of("Succeeded after retry");
                    }
                }
            });
        }
    };

    @Test
    public void retryWhenContentMatched() {
        final HttpClient client = new com.linecorp.armeria.client.HttpClientBuilder(server.uri("/")).factory(RetryingHttpClientTest.clientFactory).decorator(newDecorator()).build();
        final AggregatedHttpMessage res = client.get("/retry-content").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
    }

    @Test
    public void retryWhenStatusMatched() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus());
        final AggregatedHttpMessage res = client.get("/503-then-success").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
    }

    @Test
    public void disableResponseTimeout() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus(), 0, 0, 100);
        final AggregatedHttpMessage res = client.get("/503-then-success").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
        // response timeout did not happen.
    }

    @Test
    public void respectRetryAfter() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus());
        final Stopwatch sw = Stopwatch.createStarted();
        final AggregatedHttpMessage res = client.get("/retry-after-1-second").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(((long) ((TimeUnit.SECONDS.toMillis(1)) * 0.9)));
    }

    @Test
    public void respectRetryAfterWithHttpDate() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus());
        final Stopwatch sw = Stopwatch.createStarted();
        final AggregatedHttpMessage res = client.get("/retry-after-with-http-date").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
        // Since ZonedDateTime doesn't express exact time,
        // just check out whether it is retried after delayed some time.
        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(1000);
    }

    @Test
    public void propagateLastResponseWhenNextRetryIsAfterTimeout() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus(Backoff.fixed(10000000)));
        final AggregatedHttpMessage res = client.get("/service-unavailable").aggregate().join();
        assertThat(res.status()).isSameAs(SERVICE_UNAVAILABLE);
    }

    @Test
    public void propagateLastResponseWhenExceedMaxAttempts() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus(Backoff.fixed(1)), 0, 0, 3);
        final AggregatedHttpMessage res = client.get("/service-unavailable").aggregate().join();
        assertThat(res.status()).isSameAs(SERVICE_UNAVAILABLE);
    }

    @Test
    public void retryAfterOneYear() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus());
        // The response will be the last response whose headers contains HttpHeaderNames.RETRY_AFTER
        // because next retry is after timeout
        final HttpHeaders headers = client.get("retry-after-one-year").aggregate().join().headers();
        assertThat(headers.status()).isSameAs(SERVICE_UNAVAILABLE);
        assertThat(headers.get(RETRY_AFTER)).isNotNull();
    }

    @Test
    public void retryOnResponseTimeout() {
        final Backoff backoff = Backoff.fixed(100);
        final RetryStrategy strategy = ( ctx, cause) -> {
            if (cause instanceof ResponseTimeoutException) {
                return CompletableFuture.completedFuture(backoff);
            }
            return CompletableFuture.completedFuture(null);
        };
        final HttpClient client = client(strategy, 0, 500, 100);
        final AggregatedHttpMessage res = client.get("/1sleep-then-success").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
    }

    @Test
    public void differentBackoffBasedOnStatus() {
        final HttpClient client = client(RetryStrategy.onStatus(RetryingHttpClientTest.statusBasedBackoff()));
        final Stopwatch sw = Stopwatch.createStarted();
        AggregatedHttpMessage res = client.get("/503-then-success").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isBetween(((long) (10 * 0.9)), ((long) (1000 * 1.1)));
        sw.reset().start();
        res = client.get("/500-then-success").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("Succeeded after retry");
        assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(((long) (1000 * 0.9)));
    }

    @Test
    public void retryWithRequestBody() {
        final HttpClient client = client(RetryStrategy.onServerErrorStatus(Backoff.fixed(10)));
        final AggregatedHttpMessage res = client.post("/post-ping-pong", "bar").aggregate().join();
        assertThat(res.contentUtf8()).isEqualTo("bar");
    }

    @Test
    public void shouldGetExceptionWhenFactoryIsClosed() {
        final ClientFactory factory = new ClientFactoryBuilder().workerGroup(EventLoopGroups.newEventLoopGroup(2), true).build();
        final HttpClient client = new com.linecorp.armeria.client.HttpClientBuilder(server.uri("/")).factory(factory).defaultResponseTimeoutMillis(10000).decorator(newDecorator()).build();
        // There's no way to notice that the RetryingClient has scheduled the next retry.
        // The next retry will be after 8 seconds so closing the factory after 3 seconds should work.
        Executors.newSingleThreadScheduledExecutor().schedule(factory::close, 3, TimeUnit.SECONDS);
        assertThatThrownBy(() -> client.get("/service-unavailable").aggregate().join()).hasCauseInstanceOf(IllegalStateException.class).satisfies(( cause) -> assertThat(cause.getCause().getMessage()).matches("(?i).*(factory has been closed|not accepting a task).*"));
    }

    @Test
    public void doNotRetryWhenResponseIsAborted() throws Exception {
        final HttpClient client = client(RetryingHttpClientTest.retryAlways);
        final HttpResponse httpResponse = client.get("/response-abort");
        httpResponse.abort();
        await().untilAsserted(() -> assertThat(responseAbortServiceCallCounter.get()).isOne());
        // Sleep 3 seconds more to check if there was another retry.
        TimeUnit.SECONDS.sleep(3);
        assertThat(responseAbortServiceCallCounter.get()).isOne();
    }

    @Test
    public void retryDoNotStopUntilGetResponseWhenSubscriberCancel() {
        final HttpClient client = client(RetryingHttpClientTest.retryAlways);
        client.get("/subscriber-cancel").subscribe(new org.reactivestreams.Subscriber<HttpObject>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.cancel();// Cancel as soon as getting the subscription.

            }

            @Override
            public void onNext(HttpObject httpObject) {
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });
        await().untilAsserted(() -> assertThat(subscriberCancelServiceCallCounter.get()).isEqualTo(3));
    }

    @Test
    public void doNotRetryWhenRequestIsAborted() throws Exception {
        final HttpClient client = client(RetryingHttpClientTest.retryAlways);
        final HttpRequestWriter req = HttpRequest.streaming(GET, "/request-abort");
        req.write(HttpData.ofUtf8("I'm going to abort this request"));
        req.abort();
        client.execute(req);
        TimeUnit.SECONDS.sleep(1);
        // No request is made.
        assertThat(responseAbortServiceCallCounter.get()).isZero();
    }

    @Test
    public void exceptionInDecorator() {
        final AtomicInteger retryCounter = new AtomicInteger();
        final RetryStrategy strategy = ( ctx, cause) -> {
            retryCounter.incrementAndGet();
            return CompletableFuture.completedFuture(Backoff.withoutDelay());
        };
        final HttpClient client = decorator(( delegate, ctx, req) -> {
            throw new AnticipatedException();
        }).decorator(RetryingHttpClient.newDecorator(strategy, 5)).build();
        assertThatThrownBy(() -> client.get("/").aggregate().join()).hasCauseExactlyInstanceOf(AnticipatedException.class);
        assertThat(retryCounter.get()).isEqualTo(5);
    }

    private static class RetryIfContentMatch implements RetryStrategyWithContent<HttpResponse> {
        private final String retryContent;

        private final Backoff backoffOnContent = Backoff.fixed(100);

        RetryIfContentMatch(String retryContent) {
            this.retryContent = retryContent;
        }

        @Override
        public CompletionStage<Backoff> shouldRetry(ClientRequestContext ctx, HttpResponse response) {
            final CompletableFuture<AggregatedHttpMessage> future = response.aggregate();
            return future.handle(( message, unused) -> {
                if ((message != null) && (message.contentUtf8().equalsIgnoreCase(retryContent))) {
                    return backoffOnContent;
                }
                return null;
            });
        }
    }
}
