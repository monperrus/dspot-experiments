/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.topic;


import TopicService.SERVICE_NAME;
import com.hazelcast.config.Config;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.hazelcast.core.Message;
import com.hazelcast.instance.MemberImpl;
import com.hazelcast.monitor.impl.LocalTopicStatsImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.NightlyTest;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import com.hazelcast.test.annotation.Repeat;
import com.hazelcast.topic.impl.TopicService;
import com.hazelcast.util.UuidUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class TopicTest extends HazelcastTestSupport {
    @Test
    public void testDestroyTopicRemovesStatistics() {
        String randomTopicName = HazelcastTestSupport.randomString();
        HazelcastInstance instance = createHazelcastInstance();
        final ITopic<String> topic = instance.getTopic(randomTopicName);
        topic.publish("foobar");
        // we need to give the message the chance to be processed, else the topic statistics are recreated
        // so in theory the destroy for the topic is broken
        HazelcastTestSupport.sleepSeconds(1);
        topic.destroy();
        final TopicService topicService = HazelcastTestSupport.getNode(instance).nodeEngine.getService(SERVICE_NAME);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                boolean containsStats = topicService.getStatsMap().containsKey(topic.getName());
                Assert.assertFalse(containsStats);
            }
        });
    }

    @Test
    public void testTopicPublishingMember() {
        final int nodeCount = 3;
        final String randomName = "testTopicPublishingMember" + (HazelcastTestSupport.generateRandomString(5));
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        HazelcastInstance[] instances = factory.newInstances();
        final AtomicInteger count1 = new AtomicInteger(0);
        final AtomicInteger count2 = new AtomicInteger(0);
        final AtomicInteger count3 = new AtomicInteger(0);
        for (int i = 0; i < nodeCount; i++) {
            final HazelcastInstance instance = instances[i];
            ITopic<Member> topic = instance.getTopic(randomName);
            topic.addMessageListener(new com.hazelcast.core.MessageListener<Member>() {
                public void onMessage(Message<Member> message) {
                    Member publishingMember = message.getPublishingMember();
                    if (publishingMember.equals(instance.getCluster().getLocalMember())) {
                        count1.incrementAndGet();
                    }
                    Member messageObject = message.getMessageObject();
                    if (publishingMember.equals(messageObject)) {
                        count2.incrementAndGet();
                    }
                    if (publishingMember.localMember()) {
                        count3.incrementAndGet();
                    }
                }
            });
        }
        for (int i = 0; i < nodeCount; i++) {
            HazelcastInstance instance = instances[i];
            instance.getTopic(randomName).publish(instance.getCluster().getLocalMember());
        }
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                Assert.assertEquals(nodeCount, count1.get());
                Assert.assertEquals((nodeCount * nodeCount), count2.get());
                Assert.assertEquals(nodeCount, count3.get());
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTopicLocalOrder() throws Exception {
        final int nodeCount = 5;
        final int count = 1000;
        final String randomTopicName = HazelcastTestSupport.randomString();
        Config config = new Config();
        config.getTopicConfig(randomTopicName).setGlobalOrderingEnabled(false);
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        final HazelcastInstance[] instances = factory.newInstances(config);
        final List<TopicTest.TestMessage>[] messageLists = new List[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            messageLists[i] = new CopyOnWriteArrayList<TopicTest.TestMessage>();
        }
        final CountDownLatch startLatch = new CountDownLatch(nodeCount);
        final CountDownLatch messageLatch = new CountDownLatch(((nodeCount * nodeCount) * count));
        final CountDownLatch publishLatch = new CountDownLatch((nodeCount * count));
        ExecutorService ex = Executors.newFixedThreadPool(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            final int finalI = i;
            ex.execute(new Runnable() {
                public void run() {
                    final List<TopicTest.TestMessage> messages = messageLists[finalI];
                    HazelcastInstance hz = instances[finalI];
                    ITopic<TopicTest.TestMessage> topic = hz.getTopic(randomTopicName);
                    topic.addMessageListener(new com.hazelcast.core.MessageListener<TopicTest.TestMessage>() {
                        public void onMessage(Message<TopicTest.TestMessage> message) {
                            messages.add(message.getMessageObject());
                            messageLatch.countDown();
                        }
                    });
                    startLatch.countDown();
                    try {
                        startLatch.await(1, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    Member localMember = hz.getCluster().getLocalMember();
                    for (int j = 0; j < count; j++) {
                        topic.publish(new TopicTest.TestMessage(localMember, UuidUtil.newUnsecureUuidString()));
                        publishLatch.countDown();
                    }
                }
            });
        }
        try {
            Assert.assertTrue(publishLatch.await(2, TimeUnit.MINUTES));
            Assert.assertTrue(messageLatch.await(5, TimeUnit.MINUTES));
            TopicTest.TestMessage[] ref = new TopicTest.TestMessage[messageLists[0].size()];
            messageLists[0].toArray(ref);
            Comparator<TopicTest.TestMessage> comparator = new Comparator<TopicTest.TestMessage>() {
                public int compare(TopicTest.TestMessage m1, TopicTest.TestMessage m2) {
                    // sort only publisher blocks. if publishers are the same, leave them as they are
                    return m1.publisher.getUuid().compareTo(m2.publisher.getUuid());
                }
            };
            Arrays.sort(ref, comparator);
            for (int i = 1; i < nodeCount; i++) {
                TopicTest.TestMessage[] messages = new TopicTest.TestMessage[messageLists[i].size()];
                messageLists[i].toArray(messages);
                Arrays.sort(messages, comparator);
                Assert.assertArrayEquals(ref, messages);
            }
        } finally {
            ex.shutdownNow();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTopicGlobalOrder() throws Exception {
        final int nodeCount = 5;
        final int count = 1000;
        final String randomTopicName = HazelcastTestSupport.randomString();
        Config config = new Config();
        config.getTopicConfig(randomTopicName).setGlobalOrderingEnabled(true);
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        final HazelcastInstance[] nodes = factory.newInstances(config);
        final List<TopicTest.TestMessage>[] messageListPerNode = new List[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            messageListPerNode[i] = new CopyOnWriteArrayList<TopicTest.TestMessage>();
        }
        final CountDownLatch messageLatch = new CountDownLatch((nodeCount * count));
        // add message listeners
        for (int i = 0; i < (nodes.length); i++) {
            final int nodeIndex = i;
            ITopic<TopicTest.TestMessage> topic = nodes[i].getTopic(randomTopicName);
            topic.addMessageListener(new com.hazelcast.core.MessageListener<TopicTest.TestMessage>() {
                public void onMessage(Message<TopicTest.TestMessage> message) {
                    messageListPerNode[nodeIndex].add(message.getMessageObject());
                    messageLatch.countDown();
                }
            });
        }
        // publish messages
        for (HazelcastInstance node : nodes) {
            Member localMember = node.getCluster().getLocalMember();
            for (int j = 0; j < count; j++) {
                TopicTest.TestMessage message = new TopicTest.TestMessage(localMember, UUID.randomUUID().toString());
                ITopic<Object> topic = node.getTopic(randomTopicName);
                topic.publish(message);
            }
        }
        // all messages in nodes messageLists should be equal
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() throws Exception {
                int i = 0;
                do {
                    Assert.assertEquals(messageListPerNode[i], messageListPerNode[(i++)]);
                } while (i < nodeCount );
            }
        });
    }

    private static class TestMessage implements DataSerializable {
        Member publisher;

        String data;

        @SuppressWarnings("unused")
        TestMessage() {
        }

        TestMessage(Member publisher, String data) {
            this.publisher = publisher;
            this.data = data;
        }

        public void writeData(ObjectDataOutput out) throws IOException {
            publisher.writeData(out);
            out.writeUTF(data);
        }

        public void readData(ObjectDataInput in) throws IOException {
            publisher = new MemberImpl();
            publisher.readData(in);
            data = in.readUTF();
        }

        @Override
        public boolean equals(Object o) {
            if ((this) == o) {
                return true;
            }
            if ((o == null) || ((getClass()) != (o.getClass()))) {
                return false;
            }
            TopicTest.TestMessage that = ((TopicTest.TestMessage) (o));
            if ((data) != null ? !(data.equals(that.data)) : (that.data) != null) {
                return false;
            }
            if ((publisher) != null ? !(publisher.equals(that.publisher)) : (that.publisher) != null) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = ((publisher) != null) ? publisher.hashCode() : 0;
            result = (31 * result) + ((data) != null ? data.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return (((("TestMessage{" + "publisher=") + (publisher)) + ", data='") + (data)) + "'}";
        }
    }

    @Test
    public void testName() {
        String randomTopicName = HazelcastTestSupport.randomString();
        HazelcastInstance hClient = createHazelcastInstance();
        ITopic<?> topic = hClient.getTopic(randomTopicName);
        Assert.assertEquals(randomTopicName, topic.getName());
    }

    @Test
    public void addMessageListener() throws InterruptedException {
        String randomTopicName = "addMessageListener" + (HazelcastTestSupport.generateRandomString(5));
        HazelcastInstance instance = createHazelcastInstance();
        ITopic<String> topic = instance.getTopic(randomTopicName);
        final CountDownLatch latch = new CountDownLatch(1);
        final String message = "Hazelcast Rocks!";
        topic.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message<String> msg) {
                if (msg.getMessageObject().equals(message)) {
                    latch.countDown();
                }
            }
        });
        topic.publish(message);
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testConfigListenerRegistration() throws InterruptedException {
        String topicName = "default";
        Config config = new Config();
        final CountDownLatch latch = new CountDownLatch(1);
        config.getTopicConfig(topicName).addMessageListenerConfig(new ListenerConfig().setImplementation(new com.hazelcast.core.MessageListener() {
            public void onMessage(Message message) {
                latch.countDown();
            }
        }));
        HazelcastInstance instance = createHazelcastInstance(config);
        instance.getTopic(topicName).publish(1);
        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void addTwoMessageListener() throws InterruptedException {
        String topicName = "addTwoMessageListener" + (HazelcastTestSupport.generateRandomString(5));
        HazelcastInstance instance = createHazelcastInstance();
        ITopic<String> topic = instance.getTopic(topicName);
        final CountDownLatch latch = new CountDownLatch(2);
        final String message = "Hazelcast Rocks!";
        topic.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message<String> msg) {
                if (msg.getMessageObject().equals(message)) {
                    latch.countDown();
                }
            }
        });
        topic.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message<String> msg) {
                if (msg.getMessageObject().equals(message)) {
                    latch.countDown();
                }
            }
        });
        topic.publish(message);
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    @Repeat(10)
    public void removeMessageListener() throws InterruptedException {
        String topicName = "removeMessageListener" + (HazelcastTestSupport.generateRandomString(5));
        try {
            HazelcastInstance instance = createHazelcastInstance();
            ITopic<String> topic = instance.getTopic(topicName);
            final AtomicInteger onMessageCount = new AtomicInteger(0);
            final CountDownLatch onMessageInvoked = new CountDownLatch(1);
            com.hazelcast.core.MessageListener<String> messageListener = new com.hazelcast.core.MessageListener<String>() {
                public void onMessage(Message<String> msg) {
                    onMessageCount.incrementAndGet();
                    onMessageInvoked.countDown();
                }
            };
            final String message = ("message_" + (messageListener.hashCode())) + "_";
            final String id = topic.addMessageListener(messageListener);
            topic.publish((message + "1"));
            onMessageInvoked.await();
            Assert.assertTrue(topic.removeMessageListener(id));
            topic.publish((message + "2"));
            HazelcastTestSupport.assertTrueEventually(new AssertTask() {
                @Override
                public void run() {
                    Assert.assertEquals(1, onMessageCount.get());
                }
            });
        } finally {
            shutdownNodeFactory();
        }
    }

    @Test
    public void testPerformance() throws InterruptedException {
        int count = 10000;
        String randomTopicName = HazelcastTestSupport.randomString();
        HazelcastInstance instance = createHazelcastInstance();
        ExecutorService ex = Executors.newFixedThreadPool(10);
        final ITopic<String> topic = instance.getTopic(randomTopicName);
        final CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            ex.submit(new Runnable() {
                public void run() {
                    topic.publish("my object");
                    latch.countDown();
                }
            });
        }
        Assert.assertTrue(latch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void addTwoListenerAndRemoveOne() throws InterruptedException {
        String topicName = "addTwoListenerAndRemoveOne" + (HazelcastTestSupport.generateRandomString(5));
        HazelcastInstance instance = createHazelcastInstance();
        ITopic<String> topic = instance.getTopic(topicName);
        final CountDownLatch latch = new CountDownLatch(3);
        final CountDownLatch cp = new CountDownLatch(2);
        final AtomicInteger atomicInteger = new AtomicInteger();
        final String message = "Hazelcast Rocks!";
        com.hazelcast.core.MessageListener<String> messageListener1 = new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message<String> msg) {
                atomicInteger.incrementAndGet();
                latch.countDown();
                cp.countDown();
            }
        };
        com.hazelcast.core.MessageListener<String> messageListener2 = new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message<String> msg) {
                atomicInteger.incrementAndGet();
                latch.countDown();
                cp.countDown();
            }
        };
        String messageListenerId = topic.addMessageListener(messageListener1);
        topic.addMessageListener(messageListener2);
        topic.publish(message);
        HazelcastTestSupport.assertOpenEventually(cp);
        topic.removeMessageListener(messageListenerId);
        topic.publish(message);
        HazelcastTestSupport.assertOpenEventually(latch);
        Assert.assertEquals(3, atomicInteger.get());
    }

    /**
     * Testing if topic can properly listen messages and if topic has any issue after a shutdown.
     */
    @Test
    public void testTopicCluster() throws InterruptedException {
        String topicName = "TestMessages" + (HazelcastTestSupport.generateRandomString(5));
        Config cfg = new Config();
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        HazelcastInstance[] instances = factory.newInstances(cfg);
        HazelcastInstance instance1 = instances[0];
        HazelcastInstance instance2 = instances[1];
        ITopic<String> topic1 = instance1.getTopic(topicName);
        final CountDownLatch latch1 = new CountDownLatch(1);
        final String message = "Test" + (HazelcastTestSupport.randomString());
        topic1.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message msg) {
                Assert.assertEquals(message, msg.getMessageObject());
                latch1.countDown();
            }
        });
        ITopic<String> topic2 = instance2.getTopic(topicName);
        final CountDownLatch latch2 = new CountDownLatch(2);
        topic2.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message msg) {
                Assert.assertEquals(message, msg.getMessageObject());
                latch2.countDown();
            }
        });
        topic1.publish(message);
        HazelcastTestSupport.assertOpenEventually(latch1);
        instance1.shutdown();
        topic2.publish(message);
        HazelcastTestSupport.assertOpenEventually(latch2);
    }

    @Test
    public void testTopicStats() throws InterruptedException {
        String topicName = "testTopicStats" + (HazelcastTestSupport.generateRandomString(5));
        HazelcastInstance instance = createHazelcastInstance();
        ITopic<String> topic = instance.getTopic(topicName);
        final CountDownLatch latch1 = new CountDownLatch(1000);
        topic.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message msg) {
                latch1.countDown();
            }
        });
        final CountDownLatch latch2 = new CountDownLatch(1000);
        topic.addMessageListener(new com.hazelcast.core.MessageListener<String>() {
            public void onMessage(Message msg) {
                latch2.countDown();
            }
        });
        for (int i = 0; i < 1000; i++) {
            topic.publish("sancar");
        }
        Assert.assertTrue(latch1.await(1, TimeUnit.MINUTES));
        Assert.assertTrue(latch2.await(1, TimeUnit.MINUTES));
        LocalTopicStatsImpl stats = ((LocalTopicStatsImpl) (topic.getLocalTopicStats()));
        Assert.assertEquals(1000, stats.getPublishOperationCount());
        Assert.assertEquals(2000, stats.getReceiveOperationCount());
    }

    @Test
    @Category(NightlyTest.class)
    @SuppressWarnings("unchecked")
    public void testTopicMultiThreading() throws Exception {
        final int nodeCount = 5;
        final int count = 1000;
        final String randomTopicName = HazelcastTestSupport.randomString();
        Config config = new Config();
        config.getTopicConfig(randomTopicName).setGlobalOrderingEnabled(false);
        config.getTopicConfig(randomTopicName).setMultiThreadingEnabled(true);
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        final HazelcastInstance[] instances = factory.newInstances(config);
        final Set<String>[] threads = new Set[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            threads[i] = new HashSet<String>();
        }
        final CountDownLatch startLatch = new CountDownLatch(nodeCount);
        final CountDownLatch messageLatch = new CountDownLatch(((nodeCount * nodeCount) * count));
        final CountDownLatch publishLatch = new CountDownLatch((nodeCount * count));
        ExecutorService ex = Executors.newFixedThreadPool(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            final int finalI = i;
            ex.execute(new Runnable() {
                public void run() {
                    final Set<String> thNames = threads[finalI];
                    HazelcastInstance hz = instances[finalI];
                    ITopic<TopicTest.TestMessage> topic = hz.getTopic(randomTopicName);
                    topic.addMessageListener(new com.hazelcast.core.MessageListener<TopicTest.TestMessage>() {
                        public void onMessage(Message<TopicTest.TestMessage> message) {
                            thNames.add(Thread.currentThread().getName());
                            messageLatch.countDown();
                        }
                    });
                    startLatch.countDown();
                    try {
                        startLatch.await(1, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    Member localMember = hz.getCluster().getLocalMember();
                    for (int j = 0; j < count; j++) {
                        topic.publish(new TopicTest.TestMessage(localMember, UuidUtil.newUnsecureUuidString()));
                        publishLatch.countDown();
                    }
                }
            });
        }
        try {
            Assert.assertTrue(publishLatch.await(2, TimeUnit.MINUTES));
            Assert.assertTrue(messageLatch.await(5, TimeUnit.MINUTES));
            boolean passed = false;
            for (int i = 0; i < nodeCount; i++) {
                if ((threads[i].size()) > 1) {
                    passed = true;
                }
            }
            Assert.assertTrue("All listeners received messages in single thread. Expecting more threads involved", passed);
        } finally {
            ex.shutdownNow();
        }
    }

    @Test
    public void givenTopicHasNoSubscriber_whenMessageIsPublished_thenNoSerialializationIsInvoked() {
        final int nodeCount = 2;
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        final HazelcastInstance[] instances = factory.newInstances();
        ITopic<TopicTest.SerializationCounting> topic = instances[0].getTopic(HazelcastTestSupport.randomString());
        TopicTest.SerializationCounting message = new TopicTest.SerializationCounting();
        topic.publish(message);
        assertNoSerializationInvoked(message);
    }

    public static class SerializationCounting implements DataSerializable {
        private AtomicInteger counter = new AtomicInteger();

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            counter.incrementAndGet();
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
        }

        public int getSerializationCount() {
            return counter.get();
        }
    }
}
