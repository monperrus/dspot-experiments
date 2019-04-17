/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tests.api.java.util;


import java.util.NoSuchElementException;
import java.util.Vector;
import junit.framework.TestCase;


public class NoSuchElementExceptionTest extends TestCase {
    /**
     * java.util.NoSuchElementException#NoSuchElementException()
     */
    public void test_Constructor() {
        // Test for method java.util.NoSuchElementException()
        TestCase.assertNotNull(new NoSuchElementException());
        try {
            Vector v = new Vector();
            v.elements().nextElement();
            TestCase.fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    /**
     * java.util.NoSuchElementException#NoSuchElementException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method java.util.NoSuchElementException(java.lang.String)
        TestCase.assertNotNull(new NoSuchElementException("String"));
        TestCase.assertNotNull(new NoSuchElementException(null));
        try {
            Vector v = new Vector();
            v.firstElement();
            TestCase.fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
}
