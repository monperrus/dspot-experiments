/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.trogdor.common;


import Coordinator.DEFAULT_PORT;
import Platform.Config.TROGDOR_AGENT_PORT;
import Platform.Config.TROGDOR_COORDINATOR_PORT;
import Topology.Util;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import org.apache.kafka.trogdor.basic.BasicNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;


public class TopologyTest {
    @Rule
    public final Timeout globalTimeout = Timeout.millis(120000);

    @Test
    public void testAgentNodeNames() throws Exception {
        TreeMap<String, Node> nodes = new TreeMap<>();
        final int numNodes = 5;
        for (int i = 0; i < numNodes; i++) {
            HashMap<String, String> conf = new HashMap<>();
            if (i == 0) {
                conf.put(TROGDOR_COORDINATOR_PORT, String.valueOf(DEFAULT_PORT));
            } else {
                conf.put(TROGDOR_AGENT_PORT, String.valueOf(Agent.DEFAULT_PORT));
            }
            BasicNode node = new BasicNode(String.format("node%02d", i), String.format("node%d.example.com", i), conf, new HashSet<String>());
            nodes.put(node.name(), node);
        }
        Topology topology = new org.apache.kafka.trogdor.basic.BasicTopology(nodes);
        Set<String> names = Util.agentNodeNames(topology);
        Assert.assertEquals(4, names.size());
        for (int i = 1; i < (numNodes - 1); i++) {
            Assert.assertTrue(names.contains(String.format("node%02d", i)));
        }
    }
}
