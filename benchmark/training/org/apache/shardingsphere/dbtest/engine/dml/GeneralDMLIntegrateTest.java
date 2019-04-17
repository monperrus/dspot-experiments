/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.shardingsphere.dbtest.engine.dml;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import javax.xml.bind.JAXBException;
import org.apache.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.junit.Test;


public final class GeneralDMLIntegrateTest extends BaseDMLIntegrateTest {
    private final DMLIntegrateTestCaseAssertion assertion;

    public GeneralDMLIntegrateTest(final String sqlCaseId, final String path, final DMLIntegrateTestCaseAssertion assertion, final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, SQLException, ParseException, JAXBException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
    }

    @Test
    public void assertExecuteUpdate() throws IOException, SQLException, ParseException, JAXBException {
        // TODO fix masterslave
        if ((!(getDatabaseTypeEnvironment().isEnabled())) || ("masterslave".equals(getShardingRuleType()))) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getDataSource().getConnection()) {
            actualUpdateCount = ((SQLCaseType.Literal) == (getCaseType())) ? executeUpdateForStatement(connection) : executeUpdateForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }

    @Test
    public void assertExecute() throws IOException, SQLException, ParseException, JAXBException {
        // TODO fix masterslave
        if ((!(getDatabaseTypeEnvironment().isEnabled())) || ("masterslave".equals(getShardingRuleType()))) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getDataSource().getConnection()) {
            actualUpdateCount = ((SQLCaseType.Literal) == (getCaseType())) ? executeForStatement(connection) : executeForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
}
