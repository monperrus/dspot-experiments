/**
 * ! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ****************************************************************************
 */
package org.pentaho.di.trans.steps.fuzzymatch;


import java.util.Random;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;


public class FuzzyMatchMetaTest {
    @ClassRule
    public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

    LoadSaveTester loadSaveTester;

    @Test
    public void testSerialization() throws KettleException {
        loadSaveTester.testSerialization();
    }

    // Clone test removed as it's covered by the load/save tester now.
    public class AlgorithmLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
        final Random rand = new Random();

        @Override
        public Integer getTestObject() {
            return rand.nextInt(10);
        }

        @Override
        public boolean validateTestObject(Integer testObject, Object actual) {
            if (!(actual instanceof Integer)) {
                return false;
            }
            Integer actualInt = ((Integer) (actual));
            return actualInt.equals(testObject);
        }
    }
}

