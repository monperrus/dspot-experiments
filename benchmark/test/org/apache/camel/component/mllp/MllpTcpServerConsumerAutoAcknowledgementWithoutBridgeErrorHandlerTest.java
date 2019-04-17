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
package org.apache.camel.component.mllp;


import Exchange.EXCEPTION_CAUGHT;
import MllpConstants.MLLP_ACKNOWLEDGEMENT;
import MllpConstants.MLLP_ACKNOWLEDGEMENT_STRING;
import MllpConstants.MLLP_ACKNOWLEDGEMENT_TYPE;
import org.apache.camel.Exchange;
import org.hamcrest.CoreMatchers;
import org.junit.Test;


public class MllpTcpServerConsumerAutoAcknowledgementWithoutBridgeErrorHandlerTest extends TcpServerConsumerAcknowledgementTestSupport {
    @Test
    public void testReceiveSingleMessage() throws Exception {
        result.expectedBodiesReceived(TcpServerConsumerAcknowledgementTestSupport.TEST_MESSAGE);
        complete.expectedBodiesReceived(TcpServerConsumerAcknowledgementTestSupport.TEST_MESSAGE);
        complete.expectedHeaderReceived(MLLP_ACKNOWLEDGEMENT_TYPE, "AA");
        receiveSingleMessage();
        Exchange completeExchange = complete.getReceivedExchanges().get(0);
        assertNotNull(completeExchange.getIn().getHeader(MLLP_ACKNOWLEDGEMENT));
        assertNotNull(completeExchange.getIn().getHeader(MLLP_ACKNOWLEDGEMENT_STRING));
        String acknowledgement = completeExchange.getIn().getHeader(MLLP_ACKNOWLEDGEMENT_STRING, String.class);
        assertThat(acknowledgement, CoreMatchers.startsWith("MSH|^~\\&|^org^sys||APP_A|FAC_A|"));
        assertThat(acknowledgement, CoreMatchers.endsWith("||ACK^A04^ADT_A04|||2.6\rMSA|AA|\r"));
    }

    @Test
    public void testUnparsableMessage() throws Exception {
        final String testMessage = "MSH" + (TcpServerConsumerAcknowledgementTestSupport.TEST_MESSAGE);
        result.expectedBodiesReceived(testMessage);
        complete.expectedMessageCount(1);
        unparsableMessage(testMessage);
        assertNull("Should not have the exception in the exchange property", result.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
        assertNull("Should not have the exception in the exchange property", complete.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
    }

    @Test
    public void testMessageWithEmptySegment() throws Exception {
        final String testMessage = TcpServerConsumerAcknowledgementTestSupport.TEST_MESSAGE.replace("\rPID|", "\r\rPID|");
        result.expectedBodiesReceived(testMessage);
        complete.expectedMessageCount(1);
        unparsableMessage(testMessage);
        assertNull("Should not have the exception in the exchange property", result.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
        assertNull("Should not have the exception in the exchange property", complete.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
    }

    @Test
    public void testMessageWithEmbeddedNewlines() throws Exception {
        final String testMessage = TcpServerConsumerAcknowledgementTestSupport.TEST_MESSAGE.replace("\rPID|", "\r\n\rPID|\n");
        result.expectedBodiesReceived(testMessage);
        complete.expectedMessageCount(1);
        unparsableMessage(testMessage);
        assertNull("Should not have the exception in the exchange property", result.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
        assertNull("Should not have the exception in the exchange property", complete.getReceivedExchanges().get(0).getProperty(EXCEPTION_CAUGHT));
    }
}
