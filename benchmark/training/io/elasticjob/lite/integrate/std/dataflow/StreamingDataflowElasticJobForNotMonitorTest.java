/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package io.elasticjob.lite.integrate.std.dataflow;


import io.elasticjob.lite.integrate.AbstractBaseStdJobAutoInitTest;
import io.elasticjob.lite.integrate.WaitingUtils;
import io.elasticjob.lite.integrate.fixture.dataflow.StreamingDataflowElasticJob;
import org.junit.Assert;
import org.junit.Test;


public final class StreamingDataflowElasticJobForNotMonitorTest extends AbstractBaseStdJobAutoInitTest {
    public StreamingDataflowElasticJobForNotMonitorTest() {
        super(StreamingDataflowElasticJob.class);
    }

    @Test
    public void assertJobInit() {
        while (!(StreamingDataflowElasticJob.isCompleted())) {
            WaitingUtils.waitingShortTime();
        } 
        Assert.assertTrue(getRegCenter().isExisted((("/" + (getJobName())) + "/sharding")));
    }
}
