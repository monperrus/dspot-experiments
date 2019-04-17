/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.scanner;


import java.io.IOException;
import org.drools.core.util.FileManager;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;


public class KieScannerIncrementalCompilationTest extends AbstractKieCiTest {
    private final int FIRST_VALUE = 5;

    private final int SECOND_VALUE = 10;

    private FileManager fileManager;

    private ReleaseId releaseId;

    @Test
    public void testChangeJavaClassButNotDrl() throws IOException {
        checkIncrementalCompilation(true);
    }

    @Test
    public void testChangeDrlNotUsingJava() throws IOException {
        checkIncrementalCompilation(false);
    }
}
