/**
 * ! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.salesforceupdate;


import org.junit.Assert;
import org.junit.Test;


public class SalesforceUpdateDataTest {
    @Test
    public void testConstructor() {
        SalesforceUpdateData data = new SalesforceUpdateData();
        Assert.assertNull(data.inputRowMeta);
        Assert.assertNull(data.outputRowMeta);
        Assert.assertEquals(0, data.nrfields);
        Assert.assertNull(data.fieldnrs);
        Assert.assertNull(data.saveResult);
        Assert.assertNull(data.sfBuffer);
        Assert.assertNull(data.outputBuffer);
        Assert.assertEquals(0, data.iBufferPos);
    }
}
