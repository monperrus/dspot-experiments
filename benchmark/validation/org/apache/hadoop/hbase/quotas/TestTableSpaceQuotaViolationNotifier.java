/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.quotas;


import QuotaProtos.SpaceQuotaStatus;
import QuotaProtos.SpaceViolationPolicy.NO_INSERTS;
import QuotaTableUtil.QUOTA_TABLE_NAME;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseClassTestRule;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.shaded.protobuf.generated.QuotaProtos;
import org.apache.hadoop.hbase.testclassification.SmallTests;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static SpaceViolationPolicy.NO_INSERTS;


/**
 * Test case for {@link TableSpaceQuotaSnapshotNotifier}.
 */
@Category(SmallTests.class)
public class TestTableSpaceQuotaViolationNotifier {
    @ClassRule
    public static final HBaseClassTestRule CLASS_RULE = HBaseClassTestRule.forClass(TestTableSpaceQuotaViolationNotifier.class);

    private TableSpaceQuotaSnapshotNotifier notifier;

    private Connection conn;

    @Test
    public void testToViolation() throws Exception {
        final TableName tn = TableName.valueOf("inviolation");
        final SpaceQuotaSnapshot snapshot = new SpaceQuotaSnapshot(new org.apache.hadoop.hbase.quotas.SpaceQuotaSnapshot.SpaceQuotaStatus(NO_INSERTS), 1024L, 512L);
        final Table quotaTable = Mockito.mock(Table.class);
        Mockito.when(conn.getTable(QUOTA_TABLE_NAME)).thenReturn(quotaTable);
        final Put expectedPut = new Put(Bytes.toBytes(("t." + (tn.getNameAsString()))));
        final QuotaProtos.SpaceQuotaSnapshot protoQuota = QuotaProtos.SpaceQuotaSnapshot.newBuilder().setQuotaStatus(SpaceQuotaStatus.newBuilder().setInViolation(true).setViolationPolicy(NO_INSERTS)).setQuotaLimit(512L).setQuotaUsage(1024L).build();
        expectedPut.addColumn(Bytes.toBytes("u"), Bytes.toBytes("p"), protoQuota.toByteArray());
        notifier.transitionTable(tn, snapshot);
        Mockito.verify(quotaTable).put(ArgumentMatchers.argThat(new TestTableSpaceQuotaViolationNotifier.SingleCellMutationMatcher<Put>(expectedPut)));
    }

    /**
     * Quick hack to verify a Mutation with one column.
     */
    private static final class SingleCellMutationMatcher<T> implements ArgumentMatcher<T> {
        private final Mutation expected;

        private SingleCellMutationMatcher(Mutation expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(T argument) {
            if (!(expected.getClass().isAssignableFrom(argument.getClass()))) {
                return false;
            }
            Mutation actual = ((Mutation) (argument));
            if (!(Arrays.equals(expected.getRow(), actual.getRow()))) {
                return false;
            }
            if ((expected.size()) != (actual.size())) {
                return false;
            }
            NavigableMap<byte[], List<Cell>> expectedCells = expected.getFamilyCellMap();
            NavigableMap<byte[], List<Cell>> actualCells = actual.getFamilyCellMap();
            Map.Entry<byte[], List<Cell>> expectedEntry = expectedCells.entrySet().iterator().next();
            Map.Entry<byte[], List<Cell>> actualEntry = actualCells.entrySet().iterator().next();
            if (!(Arrays.equals(expectedEntry.getKey(), actualEntry.getKey()))) {
                return false;
            }
            return Objects.equals(expectedEntry.getValue(), actualEntry.getValue());
        }
    }
}
