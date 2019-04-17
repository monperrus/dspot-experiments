/**
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
package org.flowable.spring.test.autodeployment;


import DefaultAutoDeploymentStrategy.DEPLOYMENT_MODE;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;
import org.flowable.spring.configurator.DefaultAutoDeploymentStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;


/**
 *
 *
 * @author Tiese Barrell
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultAutoDeploymentStrategyTest extends AbstractAutoDeploymentStrategyTest {
    private DefaultAutoDeploymentStrategy classUnderTest;

    @Test
    public void testHandlesMode() {
        Assert.assertTrue(classUnderTest.handlesMode(DEPLOYMENT_MODE));
        Assert.assertFalse(classUnderTest.handlesMode("other-mode"));
        Assert.assertFalse(classUnderTest.handlesMode(null));
    }

    @Test
    public void testDeployResources() {
        final Resource[] resources = new Resource[]{ resourceMock1, resourceMock2, resourceMock3, resourceMock4, resourceMock5 };
        classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock);
        Mockito.verify(repositoryServiceMock, Mockito.times(1)).createDeployment();
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).enableDuplicateFiltering();
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).name(deploymentNameHint);
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).addInputStream(ArgumentMatchers.eq(resourceName1), ArgumentMatchers.isA(InputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).addInputStream(ArgumentMatchers.eq(resourceName2), ArgumentMatchers.isA(InputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.times(3)).addZipInputStream(ArgumentMatchers.isA(ZipInputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).deploy();
    }

    @Test
    public void testDeployResourcesNoResources() {
        final Resource[] resources = new Resource[]{  };
        classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock);
        Mockito.verify(repositoryServiceMock, Mockito.times(1)).createDeployment();
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).enableDuplicateFiltering();
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).name(deploymentNameHint);
        Mockito.verify(deploymentBuilderMock, Mockito.never()).addInputStream(ArgumentMatchers.isA(String.class), ArgumentMatchers.isA(InputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.never()).addInputStream(ArgumentMatchers.eq(resourceName2), ArgumentMatchers.isA(InputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.never()).addZipInputStream(ArgumentMatchers.isA(ZipInputStream.class));
        Mockito.verify(deploymentBuilderMock, Mockito.times(1)).deploy();
    }

    @Test
    public void testDetermineResourceNameWithExceptionFailsGracefully() throws Exception {
        Mockito.when(resourceMock3.getFile()).thenThrow(new IOException());
        Mockito.when(resourceMock3.getFilename()).thenReturn(resourceName3);
        final Resource[] resources = new Resource[]{ resourceMock3 };
        classUnderTest.deployResources(deploymentNameHint, resources, repositoryServiceMock);
    }
}
