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
package org.apache.camel.component.aws.ddbstream;


import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class ShardListAtSequenceParametrised {
    private ShardList undertest;

    private final String inputSequenceNumber;

    private final String expectedShardId;

    public ShardListAtSequenceParametrised(String inputSequenceNumber, String expectedShardId) {
        this.inputSequenceNumber = inputSequenceNumber;
        this.expectedShardId = expectedShardId;
    }

    @Test
    public void assertions() throws Exception {
        Assert.assertThat(undertest.atSeq(inputSequenceNumber).getShardId(), CoreMatchers.is(expectedShardId));
    }
}
