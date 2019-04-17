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
package org.apache.druid.sql.avatica;


import DruidAvaticaHandler.AVATICA_PATH;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Injector;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.calcite.avatica.AvaticaClientRuntimeException;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.avatica.MissingResultsException;
import org.apache.calcite.avatica.NoSuchStatementException;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.common.Pair;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.java.util.common.io.Closer;
import org.apache.druid.math.expr.ExprMacroTable;
import org.apache.druid.query.QueryRunnerFactoryConglomerate;
import org.apache.druid.server.DruidNode;
import org.apache.druid.server.RequestLogLine;
import org.apache.druid.server.log.TestRequestLogger;
import org.apache.druid.server.security.AuthTestUtils;
import org.apache.druid.sql.calcite.planner.Calcites;
import org.apache.druid.sql.calcite.planner.DruidOperatorTable;
import org.apache.druid.sql.calcite.planner.PlannerConfig;
import org.apache.druid.sql.calcite.schema.DruidSchema;
import org.apache.druid.sql.calcite.schema.SystemSchema;
import org.apache.druid.sql.calcite.util.CalciteTestBase;
import org.apache.druid.sql.calcite.util.CalciteTests;
import org.apache.druid.sql.calcite.util.QueryLogHook;
import org.apache.druid.sql.calcite.util.SpecificSegmentsQuerySegmentWalker;
import org.eclipse.jetty.server.Server;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;


public class DruidAvaticaHandlerTest extends CalciteTestBase {
    private static final AvaticaServerConfig AVATICA_CONFIG = new AvaticaServerConfig() {
        @Override
        public int getMaxConnections() {
            // This must match the number of Connection objects created in setUp()
            return 3;
        }

        @Override
        public int getMaxStatementsPerConnection() {
            return 4;
        }
    };

    private static final String DUMMY_SQL_QUERY_ID = "dummy";

    private static QueryRunnerFactoryConglomerate conglomerate;

    private static Closer resourceCloser;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public QueryLogHook queryLogHook = QueryLogHook.create();

    private SpecificSegmentsQuerySegmentWalker walker;

    private Server server;

    private Connection client;

    private Connection superuserClient;

    private Connection clientLosAngeles;

    private DruidMeta druidMeta;

    private String url;

    private Injector injector;

    private TestRequestLogger testRequestLogger;

    @Test
    public void testSelectCount() throws Exception {
        final ResultSet resultSet = client.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM druid.foo");
        final List<Map<String, Object>> rows = DruidAvaticaHandlerTest.getRows(resultSet);
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("cnt", 6L)), rows);
    }

    @Test
    public void testSelectCountAlternateStyle() throws Exception {
        final ResultSet resultSet = client.prepareStatement("SELECT COUNT(*) AS cnt FROM druid.foo").executeQuery();
        final List<Map<String, Object>> rows = DruidAvaticaHandlerTest.getRows(resultSet);
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("cnt", 6L)), rows);
    }

    @Test
    public void testTimestampsInResponse() throws Exception {
        final ResultSet resultSet = client.createStatement().executeQuery("SELECT __time, CAST(__time AS DATE) AS t2 FROM druid.foo LIMIT 1");
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("__time", new java.sql.Timestamp(DateTimes.of("2000-01-01T00:00:00.000Z").getMillis()), "t2", new java.sql.Date(DateTimes.of("2000-01-01").getMillis()))), DruidAvaticaHandlerTest.getRows(resultSet));
    }

    @Test
    public void testTimestampsInResponseLosAngelesTimeZone() throws Exception {
        final ResultSet resultSet = clientLosAngeles.createStatement().executeQuery("SELECT __time, CAST(__time AS DATE) AS t2 FROM druid.foo LIMIT 1");
        final DateTimeZone timeZone = DateTimes.inferTzFromString("America/Los_Angeles");
        final DateTime localDateTime = new DateTime("2000-01-01T00Z", timeZone);
        final List<Map<String, Object>> resultRows = DruidAvaticaHandlerTest.getRows(resultSet);
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("__time", new java.sql.Timestamp(Calcites.jodaToCalciteTimestamp(localDateTime, timeZone)), "t2", new java.sql.Date(Calcites.jodaToCalciteTimestamp(localDateTime.dayOfMonth().roundFloorCopy(), timeZone)))), resultRows);
    }

    @Test
    public void testFieldAliasingSelect() throws Exception {
        final ResultSet resultSet = client.createStatement().executeQuery("SELECT dim2 AS \"x\", dim2 AS \"y\" FROM druid.foo LIMIT 1");
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("x", "a", "y", "a")), DruidAvaticaHandlerTest.getRows(resultSet));
    }

    @Test
    public void testSelectBoolean() throws Exception {
        final ResultSet resultSet = client.createStatement().executeQuery("SELECT dim2, dim2 IS NULL AS isnull FROM druid.foo LIMIT 1");
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("dim2", "a", "isnull", false)), DruidAvaticaHandlerTest.getRows(resultSet));
    }

    @Test
    public void testExplainSelectCount() throws Exception {
        final ResultSet resultSet = clientLosAngeles.createStatement().executeQuery("EXPLAIN PLAN FOR SELECT COUNT(*) AS cnt FROM druid.foo");
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("PLAN", StringUtils.format("DruidQueryRel(query=[{\"queryType\":\"timeseries\",\"dataSource\":{\"type\":\"table\",\"name\":\"foo\"},\"intervals\":{\"type\":\"intervals\",\"intervals\":[\"-146136543-09-08T08:23:32.096Z/146140482-04-24T15:36:27.903Z\"]},\"descending\":false,\"virtualColumns\":[],\"filter\":null,\"granularity\":{\"type\":\"all\"},\"aggregations\":[{\"type\":\"count\",\"name\":\"a0\"}],\"postAggregations\":[],\"limit\":2147483647,\"context\":{\"skipEmptyBuckets\":true,\"sqlQueryId\":\"%s\",\"sqlTimeZone\":\"America/Los_Angeles\"}}], signature=[{a0:LONG}])\n", DruidAvaticaHandlerTest.DUMMY_SQL_QUERY_ID))), DruidAvaticaHandlerTest.getRows(resultSet));
    }

    @Test
    public void testDatabaseMetaDataCatalogs() throws Exception {
        final DatabaseMetaData metaData = client.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"))), DruidAvaticaHandlerTest.getRows(metaData.getCatalogs()));
    }

    @Test
    public void testDatabaseMetaDataSchemas() throws Exception {
        final DatabaseMetaData metaData = client.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_CATALOG", "druid"), Pair.of("TABLE_SCHEM", "druid"))), DruidAvaticaHandlerTest.getRows(metaData.getSchemas(null, "druid")));
    }

    @Test
    public void testDatabaseMetaDataTables() throws Exception {
        final DatabaseMetaData metaData = client.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE1), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE2), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE3), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE"))), DruidAvaticaHandlerTest.getRows(metaData.getTables(null, "druid", "%", null), ImmutableSet.of("TABLE_CAT", "TABLE_NAME", "TABLE_SCHEM", "TABLE_TYPE")));
    }

    @Test
    public void testDatabaseMetaDataTablesAsSuperuser() throws Exception {
        final DatabaseMetaData metaData = superuserClient.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE1), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE2), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_CAT", "druid"), Pair.of("TABLE_NAME", CalciteTests.DATASOURCE3), Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_TYPE", "TABLE"))), DruidAvaticaHandlerTest.getRows(metaData.getTables(null, "druid", "%", null), ImmutableSet.of("TABLE_CAT", "TABLE_NAME", "TABLE_SCHEM", "TABLE_TYPE")));
    }

    @Test
    public void testDatabaseMetaDataColumns() throws Exception {
        final DatabaseMetaData metaData = client.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "__time"), Pair.of("DATA_TYPE", Types.TIMESTAMP), Pair.of("TYPE_NAME", "TIMESTAMP"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "cnt"), Pair.of("DATA_TYPE", Types.BIGINT), Pair.of("TYPE_NAME", "BIGINT"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "dim1"), Pair.of("DATA_TYPE", Types.VARCHAR), Pair.of("TYPE_NAME", "VARCHAR"), Pair.of("IS_NULLABLE", "YES")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "dim2"), Pair.of("DATA_TYPE", Types.VARCHAR), Pair.of("TYPE_NAME", "VARCHAR"), Pair.of("IS_NULLABLE", "YES")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "dim3"), Pair.of("DATA_TYPE", Types.VARCHAR), Pair.of("TYPE_NAME", "VARCHAR"), Pair.of("IS_NULLABLE", "YES")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "m1"), Pair.of("DATA_TYPE", Types.FLOAT), Pair.of("TYPE_NAME", "FLOAT"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "m2"), Pair.of("DATA_TYPE", Types.DOUBLE), Pair.of("TYPE_NAME", "DOUBLE"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", "foo"), Pair.of("COLUMN_NAME", "unique_dim1"), Pair.of("DATA_TYPE", Types.OTHER), Pair.of("TYPE_NAME", "OTHER"), Pair.of("IS_NULLABLE", "YES"))), DruidAvaticaHandlerTest.getRows(metaData.getColumns(null, "dr_id", "foo", null), ImmutableSet.of("IS_NULLABLE", "TABLE_NAME", "TABLE_SCHEM", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME")));
    }

    @Test
    public void testDatabaseMetaDataColumnsOnForbiddenDatasource() throws Exception {
        final DatabaseMetaData metaData = client.getMetaData();
        Assert.assertEquals(ImmutableList.of(), DruidAvaticaHandlerTest.getRows(metaData.getColumns(null, "dr_id", CalciteTests.FORBIDDEN_DATASOURCE, null), ImmutableSet.of("IS_NULLABLE", "TABLE_NAME", "TABLE_SCHEM", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME")));
    }

    @Test
    public void testDatabaseMetaDataColumnsWithSuperuser() throws Exception {
        final DatabaseMetaData metaData = superuserClient.getMetaData();
        Assert.assertEquals(ImmutableList.of(DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "__time"), Pair.of("DATA_TYPE", Types.TIMESTAMP), Pair.of("TYPE_NAME", "TIMESTAMP"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "cnt"), Pair.of("DATA_TYPE", Types.BIGINT), Pair.of("TYPE_NAME", "BIGINT"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "dim1"), Pair.of("DATA_TYPE", Types.VARCHAR), Pair.of("TYPE_NAME", "VARCHAR"), Pair.of("IS_NULLABLE", "YES")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "dim2"), Pair.of("DATA_TYPE", Types.VARCHAR), Pair.of("TYPE_NAME", "VARCHAR"), Pair.of("IS_NULLABLE", "YES")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "m1"), Pair.of("DATA_TYPE", Types.FLOAT), Pair.of("TYPE_NAME", "FLOAT"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "m2"), Pair.of("DATA_TYPE", Types.DOUBLE), Pair.of("TYPE_NAME", "DOUBLE"), Pair.of("IS_NULLABLE", "NO")), DruidAvaticaHandlerTest.row(Pair.of("TABLE_SCHEM", "druid"), Pair.of("TABLE_NAME", CalciteTests.FORBIDDEN_DATASOURCE), Pair.of("COLUMN_NAME", "unique_dim1"), Pair.of("DATA_TYPE", Types.OTHER), Pair.of("TYPE_NAME", "OTHER"), Pair.of("IS_NULLABLE", "YES"))), DruidAvaticaHandlerTest.getRows(metaData.getColumns(null, "dr_id", CalciteTests.FORBIDDEN_DATASOURCE, null), ImmutableSet.of("IS_NULLABLE", "TABLE_NAME", "TABLE_SCHEM", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME")));
    }

    @Test(timeout = 90000L)
    public void testConcurrentQueries() throws Exception {
        final List<ListenableFuture<Integer>> futures = new ArrayList<>();
        final ListeningExecutorService exec = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(DruidAvaticaHandlerTest.AVATICA_CONFIG.getMaxStatementsPerConnection()));
        for (int i = 0; i < 2000; i++) {
            final String query = StringUtils.format("SELECT COUNT(*) + %s AS ci FROM foo", i);
            futures.add(exec.submit(() -> {
                try (final Statement statement = client.createStatement();final ResultSet resultSet = statement.executeQuery(query)) {
                    final List<Map<String, Object>> rows = DruidAvaticaHandlerTest.getRows(resultSet);
                    return ((Number) (Iterables.getOnlyElement(rows).get("ci"))).intValue();
                } catch (SQLException e) {
                    throw Throwables.propagate(e);
                }
            }));
        }
        final List<Integer> integers = Futures.allAsList(futures).get();
        for (int i = 0; i < 2000; i++) {
            Assert.assertEquals((i + 6), ((int) (integers.get(i))));
        }
    }

    @Test
    public void testTooManyStatements() throws Exception {
        final Statement statement1 = client.createStatement();
        final Statement statement2 = client.createStatement();
        final Statement statement3 = client.createStatement();
        final Statement statement4 = client.createStatement();
        expectedException.expect(AvaticaClientRuntimeException.class);
        expectedException.expectMessage("Too many open statements, limit is[4]");
        final Statement statement5 = client.createStatement();
    }

    @Test
    public void testNotTooManyStatementsWhenYouCloseThem() throws Exception {
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        client.createStatement().close();
        Assert.assertTrue(true);
    }

    @Test
    public void testNotTooManyStatementsWhenYouFullyIterateThem() throws Exception {
        for (int i = 0; i < 50; i++) {
            final ResultSet resultSet = client.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM druid.foo");
            Assert.assertEquals(ImmutableList.of(ImmutableMap.of("cnt", 6L)), DruidAvaticaHandlerTest.getRows(resultSet));
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testNotTooManyStatementsWhenTheyThrowErrors() throws Exception {
        for (int i = 0; i < 50; i++) {
            Exception thrown = null;
            try {
                client.createStatement().executeQuery("SELECT SUM(nonexistent) FROM druid.foo");
            } catch (Exception e) {
                thrown = e;
            }
            Assert.assertNotNull(thrown);
            final ResultSet resultSet = client.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM druid.foo");
            Assert.assertEquals(ImmutableList.of(ImmutableMap.of("cnt", 6L)), DruidAvaticaHandlerTest.getRows(resultSet));
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testAutoReconnectOnNoSuchConnection() throws Exception {
        for (int i = 0; i < 50; i++) {
            final ResultSet resultSet = client.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM druid.foo");
            Assert.assertEquals(ImmutableList.of(ImmutableMap.of("cnt", 6L)), DruidAvaticaHandlerTest.getRows(resultSet));
            druidMeta.closeAllConnections();
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testTooManyConnections() throws Exception {
        final Connection connection1 = DriverManager.getConnection(url);
        final Statement statement1 = connection1.createStatement();
        final Connection connection2 = DriverManager.getConnection(url);
        final Statement statement2 = connection2.createStatement();
        final Connection connection3 = DriverManager.getConnection(url);
        final Statement statement3 = connection3.createStatement();
        expectedException.expect(AvaticaClientRuntimeException.class);
        expectedException.expectMessage("Too many connections, limit is[3]");
        final Connection connection4 = DriverManager.getConnection(url);
    }

    @Test
    public void testNotTooManyConnectionsWhenTheyAreEmpty() throws Exception {
        final Connection connection1 = DriverManager.getConnection(url);
        connection1.createStatement().close();
        final Connection connection2 = DriverManager.getConnection(url);
        connection2.createStatement().close();
        final Connection connection3 = DriverManager.getConnection(url);
        connection3.createStatement().close();
        final Connection connection4 = DriverManager.getConnection(url);
        Assert.assertTrue(true);
    }

    @Test
    public void testMaxRowsPerFrame() throws Exception {
        final AvaticaServerConfig smallFrameConfig = new AvaticaServerConfig() {
            @Override
            public int getMaxConnections() {
                return 2;
            }

            @Override
            public int getMaxStatementsPerConnection() {
                return 4;
            }

            @Override
            public int getMaxRowsPerFrame() {
                return 2;
            }
        };
        final PlannerConfig plannerConfig = new PlannerConfig();
        final DruidSchema druidSchema = CalciteTests.createMockSchema(DruidAvaticaHandlerTest.conglomerate, walker, plannerConfig);
        final SystemSchema systemSchema = CalciteTests.createMockSystemSchema(druidSchema, walker, plannerConfig);
        final DruidOperatorTable operatorTable = CalciteTests.createOperatorTable();
        final ExprMacroTable macroTable = CalciteTests.createExprMacroTable();
        final List<Meta.Frame> frames = new ArrayList<>();
        DruidMeta smallFrameDruidMeta = new DruidMeta(CalciteTests.createSqlLifecycleFactory(new org.apache.druid.sql.calcite.planner.PlannerFactory(druidSchema, systemSchema, CalciteTests.createMockQueryLifecycleFactory(walker, DruidAvaticaHandlerTest.conglomerate), operatorTable, macroTable, plannerConfig, AuthTestUtils.TEST_AUTHORIZER_MAPPER, CalciteTests.getJsonMapper())), smallFrameConfig, injector) {
            @Override
            public Frame fetch(final StatementHandle statement, final long offset, final int fetchMaxRowCount) throws MissingResultsException, NoSuchStatementException {
                // overriding fetch allows us to track how many frames are processed after the first frame
                Frame frame = super.fetch(statement, offset, fetchMaxRowCount);
                frames.add(frame);
                return frame;
            }
        };
        final DruidAvaticaHandler handler = new DruidAvaticaHandler(smallFrameDruidMeta, new DruidNode("dummy", "dummy", false, 1, null, true, false), new AvaticaMonitor());
        final int port = (ThreadLocalRandom.current().nextInt(9999)) + 20000;
        Server smallFrameServer = new Server(new InetSocketAddress("127.0.0.1", port));
        smallFrameServer.setHandler(handler);
        smallFrameServer.start();
        String smallFrameUrl = StringUtils.format("jdbc:avatica:remote:url=http://127.0.0.1:%d%s", port, AVATICA_PATH);
        Connection smallFrameClient = DriverManager.getConnection(smallFrameUrl, "regularUser", "druid");
        final ResultSet resultSet = smallFrameClient.createStatement().executeQuery("SELECT dim1 FROM druid.foo");
        List<Map<String, Object>> rows = DruidAvaticaHandlerTest.getRows(resultSet);
        Assert.assertEquals(2, frames.size());
        Assert.assertEquals(ImmutableList.of(ImmutableMap.of("dim1", ""), ImmutableMap.of("dim1", "10.1"), ImmutableMap.of("dim1", "2"), ImmutableMap.of("dim1", "1"), ImmutableMap.of("dim1", "def"), ImmutableMap.of("dim1", "abc")), rows);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSqlRequestLog() throws Exception {
        // valid sql
        for (int i = 0; i < 3; i++) {
            client.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM druid.foo");
        }
        Assert.assertEquals(3, testRequestLogger.getSqlQueryLogs().size());
        for (RequestLogLine logLine : testRequestLogger.getSqlQueryLogs()) {
            final Map<String, Object> stats = logLine.getQueryStats().getStats();
            Assert.assertEquals(true, stats.get("success"));
            Assert.assertEquals("regularUser", stats.get("identity"));
            Assert.assertTrue(stats.containsKey("sqlQuery/time"));
            Assert.assertTrue(stats.containsKey("sqlQuery/bytes"));
        }
        // invalid sql
        testRequestLogger.clear();
        try {
            client.createStatement().executeQuery("SELECT notexist FROM druid.foo");
            Assert.fail("invalid sql should throw SQLException");
        } catch (SQLException e) {
        }
        Assert.assertEquals(1, testRequestLogger.getSqlQueryLogs().size());
        final Map<String, Object> stats = testRequestLogger.getSqlQueryLogs().get(0).getQueryStats().getStats();
        Assert.assertEquals(false, stats.get("success"));
        Assert.assertEquals("regularUser", stats.get("identity"));
        Assert.assertTrue(stats.containsKey("exception"));
        // unauthorized sql
        testRequestLogger.clear();
        try {
            client.createStatement().executeQuery("SELECT count(*) FROM druid.forbiddenDatasource");
            Assert.fail("unauthorzed sql should throw SQLException");
        } catch (SQLException e) {
        }
        Assert.assertEquals(0, testRequestLogger.getSqlQueryLogs().size());
    }
}
