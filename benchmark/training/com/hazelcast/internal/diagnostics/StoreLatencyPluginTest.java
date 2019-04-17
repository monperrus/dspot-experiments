/**
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
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
package com.hazelcast.internal.diagnostics;


import com.hazelcast.internal.diagnostics.StoreLatencyPlugin.LatencyProbe;
import com.hazelcast.internal.diagnostics.StoreLatencyPlugin.LatencyProbeImpl;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class StoreLatencyPluginTest extends AbstractDiagnosticsPluginTest {
    private StoreLatencyPlugin plugin;

    @Test
    public void getProbe() {
        LatencyProbe probe = plugin.newProbe("foo", "queue", "somemethod");
        Assert.assertNotNull(probe);
    }

    @Test
    public void getProbe_whenSameProbeRequestedMoreThanOnce() {
        LatencyProbe probe1 = plugin.newProbe("foo", "queue", "somemethod");
        LatencyProbe probe2 = plugin.newProbe("foo", "queue", "somemethod");
        Assert.assertSame(probe1, probe2);
    }

    @Test
    public void testMaxMicros() {
        LatencyProbeImpl probe = ((LatencyProbeImpl) (plugin.newProbe("foo", "queue", "somemethod")));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(10));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(1000));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(4));
        Assert.assertEquals(1000, probe.stats.maxMicros);
    }

    @Test
    public void testCount() {
        LatencyProbeImpl probe = ((LatencyProbeImpl) (plugin.newProbe("foo", "queue", "somemethod")));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(10));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(10));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(10));
        Assert.assertEquals(3, probe.stats.count);
    }

    @Test
    public void testTotalMicros() {
        LatencyProbeImpl probe = ((LatencyProbeImpl) (plugin.newProbe("foo", "queue", "somemethod")));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(10));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(20));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(30));
        Assert.assertEquals(60, probe.stats.totalMicros);
    }

    @Test
    public void render() {
        LatencyProbeImpl probe = ((LatencyProbeImpl) (plugin.newProbe("foo", "queue", "somemethod")));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(100));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(200));
        probe.recordValue(TimeUnit.MICROSECONDS.toNanos(300));
        plugin.run(logWriter);
        assertContains("foo");
        assertContains("queue");
        assertContains("somemethod");
        assertContains("count=3");
        assertContains("totalTime(us)=600");
        assertContains("avg(us)=200");
        assertContains("max(us)=300");
        assertContains("100..199us=1");
        assertContains("200..399us=2");
    }
}
