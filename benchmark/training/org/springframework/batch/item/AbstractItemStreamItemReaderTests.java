/**
 * Copyright 2009-2012 the original author or authors.
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
package org.springframework.batch.item;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.batch.item.sample.Foo;


/**
 * Common tests for readers implementing both {@link ItemReader} and
 * {@link ItemStream}. Expected input is five {@link Foo} objects with values 1
 * to 5.
 */
public abstract class AbstractItemStreamItemReaderTests extends AbstractItemReaderTests {
    protected ExecutionContext executionContext = new ExecutionContext();

    /**
     * Restart scenario - read items, update execution context, create new
     * reader and restore from restart data - the new input source should
     * continue where the old one finished.
     */
    @Test
    public void testRestart() throws Exception {
        testedAsStream().update(executionContext);
        Foo foo1 = tested.read();
        Assert.assertEquals(1, foo1.getValue());
        Foo foo2 = tested.read();
        Assert.assertEquals(2, foo2.getValue());
        testedAsStream().update(executionContext);
        testedAsStream().close();
        // create new input source
        tested = getItemReader();
        testedAsStream().open(executionContext);
        Foo fooAfterRestart = tested.read();
        Assert.assertEquals(3, fooAfterRestart.getValue());
    }

    /**
     * Restart scenario - read items, rollback to last marked position, update
     * execution context, create new reader and restore from restart data - the
     * new input source should continue where the old one finished.
     */
    @Test
    public void testResetAndRestart() throws Exception {
        testedAsStream().update(executionContext);
        Foo foo1 = tested.read();
        Assert.assertEquals(1, foo1.getValue());
        Foo foo2 = tested.read();
        Assert.assertEquals(2, foo2.getValue());
        testedAsStream().update(executionContext);
        Foo foo3 = tested.read();
        Assert.assertEquals(3, foo3.getValue());
        testedAsStream().close();
        // create new input source
        tested = getItemReader();
        testedAsStream().open(executionContext);
        Foo fooAfterRestart = tested.read();
        Assert.assertEquals(3, fooAfterRestart.getValue());
    }

    @Test
    public void testReopen() throws Exception {
        testedAsStream().update(executionContext);
        Foo foo1 = tested.read();
        Assert.assertEquals(1, foo1.getValue());
        Foo foo2 = tested.read();
        Assert.assertEquals(2, foo2.getValue());
        testedAsStream().update(executionContext);
        // create new input source
        testedAsStream().close();
        testedAsStream().open(executionContext);
        Foo fooAfterRestart = tested.read();
        Assert.assertEquals(3, fooAfterRestart.getValue());
    }
}
