/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.query.aggregation.momentsketch.aggregator;


import Granularities.NONE;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import org.apache.druid.data.input.Row;
import org.apache.druid.initialization.DruidModule;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.apache.druid.java.util.common.guava.Sequence;
import org.apache.druid.query.aggregation.AggregationTestHelper;
import org.apache.druid.query.aggregation.momentsketch.MomentSketchModule;
import org.apache.druid.query.aggregation.momentsketch.MomentSketchWrapper;
import org.apache.druid.query.groupby.GroupByQueryConfig;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class MomentsSketchAggregatorTest {
    private final AggregationTestHelper helper;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    public MomentsSketchAggregatorTest(final GroupByQueryConfig config) {
        MomentSketchModule.registerSerde();
        DruidModule module = new MomentSketchModule();
        helper = AggregationTestHelper.createGroupByQueryAggregationTestHelper(module.getJacksonModules(), config, tempFolder);
    }

    // this is to test Json properties and equals
    @Test
    public void serializeDeserializeFactoryWithFieldName() throws Exception {
        ObjectMapper objectMapper = new DefaultObjectMapper();
        MomentSketchAggregatorFactory factory = new MomentSketchAggregatorFactory("name", "fieldName", 128, true);
        MomentSketchAggregatorFactory other = objectMapper.readValue(objectMapper.writeValueAsString(factory), MomentSketchAggregatorFactory.class);
        Assert.assertEquals(factory, other);
    }

    @Test
    public void buildingSketchesAtIngestionTime() throws Exception {
        Sequence<Row> seq = // minTimestamp
        // maxRowCount
        helper.createIndexAndRunQueryOnSegment(new File(this.getClass().getClassLoader().getResource("doubles_build_data.tsv").getFile()), String.join("\n", "{", "  \"type\": \"string\",", "  \"parseSpec\": {", "    \"format\": \"tsv\",", "    \"timestampSpec\": {\"column\": \"timestamp\", \"format\": \"yyyyMMddHH\"},", "    \"dimensionsSpec\": {", "      \"dimensions\": [\"product\"],", "      \"dimensionExclusions\": [ \"sequenceNumber\"],", "      \"spatialDimensions\": []", "    },", "    \"columns\": [\"timestamp\", \"sequenceNumber\", \"product\", \"value\"]", "  }", "}"), "[{\"type\": \"momentSketch\", \"name\": \"sketch\", \"fieldName\": \"value\", \"k\": 10, \"compress\": true}]", 0, NONE, 10, String.join("\n", "{", "  \"queryType\": \"groupBy\",", "  \"dataSource\": \"test_datasource\",", "  \"granularity\": \"ALL\",", "  \"dimensions\": [],", "  \"aggregations\": [", "    {\"type\": \"momentSketchMerge\", \"name\": \"sketch\", \"fieldName\": \"sketch\", \"k\": 10, \"compress\": true}", "  ],", "  \"postAggregations\": [", "    {\"type\": \"momentSketchSolveQuantiles\", \"name\": \"quantiles\", \"fractions\": [0, 0.5, 1], \"field\": {\"type\": \"fieldAccess\", \"fieldName\": \"sketch\"}},", "    {\"type\": \"momentSketchMin\", \"name\": \"min\", \"field\": {\"type\": \"fieldAccess\", \"fieldName\": \"sketch\"}},", "    {\"type\": \"momentSketchMax\", \"name\": \"max\", \"field\": {\"type\": \"fieldAccess\", \"fieldName\": \"sketch\"}}", "  ],", "  \"intervals\": [\"2016-01-01T00:00:00.000Z/2016-01-31T00:00:00.000Z\"]", "}"));
        List<Row> results = seq.toList();
        Assert.assertEquals(1, results.size());
        Row row = results.get(0);
        double[] quantilesArray = ((double[]) (row.getRaw("quantiles")));
        Assert.assertEquals(0, quantilesArray[0], 0.05);
        Assert.assertEquals(0.5, quantilesArray[1], 0.05);
        Assert.assertEquals(1.0, quantilesArray[2], 0.05);
        Double minValue = ((Double) (row.getRaw("min")));
        Assert.assertEquals(0.0011, minValue, 1.0E-4);
        Double maxValue = ((Double) (row.getRaw("max")));
        Assert.assertEquals(0.9969, maxValue, 1.0E-4);
        MomentSketchWrapper sketchObject = ((MomentSketchWrapper) (row.getRaw("sketch")));
        Assert.assertEquals(400.0, sketchObject.getPowerSums()[0], 1.0E-10);
    }

    @Test
    public void buildingSketchesAtQueryTime() throws Exception {
        Sequence<Row> seq = // minTimestamp
        // maxRowCount
        helper.createIndexAndRunQueryOnSegment(new File(this.getClass().getClassLoader().getResource("doubles_build_data.tsv").getFile()), String.join("\n", "{", "  \"type\": \"string\",", "  \"parseSpec\": {", "    \"format\": \"tsv\",", "    \"timestampSpec\": {\"column\": \"timestamp\", \"format\": \"yyyyMMddHH\"},", "    \"dimensionsSpec\": {", "      \"dimensions\": [ \"product\"],", "      \"dimensionExclusions\": [\"sequenceNumber\"],", "      \"spatialDimensions\": []", "    },", "    \"columns\": [\"timestamp\", \"sequenceNumber\", \"product\", \"value\"]", "  }", "}"), "[{\"type\": \"doubleSum\", \"name\": \"value\", \"fieldName\": \"value\"}]", 0, NONE, 10, String.join("\n", "{", "  \"queryType\": \"groupBy\",", "  \"dataSource\": \"test_datasource\",", "  \"granularity\": \"ALL\",", "  \"dimensions\": [],", "  \"aggregations\": [", "    {\"type\": \"momentSketch\", \"name\": \"sketch\", \"fieldName\": \"value\", \"k\": 10}", "  ],", "  \"intervals\": [\"2016-01-01T00:00:00.000Z/2016-01-31T00:00:00.000Z\"]", "}"));
        List<Row> results = seq.toList();
        Assert.assertEquals(1, results.size());
        Row row = results.get(0);
        MomentSketchWrapper sketchObject = ((MomentSketchWrapper) (row.getRaw("sketch")));
        // 9 total products since we pre-sum the values.
        Assert.assertEquals(9.0, sketchObject.getPowerSums()[0], 1.0E-10);
    }
}
