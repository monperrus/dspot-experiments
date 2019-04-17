/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.test;


import CalciteConnectionProperty.CONFORMANCE;
import Quidem.SqlCommand;
import SqlBabelParserImpl.FACTORY;
import SqlConformanceEnum.BABEL;
import SqlParser.ConfigBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.hydromatic.quidem.AbstractCommand;
import net.hydromatic.quidem.Command;
import net.hydromatic.quidem.CommandHandler;
import net.hydromatic.quidem.Quidem;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.materialize.MaterializationService;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * Unit tests for the Babel SQL parser.
 */
@RunWith(Parameterized.class)
@Ignore
public class BabelQuidemTest extends QuidemTest {
    /**
     * Creates a BabelQuidemTest. Public per {@link Parameterized}.
     */
    @SuppressWarnings("WeakerAccess")
    public BabelQuidemTest(String path) {
        super(path);
    }

    @Override
    @Test
    public void test() throws Exception {
        MaterializationService.setThreadLocal();
        super.test();
    }

    /**
     * Command that prints the validated parse tree of a SQL statement.
     */
    static class ExplainValidatedCommand extends AbstractCommand {
        private final ImmutableList<String> lines;

        private final ImmutableList<String> content;

        private final Set<String> productSet;

        ExplainValidatedCommand(List<String> lines, List<String> content, Set<String> productSet) {
            this.lines = ImmutableList.copyOf(lines);
            this.content = ImmutableList.copyOf(content);
            this.productSet = ImmutableSet.copyOf(productSet);
        }

        @Override
        public void execute(Context x, boolean execute) throws Exception {
            if (execute) {
                // use Babel parser
                final SqlParser.ConfigBuilder parserConfig = SqlParser.configBuilder().setParserFactory(FACTORY);
                // use Babel conformance for validation
                final Properties properties = new Properties();
                properties.setProperty(CONFORMANCE.name(), BABEL.name());
                final CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(properties);
                // extract named schema from connection and use it in planner
                final CalciteConnection calciteConnection = x.connection().unwrap(CalciteConnection.class);
                final String schemaName = calciteConnection.getSchema();
                final SchemaPlus schema = (schemaName != null) ? calciteConnection.getRootSchema().getSubSchema(schemaName) : calciteConnection.getRootSchema();
                final Frameworks.ConfigBuilder config = Frameworks.newConfigBuilder().defaultSchema(schema).parserConfig(parserConfig.build()).context(Contexts.of(connectionConfig));
                // parse, validate and un-parse
                final Quidem.SqlCommand sqlCommand = x.previousSqlCommand();
                final Planner planner = Frameworks.getPlanner(config.build());
                final SqlNode node = planner.parse(sqlCommand.sql);
                final SqlNode validateNode = planner.validate(node);
                final SqlWriter sqlWriter = new org.apache.calcite.sql.pretty.SqlPrettyWriter(CalciteSqlDialect.DEFAULT);
                validateNode.unparse(sqlWriter, 0, 0);
                x.echo(ImmutableList.of(sqlWriter.toSqlString().getSql()));
            } else {
                x.echo(content);
            }
            x.echo(lines);
        }
    }

    /**
     * Command handler that adds a "!explain-validated-on dialect..." command
     * (see {@link ExplainValidatedCommand}).
     */
    private static class BabelCommandHandler implements CommandHandler {
        @Override
        public Command parseCommand(List<String> lines, List<String> content, String line) {
            final String prefix = "explain-validated-on";
            if (line.startsWith(prefix)) {
                final Pattern pattern = Pattern.compile("explain-validated-on( [-_+a-zA-Z0-9]+)*?");
                final Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    final ImmutableSet.Builder<String> set = ImmutableSet.builder();
                    for (int i = 0; i < (matcher.groupCount()); i++) {
                        set.add(matcher.group((i + 1)));
                    }
                    return new BabelQuidemTest.ExplainValidatedCommand(lines, content, set.build());
                }
            }
            return null;
        }
    }
}

/**
 * End BabelQuidemTest.java
 */