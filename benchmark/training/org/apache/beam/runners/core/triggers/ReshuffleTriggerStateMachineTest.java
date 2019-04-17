/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.core.triggers;


import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link ReshuffleTriggerStateMachine}.
 */
@RunWith(JUnit4.class)
public class ReshuffleTriggerStateMachineTest {
    @Test
    public void testShouldFire() throws Exception {
        TriggerStateMachineTester<Integer, IntervalWindow> tester = TriggerStateMachineTester.forTrigger(ReshuffleTriggerStateMachine.create(), FixedWindows.of(Duration.millis(100)));
        IntervalWindow arbitraryWindow = new IntervalWindow(new Instant(300), new Instant(400));
        Assert.assertTrue(tester.shouldFire(arbitraryWindow));
    }

    @Test
    public void testOnTimer() throws Exception {
        TriggerStateMachineTester<Integer, IntervalWindow> tester = TriggerStateMachineTester.forTrigger(ReshuffleTriggerStateMachine.create(), FixedWindows.of(Duration.millis(100)));
        IntervalWindow arbitraryWindow = new IntervalWindow(new Instant(100), new Instant(200));
        tester.fireIfShouldFire(arbitraryWindow);
        Assert.assertFalse(tester.isMarkedFinished(arbitraryWindow));
    }

    @Test
    public void testToString() {
        TriggerStateMachine trigger = ReshuffleTriggerStateMachine.create();
        Assert.assertEquals("ReshuffleTriggerStateMachine()", trigger.toString());
    }
}
