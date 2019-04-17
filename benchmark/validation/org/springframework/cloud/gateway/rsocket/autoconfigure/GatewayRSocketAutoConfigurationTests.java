/**
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.cloud.gateway.rsocket.autoconfigure;


import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;


public class GatewayRSocketAutoConfigurationTests {
    @Test
    public void gatewayRSocketConfigured() {
        new ReactiveWebApplicationContextRunner().withConfiguration(AutoConfigurations.of(GatewayRSocketAutoConfiguration.class, CompositeMeterRegistryAutoConfiguration.class, MetricsAutoConfiguration.class)).run(( context) -> assertThat(context).hasSingleBean(.class).hasSingleBean(.class).hasSingleBean(.class).hasSingleBean(.class).hasSingleBean(.class).hasSingleBean(.class).hasSingleBean(.class).doesNotHaveBean(.class));
    }
}
