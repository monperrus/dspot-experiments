/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.ml.selection.scoring.evaluator;


import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.ml.common.TrainerTest;
import org.apache.ignite.ml.knn.regression.KNNRegressionModel;
import org.apache.ignite.ml.knn.regression.KNNRegressionTrainer;
import org.apache.ignite.ml.math.distances.EuclideanDistance;
import org.apache.ignite.ml.math.functions.IgniteBiFunction;
import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.apache.ignite.ml.selection.scoring.metric.regression.RegressionMetrics;
import org.apache.ignite.ml.selection.split.TrainTestSplit;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link Evaluator}.
 */
public class RegressionEvaluatorTest extends TrainerTest {
    /**
     * Test evalutor and trainer.
     */
    @Test
    public void testEvaluatorWithoutFilter() {
        Map<Integer, Vector> data = new HashMap<>();
        data.put(0, VectorUtils.of(60323, 83.0, 234289, 2356, 1590, 107608, 1947));
        data.put(1, VectorUtils.of(61122, 88.5, 259426, 2325, 1456, 108632, 1948));
        data.put(2, VectorUtils.of(60171, 88.2, 258054, 3682, 1616, 109773, 1949));
        data.put(3, VectorUtils.of(61187, 89.5, 284599, 3351, 1650, 110929, 1950));
        data.put(4, VectorUtils.of(63221, 96.2, 328975, 2099, 3099, 112075, 1951));
        data.put(5, VectorUtils.of(63639, 98.1, 346999, 1932, 3594, 113270, 1952));
        data.put(6, VectorUtils.of(64989, 99.0, 365385, 1870, 3547, 115094, 1953));
        data.put(7, VectorUtils.of(63761, 100.0, 363112, 3578, 3350, 116219, 1954));
        data.put(8, VectorUtils.of(66019, 101.2, 397469, 2904, 3048, 117388, 1955));
        data.put(9, VectorUtils.of(68169, 108.4, 442769, 2936, 2798, 120445, 1957));
        data.put(10, VectorUtils.of(66513, 110.8, 444546, 4681, 2637, 121950, 1958));
        data.put(11, VectorUtils.of(68655, 112.6, 482704, 3813, 2552, 123366, 1959));
        data.put(12, VectorUtils.of(69564, 114.2, 502601, 3931, 2514, 125368, 1960));
        data.put(13, VectorUtils.of(69331, 115.7, 518173, 4806, 2572, 127852, 1961));
        data.put(14, VectorUtils.of(70551, 116.9, 554894, 4007, 2827, 130081, 1962));
        KNNRegressionTrainer trainer = new KNNRegressionTrainer();
        IgniteBiFunction<Integer, Vector, Vector> featureExtractor = ( k, v) -> v.copyOfRange(1, v.size());
        IgniteBiFunction<Integer, Vector, Double> lbExtractor = ( k, v) -> v.get(0);
        KNNRegressionModel mdl = ((KNNRegressionModel) (trainer.fit(new org.apache.ignite.ml.dataset.impl.local.LocalDatasetBuilder(data, parts), featureExtractor, lbExtractor).withK(3).withDistanceMeasure(new EuclideanDistance())));
        double score = Evaluator.evaluate(data, mdl, featureExtractor, lbExtractor, new RegressionMetrics().withMetric(RegressionMetricValues::rss));
        Assert.assertEquals(1068809.6666666653, score, 1.0E-4);
    }

    /**
     * Test evalutor and trainer with test-train splitting.
     */
    @Test
    public void testEvaluatorWithFilter() {
        Map<Integer, Vector> data = new HashMap<>();
        data.put(0, VectorUtils.of(60323, 83.0, 234289, 2356, 1590, 107608, 1947));
        data.put(1, VectorUtils.of(61122, 88.5, 259426, 2325, 1456, 108632, 1948));
        data.put(2, VectorUtils.of(60171, 88.2, 258054, 3682, 1616, 109773, 1949));
        data.put(3, VectorUtils.of(61187, 89.5, 284599, 3351, 1650, 110929, 1950));
        data.put(4, VectorUtils.of(63221, 96.2, 328975, 2099, 3099, 112075, 1951));
        data.put(5, VectorUtils.of(63639, 98.1, 346999, 1932, 3594, 113270, 1952));
        data.put(6, VectorUtils.of(64989, 99.0, 365385, 1870, 3547, 115094, 1953));
        data.put(7, VectorUtils.of(63761, 100.0, 363112, 3578, 3350, 116219, 1954));
        data.put(8, VectorUtils.of(66019, 101.2, 397469, 2904, 3048, 117388, 1955));
        data.put(9, VectorUtils.of(68169, 108.4, 442769, 2936, 2798, 120445, 1957));
        data.put(10, VectorUtils.of(66513, 110.8, 444546, 4681, 2637, 121950, 1958));
        data.put(11, VectorUtils.of(68655, 112.6, 482704, 3813, 2552, 123366, 1959));
        data.put(12, VectorUtils.of(69564, 114.2, 502601, 3931, 2514, 125368, 1960));
        data.put(13, VectorUtils.of(69331, 115.7, 518173, 4806, 2572, 127852, 1961));
        data.put(14, VectorUtils.of(70551, 116.9, 554894, 4007, 2827, 130081, 1962));
        KNNRegressionTrainer trainer = new KNNRegressionTrainer();
        IgniteBiFunction<Integer, Vector, Vector> featureExtractor = ( k, v) -> v.copyOfRange(1, v.size());
        IgniteBiFunction<Integer, Vector, Double> lbExtractor = ( k, v) -> v.get(0);
        TrainTestSplit<Integer, Vector> split = new org.apache.ignite.ml.selection.split.TrainTestDatasetSplitter<Integer, Vector>().split(0.5);
        KNNRegressionModel mdl = ((KNNRegressionModel) (trainer.fit(data, split.getTestFilter(), parts, featureExtractor, lbExtractor).withK(3).withDistanceMeasure(new EuclideanDistance())));
        double score = Evaluator.evaluate(data, split.getTrainFilter(), mdl, featureExtractor, lbExtractor, new RegressionMetrics().withMetric(RegressionMetricValues::rss));
        Assert.assertEquals(4800164.444444457, score, 1.0E-4);
    }
}
