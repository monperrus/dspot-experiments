/**
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.postgresql;


import PostgresConnectorConfig.SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE;
import io.debezium.config.Configuration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.connect.source.SourceRecord;
import org.fest.assertions.Assertions;
import org.junit.Test;


/**
 * Integration test for {@link io.debezium.connector.postgresql.PostgresConnectorConfig.SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE}
 *
 * @author Jiri Pechanec (jpechane@redhat.com)
 */
public class SnapshotWithOverridesProducerIT extends AbstractRecordsProducerTest {
    private static final String STATEMENTS = "CREATE SCHEMA over;" + ((((((((((((("CREATE TABLE over.t1 (pk INT, PRIMARY KEY(pk));" + "CREATE TABLE over.t2 (pk INT, PRIMARY KEY(pk));") + "INSERT INTO over.t1 VALUES (1);") + "INSERT INTO over.t1 VALUES (2);") + "INSERT INTO over.t1 VALUES (3);") + "INSERT INTO over.t1 VALUES (101);") + "INSERT INTO over.t1 VALUES (102);") + "INSERT INTO over.t1 VALUES (103);") + "INSERT INTO over.t2 VALUES (1);") + "INSERT INTO over.t2 VALUES (2);") + "INSERT INTO over.t2 VALUES (3);") + "INSERT INTO over.t2 VALUES (101);") + "INSERT INTO over.t2 VALUES (102);") + "INSERT INTO over.t2 VALUES (103);");

    private RecordsSnapshotProducer snapshotProducer;

    private PostgresTaskContext context;

    @Test
    public void shouldUseOverriddenSelectStatementDuringSnapshotting() throws Exception {
        before(Configuration.create().with(SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE, "over.t1").with(((SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE.name()) + ".over.t1"), "SELECT * FROM over.t1 WHERE pk > 100").build());
        snapshotProducer = new RecordsSnapshotProducer(context, new SourceInfo(TestHelper.TEST_SERVER, TestHelper.TEST_DATABASE), false);
        final int expectedRecordsCount = 3 + 6;
        TestHelper.execute(SnapshotWithOverridesProducerIT.STATEMENTS);
        AbstractRecordsProducerTest.TestConsumer consumer = testConsumer(expectedRecordsCount, "over");
        snapshotProducer.start(consumer, ( e) -> {
        });
        consumer.await(TestHelper.waitTimeForRecords(), TimeUnit.SECONDS);
        final Map<String, List<SourceRecord>> recordsByTopic = recordsByTopic(expectedRecordsCount, consumer);
        Assertions.assertThat(recordsByTopic.get("test_server.over.t1")).hasSize(3);
        Assertions.assertThat(recordsByTopic.get("test_server.over.t2")).hasSize(6);
    }

    @Test
    public void shouldUseMultipleOverriddenSelectStatementsDuringSnapshotting() throws Exception {
        before(Configuration.create().with(SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE, "over.t1,over.t2").with(((SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE.name()) + ".over.t1"), "SELECT * FROM over.t1 WHERE pk > 101").with(((SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE.name()) + ".over.t2"), "SELECT * FROM over.t2 WHERE pk > 100").build());
        snapshotProducer = new RecordsSnapshotProducer(context, new SourceInfo(TestHelper.TEST_SERVER, TestHelper.TEST_DATABASE), false);
        final int expectedRecordsCount = 2 + 3;
        TestHelper.execute(SnapshotWithOverridesProducerIT.STATEMENTS);
        AbstractRecordsProducerTest.TestConsumer consumer = testConsumer(expectedRecordsCount, "over");
        snapshotProducer.start(consumer, ( e) -> {
        });
        consumer.await(TestHelper.waitTimeForRecords(), TimeUnit.SECONDS);
        final Map<String, List<SourceRecord>> recordsByTopic = recordsByTopic(expectedRecordsCount, consumer);
        Assertions.assertThat(recordsByTopic.get("test_server.over.t1")).hasSize(2);
        Assertions.assertThat(recordsByTopic.get("test_server.over.t2")).hasSize(3);
    }
}
