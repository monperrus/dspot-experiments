/**
 * Copyright 2002-2015 the original author or authors.
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
package org.springframework.messaging.simp.broker;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MultiValueMap;


/**
 * Test fixture for
 * {@link org.springframework.messaging.simp.broker.DefaultSubscriptionRegistry}.
 *
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 */
public class DefaultSubscriptionRegistryTests {
    private final DefaultSubscriptionRegistry registry = new DefaultSubscriptionRegistry();

    @Test
    public void registerSubscriptionInvalidInput() {
        String sessId = "sess01";
        String subsId = "subs01";
        String dest = "/foo";
        this.registry.registerSubscription(subscribeMessage(null, subsId, dest));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
        this.registry.registerSubscription(subscribeMessage(sessId, null, dest));
        actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
        this.registry.registerSubscription(subscribeMessage(sessId, subsId, null));
        actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void registerSubscription() {
        String sessId = "sess01";
        String subsId = "subs01";
        String dest = "/foo";
        this.registry.registerSubscription(subscribeMessage(sessId, subsId, dest));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected one element " + actual), 1, actual.size());
        Assert.assertEquals(Collections.singletonList(subsId), actual.get(sessId));
    }

    @Test
    public void registerSubscriptionOneSession() {
        String sessId = "sess01";
        List<String> subscriptionIds = Arrays.asList("subs01", "subs02", "subs03");
        String dest = "/foo";
        for (String subId : subscriptionIds) {
            this.registry.registerSubscription(subscribeMessage(sessId, subId, dest));
        }
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessId)));
    }

    @Test
    public void registerSubscriptionMultipleSessions() {
        List<String> sessIds = Arrays.asList("sess01", "sess02", "sess03");
        List<String> subscriptionIds = Arrays.asList("subs01", "subs02", "subs03");
        String dest = "/foo";
        for (String sessId : sessIds) {
            for (String subsId : subscriptionIds) {
                this.registry.registerSubscription(subscribeMessage(sessId, subsId, dest));
            }
        }
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(0))));
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(1))));
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(2))));
    }

    @Test
    public void registerSubscriptionWithDestinationPattern() {
        String sessId = "sess01";
        String subsId = "subs01";
        String destPattern = "/topic/PRICE.STOCK.*.IBM";
        String dest = "/topic/PRICE.STOCK.NASDAQ.IBM";
        this.registry.registerSubscription(subscribeMessage(sessId, subsId, destPattern));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected one element " + actual), 1, actual.size());
        Assert.assertEquals(Collections.singletonList(subsId), actual.get(sessId));
    }

    // SPR-11657
    @Test
    public void registerSubscriptionsWithSimpleAndPatternDestinations() {
        String sess1 = "sess01";
        String sess2 = "sess02";
        String subs1 = "subs01";
        String subs2 = "subs02";
        String subs3 = "subs03";
        String destNasdaqIbm = "/topic/PRICE.STOCK.NASDAQ.IBM";
        Message<?> destNasdaqIbmMessage = createMessage(destNasdaqIbm);
        this.registry.registerSubscription(subscribeMessage(sess1, subs2, destNasdaqIbm));
        this.registry.registerSubscription(subscribeMessage(sess1, subs1, "/topic/PRICE.STOCK.*.IBM"));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(Arrays.asList(subs2, subs1), actual.get(sess1));
        this.registry.registerSubscription(subscribeMessage(sess2, subs1, destNasdaqIbm));
        this.registry.registerSubscription(subscribeMessage(sess2, subs2, "/topic/PRICE.STOCK.NYSE.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess2, subs3, "/topic/PRICE.STOCK.NASDAQ.GOOG"));
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals(Arrays.asList(subs2, subs1), actual.get(sess1));
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess2));
        this.registry.unregisterAllSubscriptions(sess1);
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess2));
        this.registry.registerSubscription(subscribeMessage(sess1, subs1, "/topic/PRICE.STOCK.*.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs2, destNasdaqIbm));
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals(Arrays.asList(subs1, subs2), actual.get(sess1));
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess2));
        this.registry.unregisterSubscription(unsubscribeMessage(sess1, subs2));
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess1));
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess2));
        this.registry.unregisterSubscription(unsubscribeMessage(sess1, subs1));
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(Collections.singletonList(subs1), actual.get(sess2));
        this.registry.unregisterSubscription(unsubscribeMessage(sess2, subs1));
        actual = this.registry.findSubscriptions(destNasdaqIbmMessage);
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
    }

    // SPR-11755
    @Test
    public void registerAndUnregisterMultipleDestinations() {
        String sess1 = "sess01";
        String sess2 = "sess02";
        String subs1 = "subs01";
        String subs2 = "subs02";
        String subs3 = "subs03";
        String subs4 = "subs04";
        String subs5 = "subs05";
        this.registry.registerSubscription(subscribeMessage(sess1, subs1, "/topic/PRICE.STOCK.NASDAQ.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs2, "/topic/PRICE.STOCK.NYSE.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs3, "/topic/PRICE.STOCK.NASDAQ.GOOG"));
        this.registry.findSubscriptions(createMessage("/topic/PRICE.STOCK.NYSE.IBM"));
        this.registry.findSubscriptions(createMessage("/topic/PRICE.STOCK.NASDAQ.GOOG"));
        this.registry.findSubscriptions(createMessage("/topic/PRICE.STOCK.NASDAQ.IBM"));
        this.registry.unregisterSubscription(unsubscribeMessage(sess1, subs1));
        this.registry.unregisterSubscription(unsubscribeMessage(sess1, subs2));
        this.registry.unregisterSubscription(unsubscribeMessage(sess1, subs3));
        this.registry.registerSubscription(subscribeMessage(sess1, subs1, "/topic/PRICE.STOCK.NASDAQ.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs2, "/topic/PRICE.STOCK.NYSE.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs3, "/topic/PRICE.STOCK.NASDAQ.GOOG"));
        this.registry.registerSubscription(subscribeMessage(sess1, subs4, "/topic/PRICE.STOCK.NYSE.IBM"));
        this.registry.registerSubscription(subscribeMessage(sess2, subs5, "/topic/PRICE.STOCK.NASDAQ.GOOG"));
        this.registry.unregisterAllSubscriptions(sess1);
        this.registry.unregisterAllSubscriptions(sess2);
    }

    @Test
    public void registerSubscriptionWithDestinationPatternRegex() {
        String sessId = "sess01";
        String subsId = "subs01";
        String destPattern = "/topic/PRICE.STOCK.*.{ticker:(IBM|MSFT)}";
        this.registry.registerSubscription(subscribeMessage(sessId, subsId, destPattern));
        Message<?> message = createMessage("/topic/PRICE.STOCK.NASDAQ.IBM");
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(message);
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected one element " + actual), 1, actual.size());
        Assert.assertEquals(Collections.singletonList(subsId), actual.get(sessId));
        message = createMessage("/topic/PRICE.STOCK.NASDAQ.MSFT");
        actual = this.registry.findSubscriptions(message);
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected one element " + actual), 1, actual.size());
        Assert.assertEquals(Collections.singletonList(subsId), actual.get(sessId));
        message = createMessage("/topic/PRICE.STOCK.NASDAQ.VMW");
        actual = this.registry.findSubscriptions(message);
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected no elements " + actual), 0, actual.size());
    }

    @Test
    public void registerSubscriptionWithSelector() {
        String sessionId = "sess01";
        String subscriptionId = "subs01";
        String destination = "/foo";
        String selector = "headers.foo == 'bar'";
        this.registry.registerSubscription(subscribeMessage(sessionId, subscriptionId, destination, selector));
        // First, try with selector header
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setDestination(destination);
        accessor.setNativeHeader("foo", "bar");
        Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(message);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(Collections.singletonList(subscriptionId), actual.get(sessionId));
        // Then without
        actual = this.registry.findSubscriptions(createMessage(destination));
        Assert.assertNotNull(actual);
        Assert.assertEquals(0, actual.size());
    }

    @Test
    public void registerSubscriptionWithSelectorNotSupported() {
        String sessionId = "sess01";
        String subscriptionId = "subs01";
        String destination = "/foo";
        String selector = "headers.foo == 'bar'";
        this.registry.setSelectorHeaderName(null);
        this.registry.registerSubscription(subscribeMessage(sessionId, subscriptionId, destination, selector));
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setDestination(destination);
        accessor.setNativeHeader("foo", "bazz");
        Message<?> message = MessageBuilder.createMessage("", accessor.getMessageHeaders());
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(message);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals(Collections.singletonList(subscriptionId), actual.get(sessionId));
    }

    // SPR-11931
    @Test
    public void registerSubscriptionTwiceAndUnregister() {
        this.registry.registerSubscription(subscribeMessage("sess01", "subs01", "/foo"));
        this.registry.registerSubscription(subscribeMessage("sess01", "subs02", "/foo"));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(actual);
        Assert.assertEquals("Expected 1 element", 1, actual.size());
        Assert.assertEquals(Arrays.asList("subs01", "subs02"), actual.get("sess01"));
        this.registry.unregisterSubscription(unsubscribeMessage("sess01", "subs01"));
        actual = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(actual);
        Assert.assertEquals("Expected 1 element", 1, actual.size());
        Assert.assertEquals(Collections.singletonList("subs02"), actual.get("sess01"));
        this.registry.unregisterSubscription(unsubscribeMessage("sess01", "subs02"));
        actual = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(actual);
        Assert.assertEquals("Expected no element", 0, actual.size());
    }

    @Test
    public void unregisterSubscription() {
        List<String> sessIds = Arrays.asList("sess01", "sess02", "sess03");
        List<String> subscriptionIds = Arrays.asList("subs01", "subs02", "subs03");
        String dest = "/foo";
        for (String sessId : sessIds) {
            for (String subsId : subscriptionIds) {
                this.registry.registerSubscription(subscribeMessage(sessId, subsId, dest));
            }
        }
        this.registry.unregisterSubscription(unsubscribeMessage(sessIds.get(0), subscriptionIds.get(0)));
        this.registry.unregisterSubscription(unsubscribeMessage(sessIds.get(0), subscriptionIds.get(1)));
        this.registry.unregisterSubscription(unsubscribeMessage(sessIds.get(0), subscriptionIds.get(2)));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected two elements: " + actual), 2, actual.size());
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(1))));
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(2))));
    }

    @Test
    public void unregisterAllSubscriptions() {
        List<String> sessIds = Arrays.asList("sess01", "sess02", "sess03");
        List<String> subscriptionIds = Arrays.asList("subs01", "subs02", "subs03");
        String dest = "/foo";
        for (String sessId : sessIds) {
            for (String subsId : subscriptionIds) {
                this.registry.registerSubscription(subscribeMessage(sessId, subsId, dest));
            }
        }
        this.registry.unregisterAllSubscriptions(sessIds.get(0));
        this.registry.unregisterAllSubscriptions(sessIds.get(1));
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage(dest));
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected one element: " + actual), 1, actual.size());
        Assert.assertEquals(subscriptionIds, sort(actual.get(sessIds.get(2))));
    }

    @Test
    public void unregisterAllSubscriptionsNoMatch() {
        this.registry.unregisterAllSubscriptions("bogus");
        // no exceptions
    }

    @Test
    public void findSubscriptionsNoMatches() {
        MultiValueMap<String, String> actual = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(actual);
        Assert.assertEquals(("Expected no elements " + actual), 0, actual.size());
    }

    // SPR-12665
    @Test
    public void findSubscriptionsReturnsMapSafeToIterate() throws Exception {
        this.registry.registerSubscription(subscribeMessage("sess1", "1", "/foo"));
        this.registry.registerSubscription(subscribeMessage("sess2", "1", "/foo"));
        MultiValueMap<String, String> subscriptions = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(subscriptions);
        Assert.assertEquals(2, subscriptions.size());
        Iterator<Map.Entry<String, List<String>>> iterator = subscriptions.entrySet().iterator();
        iterator.next();
        this.registry.registerSubscription(subscribeMessage("sess3", "1", "/foo"));
        iterator.next();
        // no ConcurrentModificationException
    }

    // SPR-13185
    @Test
    public void findSubscriptionsReturnsMapSafeToIterateIncludingValues() throws Exception {
        this.registry.registerSubscription(subscribeMessage("sess1", "1", "/foo"));
        this.registry.registerSubscription(subscribeMessage("sess1", "2", "/foo"));
        MultiValueMap<String, String> allSubscriptions = this.registry.findSubscriptions(createMessage("/foo"));
        Assert.assertNotNull(allSubscriptions);
        Assert.assertEquals(1, allSubscriptions.size());
        Iterator<String> iteratorValues = allSubscriptions.get("sess1").iterator();
        iteratorValues.next();
        this.registry.unregisterSubscription(unsubscribeMessage("sess1", "2"));
        iteratorValues.next();
        // no ConcurrentModificationException
    }

    // SPR-13555
    @Test
    public void cacheLimitExceeded() throws Exception {
        this.registry.setCacheLimit(1);
        this.registry.registerSubscription(subscribeMessage("sess1", "1", "/foo"));
        this.registry.registerSubscription(subscribeMessage("sess1", "2", "/bar"));
        Assert.assertEquals(1, this.registry.findSubscriptions(createMessage("/foo")).size());
        Assert.assertEquals(1, this.registry.findSubscriptions(createMessage("/bar")).size());
        this.registry.registerSubscription(subscribeMessage("sess2", "1", "/foo"));
        this.registry.registerSubscription(subscribeMessage("sess2", "2", "/bar"));
        Assert.assertEquals(2, this.registry.findSubscriptions(createMessage("/foo")).size());
        Assert.assertEquals(2, this.registry.findSubscriptions(createMessage("/bar")).size());
    }
}
