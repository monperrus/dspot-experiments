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
package reactor.core.publisher.scenarios;


import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.junit.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.test.subscriber.AssertSubscriber;


public class FluxWindowConsistencyTest {
    DirectProcessor<Integer> sourceProcessor = DirectProcessor.create();

    Flux<Integer> source;

    AssertSubscriber<Flux<Integer>> mainSubscriber;

    private AtomicInteger sourceCount = new AtomicInteger();

    private AtomicInteger innerCreated = new AtomicInteger();

    private AtomicInteger innerCancelled = new AtomicInteger();

    private AtomicInteger innerCompleted = new AtomicInteger();

    private AtomicInteger innerTerminated = new AtomicInteger();

    private AtomicInteger mainCancelled = new AtomicInteger();

    private AtomicInteger mainCompleted = new AtomicInteger();

    private AtomicInteger mainTerminated = new AtomicInteger();

    @Test
    public void windowExactComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.window(3, 3);
        subscribe(windows);
        generateAndComplete(0, 6);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowSkipComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.window(3, 5);
        subscribe(windows);
        generateAndComplete(0, 10);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(5, 6, 7));
    }

    @Test
    public void windowOverlapComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 3);
        subscribe(windows);
        generateAndComplete(0, 5);
        verifyMainComplete(Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(3, 4));
    }

    @Test
    public void windowDurationComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.window(Duration.ofMillis(200));
        subscribe(windows);
        generate(0, 3);
        Thread.sleep(300);
        generateAndComplete(3, 3);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowTimeoutComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.windowTimeout(5, Duration.ofMillis(200));
        subscribe(windows);
        generate(0, 3);
        Thread.sleep(300);
        generateAndComplete(3, 3);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowBoundaryComplete() throws Exception {
        DirectProcessor<Integer> boundary = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.window(boundary);
        subscribe(windows);
        generate(0, 3);
        boundary.onNext(1);
        generateAndComplete(3, 3);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowStartEndComplete() throws Exception {
        DirectProcessor<Integer> start = DirectProcessor.create();
        DirectProcessor<Integer> end1 = DirectProcessor.create();
        DirectProcessor<Integer> end2 = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.windowWhen(start, ( v) -> v == 1 ? end1 : end2);
        subscribe(windows);
        start.onNext(1);
        generate(0, 3);
        end1.onNext(1);
        start.onNext(2);
        generateAndComplete(3, 3);
        verifyMainComplete(Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowUntilComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.windowUntil(( i) -> (i % 3) == 0);
        subscribe(windows);
        generateAndComplete(1, 5);
        verifyMainComplete(Arrays.asList(1, 2, 3), Arrays.asList(4, 5));
    }

    @Test
    public void windowWhileComplete() throws Exception {
        Flux<Flux<Integer>> windows = source.windowWhile(( i) -> (i % 3) != 0);
        subscribe(windows);
        generateAndComplete(1, 5);
        verifyMainComplete(Arrays.asList(1, 2), Arrays.asList(4, 5));
    }

    @Test
    public void groupByComplete() throws Exception {
        Flux<GroupedFlux<Integer, Integer>> windows = source.groupBy(( i) -> i % 2);
        subscribeGroups(windows);
        generateAndComplete(0, 6);
        verifyMainComplete(Arrays.asList(0, 2, 4), Arrays.asList(1, 3, 5));
    }

    @Test
    public void windowExactMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 5);
        subscribe(windows);
        generateWithCancel(0, 7, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(5, 6, 7, 8, 9));
    }

    @Test
    public void windowSkipMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(3, 5);
        subscribe(windows);
        generateWithCancel(0, 6, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2), Arrays.asList(5, 6, 7));
    }

    @Test
    public void windowOverlapMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 3);
        subscribe(windows);
        generateWithCancel(0, 4, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(3, 4, 5, 6, 7));
    }

    @Test
    public void windowDurationMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(Duration.ofMillis(100));
        subscribe(windows);
        generate(0, 2);
        mainSubscriber.cancel();
        generate(2, 3);
        Thread.sleep(200);
        generate(5, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2, 3, 4));
    }

    @Test
    public void windowTimeoutMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowTimeout(10, Duration.ofMillis(100));
        subscribe(windows);
        generate(0, 2);
        mainSubscriber.cancel();
        generate(2, 3);
        Thread.sleep(200);
        generate(5, 10);
        verifyMainCancel(true);
    }

    @Test
    public void windowBoundaryMainCancel() throws Exception {
        DirectProcessor<Integer> boundary = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.window(boundary);
        subscribe(windows);
        generate(0, 3);
        boundary.onNext(1);
        generate(3, 1);
        mainSubscriber.cancel();
        generate(4, 2);
        boundary.onNext(1);
        generate(6, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowStartEndMainCancel() throws Exception {
        DirectProcessor<Integer> start = DirectProcessor.create();
        DirectProcessor<Integer> end1 = DirectProcessor.create();
        DirectProcessor<Integer> end2 = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.windowWhen(start, ( v) -> v == 1 ? end1 : end2);
        subscribe(windows);
        start.onNext(1);
        generate(0, 3);
        end1.onNext(1);
        start.onNext(2);
        generate(3, 1);
        mainSubscriber.cancel();
        generate(4, 2);
        end2.onNext(1);
        start.onNext(3);
        generate(7, 10);
        verifyMainCancel(true, Arrays.asList(0, 1, 2), Arrays.asList(3, 4, 5));
    }

    @Test
    public void windowUntilMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowUntil(( i) -> (i % 3) == 0);
        subscribe(windows);
        generateWithCancel(1, 4, 10);
        verifyMainCancel(true, Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6));
    }

    @Test
    public void windowWhileMainCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowWhile(( i) -> (i % 3) != 0);
        subscribe(windows);
        generateWithCancel(1, 4, 10);
        verifyMainCancel(true, Arrays.asList(1, 2), Arrays.asList(4, 5));
    }

    @Test
    public void groupByMainCancel() throws Exception {
        Flux<GroupedFlux<Integer, Integer>> windows = source.groupBy(( i) -> i % 2);
        subscribeGroups(windows);
        generateWithCancel(0, 5, 1);
        verifyMainCancel(false, Arrays.asList(0, 2, 4), Arrays.asList(1, 3, 5));
    }

    @Test
    public void windowExactMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 5);
        subscribe(windows);
        generateWithCancel(0, 10, 1);
        verifyMainCancelNoNewWindow(2, Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(5, 6, 7, 8, 9));
    }

    @Test
    public void windowSkipMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.window(2, 5);
        subscribe(windows);
        generateWithCancel(0, 5, 1);
        verifyMainCancelNoNewWindow(1, Arrays.asList(0, 1));
    }

    @Test
    public void windowOverlapMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 1);
        subscribe(windows);
        generateWithCancel(0, 2, 1);
        verifyMainCancelNoNewWindow(0, Arrays.asList(0, 1, 2), Arrays.asList(1, 2));
    }

    @Test
    public void windowDurationMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.window(Duration.ofMillis(100));
        subscribe(windows);
        generate(0, 2);
        mainSubscriber.cancel();
        Thread.sleep(200);
        generate(2, 1);
        verifyMainCancelNoNewWindow(1, Arrays.asList(0, 1));
    }

    @Test
    public void windowTimeoutMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.windowTimeout(5, Duration.ofMillis(200));
        subscribe(windows);
        generate(0, 1);
        Thread.sleep(300);
        generate(1, 1);
        mainSubscriber.cancel();
        Thread.sleep(300);
        generate(2, 1);
        verifyMainCancelNoNewWindow(1, Arrays.asList(0), Arrays.asList(1));
    }

    @Test
    public void windowBoundaryMainCancelNoNewWindow() throws Exception {
        DirectProcessor<Integer> boundary = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.window(boundary);
        subscribe(windows);
        generate(0, 3);
        boundary.onNext(1);
        mainSubscriber.cancel();
        generate(3, 1);
        verifyMainCancelNoNewWindow(1, Arrays.asList(0, 1, 2));
    }

    @Test
    public void windowStartEndMainCancelNoNewWindow() throws Exception {
        DirectProcessor<Integer> start = DirectProcessor.create();
        DirectProcessor<Integer> end1 = DirectProcessor.create();
        DirectProcessor<Integer> end2 = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.windowWhen(start, ( v) -> v == 1 ? end1 : end2);
        subscribe(windows);
        start.onNext(1);
        generate(0, 4);
        end1.onNext(1);
        start.onNext(2);
        mainSubscriber.cancel();
        generate(5, 1);
        verifyMainCancelNoNewWindow(1, Arrays.asList(0, 1, 2, 3));
    }

    @Test
    public void windowUntilMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.windowUntil(( i) -> (i % 3) == 0);
        subscribe(windows);
        generateWithCancel(0, 4, 1);
        verifyMainCancelNoNewWindow(2, Arrays.asList(0), Arrays.asList(1, 2, 3));
    }

    @Test
    public void windowWhileMainCancelNoNewWindow() throws Exception {
        Flux<Flux<Integer>> windows = source.windowWhile(( i) -> (i % 3) != 1);
        subscribe(windows);
        generateWithCancel(0, 4, 1);
        verifyMainCancelNoNewWindow(2, Arrays.asList(0), Arrays.asList(2, 3));
    }

    @Test
    public void groupByMainCancelNoNewWindow() throws Exception {
        Flux<GroupedFlux<Integer, Integer>> windows = source.groupBy(( i) -> i % 2);
        subscribeGroups(windows);
        generateWithCancel(0, 1, 1);
        verifyMainCancelNoNewWindow(0, Arrays.asList(0));
    }

    @Test
    public void windowExactInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 5);
        subscribe(windows);
        generateWithCancel(0, 49, 1);
        verifyInnerCancel(1, ( i) -> i != 7, Arrays.asList(0, 1, 2, 3, 4), Arrays.asList(5, 6));
    }

    @Test
    public void windowSkipInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(2, 5);
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(1, ( i) -> i != 6, Arrays.asList(0, 1), Arrays.asList(5));
    }

    @Test
    public void windowOverlapInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(5, 1);
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(0, ( i) -> i != 2, Arrays.asList(0, 1), Arrays.asList(1));
    }

    @Test
    public void windowDurationInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.window(Duration.ofMillis(5000));
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(0, ( i) -> i != 2, Arrays.asList(0, 1));
    }

    @Test
    public void windowTimeoutInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowTimeout(5, Duration.ofMillis(5000));
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(0, ( i) -> i != 2, Arrays.asList(0, 1));
    }

    @Test
    public void windowBoundaryInnerCancel() throws Exception {
        DirectProcessor<Integer> boundaryProcessor = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.window(boundaryProcessor);
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(0, ( i) -> i != 2, Arrays.asList(0, 1));
    }

    @Test
    public void windowStartEndInnerCancel() throws Exception {
        DirectProcessor<Integer> start = DirectProcessor.create();
        DirectProcessor<Integer> end1 = DirectProcessor.create();
        DirectProcessor<Integer> end2 = DirectProcessor.create();
        Flux<Flux<Integer>> windows = source.windowWhen(start, ( v) -> v == 1 ? end1 : end2);
        subscribe(windows);
        start.onNext(1);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(0, ( i) -> i != 2, Arrays.asList(0, 1));
    }

    @Test
    public void windowUntilInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowUntil(( i) -> (i % 3) == 0);
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(1, ( i) -> i != 3, Arrays.asList(0), Arrays.asList(1, 2));
    }

    @Test
    public void windowWhileInnerCancel() throws Exception {
        Flux<Flux<Integer>> windows = source.windowWhile(( i) -> (i % 3) != 1);
        subscribe(windows);
        generateWithCancel(0, 6, 1);
        verifyInnerCancel(1, ( i) -> i != 3, Arrays.asList(0), Arrays.asList(2));
    }

    @Test
    public void groupByInnerCancel() throws Exception {
        Flux<GroupedFlux<Integer, Integer>> windows = source.groupBy(( i) -> i % 2);
        subscribeGroups(windows);
        generateWithCancel(0, 9, 1);
        verifyInnerCancel(0, ( i) -> i < 6, Arrays.asList(0, 2, 4), Arrays.asList(1, 3, 5));
    }
}
