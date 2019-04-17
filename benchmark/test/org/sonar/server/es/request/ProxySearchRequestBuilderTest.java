/**
 * SonarQube
 * Copyright (C) 2009-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.es.request;


import LoggerLevel.TRACE;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.server.es.EsTester;
import org.sonar.server.es.FakeIndexDefinition;
import org.sonar.server.es.IndexType;


public class ProxySearchRequestBuilderTest {
    @Rule
    public EsTester es = EsTester.createCustom(new FakeIndexDefinition());

    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void search() {
        es.client().prepareSearch(FakeIndexDefinition.INDEX).get();
    }

    @Test
    public void to_string() {
        assertThat(es.client().prepareSearch(FakeIndexDefinition.INDEX).setTypes(FakeIndexDefinition.TYPE).toString()).contains("ES search request '").contains("' on indices '[fakes]' on types '[fake]'");
        assertThat(es.client().prepareSearch(FakeIndexDefinition.INDEX).toString()).contains("ES search request '").contains("' on indices '[fakes]'");
        assertThat(es.client().prepareSearch(new IndexType[0]).toString()).contains("ES search request");
    }

    @Test
    public void trace_logs() {
        logTester.setLevel(TRACE);
        es.client().prepareSearch(FakeIndexDefinition.INDEX).get();
        assertThat(logTester.logs(TRACE)).hasSize(1);
    }

    @Test
    public void fail_to_search_bad_query() {
        try {
            es.client().prepareSearch("non-existing-index").get();
            Assert.fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).contains("Fail to execute ES search request 'SearchRequest{").contains("}' on indices '[non-existing-index]'");
        }
    }

    @Test
    public void get_with_string_timeout_is_not_yet_implemented() {
        try {
            es.client().prepareSearch(FakeIndexDefinition.INDEX).get("1");
            Assert.fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class).hasMessage("Not yet implemented");
        }
    }

    @Test
    public void get_with_time_value_timeout_is_not_yet_implemented() {
        try {
            es.client().prepareSearch(FakeIndexDefinition.INDEX).get(TimeValue.timeValueMinutes(1));
            Assert.fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class).hasMessage("Not yet implemented");
        }
    }

    @Test
    public void execute_should_throw_an_unsupported_operation_exception() {
        try {
            es.client().prepareSearch(FakeIndexDefinition.INDEX).execute();
            Assert.fail();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class).hasMessage("execute() should not be called as it's used for asynchronous");
        }
    }
}
