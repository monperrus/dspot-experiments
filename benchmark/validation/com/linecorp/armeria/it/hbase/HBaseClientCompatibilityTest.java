/**
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.it.hbase;


import com.google.common.base.Stopwatch;
import com.linecorp.armeria.common.util.Version;
import com.linecorp.armeria.server.Server;
import java.lang.reflect.Modifier;
import org.apache.hadoop.hbase.NotAllMetaRegionsOnlineException;
import org.apache.hadoop.hbase.shaded.org.apache.zookeeper.data.Stat;
import org.apache.hadoop.hbase.zookeeper.MetaTableLocator;
import org.apache.hadoop.hbase.zookeeper.RecoverableZooKeeper;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class HBaseClientCompatibilityTest {
    /**
     * Ensure Armeria's dependencies do not cause a trouble with hbase-shaded-client.
     *
     * @see <a href="https://issues.apache.org/jira/browse/HBASE-14963">HBASE-14963</a>
     */
    @Test(expected = NotAllMetaRegionsOnlineException.class)
    public void testGuavaConflict() throws Exception {
        // Make sure Armeria is available in the class path.
        assertThat(Version.identify(Server.class.getClassLoader())).isNotNull();
        // Make sure newer Guava is available in the class path.
        assertThat(Stopwatch.class.getDeclaredConstructor().getModifiers()).is(new org.assertj.core.api.Condition(( value) -> !(Modifier.isPublic(value)), "Recent Guava Stopwatch should have non-public default constructor."));
        final MetaTableLocator locator = new MetaTableLocator();
        final ZooKeeperWatcher zkw = Mockito.mock(ZooKeeperWatcher.class);
        final RecoverableZooKeeper zk = Mockito.mock(RecoverableZooKeeper.class);
        Mockito.when(zkw.getRecoverableZooKeeper()).thenReturn(zk);
        Mockito.when(zk.exists(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new Stat(0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0));
        locator.waitMetaRegionLocation(zkw, 100);
    }
}
