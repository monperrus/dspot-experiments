/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoft;


import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverterTest;


public class HardMediumSoftScoreXStreamConverterTest extends AbstractScoreXStreamConverterTest {
    @Test
    public void serializeAndDeserialize() {
        assertSerializeAndDeserialize(null, new HardMediumSoftScoreXStreamConverterTest.TestHardMediumSoftScoreWrapper(null));
        HardMediumSoftScore score = HardMediumSoftScore.of(1200, 30, 4);
        assertSerializeAndDeserialize(score, new HardMediumSoftScoreXStreamConverterTest.TestHardMediumSoftScoreWrapper(score));
        score = HardMediumSoftScore.ofUninitialized((-7), 1200, 30, 4);
        assertSerializeAndDeserialize(score, new HardMediumSoftScoreXStreamConverterTest.TestHardMediumSoftScoreWrapper(score));
    }

    public static class TestHardMediumSoftScoreWrapper extends AbstractScoreXStreamConverterTest.TestScoreWrapper<HardMediumSoftScore> {
        @XStreamConverter(HardMediumSoftScoreXStreamConverter.class)
        private HardMediumSoftScore score;

        public TestHardMediumSoftScoreWrapper(HardMediumSoftScore score) {
            this.score = score;
        }

        @Override
        public HardMediumSoftScore getScore() {
            return score;
        }
    }
}
