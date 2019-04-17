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
package org.apache.hadoop.hbase.ipc;


import RpcServer.MAX_REQUEST_SIZE;
import RpcServerFactory.CUSTOM_RPC_SERVER_IMPL_CONF_KEY;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestProtos.EchoRequestProto;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestProtos.EchoResponseProto;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestProtos.EmptyRequestProto;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestProtos.EmptyResponseProto;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestProtos.PauseRequestProto;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestRpcServiceProtos.TestProtobufRpcProto.BlockingInterface;
import org.apache.hadoop.hbase.shaded.ipc.protobuf.generated.TestRpcServiceProtos.TestProtobufRpcProto.Interface;
import org.apache.hadoop.hbase.shaded.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.util.StringUtils;
import org.apache.hbase.thirdparty.com.google.common.collect.ImmutableList;
import org.apache.hbase.thirdparty.com.google.common.collect.Lists;
import org.apache.hbase.thirdparty.com.google.protobuf.ServiceException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Some basic ipc tests.
 */
public abstract class AbstractTestIPC {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestIPC.class);

    private static final byte[] CELL_BYTES = Bytes.toBytes("xyz");

    private static final KeyValue CELL = new KeyValue(AbstractTestIPC.CELL_BYTES, AbstractTestIPC.CELL_BYTES, AbstractTestIPC.CELL_BYTES, AbstractTestIPC.CELL_BYTES);

    protected static final Configuration CONF = HBaseConfiguration.create();

    static {
        // Set the default to be the old SimpleRpcServer. Subclasses test it and netty.
        AbstractTestIPC.CONF.set(CUSTOM_RPC_SERVER_IMPL_CONF_KEY, SimpleRpcServer.class.getName());
    }

    /**
     * Ensure we do not HAVE TO HAVE a codec.
     */
    @Test
    public void testNoCodec() throws IOException, ServiceException {
        Configuration conf = HBaseConfiguration.create();
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClientNoCodec(conf)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            HBaseRpcController pcrc = new HBaseRpcControllerImpl();
            String message = "hello";
            Assert.assertEquals(message, stub.echo(pcrc, EchoRequestProto.newBuilder().setMessage(message).build()).getMessage());
            Assert.assertNull(pcrc.cellScanner());
        } finally {
            rpcServer.stop();
        }
    }

    /**
     * It is hard to verify the compression is actually happening under the wraps. Hope that if
     * unsupported, we'll get an exception out of some time (meantime, have to trace it manually to
     * confirm that compression is happening down in the client and server).
     */
    @Test
    public void testCompressCellBlock() throws IOException, ServiceException {
        Configuration conf = new Configuration(HBaseConfiguration.create());
        conf.set("hbase.client.rpc.compressor", GzipCodec.class.getCanonicalName());
        List<Cell> cells = new ArrayList<>();
        int count = 3;
        for (int i = 0; i < count; i++) {
            cells.add(AbstractTestIPC.CELL);
        }
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(conf)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            HBaseRpcController pcrc = new HBaseRpcControllerImpl(CellUtil.createCellScanner(cells));
            String message = "hello";
            Assert.assertEquals(message, stub.echo(pcrc, EchoRequestProto.newBuilder().setMessage(message).build()).getMessage());
            int index = 0;
            CellScanner cellScanner = pcrc.cellScanner();
            Assert.assertNotNull(cellScanner);
            while (cellScanner.advance()) {
                Assert.assertEquals(AbstractTestIPC.CELL, cellScanner.current());
                index++;
            } 
            Assert.assertEquals(count, index);
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    public void testRTEDuringConnectionSetup() throws Exception {
        Configuration conf = HBaseConfiguration.create();
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClientRTEDuringConnectionSetup(conf)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            stub.ping(null, EmptyRequestProto.getDefaultInstance());
            Assert.fail("Expected an exception to have been thrown!");
        } catch (Exception e) {
            AbstractTestIPC.LOG.info(("Caught expected exception: " + (e.toString())));
            Assert.assertTrue(e.toString(), StringUtils.stringifyException(e).contains("Injected fault"));
        } finally {
            rpcServer.stop();
        }
    }

    /**
     * Tests that the rpc scheduler is called when requests arrive.
     */
    @Test
    public void testRpcScheduler() throws IOException, InterruptedException, ServiceException {
        RpcScheduler scheduler = Mockito.spy(new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, scheduler);
        Mockito.verify(scheduler).init(((RpcScheduler.Context) (ArgumentMatchers.anyObject())));
        try (AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF)) {
            rpcServer.start();
            Mockito.verify(scheduler).start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            EchoRequestProto param = EchoRequestProto.newBuilder().setMessage("hello").build();
            for (int i = 0; i < 10; i++) {
                stub.echo(null, param);
            }
            Mockito.verify(scheduler, VerificationModeFactory.times(10)).dispatch(((CallRunner) (ArgumentMatchers.anyObject())));
        } finally {
            rpcServer.stop();
            Mockito.verify(scheduler).stop();
        }
    }

    /**
     * Tests that the rpc scheduler is called when requests arrive.
     */
    @Test
    public void testRpcMaxRequestSize() throws IOException, ServiceException {
        Configuration conf = new Configuration(AbstractTestIPC.CONF);
        conf.setInt(MAX_REQUEST_SIZE, 1000);
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), conf, new FifoRpcScheduler(conf, 1));
        try (AbstractRpcClient<?> client = createRpcClient(conf)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            StringBuilder message = new StringBuilder(1200);
            for (int i = 0; i < 200; i++) {
                message.append("hello.");
            }
            // set total RPC size bigger than 100 bytes
            EchoRequestProto param = EchoRequestProto.newBuilder().setMessage(message.toString()).build();
            stub.echo(new HBaseRpcControllerImpl(CellUtil.createCellScanner(ImmutableList.<Cell>of(AbstractTestIPC.CELL))), param);
            Assert.fail("RPC should have failed because it exceeds max request size");
        } catch (ServiceException e) {
            AbstractTestIPC.LOG.info(("Caught expected exception: " + e));
            Assert.assertTrue(e.toString(), StringUtils.stringifyException(e).contains("RequestTooBigException"));
        } finally {
            rpcServer.stop();
        }
    }

    /**
     * Tests that the RpcServer creates & dispatches CallRunner object to scheduler with non-null
     * remoteAddress set to its Call Object
     *
     * @throws ServiceException
     * 		
     */
    @Test
    public void testRpcServerForNotNullRemoteAddressInCallObject() throws IOException, ServiceException {
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        InetSocketAddress localAddr = new InetSocketAddress("localhost", 0);
        try (AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            Assert.assertEquals(localAddr.getAddress().getHostAddress(), stub.addr(null, EmptyRequestProto.getDefaultInstance()).getAddr());
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    public void testRemoteError() throws IOException, ServiceException {
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            stub.error(null, EmptyRequestProto.getDefaultInstance());
        } catch (ServiceException e) {
            AbstractTestIPC.LOG.info(("Caught expected exception: " + e));
            IOException ioe = ProtobufUtil.handleRemoteException(e);
            Assert.assertTrue((ioe instanceof DoNotRetryIOException));
            Assert.assertTrue(ioe.getMessage().contains("server error!"));
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    public void testTimeout() throws IOException {
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            HBaseRpcController pcrc = new HBaseRpcControllerImpl();
            int ms = 1000;
            int timeout = 100;
            for (int i = 0; i < 10; i++) {
                pcrc.reset();
                pcrc.setCallTimeout(timeout);
                long startTime = System.nanoTime();
                try {
                    stub.pause(pcrc, PauseRequestProto.newBuilder().setMs(ms).build());
                } catch (ServiceException e) {
                    long waitTime = ((System.nanoTime()) - startTime) / 1000000;
                    // expected
                    AbstractTestIPC.LOG.info(("Caught expected exception: " + e));
                    IOException ioe = ProtobufUtil.handleRemoteException(e);
                    Assert.assertTrue(((ioe.getCause()) instanceof CallTimeoutException));
                    // confirm that we got exception before the actual pause.
                    Assert.assertTrue((waitTime < ms));
                }
            }
        } finally {
            rpcServer.stop();
        }
    }

    /**
     * Tests that the connection closing is handled by the client with outstanding RPC calls
     */
    @Test
    public void testConnectionCloseWithOutstandingRPCs() throws IOException, InterruptedException {
        Configuration conf = new Configuration(AbstractTestIPC.CONF);
        RpcServer rpcServer = createTestFailingRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(conf)) {
            rpcServer.start();
            BlockingInterface stub = TestProtobufRpcServiceImpl.newBlockingStub(client, rpcServer.getListenerAddress());
            EchoRequestProto param = EchoRequestProto.newBuilder().setMessage("hello").build();
            stub.echo(null, param);
            Assert.fail("RPC should have failed because connection closed");
        } catch (ServiceException e) {
            AbstractTestIPC.LOG.info(("Caught expected exception: " + (e.toString())));
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    public void testAsyncEcho() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(conf)) {
            rpcServer.start();
            Interface stub = TestProtobufRpcServiceImpl.newStub(client, rpcServer.getListenerAddress());
            int num = 10;
            List<HBaseRpcController> pcrcList = new ArrayList<>();
            List<BlockingRpcCallback<EchoResponseProto>> callbackList = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                HBaseRpcController pcrc = new HBaseRpcControllerImpl();
                BlockingRpcCallback<EchoResponseProto> done = new BlockingRpcCallback();
                stub.echo(pcrc, EchoRequestProto.newBuilder().setMessage(("hello-" + i)).build(), done);
                pcrcList.add(pcrc);
                callbackList.add(done);
            }
            for (int i = 0; i < num; i++) {
                HBaseRpcController pcrc = pcrcList.get(i);
                Assert.assertFalse(pcrc.failed());
                Assert.assertNull(pcrc.cellScanner());
                Assert.assertEquals(("hello-" + i), callbackList.get(i).get().getMessage());
            }
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    public void testAsyncRemoteError() throws IOException {
        AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF);
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try {
            rpcServer.start();
            Interface stub = TestProtobufRpcServiceImpl.newStub(client, rpcServer.getListenerAddress());
            BlockingRpcCallback<EmptyResponseProto> callback = new BlockingRpcCallback();
            HBaseRpcController pcrc = new HBaseRpcControllerImpl();
            stub.error(pcrc, EmptyRequestProto.getDefaultInstance(), callback);
            Assert.assertNull(callback.get());
            Assert.assertTrue(pcrc.failed());
            AbstractTestIPC.LOG.info(("Caught expected exception: " + (pcrc.getFailed())));
            IOException ioe = ProtobufUtil.handleRemoteException(pcrc.getFailed());
            Assert.assertTrue((ioe instanceof DoNotRetryIOException));
            Assert.assertTrue(ioe.getMessage().contains("server error!"));
        } finally {
            client.close();
            rpcServer.stop();
        }
    }

    @Test
    public void testAsyncTimeout() throws IOException {
        RpcServer rpcServer = createRpcServer(null, "testRpcServer", Lists.newArrayList(new RpcServer.BlockingServiceAndInterface(TestProtobufRpcServiceImpl.SERVICE, null)), new InetSocketAddress("localhost", 0), AbstractTestIPC.CONF, new FifoRpcScheduler(AbstractTestIPC.CONF, 1));
        try (AbstractRpcClient<?> client = createRpcClient(AbstractTestIPC.CONF)) {
            rpcServer.start();
            Interface stub = TestProtobufRpcServiceImpl.newStub(client, rpcServer.getListenerAddress());
            List<HBaseRpcController> pcrcList = new ArrayList<>();
            List<BlockingRpcCallback<EmptyResponseProto>> callbackList = new ArrayList<>();
            int ms = 1000;
            int timeout = 100;
            long startTime = System.nanoTime();
            for (int i = 0; i < 10; i++) {
                HBaseRpcController pcrc = new HBaseRpcControllerImpl();
                pcrc.setCallTimeout(timeout);
                BlockingRpcCallback<EmptyResponseProto> callback = new BlockingRpcCallback();
                stub.pause(pcrc, PauseRequestProto.newBuilder().setMs(ms).build(), callback);
                pcrcList.add(pcrc);
                callbackList.add(callback);
            }
            for (BlockingRpcCallback<?> callback : callbackList) {
                Assert.assertNull(callback.get());
            }
            long waitTime = ((System.nanoTime()) - startTime) / 1000000;
            for (HBaseRpcController pcrc : pcrcList) {
                Assert.assertTrue(pcrc.failed());
                AbstractTestIPC.LOG.info(("Caught expected exception: " + (pcrc.getFailed())));
                IOException ioe = ProtobufUtil.handleRemoteException(pcrc.getFailed());
                Assert.assertTrue(((ioe.getCause()) instanceof CallTimeoutException));
            }
            // confirm that we got exception before the actual pause.
            Assert.assertTrue((waitTime < ms));
        } finally {
            rpcServer.stop();
        }
    }
}
