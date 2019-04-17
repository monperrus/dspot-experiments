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
package org.apache.avro;


import GenericData.Record;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


/**
 * This test demonstrates the fix for a complex nested schema type.
 */
public class TestNestedRecords {
    @Test
    public void testSingleSubRecord() throws IOException {
        final Schema child = SchemaBuilder.record("Child").namespace("org.apache.avro.nested").fields().requiredString("childField").endRecord();
        final Schema parent = SchemaBuilder.record("Parent").namespace("org.apache.avro.nested").fields().requiredString("parentField1").name("child1").type(child).noDefault().requiredString("parentField2").endRecord();
        final String inputAsExpected = "{\n" + (((((" \"parentField1\": \"parentValue1\",\n" + " \"child1\":{\n") + "    \"childField\":\"childValue1\"\n") + " },\n") + " \"parentField2\":\"parentValue2\"\n") + "}");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inputAsExpected.getBytes());
        final JsonDecoder decoder = DecoderFactory.get().jsonDecoder(parent, inputStream);
        final DatumReader<Object> reader = new org.apache.avro.generic.GenericDatumReader<Object>(parent);
        final GenericData.Record decoded = ((GenericData.Record) (reader.read(null, decoder)));
        Assert.assertThat(decoded.get("parentField1").toString(), CoreMatchers.equalTo("parentValue1"));
        Assert.assertThat(decoded.get("parentField2").toString(), CoreMatchers.equalTo("parentValue2"));
        Assert.assertThat(get("childField").toString(), CoreMatchers.equalTo("childValue1"));
    }

    @Test
    public void testSingleSubRecordExtraField() throws IOException {
        final Schema child = SchemaBuilder.record("Child").namespace("org.apache.avro.nested").fields().requiredString("childField").endRecord();
        final Schema parent = SchemaBuilder.record("Parent").namespace("org.apache.avro.nested").fields().requiredString("parentField1").name("child1").type(child).noDefault().requiredString("parentField2").endRecord();
        final String inputAsExpected = "{\n" + ((((((" \"parentField1\": \"parentValue1\",\n" + " \"child1\":{\n") + "    \"childField\":\"childValue1\",\n") + // this field should be safely ignored
        "    \"extraField\":\"extraValue\"\n") + " },\n") + " \"parentField2\":\"parentValue2\"\n") + "}");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inputAsExpected.getBytes());
        final JsonDecoder decoder = DecoderFactory.get().jsonDecoder(parent, inputStream);
        final DatumReader<Object> reader = new org.apache.avro.generic.GenericDatumReader<Object>(parent);
        final GenericData.Record decoded = ((GenericData.Record) (reader.read(null, decoder)));
        Assert.assertThat(decoded.get("parentField1").toString(), CoreMatchers.equalTo("parentValue1"));
        Assert.assertThat(decoded.get("parentField2").toString(), CoreMatchers.equalTo("parentValue2"));
        Assert.assertThat(get("childField").toString(), CoreMatchers.equalTo("childValue1"));
    }
}
