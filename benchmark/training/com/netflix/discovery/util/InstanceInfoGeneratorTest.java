/**
 * Copyright 2015 Netflix, Inc.
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
package com.netflix.discovery.util;


import com.netflix.appinfo.InstanceInfo;
import java.util.Iterator;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Tomasz Bak
 */
public class InstanceInfoGeneratorTest {
    @Test
    public void testInstanceInfoStream() throws Exception {
        Iterator<InstanceInfo> it = InstanceInfoGenerator.newBuilder(4, "app1", "app2").build().serviceIterator();
        Assert.assertThat(it.next().getAppName(), CoreMatchers.is(CoreMatchers.equalTo("APP1")));
        Assert.assertThat(it.next().getAppName(), CoreMatchers.is(CoreMatchers.equalTo("APP2")));
        Assert.assertThat(it.next().getAppName(), CoreMatchers.is(CoreMatchers.equalTo("APP1")));
        Assert.assertThat(it.next().getAppName(), CoreMatchers.is(CoreMatchers.equalTo("APP2")));
    }
}
