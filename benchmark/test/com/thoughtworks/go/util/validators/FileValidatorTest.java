/**
 * ***********************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ************************GO-LICENSE-END**********************************
 */
package com.thoughtworks.go.util.validators;


import com.thoughtworks.go.util.SystemEnvironment;
import java.io.File;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


public class FileValidatorTest {
    private String realConfigDir;

    @Test
    public void shouldSetValidationToFalseIfFileDoesNotExistInClasspath() {
        FileValidator fv = FileValidator.configFileAlwaysOverwrite("does.not.exist", new SystemEnvironment());
        Validation val = new Validation();
        fv.validate(val);
        new File(new SystemEnvironment().getPropertyImpl("java.io.tmpdir"), "does.not.exist").deleteOnExit();
        Assert.assertThat(val.isSuccessful(), Matchers.is(false));
    }
}
