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


import org.apache.camel.ContextTestSupport;
import org.apache.camel.util.FileUtil;
import org.junit.Test;


/**
 * Unit test for consuming a batch of files (multiple files in one consume)
 */
public class FileConsumerExtendedAttributesTest extends ContextTestSupport {
    private static final String ROOT = "target/data/extended-attributes";

    private static final String FILE = "attributes.txt";

    @Test
    public void testBasicAttributes() throws Exception {
        testAttributes("mock:basic", "basic:");
    }

    @Test
    public void testBasicAttributesAsDefault() throws Exception {
        testAttributes("mock:basic-as-default", "basic:");
    }

    @Test
    public void testBasicAttributesAsDefaultWithFilter() throws Exception {
        testAttributes("mock:basic-as-default", "basic:");
    }

    @Test
    public void testPosixAttributes() throws Exception {
        if (FileUtil.isWindows()) {
            return;
        }
        testAttributes("mock:posix", "posix:");
    }
}
