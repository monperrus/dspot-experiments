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
package org.apache.trevni.avro;


import Schema.Type.BOOLEAN;
import Schema.Type.BYTES;
import Schema.Type.DOUBLE;
import Schema.Type.FLOAT;
import Schema.Type.INT;
import Schema.Type.LONG;
import Schema.Type.NULL;
import Schema.Type.STRING;
import java.io.File;
import java.util.Arrays;
import org.apache.avro.Schema;
import org.apache.avro.util.RandomData;
import org.apache.trevni.ColumnMetaData;
import org.apache.trevni.ValueType;
import org.junit.Test;


public class TestShredder {
    private static final long SEED = System.currentTimeMillis();

    private static final int COUNT = 100;

    private static final File FILE = new File("target", "test.trv");

    @Test
    public void testPrimitives() throws Exception {
        check(Schema.create(NULL), new ColumnMetaData("null", ValueType.NULL));
        check(Schema.create(BOOLEAN), new ColumnMetaData("boolean", ValueType.BOOLEAN));
        check(Schema.create(INT), new ColumnMetaData("int", ValueType.INT));
        check(Schema.create(LONG), new ColumnMetaData("long", ValueType.LONG));
        check(Schema.create(FLOAT), new ColumnMetaData("float", ValueType.FLOAT));
        check(Schema.create(DOUBLE), new ColumnMetaData("double", ValueType.DOUBLE));
        check(Schema.create(BYTES), new ColumnMetaData("bytes", ValueType.BYTES));
        check(Schema.create(STRING), new ColumnMetaData("string", ValueType.STRING));
        check(Schema.createEnum("E", null, null, Arrays.asList("X", "Y", "Z")), new ColumnMetaData("E", ValueType.INT));
        check(Schema.createFixed("F", null, null, 5), new ColumnMetaData("F", ValueType.BYTES));
    }

    private static final String SIMPLE_FIELDS = "{\"name\":\"x\",\"type\":\"int\"}," + "{\"name\":\"y\",\"type\":\"string\"}";

    private static final String SIMPLE_RECORD = ("{\"type\":\"record\",\"name\":\"R\",\"fields\":[" + (TestShredder.SIMPLE_FIELDS)) + "]}";

    @Test
    public void testSimpleRecord() throws Exception {
        check(Schema.parse(TestShredder.SIMPLE_RECORD), new ColumnMetaData("x", ValueType.INT), new ColumnMetaData("y", ValueType.STRING));
    }

    @Test
    public void testDefaultValue() throws Exception {
        String s = (((((("{\"type\":\"record\",\"name\":\"R\",\"fields\":[" + (TestShredder.SIMPLE_FIELDS)) + ",") + "{\"name\":\"z\",\"type\":\"int\",") + "\"default\":1,\"") + (RandomData.USE_DEFAULT)) + "\":true}") + "]}";
        checkWrite(Schema.parse(TestShredder.SIMPLE_RECORD));
        checkRead(Schema.parse(s));
    }

    @Test
    public void testNestedRecord() throws Exception {
        String s = (((("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + ("{\"name\":\"x\",\"type\":\"int\"}," + "{\"name\":\"R\",\"type\":")) + (TestShredder.SIMPLE_RECORD)) + "},") + "{\"name\":\"y\",\"type\":\"string\"}") + "]}";
        check(Schema.parse(s), new ColumnMetaData("x", ValueType.INT), new ColumnMetaData("R#x", ValueType.INT), new ColumnMetaData("R#y", ValueType.STRING), new ColumnMetaData("y", ValueType.STRING));
    }

    @Test
    public void testNamedRecord() throws Exception {
        String s = (((("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + "{\"name\":\"R1\",\"type\":") + (TestShredder.SIMPLE_RECORD)) + "},") + "{\"name\":\"R2\",\"type\":\"R\"}") + "]}";
        check(Schema.parse(s), new ColumnMetaData("R1#x", ValueType.INT), new ColumnMetaData("R1#y", ValueType.STRING), new ColumnMetaData("R2#x", ValueType.INT), new ColumnMetaData("R2#y", ValueType.STRING));
    }

    @Test
    public void testSimpleArray() throws Exception {
        String s = "{\"type\":\"array\",\"items\":\"long\"}";
        check(Schema.parse(s), isArray(true));
    }

    private static final String RECORD_ARRAY = ("{\"type\":\"array\",\"items\":" + (TestShredder.SIMPLE_RECORD)) + "}";

    @Test
    public void testArray() throws Exception {
        ColumnMetaData p = new ColumnMetaData("[]", ValueType.NULL).isArray(true);
        check(Schema.parse(TestShredder.RECORD_ARRAY), p, new ColumnMetaData("[]#x", ValueType.INT).setParent(p), new ColumnMetaData("[]#y", ValueType.STRING).setParent(p));
    }

    @Test
    public void testSimpleUnion() throws Exception {
        String s = "[\"int\",\"string\"]";
        check(Schema.parse(s), isArray(true), isArray(true));
    }

    @Test
    public void testSimpleOptional() throws Exception {
        String s = "[\"null\",\"string\"]";
        check(Schema.parse(s), isArray(true));
    }

    private static final String UNION = ("[\"null\",\"int\"," + (TestShredder.SIMPLE_RECORD)) + "]";

    @Test
    public void testUnion() throws Exception {
        ColumnMetaData p = new ColumnMetaData("R", ValueType.NULL).isArray(true);
        check(Schema.parse(TestShredder.UNION), isArray(true), p, new ColumnMetaData("R#x", ValueType.INT).setParent(p), new ColumnMetaData("R#y", ValueType.STRING).setParent(p));
    }

    @Test
    public void testNestedArray() throws Exception {
        String s = (((("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + ("{\"name\":\"x\",\"type\":\"int\"}," + "{\"name\":\"A\",\"type\":")) + (TestShredder.RECORD_ARRAY)) + "},") + "{\"name\":\"y\",\"type\":\"string\"}") + "]}";
        ColumnMetaData p = new ColumnMetaData("A[]", ValueType.NULL).isArray(true);
        check(Schema.parse(s), new ColumnMetaData("x", ValueType.INT), p, new ColumnMetaData("A[]#x", ValueType.INT).setParent(p), new ColumnMetaData("A[]#y", ValueType.STRING).setParent(p), new ColumnMetaData("y", ValueType.STRING));
    }

    @Test
    public void testNestedUnion() throws Exception {
        String s = (((("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + ("{\"name\":\"x\",\"type\":\"int\"}," + "{\"name\":\"u\",\"type\":")) + (TestShredder.UNION)) + "},") + "{\"name\":\"y\",\"type\":\"string\"}") + "]}";
        ColumnMetaData p = new ColumnMetaData("u/R", ValueType.NULL).isArray(true);
        check(Schema.parse(s), new ColumnMetaData("x", ValueType.INT), isArray(true), p, new ColumnMetaData("u/R#x", ValueType.INT).setParent(p), new ColumnMetaData("u/R#y", ValueType.STRING).setParent(p), new ColumnMetaData("y", ValueType.STRING));
    }

    @Test
    public void testUnionInArray() throws Exception {
        String s = ((("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + "{\"name\":\"a\",\"type\":{\"type\":\"array\",\"items\":") + (TestShredder.UNION)) + "}}") + "]}";
        ColumnMetaData p = new ColumnMetaData("a[]", ValueType.NULL).isArray(true);
        ColumnMetaData r = new ColumnMetaData("a[]/R", ValueType.NULL).setParent(p).isArray(true);
        check(Schema.parse(s), p, isArray(true), r, new ColumnMetaData("a[]/R#x", ValueType.INT).setParent(r), new ColumnMetaData("a[]/R#y", ValueType.STRING).setParent(r));
    }

    @Test
    public void testArrayInUnion() throws Exception {
        String s = (("{\"type\":\"record\",\"name\":\"S\",\"fields\":[" + "{\"name\":\"a\",\"type\":[\"int\",") + (TestShredder.RECORD_ARRAY)) + "]}]}";
        ColumnMetaData q = new ColumnMetaData("a/array", ValueType.NULL).isArray(true);
        ColumnMetaData r = new ColumnMetaData("a/array[]", ValueType.NULL).setParent(q).isArray(true);
        check(Schema.parse(s), isArray(true), q, r, new ColumnMetaData("a/array[]#x", ValueType.INT).setParent(r), new ColumnMetaData("a/array[]#y", ValueType.STRING).setParent(r));
    }

    @Test
    public void testSimpleMap() throws Exception {
        String s = "{\"type\":\"map\",\"values\":\"long\"}";
        ColumnMetaData p = new ColumnMetaData(">", ValueType.NULL).isArray(true);
        check(Schema.parse(s), p, new ColumnMetaData(">key", ValueType.STRING).setParent(p), new ColumnMetaData(">value", ValueType.LONG).setParent(p));
    }

    @Test
    public void testMap() throws Exception {
        String s = ("{\"type\":\"map\",\"values\":" + (TestShredder.SIMPLE_RECORD)) + "}";
        ColumnMetaData p = new ColumnMetaData(">", ValueType.NULL).isArray(true);
        check(Schema.parse(s), p, new ColumnMetaData(">key", ValueType.STRING).setParent(p), new ColumnMetaData(">value#x", ValueType.INT).setParent(p), new ColumnMetaData(">value#y", ValueType.STRING).setParent(p));
    }
}
