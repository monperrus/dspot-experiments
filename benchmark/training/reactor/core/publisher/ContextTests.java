/**
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.publisher;


import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;


/**
 *
 *
 * @author Stephane Maldini
 */
public class ContextTests {
    @Test
    public void contextPassing() throws InterruptedException {
        AtomicReference<Context> innerC = new AtomicReference<>();
        // ctx: test=baseSubscriber
        // return: test=baseSubscriber_take
        // ctx: test=baseSubscriber_take
        // return: test=baseSubscriber_take_range
        Flux.range(1, 1000).log().flatMapSequential(( d) -> // ctx: test=baseSubscriber_take_range
        // return: old (discarded since inner)
        Mono.just(d).subscriberContext(( ctx) -> {
            if ((innerC.get()) == null) {
                innerC.set(ctx.put("test", ((ctx.get("test")) + "_innerFlatmap")));
            }
            return ctx;
        }).log()).take(10).subscriberContext(( ctx) -> ctx.put("test", ((ctx.get("test")) + "_range"))).subscriberContext(( ctx) -> ctx.put("test", ((ctx.get("test")) + "_take"))).log().subscribe(new BaseSubscriber<Integer>() {
            @Override
            public Context currentContext() {
                return Context.empty().put("test", "baseSubscriber");
            }
        });
        Assert.assertThat(innerC.get().get("test"), Matchers.is("baseSubscriber_take_range_innerFlatmap"));
    }

    @Test
    public void contextPassing2() throws InterruptedException {
        AtomicReference<String> innerC = new AtomicReference<>();
        Flux.range(1, 1000).log().flatMapSequential(( d) -> Mono.just(d).subscriberContext(( ctx) -> {
            if ((innerC.get()) == null) {
                innerC.set((("" + (ctx.get("test"))) + (ctx.get("test2"))));
            }
            return ctx;
        }).log()).map(( d) -> d).take(10).subscriberContext(( ctx) -> ctx.put("test", "foo")).subscriberContext(( ctx) -> ctx.put("test2", "bar")).log().subscribe();
        Assert.assertThat(innerC.get(), Matchers.is("foobar"));
    }

    @Test
    public void contextGet() throws InterruptedException {
        StepVerifier.create(Flux.range(1, 1000).log().handle(( d, c) -> c.next((((c.currentContext().get("test")) + "") + d))).skip(3).take(3).handle(( d, c) -> c.next((((c.currentContext().get("test2")) + "") + d))).subscriberContext(( ctx) -> ctx.put("test", "foo")).subscriberContext(( ctx) -> ctx.put("test2", "bar")).log()).expectNext("barfoo4").expectNext("barfoo5").expectNext("barfoo6").verifyComplete();
    }

    @Test
    public void currentContext() throws InterruptedException {
        StepVerifier.create(Mono.just("foo").flatMap(( d) -> Mono.subscriberContext().map(( c) -> d + (c.get(.class)))).subscriberContext(( ctx) -> ctx.put(.class, ((ctx.get(.class)) + 1))).flatMapMany(Mono::just).subscriberContext(( ctx) -> ctx.put(.class, 0))).expectNext("foo1").verifyComplete();
    }

    @Test
    public void currentContextWithEmpty() throws InterruptedException {
        StepVerifier.create(Mono.just("foo").flatMap(( d) -> Mono.subscriberContext().map(( c) -> d + (c.get(.class))))).verifyErrorMatches(( t) -> (t instanceof NoSuchElementException) && ("Context is empty".equals(t.getMessage())));
    }

    @Test
    public void contextGetHide() throws InterruptedException {
        StepVerifier.create(Flux.range(1, 1000).hide().log().map(( d) -> d).handle(( d, c) -> c.next((((c.currentContext().get("test")) + "") + d))).skip(3).take(3).handle(( d, c) -> c.next((((c.currentContext().get("test2")) + "") + d))).subscriberContext(( ctx) -> ctx.put("test", "foo")).subscriberContext(( ctx) -> ctx.put("test2", "bar")).log()).expectNext("barfoo4").expectNext("barfoo5").expectNext("barfoo6").verifyComplete();
    }

    @Test
    public void contextGetMono() throws InterruptedException {
        StepVerifier.create(Mono.just(1).log().handle(( d, c) -> c.next((((c.currentContext().get("test")) + "") + d))).handle(( d, c) -> c.next((((c.currentContext().get("test2")) + "") + d))).subscriberContext(( ctx) -> ctx.put("test2", "bar")).subscriberContext(( ctx) -> ctx.put("test", "foo")).log()).expectNext("barfoo1").verifyComplete();
    }

    @Test
    public void contextGetHideMono() throws InterruptedException {
        StepVerifier.create(Mono.just(1).hide().log().handle(( d, c) -> c.next((((c.currentContext().get("test")) + "") + d))).handle(( d, c) -> c.next((((c.currentContext().get("test2")) + "") + d))).subscriberContext(( ctx) -> ctx.put("test", "foo")).subscriberContext(( ctx) -> ctx.put("test2", "bar")).log()).expectNext("barfoo1").verifyComplete();
    }

    @Test
    public void monoSubscriberContextPutsAll() {
        StepVerifier.create(Mono.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.of("foo", "bar", 1, "baz")).subscriberContext(Context.of("initial", "value"))).expectNextMatches(( c) -> ((c.hasKey("foo")) && (c.hasKey(1))) && (c.hasKey("initial"))).verifyComplete();
    }

    @Test
    public void monoSubscriberContextWithMergedEmpty() {
        StepVerifier.create(Mono.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.empty()).subscriberContext(Context.of("initial", "value"))).expectNextMatches(( c) -> c.hasKey("initial")).verifyComplete();
    }

    @Test
    public void monoSubscriberContextWithBothEmpty() {
        StepVerifier.create(Mono.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.empty()).subscriberContext(Context.empty())).expectNextMatches(Context::isEmpty).verifyComplete();
    }

    @Test
    public void fluxSubscriberContextPutsAll() {
        StepVerifier.create(Flux.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.of("foo", "bar", 1, "baz")).subscriberContext(Context.of("initial", "value"))).expectNextMatches(( c) -> ((c.hasKey("foo")) && (c.hasKey(1))) && (c.hasKey("initial"))).verifyComplete();
    }

    @Test
    public void fluxSubscriberContextWithMergedEmpty() {
        StepVerifier.create(Flux.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.empty()).subscriberContext(Context.of("initial", "value"))).expectNextMatches(( c) -> c.hasKey("initial")).verifyComplete();
    }

    @Test
    public void fluxSubscriberContextWithBothEmpty() {
        StepVerifier.create(Flux.just("foo").flatMap(( v) -> Mono.subscriberContext()).subscriberContext(Context.empty()).subscriberContext(Context.empty())).expectNextMatches(Context::isEmpty).verifyComplete();
    }

    @Test
    public void expectAccessibleContextWithInitialContext() {
        StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create().withInitialContext(Context.of("foo", "bar"));
        StepVerifier.create(Mono.just(1), stepVerifierOptions).expectAccessibleContext().contains("foo", "bar").then().expectNext(1).verifyComplete();
    }

    @Test
    public void contextAccessibleWithEmptySubscriptionAndOperator1() {
        StepVerifier.create(Flux.empty().subscriberContext(Context.of("a", "b"))).expectAccessibleContext().contains("a", "b").then().verifyComplete();
    }

    @Test
    public void contextAccessibleWithEmptySubscriptionAndOperator2() {
        StepVerifier.create(Flux.empty().map(( i) -> i), StepVerifierOptions.create().withInitialContext(Context.of("a", "b"))).expectAccessibleContext().contains("a", "b").then().verifyComplete();
    }

    @Test
    public void contextNotAccessibleWithEmptySubscriptionOnly() {
        StepVerifier.create(Flux.empty(), StepVerifierOptions.create().withInitialContext(Context.of("a", "b"))).expectNoAccessibleContext().verifyComplete();
    }
}
