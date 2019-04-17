/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.shardingsphere.core.yaml.engine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlRootMasterSlaveConfiguration;
import org.junit.Assert;
import org.junit.Test;


public final class YamlEngineMasterSlaveConfigurationTest {
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/master-slave-rule.yaml");
        Assert.assertNotNull(url);
        assertYamlMasterSlaveConfig(YamlEngine.unmarshal(new File(url.getFile()), YamlRootMasterSlaveConfiguration.class));
    }

    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/master-slave-rule.yaml");
        Assert.assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (FileReader fileReader = new FileReader(url.getFile());BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append("\n");
            } 
        }
        assertYamlMasterSlaveConfig(YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootMasterSlaveConfiguration.class));
    }
}
