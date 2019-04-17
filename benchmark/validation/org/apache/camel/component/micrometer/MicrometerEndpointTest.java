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
package org.apache.camel.component.micrometer;


import io.micrometer.core.instrument.MeterRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MicrometerEndpointTest {
    private static final String METRICS_NAME = "metrics.name";

    @Mock
    private MeterRegistry registry;

    @Mock
    private Processor processor;

    @Mock
    private Exchange exchange;

    @Mock
    private Message in;

    private MicrometerEndpoint endpoint;

    private InOrder inOrder;

    @Test
    public void testAbstractMetricsEndpoint() {
        Assert.assertThat(endpoint.getMetricsName(), Matchers.is(MicrometerEndpointTest.METRICS_NAME));
        Assert.assertThat(endpoint.getRegistry(), Matchers.is(registry));
    }

    @Test(expected = RuntimeCamelException.class)
    public void testCreateConsumer() {
        endpoint.createConsumer(processor);
    }

    @Test
    public void testIsSingleton() {
        Assert.assertThat(endpoint.isSingleton(), Matchers.is(true));
    }

    @Test
    public void testGetRegistry() {
        Assert.assertThat(endpoint.getRegistry(), Matchers.is(registry));
    }

    @Test
    public void testGetMetricsName() {
        Assert.assertThat(endpoint.getMetricsName(), Matchers.is(MicrometerEndpointTest.METRICS_NAME));
    }
}
