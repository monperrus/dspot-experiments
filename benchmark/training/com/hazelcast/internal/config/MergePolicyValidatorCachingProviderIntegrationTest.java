/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.internal.config;


import com.hazelcast.cache.merge.PutIfAbsentCacheMergePolicy;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.spi.merge.MergingCosts;
import com.hazelcast.spi.merge.SplitBrainMergeTypes.MapMergeTypes;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


/**
 * Tests the integration of the {@link MergePolicyValidator}
 * into the proxy creation of split-brain capable data structures.
 */
@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class MergePolicyValidatorCachingProviderIntegrationTest extends AbstractMergePolicyValidatorIntegrationTest {
    @Test
    public void testCache_withPutIfAbsentMergePolicy() {
        getCache("putIfAbsent", putIfAbsentMergePolicy);
    }

    @Test
    public void testCache_withHyperLogLogMergePolicy() {
        expectCardinalityEstimatorException();
        getCache("cardinalityEstimator", hyperLogLogMergePolicy);
    }

    @Test
    public void testCache_withHigherHitsMergePolicy() {
        getCache("higherHits", higherHitsMergePolicy);
    }

    @Test
    public void testCache_withInvalidMergePolicy() {
        expectedInvalidMergePolicyException();
        getCache("invalid", invalidMergePolicyConfig);
    }

    @Test
    public void testCache_withExpirationTimeMergePolicy() {
        getCache("expirationTime", expirationTimeMergePolicy);
    }

    /**
     * ICache provides only the required {@link MergingExpirationTime},
     * but not the required {@link MergingCosts} from the
     * {@link ComplexCustomMergePolicy}.
     * <p>
     * The thrown exception should contain the merge policy name
     * and the missing merge type.
     */
    @Test
    public void testCache_withComplexCustomMergePolicy() {
        expectedException.expect(InvalidConfigurationException.class);
        expectedException.expectMessage(CoreMatchers.containsString(complexCustomMergePolicy.getPolicy()));
        expectedException.expectMessage(CoreMatchers.containsString(MergingCosts.class.getName()));
        getCache("complexCustom", complexCustomMergePolicy);
    }

    /**
     * ICache provides only some of the required merge types
     * of {@link MapMergeTypes}.
     * <p>
     * The thrown exception should contain the merge policy name
     * and the missing merge type.
     */
    @Test
    public void testCache_withCustomMapMergePolicyNoTypeVariable() {
        expectedException.expect(InvalidConfigurationException.class);
        expectedException.expectMessage(CoreMatchers.containsString(customMapMergePolicyNoTypeVariable.getPolicy()));
        expectedException.expectMessage(CoreMatchers.containsString(MapMergeTypes.class.getName()));
        getCache("customMapNoTypeVariable", customMapMergePolicyNoTypeVariable);
    }

    /**
     * ICache provides only some of the required merge types
     * of {@link MapMergeTypes}.
     * <p>
     * The thrown exception should contain the merge policy name
     * and the missing merge type.
     */
    @Test
    public void testCache_withCustomMapMergePolicy() {
        expectedException.expect(InvalidConfigurationException.class);
        expectedException.expectMessage(CoreMatchers.containsString(customMapMergePolicy.getPolicy()));
        expectedException.expectMessage(CoreMatchers.containsString(MapMergeTypes.class.getName()));
        getCache("customMap", customMapMergePolicy);
    }

    @Test
    public void testCache_withLegacyPutIfAbsentMergePolicy() {
        MergePolicyConfig legacyMergePolicyConfig = new MergePolicyConfig().setPolicy(PutIfAbsentCacheMergePolicy.class.getName());
        getCache("legacyPutIfAbsent", legacyMergePolicyConfig);
    }
}
