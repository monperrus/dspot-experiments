/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.security;


import java.util.Properties;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;
import org.apache.geode.test.junit.categories.SecurityTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.apache.geode.internal.Assert.assertTrue;


@Category({ SecurityTest.class })
public class SimpleSecurityManagerTest {
    private SimpleTestSecurityManager manager;

    private Properties credentials;

    @Test
    public void testAuthenticateSuccess() {
        credentials.put("security-username", "user");
        credentials.put("security-password", "user");
        Assert.assertEquals("user", manager.authenticate(credentials));
    }

    @Test
    public void testAuthenticateFail() {
        credentials.put("security-username", "user1");
        credentials.put("security-password", "user2");
        assertThatThrownBy(() -> manager.authenticate(credentials)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    public void testAuthenticateFailNull() {
        assertThatThrownBy(() -> manager.authenticate(credentials)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    public void testAuthorization() {
        ResourcePermission permission = new ResourcePermission(Resource.CLUSTER, Operation.READ);
        assertTrue(manager.authorize("clusterRead", permission));
        assertTrue(manager.authorize("cluster", permission));
        Assert.assertFalse(manager.authorize("data", permission));
        permission = new ResourcePermission(Resource.DATA, Operation.WRITE, "regionA", "key1");
        assertTrue(manager.authorize("data", permission));
        assertTrue(manager.authorize("dataWrite", permission));
        assertTrue(manager.authorize("dataWriteRegionA", permission));
        assertTrue(manager.authorize("dataWriteRegionAKey1", permission));
        Assert.assertFalse(manager.authorize("dataRead", permission));
    }

    @Test
    public void testMultipleRoleAuthorization() {
        ResourcePermission permission = new ResourcePermission(Resource.CLUSTER, Operation.READ);
        assertTrue(manager.authorize("clusterRead,clusterWrite", permission));
        assertTrue(manager.authorize("cluster,data", permission));
        Assert.assertFalse(manager.authorize("clusterWrite,data", permission));
        permission = new ResourcePermission(Resource.DATA, Operation.WRITE, "regionA", "key1");
        assertTrue(manager.authorize("data,cluster", permission));
        assertTrue(manager.authorize("dataWrite,clusterWrite", permission));
    }
}
