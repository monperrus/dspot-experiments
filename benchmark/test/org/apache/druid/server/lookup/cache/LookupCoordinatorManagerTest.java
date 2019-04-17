/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.server.lookup.cache;


import LookupCoordinatorManager.LOOKUP_CONFIG_KEY;
import LookupCoordinatorManager.LookupsCommunicator;
import LookupCoordinatorManager.OLD_LOOKUP_CONFIG_KEY;
import Response.Status.ACCEPTED;
import Response.Status.INTERNAL_SERVER_ERROR;
import Response.Status.OK;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.SettableFuture;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.druid.audit.AuditInfo;
import org.apache.druid.common.config.ConfigManager.SetResult;
import org.apache.druid.common.config.JacksonConfigManager;
import org.apache.druid.discovery.DruidNodeDiscoveryProvider;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.apache.druid.java.util.common.IAE;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.java.util.emitter.service.ServiceEmitter;
import org.apache.druid.java.util.http.client.HttpClient;
import org.apache.druid.java.util.http.client.response.HttpResponseHandler;
import org.apache.druid.java.util.http.client.response.SequenceInputStreamResponseHandler;
import org.apache.druid.query.lookup.LookupsState;
import org.apache.druid.server.http.HostAndPortWithScheme;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class LookupCoordinatorManagerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final ObjectMapper mapper = new DefaultObjectMapper();

    private final DruidNodeDiscoveryProvider druidNodeDiscoveryProvider = EasyMock.createStrictMock(DruidNodeDiscoveryProvider.class);

    private final LookupNodeDiscovery lookupNodeDiscovery = EasyMock.createStrictMock(LookupNodeDiscovery.class);

    private final HttpClient client = EasyMock.createStrictMock(HttpClient.class);

    private final JacksonConfigManager configManager = EasyMock.createStrictMock(JacksonConfigManager.class);

    private final LookupCoordinatorManagerConfig lookupCoordinatorManagerConfig = new LookupCoordinatorManagerConfig();

    private static final String LOOKUP_TIER = "lookup_tier";

    private static final String SINGLE_LOOKUP_NAME = "lookupName";

    private static final LookupExtractorFactoryMapContainer SINGLE_LOOKUP_SPEC_V0 = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("k0", "v0"));

    private static final LookupExtractorFactoryMapContainer SINGLE_LOOKUP_SPEC_V1 = new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k1", "v1"));

    private static final Map<String, LookupExtractorFactoryMapContainer> SINGLE_LOOKUP_MAP_V0 = ImmutableMap.of(LookupCoordinatorManagerTest.SINGLE_LOOKUP_NAME, LookupCoordinatorManagerTest.SINGLE_LOOKUP_SPEC_V0);

    private static final Map<String, LookupExtractorFactoryMapContainer> SINGLE_LOOKUP_MAP_V1 = ImmutableMap.of(LookupCoordinatorManagerTest.SINGLE_LOOKUP_NAME, LookupCoordinatorManagerTest.SINGLE_LOOKUP_SPEC_V1);

    private static final Map<String, Map<String, LookupExtractorFactoryMapContainer>> TIERED_LOOKUP_MAP_V0 = ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, LookupCoordinatorManagerTest.SINGLE_LOOKUP_MAP_V0);

    private static final Map<String, Map<String, LookupExtractorFactoryMapContainer>> TIERED_LOOKUP_MAP_V1 = ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, LookupCoordinatorManagerTest.SINGLE_LOOKUP_MAP_V1);

    private static final Map<String, Map<String, LookupExtractorFactoryMapContainer>> EMPTY_TIERED_LOOKUP = ImmutableMap.of();

    private static final LookupsState<LookupExtractorFactoryMapContainer> LOOKUPS_STATE = new LookupsState(LookupCoordinatorManagerTest.SINGLE_LOOKUP_MAP_V0, LookupCoordinatorManagerTest.SINGLE_LOOKUP_MAP_V1, Collections.EMPTY_SET);

    private static final AtomicLong EVENT_EMITS = new AtomicLong(0L);

    private static ServiceEmitter SERVICE_EMITTER;

    @Test
    public void testUpdateNodeWithSuccess() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8(mapper.writeValueAsString(LookupCoordinatorManagerTest.LOOKUPS_STATE))));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(ACCEPTED.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        LookupsState<LookupExtractorFactoryMapContainer> resp = lookupsCommunicator.updateNode(HostAndPortWithScheme.fromString("localhost"), LookupCoordinatorManagerTest.LOOKUPS_STATE);
        EasyMock.verify(client, responseHandler);
        Assert.assertEquals(resp, LookupCoordinatorManagerTest.LOOKUPS_STATE);
    }

    @Test
    public void testUpdateNodeRespondedWithNotOkErrorCode() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8("server failed")));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(INTERNAL_SERVER_ERROR.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        try {
            lookupsCommunicator.updateNode(HostAndPortWithScheme.fromString("localhost"), LookupCoordinatorManagerTest.LOOKUPS_STATE);
            Assert.fail();
        } catch (IOException ex) {
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testUpdateNodeReturnsWeird() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8("weird")));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(ACCEPTED.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        try {
            lookupsCommunicator.updateNode(HostAndPortWithScheme.fromString("localhost"), LookupCoordinatorManagerTest.LOOKUPS_STATE);
            Assert.fail();
        } catch (IOException ex) {
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testUpdateNodeInterrupted() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(ACCEPTED.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        Thread.currentThread().interrupt();
        try {
            lookupsCommunicator.updateNode(HostAndPortWithScheme.fromString("localhost"), LookupCoordinatorManagerTest.LOOKUPS_STATE);
            Assert.fail();
        } catch (InterruptedException ex) {
        } finally {
            // clear the interrupt
            Thread.interrupted();
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testGetLookupsStateNodeWithSuccess() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8(mapper.writeValueAsString(LookupCoordinatorManagerTest.LOOKUPS_STATE))));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(OK.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        LookupsState<LookupExtractorFactoryMapContainer> resp = lookupsCommunicator.getLookupStateForNode(HostAndPortWithScheme.fromString("localhost"));
        EasyMock.verify(client, responseHandler);
        Assert.assertEquals(resp, LookupCoordinatorManagerTest.LOOKUPS_STATE);
    }

    @Test
    public void testGetLookupsStateNodeRespondedWithNotOkErrorCode() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8("server failed")));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(INTERNAL_SERVER_ERROR.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        try {
            lookupsCommunicator.getLookupStateForNode(HostAndPortWithScheme.fromString("localhost"));
            Assert.fail();
        } catch (IOException ex) {
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testGetLookupsStateNodeReturnsWeird() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        future.set(new ByteArrayInputStream(StringUtils.toUtf8("weird")));
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(ACCEPTED.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        try {
            lookupsCommunicator.getLookupStateForNode(HostAndPortWithScheme.fromString("localhost"));
            Assert.fail();
        } catch (IOException ex) {
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testGetLookupsStateNodeInterrupted() throws Exception {
        final HttpResponseHandler<InputStream, InputStream> responseHandler = EasyMock.createStrictMock(HttpResponseHandler.class);
        final SettableFuture<InputStream> future = SettableFuture.create();
        EasyMock.expect(client.go(EasyMock.anyObject(), EasyMock.<SequenceInputStreamResponseHandler>anyObject(), EasyMock.anyObject())).andReturn(future).once();
        EasyMock.replay(client, responseHandler);
        final LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = new LookupCoordinatorManager.LookupsCommunicator(client, lookupCoordinatorManagerConfig, mapper) {
            @Override
            HttpResponseHandler<InputStream, InputStream> makeResponseHandler(final AtomicInteger returnCode, final AtomicReference<String> reasonString) {
                returnCode.set(ACCEPTED.getStatusCode());
                reasonString.set("");
                return responseHandler;
            }
        };
        Thread.currentThread().interrupt();
        try {
            lookupsCommunicator.getLookupStateForNode(HostAndPortWithScheme.fromString("localhost"));
            Assert.fail();
        } catch (InterruptedException ex) {
        } finally {
            // clear the interrupt
            Thread.interrupted();
        }
        EasyMock.verify(client, responseHandler);
    }

    @Test
    public void testUpdateLookupsFailsUnitialized() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return null;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        expectedException.expect(ISE.class);
        manager.updateLookups(LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V0, auditInfo);
    }

    @Test
    public void testUpdateLookupsInitialization() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return null;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(LookupCoordinatorManagerTest.EMPTY_TIERED_LOOKUP), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        manager.updateLookups(LookupCoordinatorManagerTest.EMPTY_TIERED_LOOKUP, auditInfo);
        EasyMock.verify(configManager);
    }

    @Test
    public void testUpdateLookupAdds() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return LookupCoordinatorManagerTest.EMPTY_TIERED_LOOKUP;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V0), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        manager.updateLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, LookupCoordinatorManagerTest.SINGLE_LOOKUP_NAME, LookupCoordinatorManagerTest.SINGLE_LOOKUP_SPEC_V0, auditInfo);
        EasyMock.verify(configManager);
    }

    @Test
    public void testUpdateLookupsAddsNewLookup() {
        final LookupExtractorFactoryMapContainer ignore = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("prop", "old"));
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo1", new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("prop", "old"))), ((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore));
            }
        };
        manager.start();
        final LookupExtractorFactoryMapContainer newSpec = new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("prop", "new"));
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(ImmutableMap.<String, Map<String, LookupExtractorFactoryMapContainer>>of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo1", ignore, "foo2", newSpec), ((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore))), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        Assert.assertTrue(manager.updateLookups(ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo2", newSpec)), auditInfo));
        EasyMock.verify(configManager);
    }

    @Test
    public void testUpdateLookupsOnlyUpdatesToTier() {
        final LookupExtractorFactoryMapContainer ignore = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("prop", "old"));
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo", new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("prop", "new"))), ((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore));
            }
        };
        manager.start();
        final LookupExtractorFactoryMapContainer newSpec = new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("prop", "new"));
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(ImmutableMap.<String, Map<String, LookupExtractorFactoryMapContainer>>of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo", newSpec), ((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore))), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        Assert.assertTrue(manager.updateLookups(ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo", newSpec)), auditInfo));
        EasyMock.verify(configManager);
    }

    @Test
    public void testUpdateLookupsUpdates() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V0;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V1), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        manager.updateLookups(LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V1, auditInfo);
        EasyMock.verify(configManager);
    }

    @Test
    public void testUpdateLookupFailsSameVersionUpdates() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V0;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        try {
            manager.updateLookups(LookupCoordinatorManagerTest.TIERED_LOOKUP_MAP_V0, auditInfo);
            Assert.fail();
        } catch (IAE ex) {
        }
    }

    @Test
    public void testUpdateLookupsAddsNewTier() {
        final LookupExtractorFactoryMapContainer ignore = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("prop", "old"));
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore));
            }
        };
        manager.start();
        final LookupExtractorFactoryMapContainer newSpec = new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("prop", "new"));
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(ImmutableMap.<String, Map<String, LookupExtractorFactoryMapContainer>>of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo", newSpec), ((LookupCoordinatorManagerTest.LOOKUP_TIER) + "2"), ImmutableMap.of("ignore", ignore))), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        Assert.assertTrue(manager.updateLookups(ImmutableMap.of(((LookupCoordinatorManagerTest.LOOKUP_TIER) + "1"), ImmutableMap.of("foo", newSpec)), auditInfo));
        EasyMock.verify(configManager);
    }

    @Test
    public void testDeleteLookup() {
        final LookupExtractorFactoryMapContainer ignore = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("lookup", "ignore"));
        final LookupExtractorFactoryMapContainer lookup = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("lookup", "foo"));
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, ImmutableMap.of("foo", lookup, "ignore", ignore));
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, ImmutableMap.of("ignore", ignore))), EasyMock.eq(auditInfo))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        Assert.assertTrue(manager.deleteLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo", auditInfo));
        EasyMock.verify(configManager);
    }

    @Test
    public void testDeleteLookupIgnoresMissing() {
        final LookupExtractorFactoryMapContainer ignore = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("lookup", "ignore"));
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, ImmutableMap.of("ignore", ignore));
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        Assert.assertFalse(manager.deleteLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo", auditInfo));
    }

    @Test
    public void testDeleteLookupIgnoresNotReady() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return null;
            }
        };
        manager.start();
        final AuditInfo auditInfo = new AuditInfo("author", "comment", "localhost");
        Assert.assertFalse(manager.deleteLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo", auditInfo));
    }

    @Test
    public void testGetLookup() {
        final LookupExtractorFactoryMapContainer lookup = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("lookup", "foo"));
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, ImmutableMap.of("foo", lookup));
            }
        };
        Assert.assertEquals(lookup, manager.getLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo"));
        Assert.assertNull(manager.getLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "does not exit"));
        Assert.assertNull(manager.getLookup("not a tier", "foo"));
    }

    @Test
    public void testGetLookupIgnoresMalformed() {
        final LookupExtractorFactoryMapContainer lookup = new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("lookup", "foo"));
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return ImmutableMap.of(LookupCoordinatorManagerTest.LOOKUP_TIER, ImmutableMap.of("foo", lookup, "bar", new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of())));
            }
        };
        Assert.assertEquals(lookup, manager.getLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo"));
        Assert.assertNull(manager.getLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "does not exit"));
        Assert.assertNull(manager.getLookup("not a tier", "foo"));
    }

    @Test
    public void testGetLookupIgnoresNotReady() {
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig) {
            @Override
            public Map<String, Map<String, LookupExtractorFactoryMapContainer>> getKnownLookups() {
                return null;
            }
        };
        Assert.assertNull(manager.getLookup(LookupCoordinatorManagerTest.LOOKUP_TIER, "foo"));
    }

    @Test(timeout = 60000L)
    public void testLookupManagementLoop() throws Exception {
        Map<String, LookupExtractorFactoryMapContainer> lookup1 = ImmutableMap.of("lookup1", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k1", "v1")));
        Map<String, Map<String, LookupExtractorFactoryMapContainer>> configuredLookups = ImmutableMap.of("tier1", lookup1);
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.watch(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference(configuredLookups)).once();
        EasyMock.replay(configManager);
        HostAndPortWithScheme host1 = HostAndPortWithScheme.fromParts("http", "host1", 1234);
        HostAndPortWithScheme host2 = HostAndPortWithScheme.fromParts("http", "host2", 3456);
        EasyMock.reset(lookupNodeDiscovery);
        EasyMock.expect(lookupNodeDiscovery.getNodesInTier("tier1")).andReturn(ImmutableList.of(host1, host2)).anyTimes();
        EasyMock.replay(lookupNodeDiscovery);
        LookupCoordinatorManager.LookupsCommunicator lookupsCommunicator = EasyMock.createMock(LookupsCommunicator.class);
        EasyMock.expect(lookupsCommunicator.getLookupStateForNode(host1)).andReturn(new LookupsState(ImmutableMap.of("lookup0", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0"))), null, null)).once();
        LookupsState<LookupExtractorFactoryMapContainer> host1UpdatedState = new LookupsState(lookup1, null, null);
        EasyMock.expect(lookupsCommunicator.updateNode(host1, new LookupsState(null, lookup1, ImmutableSet.of("lookup0")))).andReturn(host1UpdatedState).once();
        EasyMock.expect(lookupsCommunicator.getLookupStateForNode(host2)).andReturn(new LookupsState(ImmutableMap.of("lookup3", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0")), "lookup1", new LookupExtractorFactoryMapContainer("v0", ImmutableMap.of("k0", "v0"))), null, null)).once();
        LookupsState<LookupExtractorFactoryMapContainer> host2UpdatedState = new LookupsState(null, lookup1, null);
        EasyMock.expect(lookupsCommunicator.updateNode(host2, new LookupsState(null, lookup1, ImmutableSet.of("lookup3")))).andReturn(host2UpdatedState).once();
        EasyMock.replay(lookupsCommunicator);
        LookupCoordinatorManagerConfig lookupCoordinatorManagerConfig = new LookupCoordinatorManagerConfig() {
            @Override
            public long getInitialDelay() {
                return 1;
            }

            @Override
            public int getThreadPoolSize() {
                return 2;
            }
        };
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(druidNodeDiscoveryProvider, configManager, lookupCoordinatorManagerConfig, lookupsCommunicator, lookupNodeDiscovery);
        Assert.assertTrue(manager.knownOldState.get().isEmpty());
        manager.start();
        Map<HostAndPort, LookupsState<LookupExtractorFactoryMapContainer>> expectedKnownState = ImmutableMap.of(host1.getHostAndPort(), host1UpdatedState, host2.getHostAndPort(), host2UpdatedState);
        while (!(expectedKnownState.equals(manager.knownOldState.get()))) {
            Thread.sleep(100);
        } 
        EasyMock.verify(lookupNodeDiscovery, configManager, lookupsCommunicator);
    }

    @Test
    public void testGetToBeLoadedOnNode() {
        LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig);
        LookupsState<LookupExtractorFactoryMapContainer> currNodeState = new LookupsState(ImmutableMap.of("lookup0", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0")), "lookup1", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k1", "v1"))), ImmutableMap.of("lookup2", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k2", "v2")), "lookup3", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k3", "v3"))), ImmutableSet.of("lookup2", "lookup4"));
        Map<String, LookupExtractorFactoryMapContainer> stateToBe = ImmutableMap.of("lookup0", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0")), "lookup1", new LookupExtractorFactoryMapContainer("v2", ImmutableMap.of("k1", "v1")), "lookup2", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k2", "v2")));
        Assert.assertEquals(ImmutableMap.of("lookup1", new LookupExtractorFactoryMapContainer("v2", ImmutableMap.of("k1", "v1")), "lookup2", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k2", "v2"))), manager.getToBeLoadedOnNode(currNodeState, stateToBe));
    }

    @Test
    public void testToBeDropped() {
        LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig);
        LookupsState<LookupExtractorFactoryMapContainer> currNodeState = new LookupsState(ImmutableMap.of("lookup0", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0")), "lookup1", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k1", "v1"))), ImmutableMap.of("lookup2", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k2", "v2")), "lookup3", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k3", "v3"))), ImmutableSet.of("lookup2", "lookup4"));
        Map<String, LookupExtractorFactoryMapContainer> stateToBe = ImmutableMap.of("lookup0", new LookupExtractorFactoryMapContainer("v1", ImmutableMap.of("k0", "v0")));
        Assert.assertEquals(ImmutableSet.of("lookup1", "lookup3"), manager.getToBeDroppedFromNode(currNodeState, stateToBe));
    }

    @Test
    public void testStartStop() throws Exception {
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.watch(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference<java.util.List<LookupExtractorFactoryMapContainer>>(null)).once();
        EasyMock.expect(configManager.watch(EasyMock.eq(OLD_LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference<java.util.List<Map<String, Object>>>(null)).once();
        EasyMock.replay(configManager);
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig);
        Assert.assertFalse(manager.isStarted());
        manager.start();
        Assert.assertTrue(manager.awaitStarted(1));
        Assert.assertTrue(manager.backgroundManagerIsRunning());
        Assert.assertFalse(manager.waitForBackgroundTermination(10));
        manager.stop();
        Assert.assertFalse(manager.awaitStarted(1));
        Assert.assertTrue(manager.waitForBackgroundTermination(10));
        Assert.assertFalse(manager.backgroundManagerIsRunning());
        EasyMock.verify(configManager);
    }

    @Test
    public void testMultipleStartStop() throws Exception {
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.watch(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference(Collections.EMPTY_MAP)).anyTimes();
        EasyMock.replay(configManager);
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, lookupCoordinatorManagerConfig);
        Assert.assertFalse(manager.awaitStarted(1));
        manager.start();
        Assert.assertTrue(manager.awaitStarted(1));
        Assert.assertTrue(manager.backgroundManagerIsRunning());
        Assert.assertFalse(manager.waitForBackgroundTermination(10));
        manager.stop();
        Assert.assertFalse(manager.awaitStarted(1));
        Assert.assertTrue(manager.waitForBackgroundTermination(10));
        Assert.assertFalse(manager.backgroundManagerIsRunning());
        manager.start();
        Assert.assertTrue(manager.awaitStarted(1));
        Assert.assertTrue(manager.backgroundManagerIsRunning());
        Assert.assertFalse(manager.waitForBackgroundTermination(10));
        manager.stop();
        Assert.assertFalse(manager.awaitStarted(1));
        Assert.assertTrue(manager.waitForBackgroundTermination(10));
        Assert.assertFalse(manager.backgroundManagerIsRunning());
        manager.start();
        Assert.assertTrue(manager.awaitStarted(1));
        Assert.assertTrue(manager.backgroundManagerIsRunning());
        Assert.assertFalse(manager.waitForBackgroundTermination(10));
        manager.stop();
        Assert.assertFalse(manager.awaitStarted(1));
        Assert.assertTrue(manager.waitForBackgroundTermination(10));
        Assert.assertFalse(manager.backgroundManagerIsRunning());
        EasyMock.verify(configManager);
    }

    @Test
    public void testLookupDiscoverAll() {
        final Set<String> fakeChildren = ImmutableSet.of("tier1", "tier2");
        EasyMock.reset(lookupNodeDiscovery);
        EasyMock.expect(lookupNodeDiscovery.getAllTiers()).andReturn(fakeChildren).once();
        EasyMock.replay(lookupNodeDiscovery);
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(druidNodeDiscoveryProvider, configManager, lookupCoordinatorManagerConfig, EasyMock.createMock(LookupsCommunicator.class), lookupNodeDiscovery);
        manager.start();
        Assert.assertEquals(fakeChildren, manager.discoverTiers());
        EasyMock.verify(lookupNodeDiscovery);
    }

    @Test
    public void testDiscoverNodesInTier() {
        EasyMock.reset(lookupNodeDiscovery);
        EasyMock.expect(lookupNodeDiscovery.getNodesInTier("tier")).andReturn(ImmutableSet.of(HostAndPortWithScheme.fromParts("http", "h1", 8080), HostAndPortWithScheme.fromParts("http", "h2", 8080))).once();
        EasyMock.replay(lookupNodeDiscovery);
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(druidNodeDiscoveryProvider, configManager, lookupCoordinatorManagerConfig, EasyMock.createMock(LookupsCommunicator.class), lookupNodeDiscovery);
        manager.start();
        Assert.assertEquals(ImmutableSet.of(HostAndPort.fromParts("h1", 8080), HostAndPort.fromParts("h2", 8080)), ImmutableSet.copyOf(manager.discoverNodesInTier("tier")));
        EasyMock.verify(lookupNodeDiscovery);
    }

    // tests that lookups stored in db from 0.10.0 are converted and restored.
    @Test
    public void testBackwardCompatibilityMigration() {
        EasyMock.reset(configManager);
        EasyMock.expect(configManager.watch(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference<Map<String, Map<String, Map<String, Object>>>>(null)).once();
        EasyMock.expect(configManager.watch(EasyMock.eq(OLD_LOOKUP_CONFIG_KEY), EasyMock.<TypeReference>anyObject(), EasyMock.<AtomicReference>isNull())).andReturn(new AtomicReference<Map<String, Map<String, Map<String, Object>>>>(ImmutableMap.of("tier1", ImmutableMap.of("lookup1", ImmutableMap.of("k", "v"))))).once();
        EasyMock.expect(configManager.set(EasyMock.eq(LOOKUP_CONFIG_KEY), EasyMock.eq(ImmutableMap.<String, Map<String, LookupExtractorFactoryMapContainer>>of("tier1", ImmutableMap.of("lookup1", new LookupExtractorFactoryMapContainer(null, ImmutableMap.of("k", "v"))))), EasyMock.anyObject(AuditInfo.class))).andReturn(SetResult.ok()).once();
        EasyMock.replay(configManager);
        final LookupCoordinatorManager manager = new LookupCoordinatorManager(client, druidNodeDiscoveryProvider, mapper, configManager, new LookupCoordinatorManagerConfig() {
            @Override
            public long getPeriod() {
                return 1;
            }
        });
        manager.start();
        EasyMock.verify(configManager);
    }
}
