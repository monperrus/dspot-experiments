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
package org.apache.camel.issues;


import java.util.Iterator;
import java.util.function.Consumer;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.TestSupport;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the issue stated in
 * <a href="https://issues.apache.org/jira/browse/CAMEL-12441">CAMEL-12441</a>.
 */
public class SplitterParallelWithIteratorThrowingExceptionTest extends ContextTestSupport {
    @Test
    public void testIteratorThrowExceptionOnFirst() throws Exception {
        getMockEndpoint("mock:line").expectedMessageCount(0);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        try {
            template.sendBody("direct:start", new SplitterParallelWithIteratorThrowingExceptionTest.MyIterator(1));
            Assert.fail("Should throw exception");
        } catch (Exception e) {
            IllegalArgumentException iae = TestSupport.assertIsInstanceOf(IllegalArgumentException.class, e.getCause());
            Assert.assertEquals("Forced error", iae.getMessage());
        }
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testIteratorThrowExceptionOnSecond() throws Exception {
        getMockEndpoint("mock:line").expectedMessageCount(1);
        getMockEndpoint("mock:end").expectedMessageCount(0);
        try {
            template.sendBody("direct:start", new SplitterParallelWithIteratorThrowingExceptionTest.MyIterator(0));
            Assert.fail("Should throw exception");
        } catch (Exception e) {
            IllegalArgumentException iae = TestSupport.assertIsInstanceOf(IllegalArgumentException.class, e.getCause());
            Assert.assertEquals("Forced error", iae.getMessage());
        }
        assertMockEndpointsSatisfied();
    }

    public static class MyIterator implements Iterator<String> {
        private int count;

        public MyIterator(int count) {
            this.count = count;
        }

        @Override
        public boolean hasNext() {
            return (count) < 2;
        }

        @Override
        public String next() {
            (count)++;
            if ((count) == 1) {
                return "Hello";
            } else {
                throw new IllegalArgumentException("Forced error");
            }
        }

        @Override
        public void remove() {
            // noop
        }

        @Override
        public void forEachRemaining(Consumer<? super String> action) {
            // noop
        }
    }
}
