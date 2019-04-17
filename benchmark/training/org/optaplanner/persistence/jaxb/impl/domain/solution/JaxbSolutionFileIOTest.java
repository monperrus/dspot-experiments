/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.persistence.jaxb.impl.domain.solution;


import java.io.File;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.persistence.jaxb.impl.testdata.domain.JaxbTestdataEntity;
import org.optaplanner.persistence.jaxb.impl.testdata.domain.JaxbTestdataSolution;
import org.optaplanner.persistence.jaxb.impl.testdata.domain.JaxbTestdataValue;


public class JaxbSolutionFileIOTest {
    private static File solutionTestDir;

    @Test
    public void readAndWrite() {
        JaxbSolutionFileIO<JaxbTestdataSolution> solutionFileIO = new JaxbSolutionFileIO(JaxbTestdataSolution.class);
        File file = new File(JaxbSolutionFileIOTest.solutionTestDir, "testdataSolution.xml");
        JaxbTestdataSolution original = new JaxbTestdataSolution("s1");
        JaxbTestdataValue originalV1 = new JaxbTestdataValue("v1");
        original.setValueList(Arrays.asList(originalV1, new JaxbTestdataValue("v2")));
        original.setEntityList(Arrays.asList(new JaxbTestdataEntity("e1"), new JaxbTestdataEntity("e2", originalV1), new JaxbTestdataEntity("e3")));
        original.setScore(SimpleScore.of((-123)));
        solutionFileIO.write(original, file);
        JaxbTestdataSolution copy = solutionFileIO.read(file);
        Assert.assertNotSame(original, copy);
        assertCode("s1", copy);
        assertAllCodesOfIterator(copy.getValueList().iterator(), "v1", "v2");
        assertAllCodesOfIterator(copy.getEntityList().iterator(), "e1", "e2", "e3");
        JaxbTestdataValue copyV1 = copy.getValueList().get(0);
        JaxbTestdataEntity copyE2 = copy.getEntityList().get(1);
        assertCode("v1", copyE2.getValue());
        Assert.assertSame(copyV1, copyE2.getValue());
        Assert.assertEquals(SimpleScore.of((-123)), copy.getScore());
    }
}
