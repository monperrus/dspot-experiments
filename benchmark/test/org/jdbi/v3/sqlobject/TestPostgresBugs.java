/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject;


import java.io.IOException;
import org.jdbi.v3.core.Something;
import org.jdbi.v3.core.mapper.SomethingMapper;
import org.jdbi.v3.core.rule.PgDatabaseRule;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.jdbi.v3.sqlobject.transaction.Transactional;
import org.junit.Rule;
import org.junit.Test;


public class TestPostgresBugs {
    @Rule
    public PgDatabaseRule dbRule = new PgDatabaseRule().withPlugin(new SqlObjectPlugin());

    @Test
    public void testConnected() {
        int four = dbRule.getJdbi().withHandle(( handle) -> handle.createQuery("select 2 + 2").mapTo(.class).findOnly());
        assertThat(four).isEqualTo(4);
    }

    @Test
    public void testTransactions() {
        TestPostgresBugs.Dao dao = dbRule.getJdbi().onDemand(TestPostgresBugs.Dao.class);
        Something s = dao.insertAndFetch(1, "Brian");
        assertThat(s).isEqualTo(new Something(1, "Brian"));
    }

    @Test
    public void testExplicitTransaction() {
        TestPostgresBugs.Dao dao = dbRule.getJdbi().onDemand(TestPostgresBugs.Dao.class);
        Something s = inTransaction(( transactional) -> {
            transactional.insert(1, "Brian");
            return transactional.findById(1);
        });
        assertThat(s).isEqualTo(new Something(1, "Brian"));
    }

    @RegisterRowMapper(SomethingMapper.class)
    public interface Dao extends Transactional<TestPostgresBugs.Dao> {
        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@Bind("id")
        int id, @Bind("name")
        String name);

        @SqlQuery("select id, name from something where id = :id")
        Something findById(@Bind("id")
        int id);

        @Transaction(TransactionIsolationLevel.READ_COMMITTED)
        default Something insertAndFetch(int id, String name) {
            insert(id, name);
            return findById(id);
        }

        @Transaction
        default Something failed(int id, String name) throws IOException {
            insert(id, name);
            throw new IOException("woof");
        }
    }
}
