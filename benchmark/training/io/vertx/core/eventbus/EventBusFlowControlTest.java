/**
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.core.eventbus;


import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.test.core.AsyncTestBase;
import io.vertx.test.core.VertxTestBase;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import org.junit.Test;


/**
 *
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EventBusFlowControlTest extends VertxTestBase {
    protected EventBus eb;

    @Test
    public void testFlowControl() {
        MessageProducer<String> prod = eb.sender("some-address");
        int numBatches = 1000;
        int wqms = 2000;
        prod.setWriteQueueMaxSize(wqms);
        MessageConsumer<String> consumer = eb.consumer("some-address");
        AtomicInteger cnt = new AtomicInteger();
        consumer.handler(( msg) -> {
            int c = cnt.incrementAndGet();
            if (c == (numBatches * wqms)) {
                testComplete();
            }
        });
        vertx.runOnContext(( v) -> {
            sendBatch(prod, wqms, numBatches, 0);
        });
        await();
    }

    @Test
    public void testFlowControlWithOptions() {
        MessageProducer<String> prod = eb.sender("some-address");
        prod.deliveryOptions(new DeliveryOptions().addHeader("foo", "bar"));
        int numBatches = 1000;
        int wqms = 2000;
        prod.setWriteQueueMaxSize(wqms);
        MessageConsumer<String> consumer = eb.consumer("some-address");
        AtomicInteger cnt = new AtomicInteger();
        consumer.handler(( msg) -> {
            int c = cnt.incrementAndGet();
            if (c == (numBatches * wqms)) {
                testComplete();
            }
        });
        vertx.runOnContext(( v) -> {
            sendBatch(prod, wqms, numBatches, 0);
        });
        await();
    }

    @Test
    public void testDrainHandlerCalledWhenQueueAlreadyDrained() throws Exception {
        MessageConsumer<String> consumer = eb.consumer("some-address");
        consumer.handler(( msg) -> {
        });
        MessageProducer<String> prod = eb.sender("some-address");
        prod.setWriteQueueMaxSize(1);
        prod.write("msg");
        assertTrue(prod.writeQueueFull());
        AsyncTestBase.waitUntil(() -> !(prod.writeQueueFull()));
        prod.drainHandler(( v) -> {
            testComplete();
        });
        await();
    }

    @Test
    public void testFlowControlPauseConsumer() {
        MessageProducer<String> prod = eb.sender("some-address");
        int numBatches = 10;
        int wqms = 100;
        prod.setWriteQueueMaxSize(wqms);
        MessageConsumer<String> consumer = eb.consumer("some-address");
        AtomicInteger cnt = new AtomicInteger();
        AtomicBoolean paused = new AtomicBoolean();
        consumer.handler(( msg) -> {
            assertFalse(paused.get());
            int c = cnt.incrementAndGet();
            if (c == (numBatches * wqms)) {
                testComplete();
            }
            if ((c % 100) == 0) {
                consumer.pause();
                paused.set(true);
                vertx.setTimer(100, ( tid) -> {
                    paused.set(false);
                    consumer.resume();
                });
            }
        });
        sendBatch(prod, wqms, numBatches, 0);
        await();
    }

    @Test
    public void testFlowControlNoConsumer() {
        MessageProducer<String> prod = eb.sender("some-address");
        int wqms = 2000;
        prod.setWriteQueueMaxSize(wqms);
        boolean drainHandlerSet = false;
        for (int i = 0; i < (wqms * 2); i++) {
            prod.send(("message-" + i));
            if ((prod.writeQueueFull()) && (!drainHandlerSet)) {
                prod.drainHandler(( v) -> {
                    fail("Should not be called");
                });
                drainHandlerSet = true;
            }
        }
        assertTrue(drainHandlerSet);
        vertx.setTimer(500, ( tid) -> testComplete());
        await();
    }

    @Test
    public void testResumePausedProducer() {
        BlockingQueue<Integer> sequence = new LinkedBlockingQueue<>();
        AtomicReference<Context> handlerContext = new AtomicReference<>();
        MessageConsumer<Integer> consumer = eb.consumer("some-address", ( msg) -> {
            if (sequence.isEmpty()) {
                handlerContext.set(Vertx.currentContext());
            } else {
                assertOnIOContext(handlerContext.get());
            }
            sequence.add(msg.body());
        });
        consumer.pause();
        MessageProducer<Integer> prod = eb.sender("some-address");
        LinkedList<Integer> expected = new LinkedList<>();
        int count = 0;
        while (!(prod.writeQueueFull())) {
            int val = count++;
            expected.add(val);
            prod.send(val);
        } 
        consumer.resume();
        AsyncTestBase.assertWaitUntil(() -> !(prod.writeQueueFull()));
        int theCount = count;
        AsyncTestBase.assertWaitUntil(() -> (sequence.size()) == theCount);
        while ((expected.size()) > 0) {
            assertEquals(expected.removeFirst(), sequence.poll());
        } 
        assertNotNull(handlerContext.get());
    }
}
