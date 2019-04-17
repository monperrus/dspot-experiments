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
package org.apache.camel.component.file.remote;


import Exchange.FILE_NAME;
import java.io.File;
import org.junit.Test;


public class FtpConsumerDoneFileNameTest extends FtpServerTestSupport {
    @Test
    public void testDoneFileName() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(0);
        template.sendBodyAndHeader(getFtpUrl(), "Hello World", FILE_NAME, "hello.txt");
        // wait a bit and it should not pickup the written file as there are no done file
        Thread.sleep(1000);
        assertMockEndpointsSatisfied();
        resetMocks();
        getMockEndpoint("mock:result").expectedBodiesReceived("Hello World");
        // write the done file
        template.sendBodyAndHeader(getFtpUrl(), "", FILE_NAME, "hello.dat");
        assertMockEndpointsSatisfied();
        // give time for done file to be deleted
        Thread.sleep(1000);
        // done file should be deleted now
        File file = new File(((FtpServerTestSupport.FTP_ROOT_DIR) + "done/hello.dat"));
        assertFalse(("Done file should be deleted: " + file), file.exists());
    }
}
