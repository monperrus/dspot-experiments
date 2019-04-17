/**
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
package org.flowable.test.cmmn.converter;


import org.flowable.cmmn.model.CmmnModel;
import org.junit.Test;


/**
 *
 *
 * @author Tijs Rademakers
 */
public class ScriptServiceTaskCmmnXmlConverterTest extends AbstractConverterTest {
    private static final String CMMN_RESOURCE = "org/flowable/test/cmmn/converter/script-task.cmmn";

    @Test
    public void convertXMLToModel() throws Exception {
        CmmnModel cmmnModel = readXMLFile(ScriptServiceTaskCmmnXmlConverterTest.CMMN_RESOURCE);
        validateModel(cmmnModel);
    }

    @Test
    public void convertModelToXML() throws Exception {
        CmmnModel cmmnModel = readXMLFile(ScriptServiceTaskCmmnXmlConverterTest.CMMN_RESOURCE);
        CmmnModel parsedModel = exportAndReadXMLFile(cmmnModel);
        validateModel(parsedModel);
    }
}
