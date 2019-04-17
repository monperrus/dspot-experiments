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
package org.apache.beam.examples.complete.game;


import Duration.ZERO;
import LeaderBoard.Options;
import java.io.Serializable;
import org.apache.beam.examples.complete.game.UserScore.GameActionInfo;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.transforms.windowing.IntervalWindow;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link LeaderBoard}.
 */
@RunWith(JUnit4.class)
public class LeaderBoardTest implements Serializable {
    private static final Duration ALLOWED_LATENESS = Duration.standardHours(1);

    private static final Duration TEAM_WINDOW_DURATION = Duration.standardMinutes(20);

    private Instant baseTime = new Instant(0);

    @Rule
    public TestPipeline p = TestPipeline.create();

    /**
     * Some example users, on two separate teams.
     */
    private enum TestUser {

        RED_ONE("scarlet", "red"),
        RED_TWO("burgundy", "red"),
        BLUE_ONE("navy", "blue"),
        BLUE_TWO("sky", "blue");
        private final String userName;

        private final String teamName;

        TestUser(String userName, String teamName) {
            this.userName = userName;
            this.teamName = teamName;
        }

        public String getUser() {
            return userName;
        }

        public String getTeam() {
            return teamName;
        }
    }

    /**
     * A test of the {@link CalculateTeamScores} {@link PTransform} when all of the elements arrive on
     * time (ahead of the watermark).
     */
    @Test
    public void testTeamScoresOnTime() {
        TestStream<GameActionInfo> createEvents = // The window should close and emit an ON_TIME pane
        // Add some more on time elements
        // The watermark advances slightly, but not past the end of the window
        // add some elements ahead of the watermark
        // Start at the epoch
        TestStream.create(AvroCoder.of(GameActionInfo.class)).advanceWatermarkTo(baseTime).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardSeconds(3)), event(LeaderBoardTest.TestUser.BLUE_ONE, 2, Duration.standardMinutes(1)), event(LeaderBoardTest.TestUser.RED_TWO, 3, Duration.standardSeconds(22)), event(LeaderBoardTest.TestUser.BLUE_TWO, 5, Duration.standardMinutes(3))).advanceWatermarkTo(baseTime.plus(Duration.standardMinutes(3))).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 1, Duration.standardMinutes(4)), event(LeaderBoardTest.TestUser.BLUE_ONE, 2, Duration.standardSeconds(270))).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> teamScores = p.apply(createEvents).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateTeamScores(LeaderBoardTest.TEAM_WINDOW_DURATION, LeaderBoardTest.ALLOWED_LATENESS));
        String blueTeam = LeaderBoardTest.TestUser.BLUE_ONE.getTeam();
        String redTeam = LeaderBoardTest.TestUser.RED_ONE.getTeam();
        PAssert.that(teamScores).inOnTimePane(new IntervalWindow(baseTime, LeaderBoardTest.TEAM_WINDOW_DURATION)).containsInAnyOrder(KV.of(blueTeam, 12), KV.of(redTeam, 4));
        p.run().waitUntilFinish();
    }

    /**
     * A test of the {@link CalculateTeamScores} {@link PTransform} when all of the elements arrive on
     * time, and the processing time advances far enough for speculative panes.
     */
    @Test
    public void testTeamScoresSpeculative() {
        TestStream<GameActionInfo> createEvents = // The window closes and we get an ON_TIME pane that contains all of the updates
        // Some more events occur
        // More time passes and a speculative pane containing a refined value for the blue pane
        // is
        // emitted
        // Some additional time passes and we get a speculative pane for the red team
        // Some time passes within the runner, which causes a speculative pane containing the
        // blue
        // team's score to be emitted
        // Start at the epoch
        TestStream.create(AvroCoder.of(GameActionInfo.class)).advanceWatermarkTo(baseTime).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardSeconds(3)), event(LeaderBoardTest.TestUser.BLUE_ONE, 2, Duration.standardMinutes(1))).advanceProcessingTime(Duration.standardMinutes(10)).addElements(event(LeaderBoardTest.TestUser.RED_TWO, 5, Duration.standardMinutes(3))).advanceProcessingTime(Duration.standardMinutes(12)).addElements(event(LeaderBoardTest.TestUser.BLUE_TWO, 3, Duration.standardSeconds(22))).advanceProcessingTime(Duration.standardMinutes(10)).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 4, Duration.standardMinutes(4)), event(LeaderBoardTest.TestUser.BLUE_TWO, 2, Duration.standardMinutes(2))).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> teamScores = p.apply(createEvents).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateTeamScores(LeaderBoardTest.TEAM_WINDOW_DURATION, LeaderBoardTest.ALLOWED_LATENESS));
        String blueTeam = LeaderBoardTest.TestUser.BLUE_ONE.getTeam();
        String redTeam = LeaderBoardTest.TestUser.RED_ONE.getTeam();
        IntervalWindow window = new IntervalWindow(baseTime, LeaderBoardTest.TEAM_WINDOW_DURATION);
        // The window contains speculative panes alongside the on-time pane
        /* The on-time blue pane */
        /* The on-time red pane */
        /* The first blue speculative pane */
        /* The second blue speculative pane */
        /* The red speculative pane */
        PAssert.that(teamScores).inWindow(window).containsInAnyOrder(KV.of(blueTeam, 10), KV.of(redTeam, 9), KV.of(blueTeam, 5), KV.of(blueTeam, 8), KV.of(redTeam, 5));
        PAssert.that(teamScores).inOnTimePane(window).containsInAnyOrder(KV.of(blueTeam, 10), KV.of(redTeam, 9));
        p.run().waitUntilFinish();
    }

    /**
     * A test where elements arrive behind the watermark (late data), but before the end of the
     * window. These elements are emitted on time.
     */
    @Test
    public void testTeamScoresUnobservablyLate() {
        BoundedWindow window = new IntervalWindow(baseTime, LeaderBoardTest.TEAM_WINDOW_DURATION);
        TestStream<GameActionInfo> createEvents = // These events are late, but the window hasn't closed yet, so the elements are in the
        // on-time pane
        TestStream.create(AvroCoder.of(GameActionInfo.class)).advanceWatermarkTo(baseTime).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardSeconds(3)), event(LeaderBoardTest.TestUser.BLUE_TWO, 5, Duration.standardMinutes(8)), event(LeaderBoardTest.TestUser.RED_ONE, 4, Duration.standardMinutes(2)), event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardMinutes(5))).advanceWatermarkTo(baseTime.plus(LeaderBoardTest.TEAM_WINDOW_DURATION).minus(Duration.standardMinutes(1))).addElements(event(LeaderBoardTest.TestUser.RED_TWO, 2, ZERO), event(LeaderBoardTest.TestUser.RED_TWO, 5, Duration.standardMinutes(1)), event(LeaderBoardTest.TestUser.BLUE_TWO, 2, Duration.standardSeconds(90)), event(LeaderBoardTest.TestUser.RED_TWO, 3, Duration.standardMinutes(3))).advanceWatermarkTo(baseTime.plus(LeaderBoardTest.TEAM_WINDOW_DURATION).plus(Duration.standardMinutes(1))).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> teamScores = p.apply(createEvents).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateTeamScores(LeaderBoardTest.TEAM_WINDOW_DURATION, LeaderBoardTest.ALLOWED_LATENESS));
        String blueTeam = LeaderBoardTest.TestUser.BLUE_ONE.getTeam();
        String redTeam = LeaderBoardTest.TestUser.RED_ONE.getTeam();
        // The On Time pane contains the late elements that arrived before the end of the window
        PAssert.that(teamScores).inOnTimePane(window).containsInAnyOrder(KV.of(redTeam, 14), KV.of(blueTeam, 13));
        p.run().waitUntilFinish();
    }

    /**
     * A test where elements arrive behind the watermark (late data) after the watermark passes the
     * end of the window, but before the maximum allowed lateness. These elements are emitted in a
     * late pane.
     */
    @Test
    public void testTeamScoresObservablyLate() {
        Instant firstWindowCloses = baseTime.plus(LeaderBoardTest.ALLOWED_LATENESS).plus(LeaderBoardTest.TEAM_WINDOW_DURATION);
        TestStream<GameActionInfo> createEvents = // These elements should appear in the final pane
        // A late refinement is emitted due to the advance in processing time, but the window
        // has
        // not yet closed because the watermark has not advanced
        // These events are late but should still appear in a late pane
        TestStream.create(AvroCoder.of(GameActionInfo.class)).advanceWatermarkTo(baseTime).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardSeconds(3)), event(LeaderBoardTest.TestUser.BLUE_TWO, 5, Duration.standardMinutes(8))).advanceProcessingTime(Duration.standardMinutes(10)).advanceWatermarkTo(baseTime.plus(Duration.standardMinutes(3))).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 3, Duration.standardMinutes(1)), event(LeaderBoardTest.TestUser.RED_ONE, 4, Duration.standardMinutes(2)), event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardMinutes(5))).advanceWatermarkTo(firstWindowCloses.minus(Duration.standardMinutes(1))).addElements(event(LeaderBoardTest.TestUser.RED_TWO, 2, ZERO), event(LeaderBoardTest.TestUser.RED_TWO, 5, Duration.standardMinutes(1)), event(LeaderBoardTest.TestUser.RED_TWO, 3, Duration.standardMinutes(3))).advanceProcessingTime(Duration.standardMinutes(12)).addElements(event(LeaderBoardTest.TestUser.RED_TWO, 9, Duration.standardMinutes(1)), event(LeaderBoardTest.TestUser.RED_TWO, 1, Duration.standardMinutes(3))).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> teamScores = p.apply(createEvents).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateTeamScores(LeaderBoardTest.TEAM_WINDOW_DURATION, LeaderBoardTest.ALLOWED_LATENESS));
        BoundedWindow window = new IntervalWindow(baseTime, LeaderBoardTest.TEAM_WINDOW_DURATION);
        String blueTeam = LeaderBoardTest.TestUser.BLUE_ONE.getTeam();
        String redTeam = LeaderBoardTest.TestUser.RED_ONE.getTeam();
        PAssert.that(teamScores).inWindow(window).satisfies(( input) -> {
            // The final sums need not exist in the same pane, but must appear in the output
            // PCollection
            assertThat(input, hasItem(KV.of(blueTeam, 11)));
            assertThat(input, hasItem(KV.of(redTeam, 27)));
            return null;
        });
        // The closing behavior of CalculateTeamScores precludes an inFinalPane matcher
        PAssert.thatMap(teamScores).inOnTimePane(window).isEqualTo(ImmutableMap.<String, Integer>builder().put(redTeam, 7).put(blueTeam, 11).build());
        // No final pane is emitted for the blue team, as all of their updates have been taken into
        // account in earlier panes
        PAssert.that(teamScores).inFinalPane(window).containsInAnyOrder(KV.of(redTeam, 27));
        p.run().waitUntilFinish();
    }

    /**
     * A test where elements arrive beyond the maximum allowed lateness. These elements are dropped
     * within {@link CalculateTeamScores} and do not impact the final result.
     */
    @Test
    public void testTeamScoresDroppablyLate() {
        BoundedWindow window = new IntervalWindow(baseTime, LeaderBoardTest.TEAM_WINDOW_DURATION);
        TestStream<GameActionInfo> infos = // These elements within the expired window are droppably late, and will not appear in
        // the
        // output
        // Move the watermark past the end of the allowed lateness plus the end of the window
        // Move the watermark to the end of the window to output on time
        TestStream.create(AvroCoder.of(GameActionInfo.class)).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 12, ZERO), event(LeaderBoardTest.TestUser.RED_ONE, 3, ZERO)).advanceWatermarkTo(window.maxTimestamp()).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 4, Duration.standardMinutes(2)), event(LeaderBoardTest.TestUser.BLUE_TWO, 3, ZERO), event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardMinutes(3))).advanceWatermarkTo(baseTime.plus(LeaderBoardTest.TEAM_WINDOW_DURATION)).advanceWatermarkTo(baseTime.plus(LeaderBoardTest.ALLOWED_LATENESS).plus(LeaderBoardTest.TEAM_WINDOW_DURATION).plus(Duration.standardMinutes(1))).addElements(event(LeaderBoardTest.TestUser.BLUE_TWO, 3, LeaderBoardTest.TEAM_WINDOW_DURATION.minus(Duration.standardSeconds(5))), event(LeaderBoardTest.TestUser.RED_ONE, 7, Duration.standardMinutes(4))).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> teamScores = p.apply(infos).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateTeamScores(LeaderBoardTest.TEAM_WINDOW_DURATION, LeaderBoardTest.ALLOWED_LATENESS));
        String blueTeam = LeaderBoardTest.TestUser.BLUE_ONE.getTeam();
        String redTeam = LeaderBoardTest.TestUser.RED_ONE.getTeam();
        // Only one on-time pane and no late panes should be emitted
        PAssert.that(teamScores).inWindow(window).containsInAnyOrder(KV.of(redTeam, 7), KV.of(blueTeam, 18));
        // No elements are added before the watermark passes the end of the window plus the allowed
        // lateness, so no refinement should be emitted
        PAssert.that(teamScores).inFinalPane(window).empty();
        p.run().waitUntilFinish();
    }

    /**
     * A test where elements arrive both on-time and late in {@link CalculateUserScores}, which emits
     * output into the {@link GlobalWindow}. All elements that arrive should be taken into account,
     * even if they arrive later than the maximum allowed lateness.
     */
    @Test
    public void testUserScore() {
        TestStream<GameActionInfo> infos = // Late elements are always observable within the global window - they arrive before
        // the window closes, so they will appear in a pane, even if they arrive after the
        // allowed lateness, and are taken into account alongside on-time elements
        TestStream.create(AvroCoder.of(GameActionInfo.class)).addElements(event(LeaderBoardTest.TestUser.BLUE_ONE, 12, ZERO), event(LeaderBoardTest.TestUser.RED_ONE, 3, ZERO)).advanceProcessingTime(Duration.standardMinutes(7)).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 4, Duration.standardMinutes(2)), event(LeaderBoardTest.TestUser.BLUE_TWO, 3, ZERO), event(LeaderBoardTest.TestUser.BLUE_ONE, 3, Duration.standardMinutes(3))).advanceProcessingTime(Duration.standardMinutes(5)).advanceWatermarkTo(baseTime.plus(LeaderBoardTest.ALLOWED_LATENESS).plus(Duration.standardHours(12))).addElements(event(LeaderBoardTest.TestUser.RED_ONE, 3, Duration.standardMinutes(7)), event(LeaderBoardTest.TestUser.RED_ONE, 2, LeaderBoardTest.ALLOWED_LATENESS.plus(Duration.standardHours(13)))).advanceProcessingTime(Duration.standardMinutes(6)).addElements(event(LeaderBoardTest.TestUser.BLUE_TWO, 5, Duration.standardMinutes(12))).advanceProcessingTime(Duration.standardMinutes(20)).advanceWatermarkToInfinity();
        PCollection<KV<String, Integer>> userScores = p.apply(infos).apply(new org.apache.beam.examples.complete.game.LeaderBoard.CalculateUserScores(LeaderBoardTest.ALLOWED_LATENESS));
        // User scores are emitted in speculative panes in the Global Window - this matcher choice
        // ensures that panes emitted by the watermark advancing to positive infinity are not included,
        // as that will not occur outside of tests
        PAssert.that(userScores).inEarlyGlobalWindowPanes().containsInAnyOrder(KV.of(LeaderBoardTest.TestUser.BLUE_ONE.getUser(), 15), KV.of(LeaderBoardTest.TestUser.RED_ONE.getUser(), 7), KV.of(LeaderBoardTest.TestUser.RED_ONE.getUser(), 12), KV.of(LeaderBoardTest.TestUser.BLUE_TWO.getUser(), 3), KV.of(LeaderBoardTest.TestUser.BLUE_TWO.getUser(), 8));
        p.run().waitUntilFinish();
    }

    @Test
    public void testLeaderBoardOptions() {
        PipelineOptionsFactory.as(Options.class);
    }
}
