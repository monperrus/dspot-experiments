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
package org.apache.camel.component.websocket;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketTextListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.junit.Test;


public class WebsocketClientCamelRouteTest extends CamelTestSupport {
    private static List<String> received = new ArrayList<>();

    private static CountDownLatch latch = new CountDownLatch(10);

    protected int port;

    @Test
    public void testWSHttpCall() throws Exception {
        AsyncHttpClient c = new DefaultAsyncHttpClient();
        WebSocket websocket = c.prepareGet((("ws://127.0.0.1:" + (port)) + "/test")).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketTextListener() {
            @Override
            public void onMessage(String message) {
                WebsocketClientCamelRouteTest.received.add(message);
                log.info(("received --> " + message));
                WebsocketClientCamelRouteTest.latch.countDown();
            }

            @Override
            public void onOpen(WebSocket websocket) {
            }

            @Override
            public void onClose(WebSocket websocket) {
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }
        }).build()).get();
        getMockEndpoint("mock:client").expectedBodiesReceived("Hello from WS client");
        websocket.sendMessage("Hello from WS client");
        assertTrue(WebsocketClientCamelRouteTest.latch.await(10, TimeUnit.SECONDS));
        assertMockEndpointsSatisfied();
        assertEquals(10, WebsocketClientCamelRouteTest.received.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(">> Welcome on board!", WebsocketClientCamelRouteTest.received.get(i));
        }
        websocket.close();
        c.close();
    }
}
