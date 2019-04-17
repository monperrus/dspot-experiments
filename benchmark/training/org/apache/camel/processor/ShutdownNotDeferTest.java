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
package org.apache.camel.processor;


import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.camel.Component;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Processor;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFileOperations;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;


public class ShutdownNotDeferTest extends ContextTestSupport {
    private static final AtomicBoolean CONSUMER_SUSPENDED = new AtomicBoolean();

    @Test
    public void testShutdownNotDeferred() throws Exception {
        MockEndpoint bar = getMockEndpoint("mock:bar");
        bar.expectedMinimumMessageCount(1);
        template.sendBody("seda:foo", "A");
        template.sendBody("seda:foo", "B");
        template.sendBody("seda:foo", "C");
        template.sendBody("seda:foo", "D");
        template.sendBody("seda:foo", "E");
        assertMockEndpointsSatisfied();
        context.stop();
        Assert.assertTrue("Should have been suspended", ShutdownNotDeferTest.CONSUMER_SUSPENDED.get());
    }

    private static final class MyDeferFileEndpoint extends FileEndpoint {
        private MyDeferFileEndpoint(String endpointUri, Component component) {
            super(endpointUri, component);
        }

        @Override
        protected FileConsumer newFileConsumer(Processor processor, GenericFileOperations<File> operations) {
            return new FileConsumer(this, processor, operations, createGenericFileStrategy()) {
                @Override
                protected void doSuspend() throws Exception {
                    ShutdownNotDeferTest.CONSUMER_SUSPENDED.set(true);
                    super.doSuspend();
                }
            };
        }
    }
}
