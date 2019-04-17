/**
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.config;


import io.helidon.common.CollectionsHelper;
import io.helidon.config.ProviderImpl.ChainConfigFilter;
import io.helidon.config.internal.ConfigKeyImpl;
import io.helidon.config.spi.ConfigFilter;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


/**
 * Tests {@link ChainConfigFilter}.
 */
public class ChainConfigFilterTest {
    @Test
    public void testEmptyConfigFilterList() {
        final String stringValue = "string value";
        ChainConfigFilter chain = new ChainConfigFilter();
        MatcherAssert.assertThat(chain.apply(ConfigKeyImpl.of("any"), stringValue), Matchers.is(stringValue));
    }

    @Test
    public void testAddFilterAfterEnablingCache() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            ChainConfigFilter chain = new ChainConfigFilter();
            chain.enableCaching();
            chain.addFilter(( key, value) -> value);
        });
    }

    @Test
    public void testSingleConfigFilterList() {
        final String originalValue = "string value";
        final String newValue = "new value";
        ConfigFilter filter = ( key, stringValue) -> {
            assertThat(stringValue, is(originalValue));
            return newValue;
        };
        ChainConfigFilter chain = new ChainConfigFilter();
        chain.addFilter(filter);
        MatcherAssert.assertThat(chain.apply(ConfigKeyImpl.of("any"), originalValue), Matchers.is(newValue));
    }

    @Test
    public void testDoubleConfigFilterList() {
        final String key = "key";
        final String originalValue = "string value";
        final String secondValue = "second value";
        final String lastValue = "the last value";
        ConfigFilter first = ( key1, stringValue) -> {
            assertThat(stringValue, is(originalValue));
            return secondValue;
        };
        ConfigFilter second = ( key1, stringValue) -> {
            assertThat(stringValue, is(secondValue));
            return lastValue;
        };
        ChainConfigFilter chain = new ChainConfigFilter();
        chain.addFilter(first);
        chain.addFilter(second);
        MatcherAssert.assertThat(chain.apply(ConfigKeyImpl.of(key), originalValue), Matchers.is(lastValue));
    }

    @Test
    public void testEmptyConfigFilterListWithConfig() {
        final String key = "app.key1";
        final String originalValue = "string value";
        final String defaultValue = "default value";
        Config config = Config.builder().sources(ConfigSources.create(new HashMap<String, String>() {
            {
                put(key, originalValue);
            }
        })).build();
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue(originalValue)));
        MatcherAssert.assertThat(config.get("missing-key").asString().orElse(defaultValue), Matchers.is(defaultValue));
    }

    @Test
    public void testSingleConfigFilterListWithConfig() {
        final String key = "app.key1";
        final String originalValue = "string value";
        final String newValue = "new value";
        final String defaultValue = "default value";
        Config config = Config.builder().sources(ConfigSources.create(CollectionsHelper.mapOf(key, originalValue))).addFilter(new AssertingFilter.Provider(key, originalValue, () -> newValue)).build();
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue(newValue)));
        MatcherAssert.assertThat(config.get("missing-key").asString().orElse(defaultValue), Matchers.is(defaultValue));
    }

    /**
     * The "quad" tests make sure that a sequence of 1, 2, or 3 AssertingFilters
     * yield the correct intermediate and final results.
     * <p>
     * The AssertingFilter maps a particular key to a new value and makes sure that
     * the current value as reported by the Config instance is the old value it was
     * given during set-up. (This checks the order in which the filters are applied.)
     * The class also make sure that during {@code init} the Config reports the
     * expected value resulting from applying <em>all</em> filters, not just
     * the filters that were added before the current one.
     */
    private static final class Quad {
        static final String key = "app.key1";

        static final String originalValue = "string value";

        static final String secondValue = "second value";

        static final String thirdValue = "third value";

        static final String lastValue = "the last value";

        static final String defaultValue = "default value";

        static final String referenceKey = "app.key-reference";

        static final String referenceValue = "$app.key1";

        static final AssertingFilter.Provider firstFilter = new AssertingFilter.Provider(ChainConfigFilterTest.Quad.key, ChainConfigFilterTest.Quad.originalValue, () -> ChainConfigFilterTest.Quad.secondValue);

        static final AssertingFilter.Provider secondFilter = new AssertingFilter.Provider(ChainConfigFilterTest.Quad.key, ChainConfigFilterTest.Quad.secondValue, () -> ChainConfigFilterTest.Quad.thirdValue);

        static final AssertingFilter.Provider thirdFilter = new AssertingFilter.Provider(ChainConfigFilterTest.Quad.key, ChainConfigFilterTest.Quad.thirdValue, () -> ChainConfigFilterTest.Quad.lastValue);
    }

    @Test
    public void testQuadrupleConfigFilterListWithConfig() {
        runQuadTests(true);// with caching

    }

    @Test
    public void testQuadrupleConfigFilterListWithConfigWithoutCache() {
        runQuadTests(false);// without caching

    }

    @Test
    public void testValueCachedWithConfigCachingEnabled() {
        String key = "app.key1";
        String originalValue = "string value";
        AtomicInteger counter = new AtomicInteger();
        Config config = Config.builder().sources(ConfigSources.create(CollectionsHelper.mapOf(key, originalValue))).addFilter(new AssertingFilter.Provider(key, originalValue, () -> (originalValue + ":") + (counter.incrementAndGet()))).build();
        // first call -> value cached cached
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue((originalValue + ":1"))));
        MatcherAssert.assertThat(counter.get(), Matchers.is(1));
        // second call <- used cached value
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue((originalValue + ":1"))));
        MatcherAssert.assertThat(counter.get(), Matchers.is(1));
    }

    @Test
    public void testValueCachedWithConfigCachingDisabled() {
        String key = "app.key1";
        String originalValue = "string value";
        AtomicInteger counter = new AtomicInteger();
        Config config = Config.builder().sources(ConfigSources.create(CollectionsHelper.mapOf(key, originalValue))).addFilter(new AssertingFilter.Provider(key, originalValue, () -> (originalValue + ":") + (counter.incrementAndGet()))).disableCaching().build();
        // first call
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue((originalValue + ":1"))));
        MatcherAssert.assertThat(counter.get(), Matchers.is(1));
        // second call
        MatcherAssert.assertThat(config.get(key).asString(), Matchers.is(ConfigValues.simpleValue((originalValue + ":2"))));
        MatcherAssert.assertThat(counter.get(), Matchers.is(2));
    }

    class ReferenceFilter implements ConfigFilter {
        private Config configRoot;

        ReferenceFilter(Config configRoot) {
            this.configRoot = configRoot;
        }

        @Override
        public String apply(Config.Key key, String stringValue) {
            if (stringValue.startsWith("$")) {
                String ref = stringValue.substring(1);
                return configRoot.get(ref).asString().get();
            }
            return stringValue;
        }
    }
}
