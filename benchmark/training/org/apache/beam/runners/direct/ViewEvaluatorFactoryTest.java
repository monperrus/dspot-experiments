/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.direct;


import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.runners.AppliedPTransform;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;


/**
 * Tests for {@link ViewEvaluatorFactory}.
 */
@RunWith(JUnit4.class)
public class ViewEvaluatorFactoryTest {
    private BundleFactory bundleFactory = ImmutableListBundleFactory.create();

    @Rule
    public TestPipeline p = TestPipeline.create().enableAbandonedNodeEnforcement(false);

    @Test
    public void testInMemoryEvaluator() throws Exception {
        PCollection<String> input = p.apply(Create.of("foo", "bar"));
        PCollectionView<Iterable<String>> pCollectionView = input.apply(View.asIterable());
        PCollection<Iterable<String>> concat = input.apply(WithKeys.of(((Void) (null)))).setCoder(KvCoder.of(VoidCoder.of(), StringUtf8Coder.of())).apply(GroupByKey.create()).apply(Values.create());
        PCollection<Iterable<String>> view = concat.apply(new ViewOverrideFactory.WriteView<>(pCollectionView));
        EvaluationContext context = Mockito.mock(EvaluationContext.class);
        ViewEvaluatorFactoryTest.TestViewWriter<String, Iterable<String>> viewWriter = new ViewEvaluatorFactoryTest.TestViewWriter<>();
        Mockito.when(context.createPCollectionViewWriter(concat, pCollectionView)).thenReturn(viewWriter);
        CommittedBundle<String> inputBundle = bundleFactory.createBundle(input).commit(Instant.now());
        AppliedPTransform<?, ?, ?> producer = DirectGraphs.getProducer(view);
        TransformEvaluator<Iterable<String>> evaluator = new ViewEvaluatorFactory(context).forApplication(producer, inputBundle);
        evaluator.processElement(WindowedValue.valueInGlobalWindow(ImmutableList.of("foo", "bar")));
        Assert.assertThat(viewWriter.latest, Matchers.nullValue());
        evaluator.finishBundle();
        Assert.assertThat(viewWriter.latest, Matchers.containsInAnyOrder(WindowedValue.valueInGlobalWindow("foo"), WindowedValue.valueInGlobalWindow("bar")));
    }

    private static class TestViewWriter<ElemT, ViewT> implements PCollectionViewWriter<ElemT, ViewT> {
        private Iterable<WindowedValue<ElemT>> latest;

        @Override
        public void add(Iterable<WindowedValue<ElemT>> values) {
            latest = values;
        }
    }
}
