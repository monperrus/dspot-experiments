/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.mapreduce.aggregation;


import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class MapMinAggregationTest extends AbstractAggregationTest {
    @Test
    public void testComparableMin() throws Exception {
        BigDecimal[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<BigDecimal>() {
            @Override
            public BigDecimal provideRandom(Random random) {
                return BigDecimal.valueOf((10000.0 + (AbstractAggregationTest.random(1000, 2000))));
            }
        }, BigDecimal.class);
        BigDecimal expectation = BigDecimal.ZERO;
        for (int i = 0; i < (values.length); i++) {
            BigDecimal value = values[i];
            expectation = (i == 0) ? value : expectation.min(value);
        }
        Aggregation<String, Comparable, Comparable> aggregation = Aggregations.comparableMin();
        Comparable result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testBigDecimalMin() throws Exception {
        BigDecimal[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<BigDecimal>() {
            @Override
            public BigDecimal provideRandom(Random random) {
                return BigDecimal.valueOf((10000.0 + (AbstractAggregationTest.random(1000, 2000))));
            }
        }, BigDecimal.class);
        BigDecimal expectation = BigDecimal.ZERO;
        for (int i = 0; i < (values.length); i++) {
            BigDecimal value = values[i];
            expectation = (i == 0) ? value : expectation.min(value);
        }
        Aggregation<String, BigDecimal, BigDecimal> aggregation = Aggregations.bigDecimalMin();
        BigDecimal result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testBigIntegerMin() throws Exception {
        BigInteger[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<BigInteger>() {
            @Override
            public BigInteger provideRandom(Random random) {
                return BigInteger.valueOf((10000L + (AbstractAggregationTest.random(1000, 2000))));
            }
        }, BigInteger.class);
        BigInteger expectation = BigInteger.ZERO;
        for (int i = 0; i < (values.length); i++) {
            BigInteger value = values[i];
            expectation = (i == 0) ? value : expectation.min(value);
        }
        Aggregation<String, BigInteger, BigInteger> aggregation = Aggregations.bigIntegerMin();
        BigInteger result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testDoubleMin() throws Exception {
        Double[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<Double>() {
            @Override
            public Double provideRandom(Random random) {
                return 10000.0 + (AbstractAggregationTest.random(1000, 2000));
            }
        }, Double.class);
        double expectation = Double.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            double value = values[i];
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Double, Double> aggregation = Aggregations.doubleMin();
        double result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result, 0.0);
    }

    @Test
    public void testIntegerMin() throws Exception {
        Integer[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<Integer>() {
            @Override
            public Integer provideRandom(Random random) {
                return AbstractAggregationTest.random(1000, 2000);
            }
        }, Integer.class);
        int expectation = Integer.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            int value = values[i];
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerMin();
        int result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testLongMin() throws Exception {
        Long[] values = AbstractAggregationTest.buildPlainValues(new AbstractAggregationTest.ValueProvider<Long>() {
            @Override
            public Long provideRandom(Random random) {
                return 10000L + (AbstractAggregationTest.random(1000, 2000));
            }
        }, Long.class);
        long expectation = Long.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            long value = values[i];
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Long, Long> aggregation = Aggregations.longMin();
        long result = testMin(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testBigDecimalMinWithExtractor() throws Exception {
        AbstractAggregationTest.Value<BigDecimal>[] values = AbstractAggregationTest.buildValues(new AbstractAggregationTest.ValueProvider<BigDecimal>() {
            @Override
            public BigDecimal provideRandom(Random random) {
                return BigDecimal.valueOf((10000.0 + (AbstractAggregationTest.random(1000, 2000))));
            }
        });
        BigDecimal expectation = BigDecimal.ZERO;
        for (int i = 0; i < (values.length); i++) {
            AbstractAggregationTest.Value<BigDecimal> value = values[i];
            expectation = (i == 0) ? value.value : expectation.min(value.value);
        }
        Aggregation<String, BigDecimal, BigDecimal> aggregation = Aggregations.bigDecimalMin();
        BigDecimal result = testMinWithExtractor(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testBigIntegerMinWithExtractor() throws Exception {
        AbstractAggregationTest.Value<BigInteger>[] values = AbstractAggregationTest.buildValues(new AbstractAggregationTest.ValueProvider<BigInteger>() {
            @Override
            public BigInteger provideRandom(Random random) {
                return BigInteger.valueOf((10000L + (AbstractAggregationTest.random(1000, 2000))));
            }
        });
        BigInteger expectation = BigInteger.ZERO;
        for (int i = 0; i < (values.length); i++) {
            AbstractAggregationTest.Value<BigInteger> value = values[i];
            expectation = (i == 0) ? value.value : expectation.min(value.value);
        }
        Aggregation<String, BigInteger, BigInteger> aggregation = Aggregations.bigIntegerMin();
        BigInteger result = testMinWithExtractor(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testDoubleMinWithExtractor() throws Exception {
        AbstractAggregationTest.Value<Double>[] values = AbstractAggregationTest.buildValues(new AbstractAggregationTest.ValueProvider<Double>() {
            @Override
            public Double provideRandom(Random random) {
                return 10000.0 + (AbstractAggregationTest.random(1000, 2000));
            }
        });
        double expectation = Double.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            double value = values[i].value;
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Double, Double> aggregation = Aggregations.doubleMin();
        double result = testMinWithExtractor(values, aggregation);
        Assert.assertEquals(expectation, result, 0.0);
    }

    @Test
    public void testIntegerMinWithExtractor() throws Exception {
        AbstractAggregationTest.Value<Integer>[] values = AbstractAggregationTest.buildValues(new AbstractAggregationTest.ValueProvider<Integer>() {
            @Override
            public Integer provideRandom(Random random) {
                return AbstractAggregationTest.random(1000, 2000);
            }
        });
        int expectation = Integer.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            int value = values[i].value;
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Integer, Integer> aggregation = Aggregations.integerMin();
        int result = testMinWithExtractor(values, aggregation);
        Assert.assertEquals(expectation, result);
    }

    @Test
    public void testLongMinWithExtractor() throws Exception {
        AbstractAggregationTest.Value<Long>[] values = AbstractAggregationTest.buildValues(new AbstractAggregationTest.ValueProvider<Long>() {
            @Override
            public Long provideRandom(Random random) {
                return 10000L + (AbstractAggregationTest.random(1000, 2000));
            }
        });
        long expectation = Long.MAX_VALUE;
        for (int i = 0; i < (values.length); i++) {
            long value = values[i].value;
            if (value < expectation) {
                expectation = value;
            }
        }
        Aggregation<String, Long, Long> aggregation = Aggregations.longMin();
        long result = testMinWithExtractor(values, aggregation);
        Assert.assertEquals(expectation, result);
    }
}
