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
package org.apache.beam.sdk.extensions.jackson;


import Pipeline.PipelineExecutionException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.Iterables;
import org.junit.Rule;
import org.junit.Test;


/**
 * Test Jackson transforms {@link ParseJsons} and {@link AsJsons}.
 */
public class JacksonTransformsTest {
    private static final List<String> VALID_JSONS = Arrays.asList("{\"myString\":\"abc\",\"myInt\":3}", "{\"myString\":\"def\",\"myInt\":4}");

    private static final List<String> INVALID_JSONS = Arrays.asList("{myString:\"abc\",\"myInt\":3,\"other\":1}", "{", "");

    private static final List<String> EMPTY_JSONS = Arrays.asList("{}", "{}");

    private static final List<String> EXTRA_PROPERTIES_JSONS = Arrays.asList("{\"myString\":\"abc\",\"myInt\":3,\"other\":1}", "{\"myString\":\"def\",\"myInt\":4}");

    private static final List<JacksonTransformsTest.MyPojo> POJOS = Arrays.asList(new JacksonTransformsTest.MyPojo("abc", 3), new JacksonTransformsTest.MyPojo("def", 4));

    private static final List<JacksonTransformsTest.MyEmptyBean> EMPTY_BEANS = Arrays.asList(new JacksonTransformsTest.MyEmptyBean("abc", 3), new JacksonTransformsTest.MyEmptyBean("def", 4));

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void parseValidJsons() {
        PCollection<JacksonTransformsTest.MyPojo> output = pipeline.apply(Create.of(JacksonTransformsTest.VALID_JSONS)).apply(ParseJsons.of(JacksonTransformsTest.MyPojo.class)).setCoder(SerializableCoder.of(JacksonTransformsTest.MyPojo.class));
        PAssert.that(output).containsInAnyOrder(JacksonTransformsTest.POJOS);
        pipeline.run();
    }

    @Test(expected = PipelineExecutionException.class)
    public void failParsingInvalidJsons() {
        PCollection<JacksonTransformsTest.MyPojo> output = pipeline.apply(Create.of(Iterables.concat(JacksonTransformsTest.VALID_JSONS, JacksonTransformsTest.INVALID_JSONS))).apply(ParseJsons.of(JacksonTransformsTest.MyPojo.class)).setCoder(SerializableCoder.of(JacksonTransformsTest.MyPojo.class));
        PAssert.that(output).containsInAnyOrder(JacksonTransformsTest.POJOS);
        pipeline.run();
    }

    @Test(expected = PipelineExecutionException.class)
    public void failParsingWithoutCustomMapper() {
        PCollection<JacksonTransformsTest.MyPojo> output = pipeline.apply(Create.of(JacksonTransformsTest.EXTRA_PROPERTIES_JSONS)).apply(ParseJsons.of(JacksonTransformsTest.MyPojo.class)).setCoder(SerializableCoder.of(JacksonTransformsTest.MyPojo.class));
        PAssert.that(output).empty();
        pipeline.run();
    }

    @Test
    public void parseUsingCustomMapper() {
        ObjectMapper customMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        PCollection<JacksonTransformsTest.MyPojo> output = pipeline.apply(Create.of(JacksonTransformsTest.EXTRA_PROPERTIES_JSONS)).apply(ParseJsons.of(JacksonTransformsTest.MyPojo.class).withMapper(customMapper)).setCoder(SerializableCoder.of(JacksonTransformsTest.MyPojo.class));
        PAssert.that(output).containsInAnyOrder(JacksonTransformsTest.POJOS);
        pipeline.run();
    }

    @Test
    public void writeValidObjects() {
        PCollection<String> output = pipeline.apply(Create.of(JacksonTransformsTest.POJOS)).apply(AsJsons.of(JacksonTransformsTest.MyPojo.class)).setCoder(StringUtf8Coder.of());
        PAssert.that(output).containsInAnyOrder(JacksonTransformsTest.VALID_JSONS);
        pipeline.run();
    }

    @Test(expected = PipelineExecutionException.class)
    public void failWritingWithoutCustomMapper() {
        pipeline.apply(Create.of(JacksonTransformsTest.EMPTY_BEANS)).apply(AsJsons.of(JacksonTransformsTest.MyEmptyBean.class)).setCoder(StringUtf8Coder.of());
        pipeline.run();
    }

    @Test
    public void writeUsingCustomMapper() {
        ObjectMapper customMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        PCollection<String> output = pipeline.apply(Create.of(JacksonTransformsTest.EMPTY_BEANS)).apply(AsJsons.of(JacksonTransformsTest.MyEmptyBean.class).withMapper(customMapper)).setCoder(StringUtf8Coder.of());
        PAssert.that(output).containsInAnyOrder(JacksonTransformsTest.EMPTY_JSONS);
        pipeline.run();
    }

    /**
     * Pojo for tests.
     */
    @SuppressWarnings({ "WeakerAccess", "unused" })
    public static class MyPojo implements Serializable {
        private String myString;

        private int myInt;

        public MyPojo() {
        }

        public MyPojo(String myString, int myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        public String getMyString() {
            return myString;
        }

        public void setMyString(String myString) {
            this.myString = myString;
        }

        public int getMyInt() {
            return myInt;
        }

        public void setMyInt(int myInt) {
            this.myInt = myInt;
        }

        @Override
        public boolean equals(Object o) {
            if ((this) == o) {
                return true;
            }
            if (!(o instanceof JacksonTransformsTest.MyPojo)) {
                return false;
            }
            JacksonTransformsTest.MyPojo myPojo = ((JacksonTransformsTest.MyPojo) (o));
            return ((myInt) == (myPojo.myInt)) && ((myString) != null ? myString.equals(myPojo.myString) : (myPojo.myString) == null);
        }

        @Override
        public int hashCode() {
            int result = ((myString) != null) ? myString.hashCode() : 0;
            result = (31 * result) + (myInt);
            return result;
        }
    }

    /**
     * Pojo for tests.
     */
    @SuppressWarnings({ "WeakerAccess", "unused" })
    public static class MyEmptyBean implements Serializable {
        private String myString;

        private int myInt;

        public MyEmptyBean(String myString, int myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        @Override
        public boolean equals(Object o) {
            if ((this) == o) {
                return true;
            }
            if ((o == null) || ((getClass()) != (o.getClass()))) {
                return false;
            }
            JacksonTransformsTest.MyEmptyBean that = ((JacksonTransformsTest.MyEmptyBean) (o));
            if ((myInt) != (that.myInt)) {
                return false;
            }
            return (myString) != null ? myString.equals(that.myString) : (that.myString) == null;
        }

        @Override
        public int hashCode() {
            int result = ((myString) != null) ? myString.hashCode() : 0;
            result = (31 * result) + (myInt);
            return result;
        }
    }
}
