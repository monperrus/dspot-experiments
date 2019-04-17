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
package org.apache.camel.component.netty4;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


public class NettyConsumerClientModeTest extends BaseNettyTest {
    private NettyConsumerClientModeTest.MyServer server;

    @Test
    public void testNettyRoute() throws Exception {
        try {
            startNettyServer();
            MockEndpoint receive = context.getEndpoint("mock:receive", MockEndpoint.class);
            receive.expectedBodiesReceived("Bye Willem");
            context.getRouteController().startRoute("client");
            receive.assertIsSatisfied();
        } finally {
            shutdownServer();
        }
    }

    private static class MyServer {
        private int port;

        private ServerBootstrap bootstrap;

        private Channel channel;

        private EventLoopGroup bossGroup;

        private EventLoopGroup workerGroup;

        MyServer(int port) {
            this.port = port;
        }

        public void start() throws Exception {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(io.netty.channel.socket.nio.NioServerSocketChannel.class).childHandler(new NettyConsumerClientModeTest.ServerInitializer());
            ChannelFuture cf = bootstrap.bind(port).sync();
            channel = cf.channel();
        }

        public void shutdown() {
            channel.disconnect();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class ServerHandler extends SimpleChannelInboundHandler<String> {
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.write("Willem\r\n");
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            // Do nothing here
        }

        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }

    private static class ServerInitializer extends ChannelInitializer<SocketChannel> {
        private static final StringDecoder DECODER = new StringDecoder();

        private static final StringEncoder ENCODER = new StringEncoder();

        private static final NettyConsumerClientModeTest.ServerHandler SERVERHANDLER = new NettyConsumerClientModeTest.ServerHandler();

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // Add the text line codec combination first,
            pipeline.addLast("framer", new io.netty.handler.codec.DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            // the encoder and decoder are static as these are sharable
            pipeline.addLast("decoder", NettyConsumerClientModeTest.ServerInitializer.DECODER);
            pipeline.addLast("encoder", NettyConsumerClientModeTest.ServerInitializer.ENCODER);
            // and then business logic.
            pipeline.addLast("handler", NettyConsumerClientModeTest.ServerInitializer.SERVERHANDLER);
        }
    }
}
