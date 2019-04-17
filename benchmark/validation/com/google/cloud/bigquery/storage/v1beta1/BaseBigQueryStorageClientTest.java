/**
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigquery.storage.v1beta1;


import StatusCode.Code.INVALID_ARGUMENT;
import com.google.api.gax.grpc.GaxGrpcProperties;
import com.google.api.gax.grpc.testing.LocalChannelProvider;
import com.google.api.gax.grpc.testing.MockServiceHelper;
import com.google.api.gax.grpc.testing.MockStreamObserver;
import com.google.api.gax.rpc.ApiClientHeaderProvider;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.api.gax.rpc.ServerStreamingCallable;
import com.google.cloud.bigquery.storage.v1beta1.Storage.BatchCreateReadSessionStreamsRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.BatchCreateReadSessionStreamsResponse;
import com.google.cloud.bigquery.storage.v1beta1.Storage.CreateReadSessionRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.FinalizeStreamRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadRowsRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadRowsResponse;
import com.google.cloud.bigquery.storage.v1beta1.Storage.ReadSession;
import com.google.cloud.bigquery.storage.v1beta1.Storage.SplitReadStreamRequest;
import com.google.cloud.bigquery.storage.v1beta1.Storage.SplitReadStreamResponse;
import com.google.cloud.bigquery.storage.v1beta1.Storage.Stream;
import com.google.cloud.bigquery.storage.v1beta1.Storage.StreamPosition;
import com.google.cloud.bigquery.storage.v1beta1.TableReferenceProto.TableReference;
import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Generated;
import org.junit.Assert;
import org.junit.Test;


@Generated("by GAPIC")
public class BaseBigQueryStorageClientTest {
    private static MockBigQueryStorage mockBigQueryStorage;

    private static MockServiceHelper serviceHelper;

    private BaseBigQueryStorageClient client;

    private LocalChannelProvider channelProvider;

    @Test
    @SuppressWarnings("all")
    public void createReadSessionTest() {
        String name = "name3373707";
        ReadSession expectedResponse = ReadSession.newBuilder().setName(name).build();
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addResponse(expectedResponse);
        TableReference tableReference = TableReference.newBuilder().build();
        String parent = "parent-995424086";
        int requestedStreams = 1017221410;
        ReadSession actualResponse = client.createReadSession(tableReference, parent, requestedStreams);
        Assert.assertEquals(expectedResponse, actualResponse);
        List<GeneratedMessageV3> actualRequests = BaseBigQueryStorageClientTest.mockBigQueryStorage.getRequests();
        Assert.assertEquals(1, actualRequests.size());
        CreateReadSessionRequest actualRequest = ((CreateReadSessionRequest) (actualRequests.get(0)));
        Assert.assertEquals(tableReference, actualRequest.getTableReference());
        Assert.assertEquals(parent, actualRequest.getParent());
        Assert.assertEquals(requestedStreams, actualRequest.getRequestedStreams());
        Assert.assertTrue(channelProvider.isHeaderSent(ApiClientHeaderProvider.getDefaultApiClientHeaderKey(), GaxGrpcProperties.getDefaultApiClientHeaderPattern()));
    }

    @Test
    @SuppressWarnings("all")
    public void createReadSessionExceptionTest() throws Exception {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INVALID_ARGUMENT);
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addException(exception);
        try {
            TableReference tableReference = TableReference.newBuilder().build();
            String parent = "parent-995424086";
            int requestedStreams = 1017221410;
            client.createReadSession(tableReference, parent, requestedStreams);
            Assert.fail("No exception raised");
        } catch (InvalidArgumentException e) {
            // Expected exception
        }
    }

    @Test
    @SuppressWarnings("all")
    public void readRowsTest() throws Exception {
        ReadRowsResponse expectedResponse = ReadRowsResponse.newBuilder().build();
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addResponse(expectedResponse);
        StreamPosition readPosition = StreamPosition.newBuilder().build();
        ReadRowsRequest request = ReadRowsRequest.newBuilder().setReadPosition(readPosition).build();
        MockStreamObserver<ReadRowsResponse> responseObserver = new MockStreamObserver();
        ServerStreamingCallable<ReadRowsRequest, ReadRowsResponse> callable = client.readRowsCallable();
        callable.serverStreamingCall(request, responseObserver);
        List<ReadRowsResponse> actualResponses = responseObserver.future().get();
        Assert.assertEquals(1, actualResponses.size());
        Assert.assertEquals(expectedResponse, actualResponses.get(0));
    }

    @Test
    @SuppressWarnings("all")
    public void readRowsExceptionTest() throws Exception {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INVALID_ARGUMENT);
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addException(exception);
        StreamPosition readPosition = StreamPosition.newBuilder().build();
        ReadRowsRequest request = ReadRowsRequest.newBuilder().setReadPosition(readPosition).build();
        MockStreamObserver<ReadRowsResponse> responseObserver = new MockStreamObserver();
        ServerStreamingCallable<ReadRowsRequest, ReadRowsResponse> callable = client.readRowsCallable();
        callable.serverStreamingCall(request, responseObserver);
        try {
            List<ReadRowsResponse> actualResponses = responseObserver.future().get();
            Assert.fail("No exception thrown");
        } catch (ExecutionException e) {
            Assert.assertTrue(((e.getCause()) instanceof InvalidArgumentException));
            InvalidArgumentException apiException = ((InvalidArgumentException) (e.getCause()));
            Assert.assertEquals(INVALID_ARGUMENT, apiException.getStatusCode().getCode());
        }
    }

    @Test
    @SuppressWarnings("all")
    public void batchCreateReadSessionStreamsTest() {
        BatchCreateReadSessionStreamsResponse expectedResponse = BatchCreateReadSessionStreamsResponse.newBuilder().build();
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addResponse(expectedResponse);
        ReadSession session = ReadSession.newBuilder().build();
        int requestedStreams = 1017221410;
        BatchCreateReadSessionStreamsResponse actualResponse = client.batchCreateReadSessionStreams(session, requestedStreams);
        Assert.assertEquals(expectedResponse, actualResponse);
        List<GeneratedMessageV3> actualRequests = BaseBigQueryStorageClientTest.mockBigQueryStorage.getRequests();
        Assert.assertEquals(1, actualRequests.size());
        BatchCreateReadSessionStreamsRequest actualRequest = ((BatchCreateReadSessionStreamsRequest) (actualRequests.get(0)));
        Assert.assertEquals(session, actualRequest.getSession());
        Assert.assertEquals(requestedStreams, actualRequest.getRequestedStreams());
        Assert.assertTrue(channelProvider.isHeaderSent(ApiClientHeaderProvider.getDefaultApiClientHeaderKey(), GaxGrpcProperties.getDefaultApiClientHeaderPattern()));
    }

    @Test
    @SuppressWarnings("all")
    public void batchCreateReadSessionStreamsExceptionTest() throws Exception {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INVALID_ARGUMENT);
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addException(exception);
        try {
            ReadSession session = ReadSession.newBuilder().build();
            int requestedStreams = 1017221410;
            client.batchCreateReadSessionStreams(session, requestedStreams);
            Assert.fail("No exception raised");
        } catch (InvalidArgumentException e) {
            // Expected exception
        }
    }

    @Test
    @SuppressWarnings("all")
    public void finalizeStreamTest() {
        Empty expectedResponse = Empty.newBuilder().build();
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addResponse(expectedResponse);
        Stream stream = Stream.newBuilder().build();
        client.finalizeStream(stream);
        List<GeneratedMessageV3> actualRequests = BaseBigQueryStorageClientTest.mockBigQueryStorage.getRequests();
        Assert.assertEquals(1, actualRequests.size());
        FinalizeStreamRequest actualRequest = ((FinalizeStreamRequest) (actualRequests.get(0)));
        Assert.assertEquals(stream, actualRequest.getStream());
        Assert.assertTrue(channelProvider.isHeaderSent(ApiClientHeaderProvider.getDefaultApiClientHeaderKey(), GaxGrpcProperties.getDefaultApiClientHeaderPattern()));
    }

    @Test
    @SuppressWarnings("all")
    public void finalizeStreamExceptionTest() throws Exception {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INVALID_ARGUMENT);
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addException(exception);
        try {
            Stream stream = Stream.newBuilder().build();
            client.finalizeStream(stream);
            Assert.fail("No exception raised");
        } catch (InvalidArgumentException e) {
            // Expected exception
        }
    }

    @Test
    @SuppressWarnings("all")
    public void splitReadStreamTest() {
        SplitReadStreamResponse expectedResponse = SplitReadStreamResponse.newBuilder().build();
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addResponse(expectedResponse);
        Stream originalStream = Stream.newBuilder().build();
        SplitReadStreamResponse actualResponse = client.splitReadStream(originalStream);
        Assert.assertEquals(expectedResponse, actualResponse);
        List<GeneratedMessageV3> actualRequests = BaseBigQueryStorageClientTest.mockBigQueryStorage.getRequests();
        Assert.assertEquals(1, actualRequests.size());
        SplitReadStreamRequest actualRequest = ((SplitReadStreamRequest) (actualRequests.get(0)));
        Assert.assertEquals(originalStream, actualRequest.getOriginalStream());
        Assert.assertTrue(channelProvider.isHeaderSent(ApiClientHeaderProvider.getDefaultApiClientHeaderKey(), GaxGrpcProperties.getDefaultApiClientHeaderPattern()));
    }

    @Test
    @SuppressWarnings("all")
    public void splitReadStreamExceptionTest() throws Exception {
        StatusRuntimeException exception = new StatusRuntimeException(Status.INVALID_ARGUMENT);
        BaseBigQueryStorageClientTest.mockBigQueryStorage.addException(exception);
        try {
            Stream originalStream = Stream.newBuilder().build();
            client.splitReadStream(originalStream);
            Assert.fail("No exception raised");
        } catch (InvalidArgumentException e) {
            // Expected exception
        }
    }
}
