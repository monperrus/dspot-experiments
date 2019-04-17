/**
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.item.file;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;


/**
 * Tests to ensure that the current Resource is correctly being set on items that implement ResourceAware.
 * Because it there are extensive tests the reader in general, this will only test ResourceAware related
 * use cases.
 */
public class MultiResourceItemReaderResourceAwareTests {
    private MultiResourceItemReader<MultiResourceItemReaderResourceAwareTests.Foo> tested = new MultiResourceItemReader();

    private FlatFileItemReader<MultiResourceItemReaderResourceAwareTests.Foo> itemReader = new FlatFileItemReader();

    private ExecutionContext ctx = new ExecutionContext();

    // test input spans several resources
    private Resource r1 = new ByteArrayResource("1\n2\n3\n".getBytes());

    private Resource r2 = new ByteArrayResource("4\n5\n".getBytes());

    private Resource r3 = new ByteArrayResource("".getBytes());

    private Resource r4 = new ByteArrayResource("6\n".getBytes());

    private Resource r5 = new ByteArrayResource("7\n8\n".getBytes());

    /**
     * Read input from start to end.
     */
    @Test
    public void testRead() throws Exception {
        tested.open(ctx);
        assertValueAndResource(r1, "1");
        assertValueAndResource(r1, "2");
        assertValueAndResource(r1, "3");
        assertValueAndResource(r2, "4");
        assertValueAndResource(r2, "5");
        assertValueAndResource(r4, "6");
        assertValueAndResource(r5, "7");
        assertValueAndResource(r5, "8");
        Assert.assertEquals(null, tested.read());
        tested.close();
    }

    static final class FooLineMapper implements LineMapper<MultiResourceItemReaderResourceAwareTests.Foo> {
        @Override
        public MultiResourceItemReaderResourceAwareTests.Foo mapLine(String line, int lineNumber) throws Exception {
            return new MultiResourceItemReaderResourceAwareTests.Foo(line);
        }
    }

    static final class Foo implements ResourceAware {
        String value;

        Resource resource;

        Foo(String value) {
            this.value = value;
        }

        @Override
        public void setResource(Resource resource) {
            this.resource = resource;
        }
    }
}
