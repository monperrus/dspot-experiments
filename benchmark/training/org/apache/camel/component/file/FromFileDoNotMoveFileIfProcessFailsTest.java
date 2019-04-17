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
package org.apache.camel.component.file;


import Exchange.FILE_NAME;
import java.io.File;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;


public class FromFileDoNotMoveFileIfProcessFailsTest extends ContextTestSupport {
    private String body = "Hello World this file will NOT be moved";

    @Test
    public void testPollFileAndShouldNotBeMoved() throws Exception {
        template.sendBodyAndHeader("file://target/data/movefile", body, FILE_NAME, "hello.txt");
        MockEndpoint mock = getMockEndpoint("mock:error");
        // it could potentially retry the file on the 2nd poll and then fail again
        // so it should be minimum message count
        mock.expectedMinimumMessageCount(1);
        mock.assertIsSatisfied();
        oneExchangeDone.matchesMockWaitTime();
        // assert the file is not moved
        File file = new File("target/data/movefile/hello.txt");
        Assert.assertTrue("The file should NOT have been moved", file.exists());
    }
}
