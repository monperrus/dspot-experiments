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
package org.apache.camel.itest.karaf;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;


@RunWith(PaxExam.class)
public class CamelHazelcastTest extends BaseKarafTest {
    public static final String COMPONENT = extractName(CamelHazelcastTest.class);

    @Test
    public void test() throws Exception {
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-atomicvalue");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-instance");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-list");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-map");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-multimap");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-queue");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-replicatedmap");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-ringbuffer");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-seda");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-set");
        testComponent(CamelHazelcastTest.COMPONENT, "hazelcast-topic");
    }
}
