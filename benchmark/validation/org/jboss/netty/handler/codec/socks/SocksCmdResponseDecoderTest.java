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
package org.jboss.netty.handler.codec.socks;


import SocksMessage.AddressType;
import SocksMessage.CmdStatus;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.junit.Test;


public class SocksCmdResponseDecoderTest {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SocksCmdResponseDecoderTest.class);

    @Test
    public void testSocksCmdResponseDecoder() throws Exception {
        for (SocksMessage.CmdStatus cmdStatus : CmdStatus.values()) {
            for (SocksMessage.AddressType addressType : AddressType.values()) {
                SocksCmdResponseDecoderTest.testSocksCmdResponseDecoderWithDifferentParams(cmdStatus, addressType);
            }
        }
    }
}
