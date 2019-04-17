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


import FluxSample.SampleOther;
import Scannable.Attr.ACTUAL;
import Scannable.Attr.BUFFERED;
import Scannable.Attr.CANCELLED;
import Scannable.Attr.PARENT;
import Scannable.Attr.PREFETCH;
import Scannable.Attr.REQUESTED_FROM_DOWNSTREAM;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.test.StepVerifier;
import reactor.test.subscriber.AssertSubscriber;


public class FluxSampleTest {
    @Test(expected = NullPointerException.class)
    public void sourceNull() {
        new FluxSample(null, Flux.never());
    }

    @Test(expected = NullPointerException.class)
    public void otherNull() {
        Flux.never().sample(((Publisher<Object>) (null)));
    }

    @Test
    public void normal1() {
        sample(true, false);
    }

    @Test
    public void normal2() {
        sample(true, true);
    }

    @Test
    public void error1() {
        sample(false, false);
    }

    @Test
    public void error2() {
        sample(false, true);
    }

    @Test
    public void subscriberCancels() {
        DirectProcessor<Integer> main = DirectProcessor.create();
        DirectProcessor<String> other = DirectProcessor.create();
        AssertSubscriber<Integer> ts = AssertSubscriber.create();
        main.sample(other).subscribe(ts);
        Assert.assertTrue("Main no subscriber?", main.hasDownstreams());
        Assert.assertTrue("Other no subscriber?", other.hasDownstreams());
        ts.cancel();
        Assert.assertFalse("Main no subscriber?", main.hasDownstreams());
        Assert.assertFalse("Other no subscriber?", other.hasDownstreams());
        ts.assertNoValues().assertNoError().assertNotComplete();
    }

    @Test
    public void mainCompletesImmediately() {
        completeImmediately(true);
    }

    @Test
    public void otherCompletesImmediately() {
        completeImmediately(false);
    }

    @Test
    public void sampleIncludesLastItem() {
        Flux<Integer> source = Flux.concat(Flux.range(1, 5), Mono.delay(Duration.ofMillis(300)).ignoreElement().map(Long::intValue), Flux.just(80, 90, 100)).hide();
        Duration duration = StepVerifier.create(source.sample(Duration.ofMillis(250))).expectNext(5).expectNext(100).verifyComplete();
        // sanity check on the sequence duration
        assertThat(duration.toMillis()).isLessThan(500);
    }

    @Test
    public void sourceTerminatesBeforeSamplingEmits() {
        Flux<Integer> source = Flux.just(1, 2).hide();
        Duration duration = StepVerifier.create(source.sample(Duration.ofMillis(250))).expectNext(2).verifyComplete();
        // sanity check on the sequence duration
        assertThat(duration.toMillis()).isLessThan(250);
    }

    @Test
    public void sourceErrorsBeforeSamplingNoEmission() {
        Flux<Integer> source = Flux.just(1, 2).concatWith(Mono.error(new IllegalStateException("boom")));
        Duration duration = StepVerifier.create(source.sample(Duration.ofMillis(250))).verifyErrorMessage("boom");
        // sanity check on the sequence duration
        assertThat(duration.toMillis()).isLessThan(250);
    }

    @Test
    public void scanMainSubscriber() {
        CoreSubscriber<Integer> actual = new LambdaSubscriber(null, ( e) -> {
        }, null, null);
        FluxSample.SampleMainSubscriber<Integer> test = new FluxSample.SampleMainSubscriber<>(actual);
        Subscription parent = Operators.emptySubscription();
        test.onSubscribe(parent);
        assertThat(test.scan(PARENT)).isSameAs(parent);
        assertThat(test.scan(ACTUAL)).isSameAs(actual);
        test.requested = 35;
        assertThat(test.scan(REQUESTED_FROM_DOWNSTREAM)).isEqualTo(35L);
        test.value = 5;
        assertThat(test.scan(BUFFERED)).isEqualTo(1);
        assertThat(test.scan(CANCELLED)).isFalse();
        test.cancel();
        assertThat(test.scan(CANCELLED)).isTrue();
    }

    @Test
    public void scanOtherSubscriber() {
        CoreSubscriber<Integer> actual = new LambdaSubscriber(null, ( e) -> {
        }, null, null);
        FluxSample.SampleMainSubscriber<Integer> main = new FluxSample.SampleMainSubscriber<>(actual);
        SampleOther<Integer, Integer> test = new FluxSample.SampleOther<>(main);
        assertThat(test.scan(PARENT)).isSameAs(main.other);
        assertThat(test.scan(ACTUAL)).isSameAs(main);
        assertThat(test.scan(PREFETCH)).isEqualTo(Integer.MAX_VALUE);
        assertThat(test.scan(CANCELLED)).isFalse();
        main.cancelOther();
        assertThat(test.scan(CANCELLED)).isTrue();
    }
}
