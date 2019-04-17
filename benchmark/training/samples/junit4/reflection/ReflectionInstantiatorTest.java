/**
 * Copyright 2009 the original author or authors.
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
package samples.junit4.reflection;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import samples.reflection.ReflectionInstantiator;
import samples.reflection.UseMe;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ UseMe.class })
public class ReflectionInstantiatorTest {
    @Test
    public void whenClassIsInstantiatedUsingReflectionMakeSureItCanBeCastedToPowerMockLoadedVersionOfTheClass() throws Exception {
        ReflectionInstantiator reflectionInstantiator = new ReflectionInstantiator();
        Assert.assertTrue("Reflection instantiation doesn't work, Thread context class-loader not set to MockCL", reflectionInstantiator.instantiateUseMe());
    }

    @Test
    @PrepareForTest(UseMe.class)
    public void whenClassIsInstantiatedUsingReflectionMakeSureItCanBeCastedToPowerMockLoadedVersionOfTheClassWhenUsingChunking() throws Exception {
        ReflectionInstantiator reflectionInstantiator = new ReflectionInstantiator();
        Assert.assertTrue("Reflection instantiation doesn't work, Thread context class-loader not set to MockCL", reflectionInstantiator.instantiateUseMe());
    }
}
