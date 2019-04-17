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
package com.hazelcast.map.impl.nearcache.invalidation;


import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.internal.nearcache.impl.invalidation.MetaDataGenerator;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;


@RunWith(HazelcastSerialClassRunner.class)
@Category({ QuickTest.class, ParallelTest.class })
public class MemberMapMetaDataGeneratorTest extends HazelcastTestSupport {
    private static final String MAP_NAME = "MemberMapMetaDataGeneratorTest";

    @Test
    public void destroying_map_removes_related_metadata_when_near_cache_exists() {
        MapConfig mapConfig = getMapConfig(MemberMapMetaDataGeneratorTest.MAP_NAME);
        Config config = getConfig().addMapConfig(mapConfig);
        HazelcastInstance member = createHazelcastInstance(config);
        IMap<Integer, Integer> map = member.getMap(MemberMapMetaDataGeneratorTest.MAP_NAME);
        map.put(1, 1);
        final MetaDataGenerator metaDataGenerator = MemberMapMetaDataGeneratorTest.getMetaDataGenerator(member);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                Assert.assertNotNull(metaDataGenerator.getSequenceGenerators().get(MemberMapMetaDataGeneratorTest.MAP_NAME));
            }
        });
        map.destroy();
        Assert.assertNull(metaDataGenerator.getSequenceGenerators().get(MemberMapMetaDataGeneratorTest.MAP_NAME));
    }

    @Test
    public void destroying_map_removes_related_metadata_when_near_cache_not_exists() {
        Config config = getConfig();
        HazelcastInstance member = createHazelcastInstance(config);
        IMap<Integer, Integer> map = member.getMap(MemberMapMetaDataGeneratorTest.MAP_NAME);
        map.put(1, 1);
        final MetaDataGenerator metaDataGenerator = MemberMapMetaDataGeneratorTest.getMetaDataGenerator(member);
        HazelcastTestSupport.assertTrueEventually(new AssertTask() {
            @Override
            public void run() {
                Assert.assertNull(metaDataGenerator.getSequenceGenerators().get(MemberMapMetaDataGeneratorTest.MAP_NAME));
            }
        });
        map.destroy();
        Assert.assertNull(metaDataGenerator.getSequenceGenerators().get(MemberMapMetaDataGeneratorTest.MAP_NAME));
    }
}
