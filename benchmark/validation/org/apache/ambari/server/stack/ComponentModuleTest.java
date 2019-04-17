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
package org.apache.ambari.server.stack;


import UnlimitedKeyJCERequirement.ALWAYS;
import UnlimitedKeyJCERequirement.KERBEROS_ENABLED;
import UnlimitedKeyJCERequirement.NEVER;
import java.util.ArrayList;
import java.util.List;
import org.apache.ambari.server.state.AutoDeployInfo;
import org.apache.ambari.server.state.BulkCommandDefinition;
import org.apache.ambari.server.state.ClientConfigFileDefinition;
import org.apache.ambari.server.state.CommandScriptDefinition;
import org.apache.ambari.server.state.ComponentInfo;
import org.apache.ambari.server.state.CustomCommandDefinition;
import org.apache.ambari.server.state.DependencyInfo;
import org.junit.Assert;
import org.junit.Test;


/**
 * ComponentModule unit test case.
 */
public class ComponentModuleTest {
    @Test
    public void testResolve_CommandScript() {
        CommandScriptDefinition commandScript = new CommandScriptDefinition();
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setCommandScript(commandScript);
        Assert.assertSame(commandScript, resolveComponent(info, parentInfo).getModuleInfo().getCommandScript());
        // child has value set, parent value is null
        info.setCommandScript(commandScript);
        parentInfo.setCommandScript(null);
        Assert.assertSame(commandScript, resolveComponent(info, parentInfo).getModuleInfo().getCommandScript());
        // value set in both parent and child; child overwrites
        CommandScriptDefinition commandScript2 = createNiceMock(CommandScriptDefinition.class);
        info.setCommandScript(commandScript);
        parentInfo.setCommandScript(commandScript2);
        Assert.assertSame(commandScript, resolveComponent(info, parentInfo).getModuleInfo().getCommandScript());
    }

    @Test
    public void testResolve_DisplayName() {
        String displayName = "foo";
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setDisplayName(displayName);
        Assert.assertEquals(displayName, resolveComponent(info, parentInfo).getModuleInfo().getDisplayName());
        // child has value set, parent value is null
        info.setDisplayName(displayName);
        parentInfo.setDisplayName(null);
        Assert.assertEquals(displayName, resolveComponent(info, parentInfo).getModuleInfo().getDisplayName());
        // value set in both parent and child; child overwrites
        String displayName2 = "foo2";
        info.setDisplayName(displayName2);
        parentInfo.setDisplayName(displayName);
        Assert.assertEquals(displayName2, resolveComponent(info, parentInfo).getModuleInfo().getDisplayName());
    }

    @Test
    public void testResolve_ClientConfigFiles() {
        List<ClientConfigFileDefinition> clientConfigs = new ArrayList<>();
        ClientConfigFileDefinition clientConfig1 = new ClientConfigFileDefinition();
        clientConfig1.setType("type1");
        clientConfig1.setDictionaryName("dictName1");
        clientConfig1.setFileName("filename1");
        ClientConfigFileDefinition clientConfig2 = new ClientConfigFileDefinition();
        clientConfig1.setType("type1");
        clientConfig1.setDictionaryName("dictName1");
        clientConfig1.setFileName("filename1");
        clientConfigs.add(clientConfig1);
        clientConfigs.add(clientConfig2);
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setClientConfigFiles(clientConfigs);
        Assert.assertEquals(clientConfigs, resolveComponent(info, parentInfo).getModuleInfo().getClientConfigFiles());
        // child has value set, parent value is null
        info.setClientConfigFiles(clientConfigs);
        parentInfo.setClientConfigFiles(null);
        Assert.assertEquals(clientConfigs, resolveComponent(info, parentInfo).getModuleInfo().getClientConfigFiles());
        // value set in both parent and child; child overwrites with no merge
        List<ClientConfigFileDefinition> clientConfigs2 = new ArrayList<>();
        ClientConfigFileDefinition clientConfig3 = new ClientConfigFileDefinition();
        clientConfig3.setType("type1");
        clientConfig3.setDictionaryName("dictName1");
        clientConfig3.setFileName("DIFFERENT filename");
        clientConfigs2.add(clientConfig3);
        info.setClientConfigFiles(clientConfigs2);
        parentInfo.setClientConfigFiles(clientConfigs);
        Assert.assertEquals(clientConfigs2, resolveComponent(info, parentInfo).getModuleInfo().getClientConfigFiles());
    }

    @Test
    public void testResolve_Category() {
        String category = "foo";
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setCategory(category);
        Assert.assertEquals(category, resolveComponent(info, parentInfo).getModuleInfo().getCategory());
        // child has value set, parent value is null
        info.setCategory(category);
        parentInfo.setCategory(null);
        Assert.assertEquals(category, resolveComponent(info, parentInfo).getModuleInfo().getCategory());
        // value set in both parent and child; child overwrites
        String category2 = "foo2";
        info.setCategory(category2);
        parentInfo.setCategory(category);
        Assert.assertEquals(category2, resolveComponent(info, parentInfo).getModuleInfo().getCategory());
    }

    @Test
    public void testResolve_Cardinality() {
        String cardinality = "foo";
        ComponentInfo info = new ComponentInfo();
        // parent is null, child cardinality is null
        Assert.assertEquals("0+", resolveComponent(info, null).getModuleInfo().getCardinality());
        ComponentInfo parentInfo = new ComponentInfo();
        info = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setCardinality(cardinality);
        Assert.assertEquals("foo", resolveComponent(info, parentInfo).getModuleInfo().getCardinality());
        // child has value set, parent value is null
        info.setCardinality(cardinality);
        parentInfo.setCardinality(null);
        Assert.assertEquals(cardinality, resolveComponent(info, parentInfo).getModuleInfo().getCardinality());
        // value set in both parent and child; child overwrites
        String cardinality2 = "foo2";
        info.setCardinality(cardinality2);
        parentInfo.setCardinality(cardinality);
        Assert.assertEquals(cardinality2, resolveComponent(info, parentInfo).getModuleInfo().getCardinality());
    }

    @Test
    public void testResolve_TimelineAppId() {
        String timelineAppId = "app";
        ComponentInfo info = new ComponentInfo();
        Assert.assertEquals(null, resolveComponent(info, null).getModuleInfo().getTimelineAppid());
        ComponentInfo parentInfo = new ComponentInfo();
        info = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setTimelineAppid(timelineAppId);
        Assert.assertEquals(timelineAppId, resolveComponent(info, parentInfo).getModuleInfo().getTimelineAppid());
        // child has value set, parent value is null
        info.setTimelineAppid(timelineAppId);
        parentInfo.setTimelineAppid(null);
        Assert.assertEquals(timelineAppId, resolveComponent(info, parentInfo).getModuleInfo().getTimelineAppid());
        // value set in both parent and child; child overwrites
        String timelineAppId2 = "app2";
        info.setTimelineAppid(timelineAppId2);
        parentInfo.setTimelineAppid(timelineAppId);
        Assert.assertEquals(timelineAppId2, resolveComponent(info, parentInfo).getModuleInfo().getTimelineAppid());
    }

    @Test
    public void testResolve_AutoDeploy() {
        AutoDeployInfo autoDeployInfo = new AutoDeployInfo();
        autoDeployInfo.setEnabled(true);
        autoDeployInfo.setCoLocate("foo/bar");
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setAutoDeploy(autoDeployInfo);
        Assert.assertEquals(autoDeployInfo, resolveComponent(info, parentInfo).getModuleInfo().getAutoDeploy());
        // child has value set, parent value is null
        info.setAutoDeploy(autoDeployInfo);
        parentInfo.setAutoDeploy(null);
        Assert.assertEquals(autoDeployInfo, resolveComponent(info, parentInfo).getModuleInfo().getAutoDeploy());
        // value set in both parent and child; child overwrites
        AutoDeployInfo autoDeployInfo2 = new AutoDeployInfo();
        info.setAutoDeploy(autoDeployInfo);
        parentInfo.setAutoDeploy(autoDeployInfo2);
        Assert.assertEquals(autoDeployInfo, resolveComponent(info, parentInfo).getModuleInfo().getAutoDeploy());
    }

    @Test
    public void testResolve_Dependencies() {
        List<DependencyInfo> dependencies = new ArrayList<>();
        DependencyInfo dependency1 = new DependencyInfo();
        dependency1.setName("service/one");
        DependencyInfo dependency2 = new DependencyInfo();
        dependency2.setName("service/two");
        dependencies.add(dependency1);
        dependencies.add(dependency2);
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setDependencies(dependencies);
        Assert.assertEquals(dependencies, resolveComponent(info, parentInfo).getModuleInfo().getDependencies());
        // child has value set, parent value is null
        info.setDependencies(dependencies);
        parentInfo.setDependencies(null);
        Assert.assertEquals(dependencies, resolveComponent(info, parentInfo).getModuleInfo().getDependencies());
        // value set in both parent and child; merge parent and child
        // todo: currently there is no way to remove an inherited dependency
        List<DependencyInfo> dependencies2 = new ArrayList<>();
        DependencyInfo dependency3 = new DependencyInfo();
        dependency3.setName("service/two");
        DependencyInfo dependency4 = new DependencyInfo();
        dependency4.setName("service/four");
        dependencies2.add(dependency3);
        dependencies2.add(dependency4);
        info.setDependencies(dependencies2);
        parentInfo.setDependencies(dependencies);
        List<DependencyInfo> resolvedDependencies = resolveComponent(info, parentInfo).getModuleInfo().getDependencies();
        Assert.assertEquals(3, resolvedDependencies.size());
        Assert.assertTrue(resolvedDependencies.contains(dependency1));
        Assert.assertTrue(resolvedDependencies.contains(dependency3));
        Assert.assertTrue(resolvedDependencies.contains(dependency4));
    }

    @Test
    public void testResolve_CustomCommands() throws Exception {
        List<CustomCommandDefinition> commands = new ArrayList<>();
        CustomCommandDefinition command1 = new CustomCommandDefinition();
        setPrivateField(command1, "name", "one");
        CustomCommandDefinition command2 = new CustomCommandDefinition();
        setPrivateField(command2, "name", "two");
        commands.add(command1);
        commands.add(command2);
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setCustomCommands(commands);
        Assert.assertEquals(commands, resolveComponent(info, parentInfo).getModuleInfo().getCustomCommands());
        // child has value set, parent value is null
        info.setCustomCommands(commands);
        parentInfo.setCustomCommands(null);
        Assert.assertEquals(commands, resolveComponent(info, parentInfo).getModuleInfo().getCustomCommands());
        // value set in both parent and child; merge parent and child
        // todo: currently there is no way to remove an inherited command
        List<CustomCommandDefinition> commands2 = new ArrayList<>();
        CustomCommandDefinition command3 = new CustomCommandDefinition();
        // override command 2
        setPrivateField(command3, "name", "two");
        CustomCommandDefinition command4 = new CustomCommandDefinition();
        setPrivateField(command4, "name", "four");
        commands2.add(command3);
        commands2.add(command4);
        info.setCustomCommands(commands2);
        parentInfo.setCustomCommands(commands);
        List<CustomCommandDefinition> resolvedCommands = resolveComponent(info, parentInfo).getModuleInfo().getCustomCommands();
        Assert.assertEquals(3, resolvedCommands.size());
        Assert.assertTrue(resolvedCommands.contains(command1));
        Assert.assertTrue(resolvedCommands.contains(command3));
        Assert.assertTrue(resolvedCommands.contains(command4));
    }

    // merging of config dependencies is different than other non-module merges in that the collections aren't
    // merged if any config dependency is specified in the child.  So, the merged result is either the child
    // dependencies or if null, the parent dependencies.
    @Test
    public void testResolve_ConfigDependencies() {
        List<String> dependencies = new ArrayList<>();
        String dependency1 = "one";
        String dependency2 = "two";
        dependencies.add(dependency1);
        dependencies.add(dependency2);
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setConfigDependencies(dependencies);
        Assert.assertEquals(dependencies, resolveComponent(info, parentInfo).getModuleInfo().getConfigDependencies());
        // child has value set, parent value is null
        info.setConfigDependencies(dependencies);
        parentInfo.setConfigDependencies(null);
        Assert.assertEquals(dependencies, resolveComponent(info, parentInfo).getModuleInfo().getConfigDependencies());
        // value set in both parent and child; merge parent and child
        List<String> dependencies2 = new ArrayList<>();
        String dependency3 = "two";
        String dependency4 = "four";
        dependencies2.add(dependency3);
        dependencies2.add(dependency4);
        info.setConfigDependencies(dependencies2);
        parentInfo.setConfigDependencies(dependencies);
        List<String> resolvedDependencies = resolveComponent(info, parentInfo).getModuleInfo().getConfigDependencies();
        Assert.assertEquals(2, resolvedDependencies.size());
        Assert.assertTrue(resolvedDependencies.contains(dependency3));
        Assert.assertTrue(resolvedDependencies.contains(dependency4));
    }

    // merging of "client to update configs", whatever that means, is different than most other non-module merges
    // in that the collections aren't merged if any "client to update configs" is specified in the child.
    // So, the merged result is either the child collection or if null, the parent collection.
    @Test
    public void testResolve_ClientToUpdateConfigs() {
        List<String> clientsToUpdate = new ArrayList<>();
        String client1 = "one";
        String client2 = "two";
        clientsToUpdate.add(client1);
        clientsToUpdate.add(client2);
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setClientsToUpdateConfigs(clientsToUpdate);
        Assert.assertEquals(clientsToUpdate, resolveComponent(info, parentInfo).getModuleInfo().getClientsToUpdateConfigs());
        // child has value set, parent value is null
        info.setClientsToUpdateConfigs(clientsToUpdate);
        parentInfo.setClientsToUpdateConfigs(null);
        Assert.assertEquals(clientsToUpdate, resolveComponent(info, parentInfo).getModuleInfo().getClientsToUpdateConfigs());
        // value set in both parent and child; merge parent and child
        List<String> clientsToUpdate2 = new ArrayList<>();
        String client3 = "two";
        String client4 = "four";
        clientsToUpdate2.add(client3);
        clientsToUpdate2.add(client4);
        info.setClientsToUpdateConfigs(clientsToUpdate2);
        parentInfo.setClientsToUpdateConfigs(clientsToUpdate);
        List<String> resolvedClientsToUpdate = resolveComponent(info, parentInfo).getModuleInfo().getClientsToUpdateConfigs();
        Assert.assertEquals(2, resolvedClientsToUpdate.size());
        Assert.assertTrue(resolvedClientsToUpdate.contains(client3));
        Assert.assertTrue(resolvedClientsToUpdate.contains(client4));
    }

    @Test
    public void testGetId() {
        ComponentInfo info = new ComponentInfo();
        info.setName("foo");
        ComponentModule component = new ComponentModule(info);
        Assert.assertEquals("foo", component.getId());
    }

    @Test
    public void testIsDeleted() {
        // default value
        ComponentInfo info = new ComponentInfo();
        info.setName("foo");
        ComponentModule component = new ComponentModule(info);
        Assert.assertFalse(component.isDeleted());
        // explicit value
        info = new ComponentInfo();
        info.setName("foo");
        info.setDeleted(true);
        component = new ComponentModule(info);
        Assert.assertTrue(component.isDeleted());
    }

    @Test
    public void testResolve_BulkCommandsDefinition() {
        BulkCommandDefinition bulkCommandsDefinition = new BulkCommandDefinition();
        ComponentInfo info = new ComponentInfo();
        ComponentInfo parentInfo = new ComponentInfo();
        // parent has value set, child value is null
        parentInfo.setBulkCommands(bulkCommandsDefinition);
        Assert.assertSame(bulkCommandsDefinition, resolveComponent(info, parentInfo).getModuleInfo().getBulkCommandDefinition());
        // child has value set, parent value is null
        info.setBulkCommands(bulkCommandsDefinition);
        parentInfo.setBulkCommands(null);
        Assert.assertSame(bulkCommandsDefinition, resolveComponent(info, parentInfo).getModuleInfo().getBulkCommandDefinition());
        // value set in both parent and child; child overwrites
        BulkCommandDefinition bulkCommandsDefinition2 = createNiceMock(BulkCommandDefinition.class);
        info.setBulkCommands(bulkCommandsDefinition);
        parentInfo.setBulkCommands(bulkCommandsDefinition2);
        Assert.assertSame(bulkCommandsDefinition, resolveComponent(info, parentInfo).getModuleInfo().getBulkCommandDefinition());
    }

    @Test
    public void testResolve_DecommissionAllowedInheritance() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child doesn't
        parentInfo.setDecommissionAllowed("true");
        Assert.assertSame("true", resolveComponent(info, parentInfo).getModuleInfo().getDecommissionAllowed());
    }

    @Test
    public void testResolve_DecommissionAllowed() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent doesn't have it, child has it
        info.setDecommissionAllowed("false");
        Assert.assertSame("false", resolveComponent(info, parentInfo).getModuleInfo().getDecommissionAllowed());
    }

    @Test
    public void testResolve_DecommissionAllowedOverwrite() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child overwrites it
        parentInfo.setDecommissionAllowed("false");
        info.setDecommissionAllowed("true");
        Assert.assertSame("true", resolveComponent(info, parentInfo).getModuleInfo().getDecommissionAllowed());
    }

    @Test
    public void testResolve_UnlimitedKeyJCERequiredInheritance() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child doesn't
        parentInfo.setUnlimitedKeyJCERequired(ALWAYS);
        Assert.assertSame(ALWAYS, resolveComponent(info, parentInfo).getModuleInfo().getUnlimitedKeyJCERequired());
    }

    @Test
    public void testResolve_UnlimitedKeyJCERequired() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent doesn't have it, child has it
        info.setUnlimitedKeyJCERequired(NEVER);
        Assert.assertSame(NEVER, resolveComponent(info, parentInfo).getModuleInfo().getUnlimitedKeyJCERequired());
    }

    @Test
    public void testResolve_UnlimitedKeyJCERequiredOverwrite() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child overwrites it
        parentInfo.setUnlimitedKeyJCERequired(KERBEROS_ENABLED);
        info.setUnlimitedKeyJCERequired(ALWAYS);
        Assert.assertSame(ALWAYS, resolveComponent(info, parentInfo).getModuleInfo().getUnlimitedKeyJCERequired());
    }

    @Test
    public void testResolve_Reassignable() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent doesn't have it, child has it
        info.setReassignAllowed("false");
        Assert.assertSame("false", resolveComponent(info, parentInfo).getModuleInfo().getReassignAllowed());
    }

    @Test
    public void testResolve_ReassignableInheritance() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child doesn't
        parentInfo.setReassignAllowed("true");
        Assert.assertSame("true", resolveComponent(info, parentInfo).getModuleInfo().getReassignAllowed());
    }

    @Test
    public void testResolve_ReassignableOverwrite() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // parent has it, child overwrites it
        parentInfo.setReassignAllowed("false");
        info.setReassignAllowed("true");
        Assert.assertSame("true", resolveComponent(info, parentInfo).getModuleInfo().getReassignAllowed());
    }

    /**
     * Test that versionAdvertised is resolved correctly.
     */
    @Test
    public void testResolve_VersionAdvertised() {
        List<ComponentInfo> components = createComponentInfo(2);
        ComponentInfo info = components.get(0);
        ComponentInfo parentInfo = components.get(1);
        // Test cases where the current Component Info explicitly sets the value.
        // 1. Chain of versionAdvertised is: true (parent) -> true (current) => true
        parentInfo.setVersionAdvertisedField(new Boolean(true));
        parentInfo.setVersionAdvertised(true);
        info.setVersionAdvertisedField(new Boolean(true));
        Assert.assertEquals(true, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 2. Chain of versionAdvertised is: true (parent) -> false (current) => false
        parentInfo.setVersionAdvertisedField(new Boolean(true));
        parentInfo.setVersionAdvertised(true);
        info.setVersionAdvertisedField(new Boolean(false));
        Assert.assertEquals(false, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 3. Chain of versionAdvertised is: false (parent) -> true (current) => true
        parentInfo.setVersionAdvertisedField(new Boolean(false));
        parentInfo.setVersionAdvertised(false);
        info.setVersionAdvertisedField(new Boolean(true));
        Assert.assertEquals(true, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 4. Chain of versionAdvertised is: null (parent) -> true (current) => true
        parentInfo.setVersionAdvertisedField(null);
        parentInfo.setVersionAdvertised(false);
        info.setVersionAdvertisedField(new Boolean(true));
        Assert.assertEquals(true, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // Test cases where current Component Info is null so it should inherit from parent.
        // 5. Chain of versionAdvertised is: true (parent) -> null (current) => true
        parentInfo.setVersionAdvertisedField(new Boolean(true));
        parentInfo.setVersionAdvertised(true);
        info.setVersionAdvertisedField(null);
        Assert.assertEquals(true, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 6. Chain of versionAdvertised is: true (parent) -> inherit (current) => true
        parentInfo.setVersionAdvertisedField(new Boolean(true));
        parentInfo.setVersionAdvertised(true);
        info.setVersionAdvertisedField(null);
        Assert.assertEquals(true, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 7. Chain of versionAdvertised is: false (parent) -> null (current) => false
        parentInfo.setVersionAdvertisedField(new Boolean(false));
        parentInfo.setVersionAdvertised(false);
        info.setVersionAdvertisedField(null);
        Assert.assertEquals(false, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
        // 8. Chain of versionAdvertised is: false (parent) -> inherit (current) => false
        parentInfo.setVersionAdvertisedField(new Boolean(false));
        parentInfo.setVersionAdvertised(false);
        info.setVersionAdvertisedField(null);
        Assert.assertEquals(false, resolveComponent(info, parentInfo).getModuleInfo().isVersionAdvertised());
    }
}
