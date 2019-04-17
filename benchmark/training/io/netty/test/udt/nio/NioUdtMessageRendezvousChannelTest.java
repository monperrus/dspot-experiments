/**
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.test.udt.nio;


import io.netty.channel.udt.nio.NioUdtMessageRendezvousChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.junit.Assert;
import org.junit.Test;


public class NioUdtMessageRendezvousChannelTest extends AbstractUdtTest {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(NioUdtByteAcceptorChannelTest.class);

    /**
     * verify channel meta data
     */
    @Test
    public void metadata() throws Exception {
        Assert.assertFalse(new NioUdtMessageRendezvousChannel().metadata().hasDisconnect());
    }
}
