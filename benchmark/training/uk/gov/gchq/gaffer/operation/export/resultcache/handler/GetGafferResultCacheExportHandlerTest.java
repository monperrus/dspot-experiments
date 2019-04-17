/**
 * Copyright 2016-2019 Crown Copyright
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
package uk.gov.gchq.gaffer.operation.export.resultcache.handler;


import StreamUtil.STORE_PROPERTIES;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import uk.gov.gchq.gaffer.commonutil.CollectionUtil;
import uk.gov.gchq.gaffer.commonutil.JsonAssert;
import uk.gov.gchq.gaffer.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.integration.store.TestStore;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.export.resultcache.GafferResultCacheExporter;
import uk.gov.gchq.gaffer.operation.export.resultcache.handler.util.GafferResultCacheUtil;
import uk.gov.gchq.gaffer.operation.impl.export.resultcache.GetGafferResultCacheExport;
import uk.gov.gchq.gaffer.operation.impl.get.GetElements;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.schema.Schema;


public class GetGafferResultCacheExportHandlerTest {
    private final Edge validEdge = new Edge.Builder().group("result").source("jobId").dest("exportId").directed(true).property("opAuths", CollectionUtil.treeSet("user01")).property("timestamp", System.currentTimeMillis()).property("visibility", "private").property("resultClass", String.class.getName()).property("result", "test".getBytes()).build();

    private final Edge oldEdge = new Edge.Builder().group("result").source("jobId").dest("exportId").directed(true).property("opAuths", CollectionUtil.treeSet("user01")).property("timestamp", (((System.currentTimeMillis()) - (GafferResultCacheUtil.DEFAULT_TIME_TO_LIVE)) - 1)).property("visibility", "private").property("resultClass", String.class.getName()).property("result", "test".getBytes()).build();

    @Test
    public void shouldHandleOperationByDelegatingToAnExistingExporter() throws OperationException {
        // Given
        final GetGafferResultCacheExport export = new GetGafferResultCacheExport.Builder().key("key").build();
        final Context context = new Context();
        final Store store = Mockito.mock(Store.class);
        final Long timeToLive = 10000L;
        final String visibility = "visibility value";
        final GafferResultCacheExporter exporter = Mockito.mock(GafferResultCacheExporter.class);
        final CloseableIterable results = new uk.gov.gchq.gaffer.commonutil.iterable.WrappedCloseableIterable(Arrays.asList(1, 2, 3));
        BDDMockito.given(exporter.get("key")).willReturn(results);
        context.addExporter(exporter);
        final GetGafferResultCacheExportHandler handler = new GetGafferResultCacheExportHandler();
        handler.setStorePropertiesPath(STORE_PROPERTIES);
        handler.setTimeToLive(timeToLive);
        handler.setVisibility(visibility);
        // When
        final Object handlerResult = handler.doOperation(export, context, store);
        // Then
        Mockito.verify(exporter).get("key");
        Assert.assertSame(results, handlerResult);
    }

    @Test
    public void shouldHandleOperationByDelegatingToAnNewExporter() throws OperationException {
        // Given
        final GetGafferResultCacheExport export = new GetGafferResultCacheExport.Builder().key("key").build();
        final Context context = new Context();
        final Store store = Mockito.mock(Store.class);
        final Long timeToLive = 10000L;
        final String visibility = "visibility value";
        final GetGafferResultCacheExportHandler handler = new GetGafferResultCacheExportHandler();
        handler.setStorePropertiesPath(STORE_PROPERTIES);
        handler.setTimeToLive(timeToLive);
        handler.setVisibility(visibility);
        final Store cacheStore = Mockito.mock(Store.class);
        TestStore.mockStore = cacheStore;
        // When
        final Object handlerResult = handler.doOperation(export, context, store);
        // Then
        Assert.assertEquals(0, Iterables.size(((Iterable) (handlerResult))));
        final ArgumentCaptor<OperationChain> opChain = ArgumentCaptor.forClass(OperationChain.class);
        Mockito.verify(cacheStore).execute(opChain.capture(), Mockito.any());
        Assert.assertEquals(1, opChain.getValue().getOperations().size());
        Assert.assertTrue(((opChain.getValue().getOperations().get(0)) instanceof GetElements));
        final GafferResultCacheExporter exporter = context.getExporter(GafferResultCacheExporter.class);
        Assert.assertNotNull(exporter);
    }

    @Test
    public void shouldCreateCacheGraph() throws OperationException {
        // Given
        final Store store = Mockito.mock(Store.class);
        final long timeToLive = 10000L;
        final GetGafferResultCacheExportHandler handler = new GetGafferResultCacheExportHandler();
        handler.setStorePropertiesPath(STORE_PROPERTIES);
        handler.setTimeToLive(timeToLive);
        // When
        final Graph graph = handler.createGraph(store);
        // Then
        final Schema schema = graph.getSchema();
        JsonAssert.assertEquals(GafferResultCacheUtil.createSchema(timeToLive).toJson(false), schema.toJson(true));
        Assert.assertTrue(schema.validate().isValid());
        Assert.assertEquals(timeToLive, getAgeOffTime());
        Assert.assertTrue(new uk.gov.gchq.gaffer.store.ElementValidator(schema).validate(validEdge));
        Assert.assertFalse(new uk.gov.gchq.gaffer.store.ElementValidator(schema).validate(oldEdge));
    }
}
