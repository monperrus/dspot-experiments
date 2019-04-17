/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.adapter.druid;


import SqlStdOperatorTable.BETWEEN;
import SqlStdOperatorTable.IN;
import SqlTypeName.BOOLEAN;
import SqlTypeName.VARCHAR;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Tests generating Druid filters.
 */
public class DruidQueryFilterTest {
    private DruidQuery druidQuery;

    @Test
    public void testInFilter() throws IOException {
        final DruidQueryFilterTest.Fixture f = new DruidQueryFilterTest.Fixture();
        final List<? extends RexNode> listRexNodes = ImmutableList.of(f.rexBuilder.makeInputRef(f.varcharRowType, 0), f.rexBuilder.makeExactLiteral(BigDecimal.valueOf(1)), f.rexBuilder.makeExactLiteral(BigDecimal.valueOf(5)), f.rexBuilder.makeLiteral("value1"));
        RexNode inRexNode = f.rexBuilder.makeCall(IN, listRexNodes);
        DruidJsonFilter returnValue = DruidJsonFilter.toDruidFilters(inRexNode, f.varcharRowType, druidQuery);
        Assert.assertNotNull("Filter is null", returnValue);
        JsonFactory jsonFactory = new JsonFactory();
        final StringWriter sw = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(sw);
        returnValue.write(jsonGenerator);
        jsonGenerator.close();
        Assert.assertThat(sw.toString(), Is.is(("{\"type\":\"in\",\"dimension\":\"dimensionName\"," + "\"values\":[\"1\",\"5\",\"value1\"]}")));
    }

    @Test
    public void testBetweenFilterStringCase() throws IOException {
        final DruidQueryFilterTest.Fixture f = new DruidQueryFilterTest.Fixture();
        final List<RexNode> listRexNodes = ImmutableList.of(f.rexBuilder.makeLiteral(false), f.rexBuilder.makeInputRef(f.varcharRowType, 0), f.rexBuilder.makeLiteral("lower-bound"), f.rexBuilder.makeLiteral("upper-bound"));
        RelDataType relDataType = f.typeFactory.createSqlType(BOOLEAN);
        RexNode betweenRexNode = f.rexBuilder.makeCall(relDataType, BETWEEN, listRexNodes);
        DruidJsonFilter returnValue = DruidJsonFilter.toDruidFilters(betweenRexNode, f.varcharRowType, druidQuery);
        Assert.assertNotNull("Filter is null", returnValue);
        JsonFactory jsonFactory = new JsonFactory();
        final StringWriter sw = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(sw);
        returnValue.write(jsonGenerator);
        jsonGenerator.close();
        Assert.assertThat(sw.toString(), Is.is(("{\"type\":\"bound\",\"dimension\":\"dimensionName\",\"lower\":\"lower-bound\"," + ("\"lowerStrict\":false,\"upper\":\"upper-bound\",\"upperStrict\":false," + "\"ordering\":\"lexicographic\"}"))));
    }

    /**
     * Everything a test needs for a healthy, active life.
     */
    static class Fixture {
        final JavaTypeFactoryImpl typeFactory = new JavaTypeFactoryImpl(RelDataTypeSystem.DEFAULT);

        final RexBuilder rexBuilder = new RexBuilder(typeFactory);

        final DruidTable druidTable = new DruidTable(Mockito.mock(DruidSchema.class), "dataSource", null, ImmutableSet.of(), "timestamp", null, null, null);

        final RelDataType varcharType = typeFactory.createSqlType(VARCHAR);

        final RelDataType varcharRowType = typeFactory.builder().add("dimensionName", varcharType).build();
    }
}

/**
 * End DruidQueryFilterTest.java
 */