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
package org.apache.camel.component.salesforce;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test support for Salesforce compound data types.
 * This test requires a custom field in the <code>Account</code> object
 * called <code>"Shipping Location"</code> of type <code>Geolocation</code> in decimal units.
 *
 * @see <a href="https://www.salesforce.com/developer/docs/api/index_Left.htm#CSHID=compound_fields.htm|StartTopic=Content%2Fcompound_fields.htm|SkinName=webhelp">Compound data types</a>
 */
public class CompoundTypesIntegrationTest extends AbstractSalesforceTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(CompoundTypesIntegrationTest.class);

    @Test
    public void testTypes() throws Exception {
        doTestTypes("");
        doTestTypes("Xml");
    }
}
