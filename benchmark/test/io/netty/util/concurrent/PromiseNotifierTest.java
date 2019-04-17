/**
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;


public class PromiseNotifierTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNullPromisesArray() {
        expectedException.expect(NullPointerException.class);
        new PromiseNotifier<Void, Future<Void>>(((Promise<Void>[]) (null)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNullPromiseInArray() {
        expectedException.expect(IllegalArgumentException.class);
        new PromiseNotifier<Void, Future<Void>>(((Promise<Void>) (null)));
    }

    @Test
    public void testListenerSuccess() throws Exception {
        @SuppressWarnings("unchecked")
        Promise<Void> p1 = Mockito.mock(Promise.class);
        @SuppressWarnings("unchecked")
        Promise<Void> p2 = Mockito.mock(Promise.class);
        @SuppressWarnings("unchecked")
        PromiseNotifier<Void, Future<Void>> notifier = new PromiseNotifier<Void, Future<Void>>(p1, p2);
        @SuppressWarnings("unchecked")
        Future<Void> future = Mockito.mock(Future.class);
        Mockito.when(future.isSuccess()).thenReturn(true);
        Mockito.when(future.get()).thenReturn(null);
        Mockito.when(p1.trySuccess(null)).thenReturn(true);
        Mockito.when(p2.trySuccess(null)).thenReturn(true);
        notifier.operationComplete(future);
        Mockito.verify(p1).trySuccess(null);
        Mockito.verify(p2).trySuccess(null);
    }

    @Test
    public void testListenerFailure() throws Exception {
        @SuppressWarnings("unchecked")
        Promise<Void> p1 = Mockito.mock(Promise.class);
        @SuppressWarnings("unchecked")
        Promise<Void> p2 = Mockito.mock(Promise.class);
        @SuppressWarnings("unchecked")
        PromiseNotifier<Void, Future<Void>> notifier = new PromiseNotifier<Void, Future<Void>>(p1, p2);
        @SuppressWarnings("unchecked")
        Future<Void> future = Mockito.mock(Future.class);
        Throwable t = Mockito.mock(Throwable.class);
        Mockito.when(future.isSuccess()).thenReturn(false);
        Mockito.when(future.isCancelled()).thenReturn(false);
        Mockito.when(future.cause()).thenReturn(t);
        Mockito.when(p1.tryFailure(t)).thenReturn(true);
        Mockito.when(p2.tryFailure(t)).thenReturn(true);
        notifier.operationComplete(future);
        Mockito.verify(p1).tryFailure(t);
        Mockito.verify(p2).tryFailure(t);
    }
}
