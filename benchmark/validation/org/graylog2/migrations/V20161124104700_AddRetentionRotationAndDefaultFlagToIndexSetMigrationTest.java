/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.migrations;


import V20161124104700_AddRetentionRotationAndDefaultFlagToIndexSetMigration.MigrationCompleted;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import org.graylog2.indexer.indexset.IndexSetConfig;
import org.graylog2.indexer.indexset.IndexSetService;
import org.graylog2.indexer.management.IndexManagementConfig;
import org.graylog2.indexer.retention.strategies.DeletionRetentionStrategy;
import org.graylog2.indexer.retention.strategies.DeletionRetentionStrategyConfig;
import org.graylog2.indexer.rotation.strategies.MessageCountRotationStrategy;
import org.graylog2.indexer.rotation.strategies.MessageCountRotationStrategyConfig;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.indexer.retention.RetentionStrategyConfig;
import org.graylog2.plugin.indexer.rotation.RotationStrategyConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;


public class V20161124104700_AddRetentionRotationAndDefaultFlagToIndexSetMigrationTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private IndexSetService indexSetService;

    @Mock
    private ClusterConfigService clusterConfigService;

    private Migration migration;

    @Test
    public void createdAt() throws Exception {
        // Test the date to detect accidental changes to it.
        assertThat(migration.createdAt()).isEqualTo(ZonedDateTime.parse("2016-11-24T10:47:00Z"));
    }

    @Test
    public void upgrade() throws Exception {
        final String rotationStrategyClass = MessageCountRotationStrategy.class.getCanonicalName();
        final String retentionStrategyClass = DeletionRetentionStrategy.class.getCanonicalName();
        final RotationStrategyConfig rotationStrategy = MessageCountRotationStrategyConfig.createDefault();
        final RetentionStrategyConfig retentionStrategy = DeletionRetentionStrategyConfig.createDefault();
        final IndexSetConfig config1 = IndexSetConfig.builder().id("id-1").title("title-1").indexPrefix("prefix-1").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 12, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-1").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        final IndexSetConfig config2 = IndexSetConfig.builder().id("id-2").title("title-2").indexPrefix("prefix-2").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-2").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(IndexManagementConfig.create(rotationStrategyClass, retentionStrategyClass));
        Mockito.when(indexSetService.findAll()).thenReturn(Lists.newArrayList(config1, config2));
        migration.upgrade();
        Mockito.verify(indexSetService).save(config1.toBuilder().rotationStrategyClass(rotationStrategyClass).retentionStrategyClass(retentionStrategyClass).build());
        Mockito.verify(indexSetService).save(config2.toBuilder().rotationStrategyClass(rotationStrategyClass).retentionStrategyClass(retentionStrategyClass).build());
        Mockito.verify(clusterConfigService).write(MigrationCompleted.create(ImmutableSet.of("id-1", "id-2"), Collections.emptySet(), "id-2"));
    }

    @Test
    public void upgradeWhenOneAlreadyHasStrategiesSet() throws Exception {
        final String rotationStrategyClass = MessageCountRotationStrategy.class.getCanonicalName();
        final String retentionStrategyClass = DeletionRetentionStrategy.class.getCanonicalName();
        final RotationStrategyConfig rotationStrategy = MessageCountRotationStrategyConfig.createDefault();
        final RetentionStrategyConfig retentionStrategy = DeletionRetentionStrategyConfig.createDefault();
        final IndexSetConfig config1 = // Does not have the rotation strategy class name!
        IndexSetConfig.builder().id("id-1").title("title-1").indexPrefix("prefix-1").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategyClass(retentionStrategyClass).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 12, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-1").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        final IndexSetConfig config2 = IndexSetConfig.builder().id("id-2").title("title-2").indexPrefix("prefix-2").shards(1).replicas(0).rotationStrategyClass(rotationStrategyClass).rotationStrategy(rotationStrategy).retentionStrategyClass(retentionStrategyClass).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 13, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-2").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(IndexManagementConfig.create(rotationStrategyClass, retentionStrategyClass));
        Mockito.when(indexSetService.findAll()).thenReturn(Lists.newArrayList(config1, config2));
        migration.upgrade();
        Mockito.verify(indexSetService).save(config1.toBuilder().rotationStrategyClass(rotationStrategyClass).build());
        Mockito.verify(indexSetService, Mockito.never()).save(config2);
        Mockito.verify(clusterConfigService).write(MigrationCompleted.create(Collections.singleton("id-1"), Collections.singleton("id-2"), "id-1"));
    }

    @Test
    public void upgradeWithoutIndexManagementConfig() throws Exception {
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(null);
        expectedException.expect(IllegalStateException.class);
        migration.upgrade();
        Mockito.verify(indexSetService, Mockito.never()).save(ArgumentMatchers.any(IndexSetConfig.class));
        Mockito.verify(clusterConfigService, Mockito.never()).write(MigrationCompleted.class);
    }

    @Test
    public void upgradeWithWrongRotationPrefix() throws Exception {
        final String rotationStrategyClass = "foo";
        final String retentionStrategyClass = DeletionRetentionStrategy.class.getCanonicalName();
        final RotationStrategyConfig rotationStrategy = MessageCountRotationStrategyConfig.createDefault();
        final RetentionStrategyConfig retentionStrategy = DeletionRetentionStrategyConfig.createDefault();
        final IndexSetConfig config1 = IndexSetConfig.builder().id("id-1").title("title-1").indexPrefix("prefix-1").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 12, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-1").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        final IndexSetConfig config2 = IndexSetConfig.builder().id("id-2").title("title-2").indexPrefix("prefix-2").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 13, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-2").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(IndexManagementConfig.create(rotationStrategyClass, retentionStrategyClass));
        Mockito.when(indexSetService.findAll()).thenReturn(Lists.newArrayList(config1, config2));
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("rotation strategy config type <");
        migration.upgrade();
        Mockito.verify(indexSetService, Mockito.never()).save(ArgumentMatchers.any(IndexSetConfig.class));
        Mockito.verify(clusterConfigService, Mockito.never()).write(MigrationCompleted.class);
    }

    @Test
    public void upgradeWithWrongRetentionPrefix() throws Exception {
        final String rotationStrategyClass = MessageCountRotationStrategy.class.getCanonicalName();
        final String retentionStrategyClass = "bar";
        final RotationStrategyConfig rotationStrategy = MessageCountRotationStrategyConfig.createDefault();
        final RetentionStrategyConfig retentionStrategy = DeletionRetentionStrategyConfig.createDefault();
        final IndexSetConfig config1 = IndexSetConfig.builder().id("id-1").title("title-1").indexPrefix("prefix-1").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 12, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-1").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        final IndexSetConfig config2 = IndexSetConfig.builder().id("id-2").title("title-2").indexPrefix("prefix-2").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 13, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-2").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(IndexManagementConfig.create(rotationStrategyClass, retentionStrategyClass));
        Mockito.when(indexSetService.findAll()).thenReturn(Lists.newArrayList(config1, config2));
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("retention strategy config type <");
        migration.upgrade();
        Mockito.verify(indexSetService, Mockito.never()).save(ArgumentMatchers.any(IndexSetConfig.class));
        Mockito.verify(clusterConfigService, Mockito.never()).write(MigrationCompleted.class);
    }

    @Test
    public void upgradeDoeNotRunWhenAlreadyComplete() throws Exception {
        final String rotationStrategyClass = MessageCountRotationStrategy.class.getCanonicalName();
        final String retentionStrategyClass = DeletionRetentionStrategy.class.getCanonicalName();
        final RotationStrategyConfig rotationStrategy = MessageCountRotationStrategyConfig.createDefault();
        final RetentionStrategyConfig retentionStrategy = DeletionRetentionStrategyConfig.createDefault();
        final IndexSetConfig config1 = IndexSetConfig.builder().id("id-1").title("title-1").indexPrefix("prefix-1").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 12, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-1").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        final IndexSetConfig config2 = IndexSetConfig.builder().id("id-2").title("title-2").indexPrefix("prefix-2").shards(1).replicas(0).rotationStrategy(rotationStrategy).retentionStrategy(retentionStrategy).creationDate(ZonedDateTime.of(2016, 10, 13, 0, 0, 0, 0, ZoneOffset.UTC)).indexAnalyzer("standard").indexTemplateName("template-2").indexOptimizationMaxNumSegments(1).indexOptimizationDisabled(false).build();
        Mockito.when(clusterConfigService.get(IndexManagementConfig.class)).thenReturn(IndexManagementConfig.create(rotationStrategyClass, retentionStrategyClass));
        Mockito.when(indexSetService.findAll()).thenReturn(Lists.newArrayList(config1, config2));
        Mockito.when(clusterConfigService.get(MigrationCompleted.class)).thenReturn(MigrationCompleted.create(Collections.emptySet(), Collections.emptySet(), "id-1"));
        migration.upgrade();
        Mockito.verify(indexSetService, Mockito.never()).save(ArgumentMatchers.any(IndexSetConfig.class));
        Mockito.verify(clusterConfigService, Mockito.never()).write(MigrationCompleted.class);
    }
}
