/**
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.security;


import org.junit.Test;
import org.jvnet.hudson.test.Issue;


@Issue("SECURITY-765")
public class RedactSecretJsonInErrorMessageSanitizerTest {
    @Test
    public void noSecrets() throws Exception {
        assertRedaction("{'a': 1, 'b': '2', 'c': {'c1': 1, 'c2': '2', 'c3': ['3a', '3b']}, 'd': ['4a', {'d1': 1, 'd2': '2'}]}", "{'a': 1, 'b': '2', 'c': {'c1': 1, 'c2': '2', 'c3': ['3a', '3b']}, 'd': ['4a', {'d1': 1, 'd2': '2'}]}");
    }

    @Test
    public void simpleWithSecret() throws Exception {
        assertRedaction("{'a': 'secret', 'b': 'other', '$redact': 'a'}", "{'a': '[value redacted]', 'b': 'other', '$redact': 'a'}");
    }

    @Test
    public void singleWithRedactedInArray() throws Exception {
        assertRedaction("{'a': 'secret', 'b': 'other', '$redact': ['a']}", "{'a': '[value redacted]', 'b': 'other', '$redact': ['a']}");
    }

    @Test
    public void objectRedactedAcceptedButNotProcessed() throws Exception {
        assertRedaction("{'a': 'secret', 'b': 'other', '$redact': {'a': 'a'}}", "{'a': 'secret', 'b': 'other', '$redact': {'a': 'a'}}");
    }

    @Test
    public void weirdValuesInRedactedAcceptedButNotProcessed() throws Exception {
        assertRedaction("{'a': 'secret', 'b': 'other', '$redact': [null, true, false, 1, 2, 'a']}", "{'a': '[value redacted]', 'b': 'other', '$redact': [null, true, false, 1, 2, 'a']}");
    }

    @Test
    public void ensureTrueAndOneAsStringAreSupportedAsRedactedKey() throws Exception {
        // only null is not supported, as passing 'null' is considered as null
        assertRedaction("{'true': 'secret1', '1': 'secret3', 'b': 'other', '$redact': ['true', '1']}", "{'true': '[value redacted]', '1': '[value redacted]', 'b': 'other', '$redact': ['true', '1']}");
    }

    @Test
    public void redactFullBranch() throws Exception {
        assertRedaction("{'a': {'s1': 'secret1', 's2': 'secret2', 's3': [1,2,3]}, 'b': [4,5,6], 'c': 'other', '$redact': ['a', 'b']}", "{'a': '[value redacted]', 'b': '[value redacted]', 'c': 'other', '$redact': ['a', 'b']}");
    }

    @Test
    public void multipleSecretAtSameLevel() throws Exception {
        assertRedaction("{'a1': 'secret1', 'a2': 'secret2', 'b': 'other', '$redact': ['a1', 'a2']}", "{'a1': '[value redacted]', 'a2': '[value redacted]', 'b': 'other', '$redact': ['a1', 'a2']}");
    }

    @Test
    public void redactedKeyWithoutCorrespondences() throws Exception {
        assertRedaction("{'a1': 'secret1', 'a2': 'secret2', 'b': 'other', '$redact': ['a0', 'a1', 'a2', 'a3']}", "{'a1': '[value redacted]', 'a2': '[value redacted]', 'b': 'other', '$redact': ['a0', 'a1', 'a2', 'a3']}");
    }

    @Test
    public void secretsAtMultipleLevels() throws Exception {
        assertRedaction("{'a1': 'secret1', 'a2': 'secret2', 'b': 'other', '$redact': ['a1', 'a2'], 'sub': {'c1': 'secret1', 'c2': 'secret2', 'c3': 'other', '$redact': ['c1', 'c2']}}", "{'a1': '[value redacted]', 'a2': '[value redacted]', 'b': 'other', '$redact': ['a1', 'a2'], 'sub': {'c1': '[value redacted]', 'c2': '[value redacted]', 'c3': 'other', '$redact': ['c1', 'c2']}}");
    }

    @Test
    public void noInteractionBetweenLevels() throws Exception {
        assertRedaction("{'a': 'secret', 'b': 'other', 'c': 'other', '$redact': 'a', 'sub': {'a': 'other', 'b': 'secret', 'c': 'other', '$redact': 'b'}}", "{'a': '[value redacted]', 'b': 'other', 'c': 'other', '$redact': 'a', 'sub': {'a': 'other', 'b': '[value redacted]', 'c': 'other', '$redact': 'b'}}");
    }

    @Test
    public void deeplyNestedObject() throws Exception {
        assertRedaction("{'sub': {'arr': ['d1', 2, {'a1': 'other', 'b1':'other', 'c1': 'secret', '$redact': 'c1'}, 4, {'a2': 'other', 'b2': 'other', 'c2': 'secret', '$redact': 'c2'}]}, '$redact': 'b'}", "{'sub': {'arr': ['d1', 2, {'a1': 'other', 'b1':'other', 'c1': '[value redacted]', '$redact': 'c1'}, 4, {'a2': 'other', 'b2': 'other', 'c2': '[value redacted]', '$redact': 'c2'}]}, '$redact': 'b'}");
    }
}
