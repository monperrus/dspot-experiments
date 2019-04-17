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
package org.apache.ignite.internal.processors.cache.distributed.near;


import Cache.Entry;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.managers.communication.GridIoMessage;
import org.apache.ignite.internal.processors.cache.IgniteCacheAbstractQuerySelfTest;
import org.apache.ignite.internal.processors.cache.query.GridCacheQueryRequest;
import org.apache.ignite.internal.processors.cache.query.GridCacheQueryResponse;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.spi.communication.CommunicationSpi;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.junit.Test;


/**
 * Tests for partitioned cache queries.
 */
public class IgniteCachePartitionedQuerySelfTest extends IgniteCacheAbstractQuerySelfTest {
    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testFieldsQuery() throws Exception {
        IgniteCacheAbstractQuerySelfTest.Person p1 = new IgniteCacheAbstractQuerySelfTest.Person("Jon", 1500);
        IgniteCacheAbstractQuerySelfTest.Person p2 = new IgniteCacheAbstractQuerySelfTest.Person("Jane", 2000);
        IgniteCacheAbstractQuerySelfTest.Person p3 = new IgniteCacheAbstractQuerySelfTest.Person("Mike", 1800);
        IgniteCacheAbstractQuerySelfTest.Person p4 = new IgniteCacheAbstractQuerySelfTest.Person("Bob", 1900);
        IgniteCache<UUID, IgniteCacheAbstractQuerySelfTest.Person> cache0 = jcache(UUID.class, IgniteCacheAbstractQuerySelfTest.Person.class);
        cache0.put(p1.id(), p1);
        cache0.put(p2.id(), p2);
        cache0.put(p3.id(), p3);
        cache0.put(p4.id(), p4);
        assertEquals(4, cache0.localSize(CachePeekMode.ALL));
        // Fields query
        QueryCursor<List<?>> qry = cache0.query(new SqlFieldsQuery("select name from Person where salary > ?").setArgs(1600));
        Collection<List<?>> res = qry.getAll();
        assertEquals(3, res.size());
        // Fields query count(*)
        qry = cache0.query(new SqlFieldsQuery("select count(*) from Person"));
        res = qry.getAll();
        int cnt = 0;
        for (List<?> row : res)
            cnt += ((Long) (row.get(0)));

        assertEquals(4, cnt);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMultipleNodesQuery() throws Exception {
        IgniteCacheAbstractQuerySelfTest.Person p1 = new IgniteCacheAbstractQuerySelfTest.Person("Jon", 1500);
        IgniteCacheAbstractQuerySelfTest.Person p2 = new IgniteCacheAbstractQuerySelfTest.Person("Jane", 2000);
        IgniteCacheAbstractQuerySelfTest.Person p3 = new IgniteCacheAbstractQuerySelfTest.Person("Mike", 1800);
        IgniteCacheAbstractQuerySelfTest.Person p4 = new IgniteCacheAbstractQuerySelfTest.Person("Bob", 1900);
        IgniteCache<UUID, IgniteCacheAbstractQuerySelfTest.Person> cache0 = jcache(UUID.class, IgniteCacheAbstractQuerySelfTest.Person.class);
        cache0.put(p1.id(), p1);
        cache0.put(p2.id(), p2);
        cache0.put(p3.id(), p3);
        cache0.put(p4.id(), p4);
        assertEquals(4, cache0.localSize(CachePeekMode.ALL));
        assert (grid(0).cluster().nodes().size()) == (gridCount());
        QueryCursor<Entry<UUID, IgniteCacheAbstractQuerySelfTest.Person>> qry = cache0.query(new org.apache.ignite.cache.query.SqlQuery<UUID, IgniteCacheAbstractQuerySelfTest.Person>(IgniteCacheAbstractQuerySelfTest.Person.class, "salary < 2000"));
        // Execute on full projection, duplicates are expected.
        Collection<Entry<UUID, IgniteCacheAbstractQuerySelfTest.Person>> entries = qry.getAll();
        assert entries != null;
        info(("Queried entries: " + entries));
        // Expect result including backup persons.
        assertEquals(gridCount(), entries.size());
        checkResult(entries, p1, p3, p4);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testScanQueryPagination() throws Exception {
        final int pageSize = 5;
        final AtomicInteger pages = new AtomicInteger(0);
        IgniteCache<Integer, Integer> cache = jcache(Integer.class, Integer.class);
        for (int i = 0; i < 50; i++)
            cache.put(i, i);

        CommunicationSpi spi = ignite().configuration().getCommunicationSpi();
        assert spi instanceof IgniteCachePartitionedQuerySelfTest.TestTcpCommunicationSpi;
        IgniteCachePartitionedQuerySelfTest.TestTcpCommunicationSpi commSpi = ((IgniteCachePartitionedQuerySelfTest.TestTcpCommunicationSpi) (spi));
        commSpi.filter = new org.apache.ignite.lang.IgniteInClosure<Message>() {
            @Override
            public void apply(Message msg) {
                if (!(msg instanceof GridIoMessage))
                    return;

                Message msg0 = message();
                if (msg0 instanceof GridCacheQueryRequest) {
                    assertEquals(pageSize, pageSize());
                    pages.incrementAndGet();
                } else
                    if (msg0 instanceof GridCacheQueryResponse)
                        assertTrue(((data().size()) <= pageSize));


            }
        };
        try {
            ScanQuery<Integer, Integer> qry = new ScanQuery<Integer, Integer>();
            qry.setPageSize(pageSize);
            List<Entry<Integer, Integer>> all = cache.query(qry).getAll();
            assertTrue(((pages.get()) > (ignite().cluster().forDataNodes(DEFAULT_CACHE_NAME).nodes().size())));
            assertEquals(50, all.size());
        } finally {
            commSpi.filter = null;
        }
    }

    /**
     *
     */
    private static class TestTcpCommunicationSpi extends TcpCommunicationSpi {
        /**
         *
         */
        volatile org.apache.ignite.lang.IgniteInClosure<Message> filter;

        /**
         * {@inheritDoc }
         */
        @Override
        public void sendMessage(ClusterNode node, Message msg, org.apache.ignite.lang.IgniteInClosure<IgniteException> ackC) {
            if ((filter) != null)
                filter.apply(msg);

            super.sendMessage(node, msg, ackC);
        }
    }
}
