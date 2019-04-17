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


import io.netty.channel.EventLoopGroup;
import org.apache.camel.impl.JndiRegistry;
import org.junit.Test;


public class NettyUseSharedWorkerThreadPoolManyRoutesTest extends BaseNettyTest {
    private JndiRegistry jndi;

    private EventLoopGroup sharedBoosGroup;

    private EventLoopGroup sharedWorkerGroup;

    private int before;

    @Test
    public void testSharedThreadPool() throws Exception {
        int delta = (Thread.activeCount()) - (before);
        log.info("Created threads {}", delta);
        assertTrue(("There should not be created so many threads: " + delta), (delta < 50));
        sharedBoosGroup.shutdownGracefully().awaitUninterruptibly();
        sharedWorkerGroup.shutdownGracefully().awaitUninterruptibly();
    }
}
