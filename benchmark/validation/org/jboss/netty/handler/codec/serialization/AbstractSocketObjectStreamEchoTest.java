/**
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.serialization;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.TestUtil;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractSocketObjectStreamEchoTest {
    static final Random random = new Random();

    static final String[] data = new String[1024];

    static {
        for (int i = 0; i < (AbstractSocketObjectStreamEchoTest.data.length); i++) {
            int eLen = AbstractSocketObjectStreamEchoTest.random.nextInt(512);
            char[] e = new char[eLen];
            for (int j = 0; j < eLen; j++) {
                e[j] = ((char) ('a' + (AbstractSocketObjectStreamEchoTest.random.nextInt(26))));
            }
            AbstractSocketObjectStreamEchoTest.data[i] = new String(e);
        }
    }

    @Test
    public void testObjectEcho() throws Throwable {
        ServerBootstrap sb = new ServerBootstrap(newServerSocketChannelFactory(Executors.newCachedThreadPool()));
        ClientBootstrap cb = new ClientBootstrap(newClientSocketChannelFactory(Executors.newCachedThreadPool()));
        AbstractSocketObjectStreamEchoTest.EchoHandler sh = new AbstractSocketObjectStreamEchoTest.EchoHandler();
        AbstractSocketObjectStreamEchoTest.EchoHandler ch = new AbstractSocketObjectStreamEchoTest.EchoHandler();
        sb.getPipeline().addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(String.class.getClassLoader())));
        sb.getPipeline().addLast("encoder", new ObjectEncoder());
        sb.getPipeline().addLast("handler", sh);
        cb.getPipeline().addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(String.class.getClassLoader())));
        cb.getPipeline().addLast("encoder", new ObjectEncoder());
        cb.getPipeline().addLast("handler", ch);
        Channel sc = sb.bind(new InetSocketAddress(0));
        int port = ((InetSocketAddress) (sc.getLocalAddress())).getPort();
        ChannelFuture ccf = cb.connect(new InetSocketAddress(TestUtil.getLocalHost(), port));
        Assert.assertTrue(ccf.awaitUninterruptibly().isSuccess());
        Channel cc = ccf.getChannel();
        for (String element : AbstractSocketObjectStreamEchoTest.data) {
            cc.write(element);
        }
        while ((ch.counter) < (AbstractSocketObjectStreamEchoTest.data.length)) {
            if ((sh.exception.get()) != null) {
                break;
            }
            if ((ch.exception.get()) != null) {
                break;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Ignore.
            }
        } 
        while ((sh.counter) < (AbstractSocketObjectStreamEchoTest.data.length)) {
            if ((sh.exception.get()) != null) {
                break;
            }
            if ((ch.exception.get()) != null) {
                break;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // Ignore.
            }
        } 
        sh.channel.close().awaitUninterruptibly();
        ch.channel.close().awaitUninterruptibly();
        sc.close().awaitUninterruptibly();
        cb.shutdown();
        sb.shutdown();
        cb.releaseExternalResources();
        sb.releaseExternalResources();
        if (((sh.exception.get()) != null) && (!((sh.exception.get()) instanceof IOException))) {
            throw sh.exception.get();
        }
        if (((ch.exception.get()) != null) && (!((ch.exception.get()) instanceof IOException))) {
            throw ch.exception.get();
        }
        if ((sh.exception.get()) != null) {
            throw sh.exception.get();
        }
        if ((ch.exception.get()) != null) {
            throw ch.exception.get();
        }
    }

    private static class EchoHandler extends SimpleChannelUpstreamHandler {
        volatile Channel channel;

        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();

        volatile int counter;

        EchoHandler() {
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            channel = e.getChannel();
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            String m = ((String) (e.getMessage()));
            Assert.assertEquals(AbstractSocketObjectStreamEchoTest.data[counter], m);
            if ((channel.getParent()) != null) {
                channel.write(m);
            }
            (counter)++;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            if (exception.compareAndSet(null, e.getCause())) {
                e.getChannel().close();
            }
        }
    }
}
