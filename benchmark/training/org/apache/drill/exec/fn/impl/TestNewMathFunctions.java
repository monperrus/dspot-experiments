/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.fn.impl;


import ExecConstants.JSON_READER_NAN_INF_NUMBERS;
import ExecConstants.JSON_READ_NUMBERS_AS_DOUBLE;
import ParquetProperties.WriterVersion;
import java.io.File;
import java.math.BigDecimal;
import org.apache.commons.io.FileUtils;
import org.apache.drill.categories.OperatorTest;
import org.apache.drill.categories.UnlikelyTest;
import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.exec.ExecTest;
import org.apache.drill.exec.expr.fn.FunctionImplementationRegistry;
import org.apache.drill.exec.ops.FragmentContextImpl;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.test.BaseTestQuery;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category({ UnlikelyTest.class, OperatorTest.class })
public class TestNewMathFunctions extends BaseTestQuery {
    private final DrillConfig c = DrillConfig.create();

    private PhysicalPlanReader reader;

    private FunctionImplementationRegistry registry;

    private FragmentContextImpl context;

    @Test
    public void testTrigoMathFunc() throws Throwable {
        final Object[] expected = new Object[]{ Math.sin(45), Math.cos(45), Math.tan(45), Math.asin(45), Math.acos(45), Math.atan(45), Math.sinh(45), Math.cosh(45), Math.tanh(45) };
        runTest(expected, "functions/testTrigoMathFunctions.json");
    }

    @Test
    public void testExtendedMathFunc() throws Throwable {
        final BigDecimal d = new BigDecimal("100111111111111111111111111111111111.00000000000000000000000000000000000000000000000000001");
        final Object[] expected = new Object[]{ Math.cbrt(1000), Math.log(10), Math.log10(5), (Math.log(64.0)) / (Math.log(2.0)), Math.exp(10), Math.toDegrees(0.5), Math.toRadians(45.0), Math.PI, Math.cbrt(d.doubleValue()), Math.log(d.doubleValue()), (Math.log(d.doubleValue())) / (Math.log(2)), Math.exp(d.doubleValue()), Math.toDegrees(d.doubleValue()), Math.toRadians(d.doubleValue()) };
        runTest(expected, "functions/testExtendedMathFunctions.json");
    }

    @Test
    public void testTruncDivMod() throws Throwable {
        final Object[] expected = new Object[]{ 101.0, 0, 101, 1010.0, 101, 481.0, 0.001099999999931267 };
        runTest(expected, "functions/testDivModTruncFunctions.json");
    }

    @Test
    public void testIsNumeric() throws Throwable {
        final Object[] expected = new Object[]{ 1, 1, 1, 0 };
        runTest(expected, "functions/testIsNumericFunction.json");
    }

    @Test
    public void testLog10WithDouble() throws Throwable {
        String json = "{" + ((((((((("\"positive_infinity\" : Infinity," + "\"negative_infinity\" : -Infinity,") + "\"nan\" : NaN,") + "\"num1\": 0.0,") + "\"num2\": 0.1,") + "\"num3\": 1.0,") + "\"num4\": 1.5,") + "\"num5\": -1.5,") + "\"num6\": 10.0") + "}");
        String query = "select " + ((((((((("log10(positive_infinity) as pos_inf, " + "log10(negative_infinity) as neg_inf, ") + "log10(nan) as nan, ") + "log10(num1) as num1, ") + "log10(num2) as num2, ") + "log10(num3) as num3, ") + "log10(num4) as num4, ") + "log10(num5) as num5, ") + "log10(num6) as num6 ") + "from dfs.`data.json`");
        File file = new File(ExecTest.dirTestWatcher.getRootDir(), "data.json");
        try {
            FileUtils.writeStringToFile(file, json);
            BaseTestQuery.setSessionOption(JSON_READ_NUMBERS_AS_DOUBLE, true);
            BaseTestQuery.setSessionOption(JSON_READER_NAN_INF_NUMBERS, true);
            BaseTestQuery.testBuilder().sqlQuery(query).ordered().baselineColumns("pos_inf", "neg_inf", "nan", "num1", "num2", "num3", "num4", "num5", "num6").baselineValues(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY, (-1.0), 0.0, 0.17609125905568124, Double.NaN, 1.0).go();
        } finally {
            BaseTestQuery.resetSessionOption(JSON_READ_NUMBERS_AS_DOUBLE);
            BaseTestQuery.resetSessionOption(JSON_READER_NAN_INF_NUMBERS);
            FileUtils.deleteQuietly(file);
        }
    }

    @Test
    public void testLog10WithFloat() throws Throwable {
        String json = "{" + ((((((((("\"positive_infinity\" : Infinity," + "\"negative_infinity\" : -Infinity,") + "\"nan\" : NaN,") + "\"num1\": 0.0,") + "\"num2\": 0.1,") + "\"num3\": 1.0,") + "\"num4\": 1.5,") + "\"num5\": -1.5,") + "\"num6\": 10.0") + "}");
        String query = "select " + ((((((((("log10(cast(positive_infinity as float)) as pos_inf, " + "log10(cast(negative_infinity as float)) as neg_inf, ") + "log10(cast(nan as float)) as nan, ") + "log10(cast(num1 as float)) as num1, ") + "log10(cast(num2 as float)) as num2, ") + "log10(cast(num3 as float)) as num3, ") + "log10(cast(num4 as float)) as num4, ") + "log10(cast(num5 as float)) as num5, ") + "log10(cast(num6 as float)) as num6 ") + "from dfs.`data.json`");
        File file = new File(ExecTest.dirTestWatcher.getRootDir(), "data.json");
        try {
            FileUtils.writeStringToFile(file, json);
            BaseTestQuery.setSessionOption(JSON_READER_NAN_INF_NUMBERS, true);
            BaseTestQuery.testBuilder().sqlQuery(query).ordered().baselineColumns("pos_inf", "neg_inf", "nan", "num1", "num2", "num3", "num4", "num5", "num6").baselineValues(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, Double.NEGATIVE_INFINITY, (-0.999999993528508), 0.0, 0.17609125905568124, Double.NaN, 1.0).go();
        } finally {
            BaseTestQuery.resetSessionOption(JSON_READER_NAN_INF_NUMBERS);
            FileUtils.deleteQuietly(file);
        }
    }

    @Test
    public void testLog10WithInt() throws Throwable {
        String json = "{" + (((("\"num1\": 0.0," + "\"num3\": 1.0,") + "\"num5\": -1.0,") + "\"num6\": 10.0") + "}");
        String query = "select " + (((("log10(cast(num1 as int)) as num1, " + "log10(cast(num3 as int)) as num3, ") + "log10(cast(num5 as int)) as num5, ") + "log10(cast(num6 as int)) as num6 ") + "from dfs.`data.json`");
        File file = new File(ExecTest.dirTestWatcher.getRootDir(), "data.json");
        try {
            FileUtils.writeStringToFile(file, json);
            BaseTestQuery.setSessionOption(JSON_READER_NAN_INF_NUMBERS, true);
            BaseTestQuery.testBuilder().sqlQuery(query).ordered().baselineColumns("num1", "num3", "num5", "num6").baselineValues(Double.NEGATIVE_INFINITY, 0.0, Double.NaN, 1.0).go();
        } finally {
            BaseTestQuery.resetSessionOption(JSON_READER_NAN_INF_NUMBERS);
            FileUtils.deleteQuietly(file);
        }
    }

    @Test
    public void testLog10WithBigInt() throws Throwable {
        String json = "{" + (((("\"num1\": 0," + "\"num3\": 1,") + "\"num5\": -1,") + "\"num6\": 10") + "}");
        String query = "select " + (((("log10(num1) as num1, " + "log10(num3) as num3, ") + "log10(num5) as num5, ") + "log10(num6) as num6 ") + "from dfs.`data.json`");
        File file = new File(ExecTest.dirTestWatcher.getRootDir(), "data.json");
        try {
            FileUtils.writeStringToFile(file, json);
            BaseTestQuery.setSessionOption(JSON_READER_NAN_INF_NUMBERS, true);
            BaseTestQuery.testBuilder().sqlQuery(query).ordered().baselineColumns("num1", "num3", "num5", "num6").baselineValues(Double.NEGATIVE_INFINITY, 0.0, Double.NaN, 1.0).go();
        } finally {
            BaseTestQuery.resetSessionOption(JSON_READER_NAN_INF_NUMBERS);
            FileUtils.deleteQuietly(file);
        }
    }

    @Test
    public void testLog10WithUint4() throws Exception {
        Path file = new Path(ExecTest.dirTestWatcher.getRootDir().toString(), "uint8.parquet");
        String schemaText = "message test { required int32 val(UINT_8); }";
        Configuration conf = new Configuration();
        MessageType schema = MessageTypeParser.parseMessageType(schemaText);
        GroupWriteSupport.setSchema(schema, conf);
        SimpleGroupFactory groupFactory = new SimpleGroupFactory(schema);
        try {
            try (ParquetWriter<Group> writer = new ParquetWriter(file, new GroupWriteSupport(), CompressionCodecName.UNCOMPRESSED, 1024, 1024, 512, true, false, WriterVersion.PARQUET_1_0, conf)) {
                writer.write(groupFactory.newGroup().append("val", 0));
                writer.write(groupFactory.newGroup().append("val", 1));
                writer.write(groupFactory.newGroup().append("val", (-1)));
                writer.write(groupFactory.newGroup().append("val", 10));
            }
            String query = "select log10(val) as col from dfs.`uint8.parquet`";
            BaseTestQuery.testBuilder().sqlQuery(query).unOrdered().baselineColumns("col").baselineValues(Double.NEGATIVE_INFINITY).baselineValues(0.0).baselineValues(Double.NaN).baselineValues(1.0).build().run();
        } finally {
            FileUtils.deleteQuietly(new File(file.toString()));
        }
    }
}
