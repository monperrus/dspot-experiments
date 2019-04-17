/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache.tier.sockets;


import org.apache.geode.internal.cache.tier.Encryptor;
import org.apache.geode.security.AuthenticationRequiredException;
import org.apache.geode.test.junit.categories.ClientServerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;


@Category({ ClientServerTest.class })
public class MessageIdExtractorTest {
    @Mock
    Message requestMessage;

    @Mock
    Encryptor encryptor;

    private MessageIdExtractor messageIdExtractor;

    private Long connectionId = 123L;

    private Long uniqueId = 234L;

    private byte[] decryptedBytes;

    @Test
    public void getUniqueIdFromMessage() throws Exception {
        assertThat(messageIdExtractor.getUniqueIdFromMessage(requestMessage, encryptor, connectionId)).isEqualTo(uniqueId);
    }

    @Test
    public void throwsWhenConnectionIdsDoNotMatch() throws Exception {
        long otherConnectionId = 789L;
        assertThatThrownBy(() -> messageIdExtractor.getUniqueIdFromMessage(requestMessage, encryptor, otherConnectionId)).isInstanceOf(AuthenticationRequiredException.class);
    }
}
