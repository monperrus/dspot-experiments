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
package org.apache.camel.component.docker.headers;


import DockerConstants.DOCKER_CONTAINER_ID;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


/**
 * Validates Wait Container Request headers are applied properly
 */
public class WaitContainerCmdHeaderTest extends BaseDockerHeaderTest<WaitContainerCmd> {
    @Mock
    private WaitContainerCmd mockObject;

    @Mock
    private WaitContainerResultCallback callback;

    @Test
    public void waitContainerHeaderTest() {
        String containerId = "9c09acd48a25";
        Map<String, Object> headers = getDefaultParameters();
        headers.put(DOCKER_CONTAINER_ID, containerId);
        template.sendBodyAndHeaders("direct:in", "", headers);
        Mockito.verify(dockerClient, Mockito.times(1)).waitContainerCmd(containerId);
    }
}
