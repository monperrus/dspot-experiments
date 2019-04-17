/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.indexer;


import com.google.common.primitives.Bytes;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.common.Pair;
import org.hamcrest.number.OrderingComparison;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;


public class BucketTest {
    Bucket bucket;

    int shardNum;

    int partitionNum;

    DateTime time;

    @Test
    public void testToGroupKey() {
        byte[] firstPart = new byte[]{ 1, 1, 0, 10 };
        byte[] secondPart = new byte[]{ 2, 4, 0, 5 };
        byte[] expectedGroupParts = bucket.toGroupKey(firstPart, secondPart);
        Pair<Bucket, byte[]> actualPair = Bucket.fromGroupKey(expectedGroupParts);
        Assert.assertEquals("Bucket is not matching", bucket, actualPair.lhs);
        Assert.assertArrayEquals("Parts not matching", Bytes.concat(firstPart, secondPart), actualPair.rhs);
    }

    @Test
    public void testToString() {
        String expectedString = (((((("Bucket{" + "time=") + (time)) + ", partitionNum=") + (partitionNum)) + ", shardNum=") + (shardNum)) + '}';
        Assert.assertEquals(bucket.toString(), expectedString);
    }

    @Test
    public void testEquals() {
        // noinspection ObjectEqualsNull
        Assert.assertFalse("Object should not be equals to NULL", bucket.equals(null));
        // noinspection EqualsBetweenInconvertibleTypes
        Assert.assertFalse("Objects do not have the same Class", bucket.equals(0));
        Assert.assertFalse("Objects do not have the same partitionNum", bucket.equals(new Bucket(shardNum, time, ((partitionNum) + 1))));
        Assert.assertFalse("Objects do not have the same shardNum", bucket.equals(new Bucket(((shardNum) + 1), time, partitionNum)));
        Assert.assertFalse("Objects do not have the same time", bucket.equals(new Bucket(shardNum, DateTimes.nowUtc(), partitionNum)));
        Assert.assertFalse("Object do have NULL time", bucket.equals(new Bucket(shardNum, null, partitionNum)));
        Assert.assertTrue("Objects must be the same", bucket.equals(new Bucket(shardNum, time, partitionNum)));
    }

    @Test
    public void testHashCode() {
        int hashCode = bucket.hashCode();
        Assert.assertThat(hashCode, OrderingComparison.greaterThanOrEqualTo(((31 * (partitionNum)) + (shardNum))));
        bucket = new Bucket(shardNum, null, partitionNum);
        hashCode = bucket.hashCode();
        Assert.assertEquals(hashCode, ((31 * (partitionNum)) + (shardNum)));
    }
}
