/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.api.graph;


import ResourceSpec.DEFAULT;
import ResultPartitionType.PIPELINED_BOUNDED;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.operators.ResourceSpec;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.tasks.JobCheckpointingSettings;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link StreamingJobGraphGenerator}.
 */
@SuppressWarnings("serial")
public class StreamingJobGraphGeneratorTest extends TestLogger {
    @Test
    public void testParallelismOneNotChained() {
        // --------- the program ---------
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<Tuple2<String, String>> input = env.fromElements("a", "b", "c", "d", "e", "f").map(new org.apache.flink.api.common.functions.MapFunction<String, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> map(String value) {
                return new Tuple2(value, value);
            }
        });
        DataStream<Tuple2<String, String>> result = input.keyBy(0).map(new org.apache.flink.api.common.functions.MapFunction<Tuple2<String, String>, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> map(Tuple2<String, String> value) {
                return value;
            }
        });
        result.addSink(new org.apache.flink.streaming.api.functions.sink.SinkFunction<Tuple2<String, String>>() {
            @Override
            public void invoke(Tuple2<String, String> value) {
            }
        });
        // --------- the job graph ---------
        StreamGraph streamGraph = env.getStreamGraph();
        streamGraph.setJobName("test job");
        JobGraph jobGraph = streamGraph.getJobGraph();
        List<JobVertex> verticesSorted = jobGraph.getVerticesSortedTopologicallyFromSources();
        Assert.assertEquals(2, jobGraph.getNumberOfVertices());
        Assert.assertEquals(1, verticesSorted.get(0).getParallelism());
        Assert.assertEquals(1, verticesSorted.get(1).getParallelism());
        JobVertex sourceVertex = verticesSorted.get(0);
        JobVertex mapSinkVertex = verticesSorted.get(1);
        Assert.assertEquals(PIPELINED_BOUNDED, sourceVertex.getProducedDataSets().get(0).getResultType());
        Assert.assertEquals(PIPELINED_BOUNDED, mapSinkVertex.getInputs().get(0).getSource().getResultType());
    }

    /**
     * Tests that disabled checkpointing sets the checkpointing interval to Long.MAX_VALUE.
     */
    @Test
    public void testDisabledCheckpointing() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamGraph streamGraph = new StreamGraph(env);
        Assert.assertFalse("Checkpointing enabled", streamGraph.getCheckpointConfig().isCheckpointingEnabled());
        JobGraph jobGraph = StreamingJobGraphGenerator.createJobGraph(streamGraph);
        JobCheckpointingSettings snapshottingSettings = jobGraph.getCheckpointingSettings();
        Assert.assertEquals(Long.MAX_VALUE, snapshottingSettings.getCheckpointCoordinatorConfiguration().getCheckpointInterval());
    }

    /**
     * Verifies that the chain start/end is correctly set.
     */
    @Test
    public void testChainStartEndSetting() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // fromElements -> CHAIN(Map -> Print)
        env.fromElements(1, 2, 3).map(new org.apache.flink.api.common.functions.MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer value) throws Exception {
                return value;
            }
        }).print();
        JobGraph jobGraph = StreamingJobGraphGenerator.createJobGraph(env.getStreamGraph());
        List<JobVertex> verticesSorted = jobGraph.getVerticesSortedTopologicallyFromSources();
        JobVertex sourceVertex = verticesSorted.get(0);
        JobVertex mapPrintVertex = verticesSorted.get(1);
        Assert.assertEquals(PIPELINED_BOUNDED, sourceVertex.getProducedDataSets().get(0).getResultType());
        Assert.assertEquals(PIPELINED_BOUNDED, mapPrintVertex.getInputs().get(0).getSource().getResultType());
        StreamConfig sourceConfig = new StreamConfig(sourceVertex.getConfiguration());
        StreamConfig mapConfig = new StreamConfig(mapPrintVertex.getConfiguration());
        Map<Integer, StreamConfig> chainedConfigs = mapConfig.getTransitiveChainedTaskConfigs(getClass().getClassLoader());
        StreamConfig printConfig = chainedConfigs.values().iterator().next();
        Assert.assertTrue(sourceConfig.isChainStart());
        Assert.assertTrue(sourceConfig.isChainEnd());
        Assert.assertTrue(mapConfig.isChainStart());
        Assert.assertFalse(mapConfig.isChainEnd());
        Assert.assertFalse(printConfig.isChainStart());
        Assert.assertTrue(printConfig.isChainEnd());
    }

    /**
     * Verifies that the resources are merged correctly for chained operators (covers source and sink cases)
     * when generating job graph.
     */
    @Test
    public void testResourcesForChainedSourceSink() throws Exception {
        ResourceSpec resource1 = ResourceSpec.newBuilder().setCpuCores(0.1).setHeapMemoryInMB(100).build();
        ResourceSpec resource2 = ResourceSpec.newBuilder().setCpuCores(0.2).setHeapMemoryInMB(200).build();
        ResourceSpec resource3 = ResourceSpec.newBuilder().setCpuCores(0.3).setHeapMemoryInMB(300).build();
        ResourceSpec resource4 = ResourceSpec.newBuilder().setCpuCores(0.4).setHeapMemoryInMB(400).build();
        ResourceSpec resource5 = ResourceSpec.newBuilder().setCpuCores(0.5).setHeapMemoryInMB(500).build();
        Method opMethod = SingleOutputStreamOperator.class.getDeclaredMethod("setResources", ResourceSpec.class);
        opMethod.setAccessible(true);
        Method sinkMethod = DataStreamSink.class.getDeclaredMethod("setResources", ResourceSpec.class);
        sinkMethod.setAccessible(true);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Tuple2<Integer, Integer>> source = env.addSource(new org.apache.flink.streaming.api.functions.source.ParallelSourceFunction<Tuple2<Integer, Integer>>() {
            @Override
            public void run(SourceContext<Tuple2<Integer, Integer>> ctx) throws Exception {
            }

            @Override
            public void cancel() {
            }
        });
        opMethod.invoke(source, resource1);
        DataStream<Tuple2<Integer, Integer>> map = source.map(new org.apache.flink.api.common.functions.MapFunction<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> map(Tuple2<Integer, Integer> value) throws Exception {
                return value;
            }
        });
        opMethod.invoke(map, resource2);
        // CHAIN(Source -> Map -> Filter)
        DataStream<Tuple2<Integer, Integer>> filter = map.filter(new org.apache.flink.api.common.functions.FilterFunction<Tuple2<Integer, Integer>>() {
            @Override
            public boolean filter(Tuple2<Integer, Integer> value) throws Exception {
                return false;
            }
        });
        opMethod.invoke(filter, resource3);
        DataStream<Tuple2<Integer, Integer>> reduce = filter.keyBy(0).reduce(new ReduceFunction<Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> reduce(Tuple2<Integer, Integer> value1, Tuple2<Integer, Integer> value2) throws Exception {
                return new Tuple2(value1.f0, ((value1.f1) + (value2.f1)));
            }
        });
        opMethod.invoke(reduce, resource4);
        DataStreamSink<Tuple2<Integer, Integer>> sink = reduce.addSink(new org.apache.flink.streaming.api.functions.sink.SinkFunction<Tuple2<Integer, Integer>>() {
            @Override
            public void invoke(Tuple2<Integer, Integer> value) throws Exception {
            }
        });
        sinkMethod.invoke(sink, resource5);
        JobGraph jobGraph = StreamingJobGraphGenerator.createJobGraph(env.getStreamGraph());
        JobVertex sourceMapFilterVertex = jobGraph.getVerticesSortedTopologicallyFromSources().get(0);
        JobVertex reduceSinkVertex = jobGraph.getVerticesSortedTopologicallyFromSources().get(1);
        Assert.assertTrue(sourceMapFilterVertex.getMinResources().equals(resource1.merge(resource2).merge(resource3)));
        Assert.assertTrue(reduceSinkVertex.getPreferredResources().equals(resource4.merge(resource5)));
    }

    /**
     * Verifies that the resources are merged correctly for chained operators (covers middle chaining and iteration cases)
     * when generating job graph.
     */
    @Test
    public void testResourcesForIteration() throws Exception {
        ResourceSpec resource1 = ResourceSpec.newBuilder().setCpuCores(0.1).setHeapMemoryInMB(100).build();
        ResourceSpec resource2 = ResourceSpec.newBuilder().setCpuCores(0.2).setHeapMemoryInMB(200).build();
        ResourceSpec resource3 = ResourceSpec.newBuilder().setCpuCores(0.3).setHeapMemoryInMB(300).build();
        ResourceSpec resource4 = ResourceSpec.newBuilder().setCpuCores(0.4).setHeapMemoryInMB(400).build();
        ResourceSpec resource5 = ResourceSpec.newBuilder().setCpuCores(0.5).setHeapMemoryInMB(500).build();
        Method opMethod = SingleOutputStreamOperator.class.getDeclaredMethod("setResources", ResourceSpec.class);
        opMethod.setAccessible(true);
        Method sinkMethod = DataStreamSink.class.getDeclaredMethod("setResources", ResourceSpec.class);
        sinkMethod.setAccessible(true);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Integer> source = env.addSource(new org.apache.flink.streaming.api.functions.source.ParallelSourceFunction<Integer>() {
            @Override
            public void run(SourceContext<Integer> ctx) throws Exception {
            }

            @Override
            public void cancel() {
            }
        }).name("test_source");
        opMethod.invoke(source, resource1);
        IterativeStream<Integer> iteration = source.iterate(3000);
        opMethod.invoke(iteration, resource2);
        DataStream<Integer> flatMap = iteration.flatMap(new org.apache.flink.api.common.functions.FlatMapFunction<Integer, Integer>() {
            @Override
            public void flatMap(Integer value, Collector<Integer> out) throws Exception {
                out.collect(value);
            }
        }).name("test_flatMap");
        opMethod.invoke(flatMap, resource3);
        // CHAIN(flatMap -> Filter)
        DataStream<Integer> increment = flatMap.filter(new org.apache.flink.api.common.functions.FilterFunction<Integer>() {
            @Override
            public boolean filter(Integer value) throws Exception {
                return false;
            }
        }).name("test_filter");
        opMethod.invoke(increment, resource4);
        DataStreamSink<Integer> sink = iteration.closeWith(increment).addSink(new org.apache.flink.streaming.api.functions.sink.SinkFunction<Integer>() {
            @Override
            public void invoke(Integer value) throws Exception {
            }
        }).disableChaining().name("test_sink");
        sinkMethod.invoke(sink, resource5);
        JobGraph jobGraph = StreamingJobGraphGenerator.createJobGraph(env.getStreamGraph());
        for (JobVertex jobVertex : jobGraph.getVertices()) {
            if (jobVertex.getName().contains("test_source")) {
                Assert.assertTrue(jobVertex.getMinResources().equals(resource1));
            } else
                if (jobVertex.getName().contains("Iteration_Source")) {
                    Assert.assertTrue(jobVertex.getPreferredResources().equals(resource2));
                } else
                    if (jobVertex.getName().contains("test_flatMap")) {
                        Assert.assertTrue(jobVertex.getMinResources().equals(resource3.merge(resource4)));
                    } else
                        if (jobVertex.getName().contains("Iteration_Tail")) {
                            Assert.assertTrue(jobVertex.getPreferredResources().equals(DEFAULT));
                        } else
                            if (jobVertex.getName().contains("test_sink")) {
                                Assert.assertTrue(jobVertex.getMinResources().equals(resource5));
                            }




        }
    }
}
