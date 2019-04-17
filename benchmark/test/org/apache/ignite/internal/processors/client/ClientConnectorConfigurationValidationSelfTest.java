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
package org.apache.ignite.internal.processors.client;


import ClientConnectorConfiguration.DFLT_PORT;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.OdbcConfiguration;
import org.apache.ignite.configuration.SqlConnectorConfiguration;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Client connector configuration validation tests.
 */
@SuppressWarnings("deprecation")
public class ClientConnectorConfigurationValidationSelfTest extends GridCommonAbstractTest {
    /**
     * Node index generator.
     */
    private static final AtomicInteger NODE_IDX_GEN = new AtomicInteger();

    /**
     * Cache name.
     */
    private static final String CACHE_NAME = "CACHE";

    /**
     * Test host.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDefault() throws Exception {
        check(new ClientConnectorConfiguration(), true);
        checkJdbc(null, DFLT_PORT);
    }

    /**
     * Test host.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testHost() throws Exception {
        check(new ClientConnectorConfiguration().setHost("126.0.0.1"), false);
        check(new ClientConnectorConfiguration().setHost("127.0.0.1"), true);
        checkJdbc("127.0.0.1", DFLT_PORT);
        check(new ClientConnectorConfiguration().setHost("0.0.0.0"), true);
        checkJdbc("0.0.0.0", ((ClientConnectorConfiguration.DFLT_PORT) + 1));
    }

    /**
     * Test port.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPort() throws Exception {
        check(new ClientConnectorConfiguration().setPort((-1)), false);
        check(new ClientConnectorConfiguration().setPort(0), false);
        check(new ClientConnectorConfiguration().setPort(512), false);
        check(new ClientConnectorConfiguration().setPort(65536), false);
        check(new ClientConnectorConfiguration().setPort(DFLT_PORT), true);
        checkJdbc(null, DFLT_PORT);
        check(new ClientConnectorConfiguration().setPort(((ClientConnectorConfiguration.DFLT_PORT) + 200)), true);
        checkJdbc(null, ((ClientConnectorConfiguration.DFLT_PORT) + 200));
    }

    /**
     * Test port.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPortRange() throws Exception {
        check(new ClientConnectorConfiguration().setPortRange((-1)), false);
        check(new ClientConnectorConfiguration().setPortRange(0), true);
        checkJdbc(null, DFLT_PORT);
        check(new ClientConnectorConfiguration().setPortRange(10), true);
        checkJdbc(null, ((ClientConnectorConfiguration.DFLT_PORT) + 1));
    }

    /**
     * Test socket buffers.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSocketBuffers() throws Exception {
        check(new ClientConnectorConfiguration().setSocketSendBufferSize(((-4) * 1024)), false);
        check(new ClientConnectorConfiguration().setSocketReceiveBufferSize(((-4) * 1024)), false);
        check(new ClientConnectorConfiguration().setSocketSendBufferSize((4 * 1024)), true);
        checkJdbc(null, DFLT_PORT);
        check(new ClientConnectorConfiguration().setSocketReceiveBufferSize((4 * 1024)), true);
        checkJdbc(null, ((ClientConnectorConfiguration.DFLT_PORT) + 1));
    }

    /**
     * Test max open cursors per connection.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMaxOpenCusrorsPerConnection() throws Exception {
        check(new ClientConnectorConfiguration().setMaxOpenCursorsPerConnection((-1)), false);
        check(new ClientConnectorConfiguration().setMaxOpenCursorsPerConnection(0), true);
        checkJdbc(null, DFLT_PORT);
        check(new ClientConnectorConfiguration().setMaxOpenCursorsPerConnection(100), true);
        checkJdbc(null, ((ClientConnectorConfiguration.DFLT_PORT) + 1));
    }

    /**
     * Test thread pool size.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testThreadPoolSize() throws Exception {
        check(new ClientConnectorConfiguration().setThreadPoolSize(0), false);
        check(new ClientConnectorConfiguration().setThreadPoolSize((-1)), false);
        check(new ClientConnectorConfiguration().setThreadPoolSize(4), true);
        checkJdbc(null, DFLT_PORT);
    }

    /**
     * Test ODBC connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testOdbcConnectorConversion() throws Exception {
        int port = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setOdbcConfiguration(new OdbcConfiguration().setEndpointAddress(("127.0.0.1:" + port)));
        Ignition.start(cfg);
        checkJdbc(null, port);
    }

    /**
     * Test SQL connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSqlConnectorConversion() throws Exception {
        int port = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setSqlConnectorConfiguration(new SqlConnectorConfiguration().setPort(port));
        Ignition.start(cfg);
        checkJdbc(null, port);
    }

    /**
     * Test SQL connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIgnoreOdbcWhenSqlSet() throws Exception {
        int port = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setSqlConnectorConfiguration(new SqlConnectorConfiguration().setPort(port));
        cfg.setOdbcConfiguration(new OdbcConfiguration().setEndpointAddress(("127.0.0.1:" + (port - 1))));
        Ignition.start(cfg);
        checkJdbc(null, port);
    }

    /**
     * Test SQL connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIgnoreOdbcAndSqlWhenClientSet() throws Exception {
        int cliPort = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        int sqlPort = (ClientConnectorConfiguration.DFLT_PORT) - 2;
        int odbcPort = (ClientConnectorConfiguration.DFLT_PORT) - 3;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setPort(cliPort));
        cfg.setSqlConnectorConfiguration(new SqlConnectorConfiguration().setPort(sqlPort));
        cfg.setOdbcConfiguration(new OdbcConfiguration().setEndpointAddress(("127.0.0.1:" + odbcPort)));
        Ignition.start(cfg);
        checkJdbc(null, cliPort);
    }

    /**
     * Test SQL connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIgnoreOdbcWhenClientSet() throws Exception {
        int cliPort = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        int odbcPort = (ClientConnectorConfiguration.DFLT_PORT) - 2;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setPort(cliPort));
        cfg.setOdbcConfiguration(new OdbcConfiguration().setEndpointAddress(("127.0.0.1:" + odbcPort)));
        Ignition.start(cfg);
        checkJdbc(null, cliPort);
    }

    /**
     * Test SQL connector conversion.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIgnoreSqlWhenClientSet() throws Exception {
        int cliPort = (ClientConnectorConfiguration.DFLT_PORT) - 1;
        int sqlPort = (ClientConnectorConfiguration.DFLT_PORT) - 2;
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setPort(cliPort));
        cfg.setSqlConnectorConfiguration(new SqlConnectorConfiguration().setPort(sqlPort));
        Ignition.start(cfg);
        checkJdbc(null, cliPort);
    }

    /**
     * Test disabled client.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDisabled() throws Exception {
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(null);
        Ignition.start(cfg);
        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkJdbc(null, DFLT_PORT);
                return null;
            }
        }, SQLException.class, null);
    }

    /**
     * Checks if JDBC connection enabled and others are disabled, JDBC still works.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testJdbcConnectionEnabled() throws Exception {
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setJdbcEnabled(true).setOdbcEnabled(false).setThinClientEnabled(false));
        Ignition.start(cfg);
        checkJdbc(null, DFLT_PORT);
    }

    /**
     * Checks if JDBC connection disabled and others are enabled, JDBC doesn't work.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testJdbcConnectionDisabled() throws Exception {
        IgniteConfiguration cfg = baseConfiguration();
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setJdbcEnabled(false).setOdbcEnabled(true).setThinClientEnabled(true));
        Ignition.start(cfg);
        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkJdbc(null, DFLT_PORT);
                return null;
            }
        }, SQLException.class, "JDBC connection is not allowed, see ClientConnectorConfiguration.jdbcEnabled");
    }

    /**
     * Checks if JDBC connection disabled for daemon node.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testJdbcConnectionDisabledForDaemon() throws Exception {
        final IgniteConfiguration cfg = baseConfiguration().setDaemon(true);
        cfg.setClientConnectorConfiguration(new ClientConnectorConfiguration().setJdbcEnabled(true).setThinClientEnabled(true));
        Ignition.start(cfg);
        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkJdbc(null, DFLT_PORT);
                return null;
            }
        }, SQLException.class, "Failed to connect");
    }

    /**
     * Key class.
     */
    private static class ClientConnectorKey {
        /**
         *
         */
        @QuerySqlField
        public int key;
    }

    /**
     * Value class.
     */
    private static class ClientConnectorValue {
        /**
         *
         */
        @QuerySqlField
        public int val;
    }
}
