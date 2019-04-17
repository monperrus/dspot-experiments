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
package org.apache.beam.runners.dataflow.worker.fn.logging;


import BeamFnApi.LogControl;
import BeamFnApi.LogEntry;
import BeamFnApi.LogEntry.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import org.apache.beam.model.fnexecution.v1.BeamFnApi;
import org.apache.beam.model.fnexecution.v1.BeamFnLoggingGrpc;
import org.apache.beam.runners.dataflow.harness.test.TestStreams;
import org.apache.beam.runners.dataflow.worker.fn.stream.ServerStreamObserverFactory;
import org.apache.beam.runners.fnexecution.GrpcContextHeaderAccessorProvider;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.vendor.grpc.v1p13p1.io.grpc.ManagedChannel;
import org.apache.beam.vendor.grpc.v1p13p1.io.grpc.Server;
import org.apache.beam.vendor.grpc.v1p13p1.io.grpc.inprocess.InProcessChannelBuilder;
import org.apache.beam.vendor.grpc.v1p13p1.io.grpc.stub.StreamObserver;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link BeamFnLoggingService}.
 */
@RunWith(JUnit4.class)
public class BeamFnLoggingServiceTest {
    private Server server;

    @Test
    public void testMultipleClientsSuccessfullyProcessed() throws Exception {
        ConcurrentLinkedQueue<BeamFnApi.LogEntry> logs = new ConcurrentLinkedQueue<>();
        try (BeamFnLoggingService service = new BeamFnLoggingService(findOpenPort(), logs::add, ServerStreamObserverFactory.fromOptions(PipelineOptionsFactory.create())::from, GrpcContextHeaderAccessorProvider.getHeaderAccessor())) {
            server = createServer(service, service.getApiServiceDescriptor());
            Collection<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 1; i <= 3; ++i) {
                int instructionReference = i;
                tasks.add(() -> {
                    CountDownLatch waitForServerHangup = new CountDownLatch(1);
                    ManagedChannel channel = InProcessChannelBuilder.forName(service.getApiServiceDescriptor().getUrl()).build();
                    StreamObserver<BeamFnApi.LogEntry.List> outboundObserver = BeamFnLoggingGrpc.newStub(channel).logging(TestStreams.withOnNext(BeamFnLoggingServiceTest::discardMessage).withOnCompleted(waitForServerHangup::countDown).build());
                    outboundObserver.onNext(createLogsWithIds(instructionReference, (-instructionReference)));
                    outboundObserver.onCompleted();
                    waitForServerHangup.await();
                    return null;
                });
            }
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.invokeAll(tasks);
            Assert.assertThat(logs, Matchers.containsInAnyOrder(createLogWithId(1L), createLogWithId(2L), createLogWithId(3L), createLogWithId((-1L)), createLogWithId((-2L)), createLogWithId((-3L))));
        }
    }

    @Test
    public void testMultipleClientsFailingIsHandledGracefullyByServer() throws Exception {
        Collection<Callable<Void>> tasks = new ArrayList<>();
        ConcurrentLinkedQueue<BeamFnApi.LogEntry> logs = new ConcurrentLinkedQueue<>();
        try (BeamFnLoggingService service = new BeamFnLoggingService(findOpenPort(), logs::add, ServerStreamObserverFactory.fromOptions(PipelineOptionsFactory.create())::from, GrpcContextHeaderAccessorProvider.getHeaderAccessor())) {
            server = createServer(service, service.getApiServiceDescriptor());
            for (int i = 1; i <= 3; ++i) {
                int instructionReference = i;
                tasks.add(() -> {
                    CountDownLatch waitForTermination = new CountDownLatch(1);
                    ManagedChannel channel = InProcessChannelBuilder.forName(service.getApiServiceDescriptor().getUrl()).build();
                    StreamObserver<BeamFnApi.LogEntry.List> outboundObserver = BeamFnLoggingGrpc.newStub(channel).logging(TestStreams.withOnNext(BeamFnLoggingServiceTest::discardMessage).withOnError(waitForTermination::countDown).build());
                    outboundObserver.onNext(createLogsWithIds(instructionReference, (-instructionReference)));
                    outboundObserver.onError(new RuntimeException(("Client " + instructionReference)));
                    waitForTermination.await();
                    return null;
                });
            }
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.invokeAll(tasks);
        }
    }

    @Test
    public void testServerCloseHangsUpClients() throws Exception {
        LinkedBlockingQueue<BeamFnApi.LogEntry> logs = new LinkedBlockingQueue<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        Collection<Future<Void>> futures = new ArrayList<>();
        try (BeamFnLoggingService service = new BeamFnLoggingService(findOpenPort(), logs::add, ServerStreamObserverFactory.fromOptions(PipelineOptionsFactory.create())::from, GrpcContextHeaderAccessorProvider.getHeaderAccessor())) {
            server = createServer(service, service.getApiServiceDescriptor());
            for (int i = 1; i <= 3; ++i) {
                long instructionReference = i;
                futures.add(executorService.submit(() -> {
                    CountDownLatch waitForServerHangup = new CountDownLatch(1);
                    ManagedChannel channel = InProcessChannelBuilder.forName(service.getApiServiceDescriptor().getUrl()).build();
                    StreamObserver<BeamFnApi.LogEntry.List> outboundObserver = BeamFnLoggingGrpc.newStub(channel).logging(TestStreams.withOnNext(BeamFnLoggingServiceTest::discardMessage).withOnCompleted(waitForServerHangup::countDown).build());
                    outboundObserver.onNext(createLogsWithIds(instructionReference));
                    waitForServerHangup.await();
                    return null;
                }));
            }
            // Wait till each client has sent their message showing that they have connected.
            for (int i = 1; i <= 3; ++i) {
                logs.take();
            }
            service.close();
            server.shutdownNow();
        }
        for (Future<Void> future : futures) {
            future.get();
        }
    }
}
