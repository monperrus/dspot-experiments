/**
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho.testing.viewtree;


import com.facebook.litho.testing.assertj.LithoAssertions;
import org.junit.Test;


/**
 * Tests for {@link LevenshteinDistance}.
 */
public class LevenshteinDistanceTest {
    @Test
    public void testGetLevenshteinDistance() {
        String s = "sunday";
        String t = "saturday";
        int distance;
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 0);
        LithoAssertions.assertThat(distance).isEqualTo(1);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 2);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 3);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 4);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 5);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 5);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        s = "abcdef";
        t = "a";
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 0);
        LithoAssertions.assertThat(distance).isEqualTo(1);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 10);
        LithoAssertions.assertThat(distance).isEqualTo(5);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 0);
        LithoAssertions.assertThat(distance).isEqualTo(1);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 10);
        LithoAssertions.assertThat(distance).isEqualTo(5);
        s = "abcdef";
        t = "xxxdef";
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 0);
        LithoAssertions.assertThat(distance).isEqualTo(1);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        distance = LevenshteinDistance.getLevenshteinDistance(s, t, 10);
        LithoAssertions.assertThat(distance).isEqualTo(3);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 0);
        LithoAssertions.assertThat(distance).isEqualTo(1);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 1);
        LithoAssertions.assertThat(distance).isEqualTo(2);
        distance = LevenshteinDistance.getLevenshteinDistance(t, s, 10);
        LithoAssertions.assertThat(distance).isEqualTo(3);
    }
}
