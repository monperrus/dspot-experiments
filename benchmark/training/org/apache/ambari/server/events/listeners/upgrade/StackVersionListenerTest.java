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
package org.apache.ambari.server.events.listeners.upgrade;


import UpgradeState.COMPLETE;
import UpgradeState.IN_PROGRESS;
import UpgradeState.NONE;
import UpgradeState.VERSION_MISMATCH;
import com.google.inject.Provider;
import java.lang.reflect.Field;
import junit.framework.Assert;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.agent.stomp.HostLevelParamsHolder;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.events.HostComponentVersionAdvertisedEvent;
import org.apache.ambari.server.events.publishers.VersionEventPublisher;
import org.apache.ambari.server.orm.dao.RepositoryVersionDAO;
import org.apache.ambari.server.orm.entities.HostVersionEntity;
import org.apache.ambari.server.orm.entities.RepositoryVersionEntity;
import org.apache.ambari.server.orm.entities.UpgradeEntity;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.ComponentInfo;
import org.apache.ambari.server.state.Host;
import org.apache.ambari.server.state.Service;
import org.apache.ambari.server.state.ServiceComponent;
import org.apache.ambari.server.state.ServiceComponentHost;
import org.apache.ambari.server.state.StackId;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * StackVersionListener tests.
 */
@RunWith(EasyMockRunner.class)
public class StackVersionListenerTest extends EasyMockSupport {
    private static final String INVALID_NEW_VERSION = "1.2.3.4-5678";

    private static final String VALID_NEW_VERSION = "2.4.0.0-1000";

    private static final String SERVICE_COMPONENT_NAME = "Some component name";

    private static final String SERVICE_NAME = "Service name";

    private static final Long CLUSTER_ID = 1L;

    private static final String UNKNOWN_VERSION = "UNKNOWN";

    private static final String VALID_PREVIOUS_VERSION = "2.2.0.0";

    private static final HostVersionEntity DUMMY_HOST_VERSION_ENTITY = new HostVersionEntity();

    private static final UpgradeEntity DUMMY_UPGRADE_ENTITY = new UpgradeEntity();

    public static final String STACK_NAME = "HDP";

    public static final String STACK_VERSION = "2.4";

    private Cluster cluster;

    private ServiceComponentHost sch;

    private Service service;

    private ServiceComponent serviceComponent;

    private VersionEventPublisher publisher = new VersionEventPublisher();

    private StackId stackId = new StackId(StackVersionListenerTest.STACK_NAME, StackVersionListenerTest.STACK_VERSION);

    @Mock
    private Provider<AmbariMetaInfo> ambariMetaInfoProvider;

    @Mock
    private Provider<HostLevelParamsHolder> m_hostLevelParamsHolder;

    @Mock
    private ComponentInfo componentInfo;

    @Mock
    private AmbariMetaInfo ambariMetaInfo;

    @Mock(type = MockType.NICE)
    private HostLevelParamsHolder hostLevelParamsHolder;

    private StackVersionListener listener;

    @Test
    public void testRecalculateHostVersionStateWhenVersionIsNullAndNewVersionIsNotBlank() throws AmbariException {
        expect(sch.getVersion()).andReturn(null);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setVersion(StackVersionListenerTest.INVALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(null).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.INVALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateHostVersionStateWhenVersionIsUnknownAndNewVersionIsNotBlank() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.UNKNOWN_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setVersion(StackVersionListenerTest.INVALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(null).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.INVALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateClusterVersionStateWhenVersionIsNullAndNewVersionIsValid() throws AmbariException {
        expect(sch.getVersion()).andReturn(null);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setVersion(StackVersionListenerTest.VALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(StackVersionListenerTest.DUMMY_HOST_VERSION_ENTITY).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateClusterVersionStateWhenVersionIsUnknownAndNewVersionIsValid() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.UNKNOWN_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setVersion(StackVersionListenerTest.VALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(StackVersionListenerTest.DUMMY_HOST_VERSION_ENTITY).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateHostVersionStateWhenComponentDesiredVersionIsUnknownAndNewVersionIsNotValid() throws AmbariException {
        expect(serviceComponent.getDesiredVersion()).andReturn(StackVersionListenerTest.UNKNOWN_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(NONE);
        expectLastCall().once();
        sch.setVersion(StackVersionListenerTest.INVALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(null).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.INVALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateClusterVersionStateWhenComponentDesiredVersionIsUnknownAndNewVersionIsValid() throws AmbariException {
        expect(serviceComponent.getDesiredVersion()).andReturn(StackVersionListenerTest.UNKNOWN_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(NONE);
        expectLastCall().once();
        sch.setVersion(StackVersionListenerTest.VALID_NEW_VERSION);
        expectLastCall().once();
        expect(sch.recalculateHostVersionState()).andReturn(StackVersionListenerTest.DUMMY_HOST_VERSION_ENTITY).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testRecalculateClusterVersionStateWhenVersionNotAdvertised() throws AmbariException {
        expect(componentInfo.isVersionAdvertised()).andReturn(false).once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testNoActionTakenOnNullVersion() throws AmbariException {
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        resetAll();
        replayAll();
        sendEventAndVerify(null);
    }

    @Test
    public void testSetUpgradeStateToCompleteWhenUpgradeIsInProgressAndNewVersionIsEqualToComponentDesiredVersion() throws AmbariException {
        expect(cluster.getUpgradeInProgress()).andReturn(StackVersionListenerTest.DUMMY_UPGRADE_ENTITY);
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(sch.getUpgradeState()).andReturn(IN_PROGRESS);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(COMPLETE);
        expectLastCall().once();
        expect(serviceComponent.getDesiredVersion()).andStubReturn(StackVersionListenerTest.VALID_NEW_VERSION);
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetUpgradeStateToNoneWhenNoUpgradeAndNewVersionIsEqualToComponentDesiredVersion() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(sch.getUpgradeState()).andReturn(IN_PROGRESS);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(NONE);
        expectLastCall().once();
        expect(serviceComponent.getDesiredVersion()).andStubReturn(StackVersionListenerTest.VALID_NEW_VERSION);
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetUpgradeStateToVersionMismatchWhenUpgradeIsInProgressAndNewVersionIsNotEqualToComponentDesiredVersion() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(sch.getUpgradeState()).andReturn(IN_PROGRESS);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(VERSION_MISMATCH);
        expectLastCall().once();
        sch.setVersion(StackVersionListenerTest.VALID_NEW_VERSION);
        expectLastCall().once();
        expect(serviceComponent.getDesiredVersion()).andStubReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetUpgradeStateToCompleteWhenHostHasVersionMismatchAndNewVersionIsEqualToComponentDesiredVersionAndClusterUpgradeIsInProgress() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(sch.getUpgradeState()).andReturn(VERSION_MISMATCH);
        expect(cluster.getUpgradeInProgress()).andReturn(StackVersionListenerTest.DUMMY_UPGRADE_ENTITY);
        expect(serviceComponent.getDesiredVersion()).andStubReturn(StackVersionListenerTest.VALID_NEW_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(COMPLETE);
        expectLastCall().once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetUpgradeStateToNoneWhenHostHasVersionMismatchAndNewVersionIsEqualToComponentDesiredVersion() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(sch.getUpgradeState()).andReturn(VERSION_MISMATCH);
        expect(serviceComponent.getDesiredVersion()).andStubReturn(StackVersionListenerTest.VALID_NEW_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(NONE);
        expectLastCall().once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetUpgradeStateToVersionMismatchByDefaultWhenHostAndNewVersionsAreValid() throws AmbariException {
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.VALID_PREVIOUS_VERSION);
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        sch.setUpgradeState(VERSION_MISMATCH);
        expectLastCall().once();
        replayAll();
        sendEventAndVerify(StackVersionListenerTest.VALID_NEW_VERSION);
    }

    @Test
    public void testSetRepositoryVersion() throws Exception {
        Host host = createMock(Host.class);
        expect(host.getHostId()).andReturn(1L).anyTimes();
        expect(sch.getVersion()).andReturn(StackVersionListenerTest.UNKNOWN_VERSION);
        expect(sch.getHost()).andReturn(host).anyTimes();
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        RepositoryVersionDAO dao = createNiceMock(RepositoryVersionDAO.class);
        RepositoryVersionEntity entity = createNiceMock(RepositoryVersionEntity.class);
        expect(entity.getVersion()).andReturn("2.4.0.0").once();
        // when the version gets reported back, we set this repo to resolved
        entity.setResolved(true);
        expectLastCall().once();
        expect(dao.findByPK(1L)).andReturn(entity).once();
        expect(dao.merge(entity)).andReturn(entity).once();
        replayAll();
        String newVersion = StackVersionListenerTest.VALID_NEW_VERSION;
        HostComponentVersionAdvertisedEvent event = new HostComponentVersionAdvertisedEvent(cluster, sch, newVersion, 1L);
        // !!! avoid injector for test class
        Field field = StackVersionListener.class.getDeclaredField("repositoryVersionDAO");
        field.setAccessible(true);
        field.set(listener, dao);
        listener.onAmbariEvent(event);
        verifyAll();
    }

    /**
     * Tests that if a component advertises a version and the repository already
     * matches, that we ensure that it is marked as resolved.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testRepositoryResolvedWhenVersionsMatch() throws Exception {
        String version = "2.4.0.0";
        Host host = createMock(Host.class);
        expect(host.getHostId()).andReturn(1L).anyTimes();
        expect(sch.getVersion()).andReturn(version);
        expect(sch.getHost()).andReturn(host).anyTimes();
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        RepositoryVersionDAO dao = createNiceMock(RepositoryVersionDAO.class);
        RepositoryVersionEntity entity = createNiceMock(RepositoryVersionEntity.class);
        expect(entity.getVersion()).andReturn(version).once();
        expect(entity.isResolved()).andReturn(false).once();
        // when the version gets reported back, we set this repo to resolved
        entity.setResolved(true);
        expectLastCall().once();
        expect(dao.findByPK(1L)).andReturn(entity).once();
        expect(dao.merge(entity)).andReturn(entity).once();
        replayAll();
        String newVersion = version;
        HostComponentVersionAdvertisedEvent event = new HostComponentVersionAdvertisedEvent(cluster, sch, newVersion, 1L);
        // !!! avoid injector for test class
        Field field = StackVersionListener.class.getDeclaredField("repositoryVersionDAO");
        field.setAccessible(true);
        field.set(listener, dao);
        listener.onAmbariEvent(event);
        verifyAll();
    }

    /**
     * Tests that the {@link RepositoryVersionEntity} is not updated if there is
     * an upgrade, even if the repo ID is passed back and the versions don't
     * match.
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testRepositoryVersionNotSetDuringUpgrade() throws Exception {
        expect(componentInfo.isVersionAdvertised()).andReturn(true).once();
        // this call will make it seem like there is an upgrade in progress
        expect(cluster.getUpgradeInProgress()).andReturn(createNiceMock(UpgradeEntity.class));
        // create the DAO - nothing will be called on it, so make it strict
        RepositoryVersionDAO dao = createStrictMock(RepositoryVersionDAO.class);
        replayAll();
        // !!! avoid injector for test class
        Field field = StackVersionListener.class.getDeclaredField("repositoryVersionDAO");
        field.setAccessible(true);
        field.set(listener, dao);
        HostComponentVersionAdvertisedEvent event = new HostComponentVersionAdvertisedEvent(cluster, sch, StackVersionListenerTest.VALID_NEW_VERSION, 1L);
        // make sure that a repo ID will come back
        Assert.assertNotNull(event.getRepositoryVersionId());
        listener.onAmbariEvent(event);
        verifyAll();
    }
}
