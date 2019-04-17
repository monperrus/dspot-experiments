/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.api.java;


import org.apache.flink.api.common.Plan;
import org.apache.flink.api.common.operators.GenericDataSinkBase;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for multiple invocations of a plan.
 */
public class MultipleInvokationsTest {
    @Test
    public void testMultipleInvocationsGetPlan() {
        try {
            ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
            // ----------- Execution 1 ---------------
            DataSet<String> data = env.fromElements("Some", "test", "data").name("source1");
            // data.print();
            data.output(new org.apache.flink.api.java.io.DiscardingOutputFormat<String>()).name("print1");
            data.output(new org.apache.flink.api.java.io.DiscardingOutputFormat<String>()).name("output1");
            {
                Plan p = env.createProgramPlan();
                Assert.assertEquals(2, p.getDataSinks().size());
                for (GenericDataSinkBase<?> sink : p.getDataSinks()) {
                    Assert.assertTrue(((sink.getName().equals("print1")) || (sink.getName().equals("output1"))));
                    Assert.assertEquals("source1", sink.getInput().getName());
                }
            }
            // ----------- Execution 2 ---------------
            data.writeAsText("/some/file/path").name("textsink");
            {
                Plan p = env.createProgramPlan();
                Assert.assertEquals(1, p.getDataSinks().size());
                GenericDataSinkBase<?> sink = p.getDataSinks().iterator().next();
                Assert.assertEquals("textsink", sink.getName());
                Assert.assertEquals("source1", sink.getInput().getName());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
