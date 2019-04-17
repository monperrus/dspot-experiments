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
package com.hazelcast.topic.impl.reliable;


import com.hazelcast.core.ITopic;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.spi.serialization.SerializationService;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import com.hazelcast.topic.TopicOverloadException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public abstract class TopicOverloadAbstractTest extends HazelcastTestSupport {
    protected ITopic<String> topic;

    protected Ringbuffer<ReliableTopicMessage> ringbuffer;

    protected SerializationService serializationService;

    @Test
    public void whenError_andSpace() throws Exception {
        test_whenSpace();
    }

    @Test
    public void whenDiscardNewest_andSpace() throws Exception {
        test_whenSpace();
    }

    @Test
    public void whenDiscardOldest_andSpace() throws Exception {
        test_whenSpace();
    }

    @Test
    public void test_whenSpace() throws Exception {
        topic.publish("foo");
        ReliableTopicMessage msg = ringbuffer.readOne(0);
        Assert.assertEquals("foo", serializationService.toObject(msg.getPayload()));
    }

    @Test
    public void whenError_andNoSpace() {
        for (int k = 0; k < (ringbuffer.capacity()); k++) {
            topic.publish("old");
        }
        long tail = ringbuffer.tailSequence();
        long head = ringbuffer.headSequence();
        try {
            topic.publish("new");
            Assert.fail();
        } catch (TopicOverloadException expected) {
            HazelcastTestSupport.ignore(expected);
        }
        Assert.assertEquals(tail, ringbuffer.tailSequence());
        Assert.assertEquals(head, ringbuffer.headSequence());
    }

    @Test
    public void whenDiscardOldest_whenNoSpace() {
        for (int k = 0; k < (ringbuffer.capacity()); k++) {
            topic.publish("old");
        }
        long tail = ringbuffer.tailSequence();
        long head = ringbuffer.headSequence();
        topic.publish("new");
        // check that an item has been added
        Assert.assertEquals((tail + 1), ringbuffer.tailSequence());
        Assert.assertEquals((head + 1), ringbuffer.headSequence());
    }

    @Test
    public void whenDiscardNewest_whenNoSpace() {
        for (int k = 0; k < (ringbuffer.capacity()); k++) {
            topic.publish("old");
        }
        long tail = ringbuffer.tailSequence();
        long head = ringbuffer.headSequence();
        topic.publish("new");
        // check that nothing has changed
        Assert.assertEquals(tail, ringbuffer.tailSequence());
        Assert.assertEquals(head, ringbuffer.headSequence());
    }

    @Test
    public void whenBlock_whenNoSpace() {
        for (int k = 0; k < (ringbuffer.capacity()); k++) {
            topic.publish("old");
        }
        final long tail = ringbuffer.tailSequence();
        final long head = ringbuffer.headSequence();
        // add the item
        final Future f = HazelcastTestSupport.spawn(new Runnable() {
            @Override
            public void run() {
                topic.publish("new");
            }
        });
        HazelcastTestSupport.assertTrueAllTheTime(new AssertTask() {
            @Override
            public void run() throws Exception {
                Assert.assertFalse(f.isDone());
                Assert.assertEquals(tail, ringbuffer.tailSequence());
                Assert.assertEquals(head, ringbuffer.headSequence());
            }
        }, 5);
    }
}
