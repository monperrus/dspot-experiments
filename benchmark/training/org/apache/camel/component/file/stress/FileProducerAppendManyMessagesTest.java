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
package org.apache.camel.component.file.stress;


import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 */
@Ignore("Manual test")
public class FileProducerAppendManyMessagesTest extends ContextTestSupport {
    private boolean enabled;

    @Test
    public void testBigFile() throws Exception {
        if (!(enabled)) {
            return;
        }
        MockEndpoint mock = getMockEndpoint("mock:done");
        mock.expectedMessageCount(1);
        mock.setResultWaitTime((2 * 60000));
        assertMockEndpointsSatisfied();
    }
}
