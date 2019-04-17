/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.kafkarest.unit;


import EmbeddedFormat.AVRO;
import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.kafkarest.TestUtils;
import io.confluent.kafkarest.entities.AvroConsumerRecord;
import io.confluent.kafkarest.entities.ConsumerRecord;
import io.confluent.rest.RestConfigException;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;


public class PartitionsResourceAvroConsumeTest extends PartitionsResourceAbstractConsumeTest {
    public PartitionsResourceAvroConsumeTest() throws RestConfigException {
        super();
    }

    @Test
    public void testConsumeOk() {
        final List<? extends ConsumerRecord<JsonNode, JsonNode>> records = Arrays.asList(new AvroConsumerRecord(topicName, TestUtils.jsonTree("\"key1\""), TestUtils.jsonTree("\"value1\""), 0, 10));
        for (TestUtils.RequestMediaType mediatype : TestUtils.V1_ACCEPT_MEDIATYPES_AVRO) {
            expectConsume(AVRO, records);
            final Response response = request(topicName, partitionId, offset, mediatype.header);
            TestUtils.assertOKResponse(response, mediatype.expected);
            final List<AvroConsumerRecord> readResponseRecords = TestUtils.tryReadEntityOrLog(response, new javax.ws.rs.core.GenericType<List<AvroConsumerRecord>>() {});
            Assert.assertEquals(records, readResponseRecords);
            EasyMock.verify(simpleConsumerManager);
            EasyMock.reset(simpleConsumerManager);
        }
    }
}
