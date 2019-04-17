/**
 * Copyright (C) 2013-2018 Vasilis Vryniotis <bbriniotis@datumbox.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datumbox.framework.core.machinelearning.classification;


import Constants.DOUBLE_ACCURACY_HIGH;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.core.Datasets;
import com.datumbox.framework.core.common.dataobjects.Dataframe;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.datumbox.framework.core.machinelearning.modelselection.splitters.KFoldSplitter;
import com.datumbox.framework.tests.abstracts.AbstractTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test cases for BernoulliNaiveBayes.
 *
 * @author Vasilis Vryniotis <bbriniotis@datumbox.com>
 */
public class BernoulliNaiveBayesTest extends AbstractTest {
    /**
     * Test of predict method, of class BernoulliNaiveBayes.
     */
    @Test
    public void testPredict() {
        logger.info("testPredict");
        Configuration configuration = getConfiguration();
        Dataframe[] data = Datasets.carsNumeric(configuration);
        Dataframe trainingData = data[0];
        Dataframe validationData = data[1];
        String storageName = this.getClass().getSimpleName();
        BernoulliNaiveBayes instance = MLBuilder.create(new BernoulliNaiveBayes.TrainingParameters(), configuration);
        instance.fit(trainingData);
        instance.save(storageName);
        instance.close();
        instance = MLBuilder.load(BernoulliNaiveBayes.class, storageName, configuration);
        instance.predict(validationData);
        Map<Integer, Object> expResult = new HashMap<>();
        Map<Integer, Object> result = new HashMap<>();
        for (Map.Entry<Integer, Record> e : validationData.entries()) {
            Integer rId = e.getKey();
            Record r = e.getValue();
            expResult.put(rId, r.getY());
            result.put(rId, r.getYPredicted());
        }
        Assert.assertEquals(expResult, result);
        instance.delete();
        trainingData.close();
        validationData.close();
    }

    /**
     * Test of validate method, of class BernoulliNaiveBayes.
     */
    @Test
    public void testKFoldCrossValidation() {
        logger.info("testKFoldCrossValidation");
        Configuration configuration = getConfiguration();
        int k = 5;
        Dataframe[] data = Datasets.carsNumeric(configuration);
        Dataframe trainingData = data[0];
        data[1].close();
        ClassificationMetrics vm = new com.datumbox.framework.core.machinelearning.modelselection.Validator(ClassificationMetrics.class, configuration).validate(new KFoldSplitter(k).split(trainingData), new BernoulliNaiveBayes.TrainingParameters());
        double expResult = 0.6631318681318682;
        double result = vm.getMacroF1();
        Assert.assertEquals(expResult, result, DOUBLE_ACCURACY_HIGH);
        trainingData.close();
    }
}
