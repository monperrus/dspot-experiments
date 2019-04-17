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


import FluxDelaySubscription.DelaySubscriptionOtherSubscriber;
import Scannable.Attr.ACTUAL;
import Scannable.Attr.CANCELLED;
import Scannable.Attr.PARENT;
import Scannable.Attr.REQUESTED_FROM_DOWNSTREAM;
import Scannable.Attr.TERMINATED;
import java.time.Duration;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.test.StepVerifier;
import reactor.test.subscriber.AssertSubscriber;


public class FluxDelaySubscriptionTest {
    @Test(expected = NullPointerException.class)
    public void sourceNull() {
        new FluxDelaySubscription(null, Flux.never());
    }

    @Test(expected = NullPointerException.class)
    public void otherNull() {
        Flux.never().delaySubscription(((Publisher<?>) (null)));
    }

    @Test
    public void normal() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Flux.range(1, 10).delaySubscription(Flux.just(1)).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void normalBackpressured() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);
        Flux.range(1, 10).delaySubscription(Flux.just(1)).subscribe(ts);
        ts.assertNoValues().assertNotComplete().assertNoError();
        ts.request(2);
        ts.assertValues(1, 2).assertNotComplete().assertNoError();
        ts.request(5);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7).assertNotComplete().assertNoError();
        ts.request(10);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void manyTriggered() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Flux.range(1, 10).delaySubscription(Flux.range(1, 10)).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void manyTriggeredBackpressured() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);
        Flux.range(1, 10).delaySubscription(Flux.range(1, 10)).subscribe(ts);
        ts.assertNoValues().assertNotComplete().assertNoError();
        ts.request(2);
        ts.assertValues(1, 2).assertNotComplete().assertNoError();
        ts.request(5);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7).assertNotComplete().assertNoError();
        ts.request(10);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void emptyTrigger() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Flux.range(1, 10).delaySubscription(Flux.empty()).subscribe(ts);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void emptyTriggerBackpressured() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create(0);
        Flux.range(1, 10).delaySubscription(Flux.empty()).subscribe(ts);
        ts.assertNoValues().assertNotComplete().assertNoError();
        ts.request(2);
        ts.assertValues(1, 2).assertNotComplete().assertNoError();
        ts.request(5);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7).assertNotComplete().assertNoError();
        ts.request(10);
        ts.assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).assertNoError().assertComplete();
    }

    @Test
    public void neverTriggered() {
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        Flux.range(1, 10).delaySubscription(Flux.never()).subscribe(ts);
        ts.assertNoValues().assertNoError().assertNotComplete();
    }

    @Test
    public void delayedTrigger() {
        StepVerifier.withVirtualTime(this::scenario_delayedTrigger).thenAwait(Duration.ofSeconds(3)).expectNext(1).verifyComplete();
    }

    @Test
    public void delayedTrigger2() {
        StepVerifier.withVirtualTime(this::scenario_delayedTrigger2).thenAwait(Duration.ofMillis(50)).expectNext(1).verifyComplete();
    }

    @Test
    public void scanMainSubscriber() {
        CoreSubscriber<String> actual = new LambdaSubscriber(null, ( e) -> {
        }, null, ( sub) -> sub.request(100));
        DelaySubscriptionOtherSubscriber<String, Integer> arbiter = new DelaySubscriptionOtherSubscriber<String, Integer>(actual, ( s) -> {
        });
        FluxDelaySubscription.DelaySubscriptionMainSubscriber<String> test = new FluxDelaySubscription.DelaySubscriptionMainSubscriber<String>(actual, arbiter);
        assertThat(test.scan(ACTUAL)).isSameAs(actual);
    }

    @Test
    public void scanOtherSubscriber() {
        CoreSubscriber<String> actual = new LambdaSubscriber(null, ( e) -> {
        }, null, ( sub) -> sub.request(100));
        DelaySubscriptionOtherSubscriber<String, Integer> test = new DelaySubscriptionOtherSubscriber<String, Integer>(actual, ( s) -> {
        });
        Subscription parent = Operators.emptySubscription();
        test.onSubscribe(parent);
        assertThat(test.scan(REQUESTED_FROM_DOWNSTREAM)).isEqualTo(100L);
        assertThat(test.scan(PARENT)).isSameAs(parent);
        assertThat(test.scan(ACTUAL)).isSameAs(actual);
        assertThat(test.scan(CANCELLED)).isFalse();
        assertThat(test.scan(TERMINATED)).isFalse();
        test.onError(new IllegalStateException("boom"));
        assertThat(test.scan(CANCELLED)).isFalse();
        assertThat(test.scan(TERMINATED)).isTrue();
        test.cancel();
        assertThat(test.scan(CANCELLED)).isTrue();
    }
}
