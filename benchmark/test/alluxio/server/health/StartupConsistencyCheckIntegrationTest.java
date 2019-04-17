/**
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */
package alluxio.server.health;


import PropertyKey.MASTER_STARTUP_CONSISTENCY_CHECK_ENABLED;
import PropertyKey.SECURITY_AUTHORIZATION_PERMISSION_ENABLED;
import Status.COMPLETE;
import Status.DISABLED;
import UnderFileSystem.Factory;
import alluxio.AlluxioURI;
import alluxio.client.file.FileSystem;
import alluxio.conf.ServerConfiguration;
import alluxio.master.LocalAlluxioCluster;
import alluxio.master.MasterRegistry;
import alluxio.master.file.FileSystemMaster;
import alluxio.testutils.BaseIntegrationTest;
import alluxio.testutils.LocalAlluxioClusterResource;
import alluxio.testutils.master.MasterTestUtils;
import alluxio.underfs.UnderFileSystem;
import alluxio.underfs.options.DeleteOptions;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;


/**
 * Tests the consistency check which happens on master start up.
 */
public final class StartupConsistencyCheckIntegrationTest extends BaseIntegrationTest {
    private static final AlluxioURI TOP_LEVEL_FILE = new AlluxioURI("/file");

    private static final AlluxioURI TOP_LEVEL_DIR = new AlluxioURI("/dir");

    private static final AlluxioURI SECOND_LEVEL_FILE = new AlluxioURI("/dir/file");

    private static final AlluxioURI SECOND_LEVEL_DIR = new AlluxioURI("/dir/dir");

    private static final AlluxioURI THIRD_LEVEL_FILE = new AlluxioURI("/dir/dir/file");

    private LocalAlluxioCluster mCluster;

    private FileSystem mFileSystem;

    @Rule
    public LocalAlluxioClusterResource mLocalAlluxioClusterResource = new LocalAlluxioClusterResource.Builder().setProperty(SECURITY_AUTHORIZATION_PERMISSION_ENABLED, "false").setProperty(MASTER_STARTUP_CONSISTENCY_CHECK_ENABLED, "true").build();

    /**
     * Tests that a consistent Alluxio system's startup check does not detect any inconsistencies
     * and completes within 1 minute.
     */
    @Test
    public void consistent() throws Exception {
        mCluster.stopFS();
        MasterRegistry registry = MasterTestUtils.createLeaderFileSystemMasterFromJournal();
        FileSystemMaster master = registry.get(FileSystemMaster.class);
        MasterTestUtils.waitForStartupConsistencyCheck(master);
        Assert.assertTrue(master.getStartupConsistencyCheck().getStatus().equals(COMPLETE));
        Assert.assertTrue(master.getStartupConsistencyCheck().getInconsistentUris().isEmpty());
        registry.stop();
    }

    /**
     * Tests that an inconsistent Alluxio system's startup check correctly detects the inconsistent
     * files.
     */
    @Test
    public void inconsistent() throws Exception {
        String topLevelFileUfsPath = mFileSystem.getStatus(StartupConsistencyCheckIntegrationTest.TOP_LEVEL_FILE).getUfsPath();
        String secondLevelDirUfsPath = mFileSystem.getStatus(StartupConsistencyCheckIntegrationTest.SECOND_LEVEL_DIR).getUfsPath();
        mCluster.stopFS();
        UnderFileSystem ufs = Factory.create(topLevelFileUfsPath, ServerConfiguration.global());
        ufs.deleteFile(topLevelFileUfsPath);
        ufs.deleteDirectory(secondLevelDirUfsPath, DeleteOptions.defaults().setRecursive(true));
        MasterRegistry registry = MasterTestUtils.createLeaderFileSystemMasterFromJournal();
        FileSystemMaster master = registry.get(FileSystemMaster.class);
        MasterTestUtils.waitForStartupConsistencyCheck(master);
        Assert.assertTrue(master.getStartupConsistencyCheck().getStatus().equals(COMPLETE));
        List<AlluxioURI> expected = Lists.newArrayList(StartupConsistencyCheckIntegrationTest.TOP_LEVEL_FILE, StartupConsistencyCheckIntegrationTest.SECOND_LEVEL_DIR, StartupConsistencyCheckIntegrationTest.THIRD_LEVEL_FILE);
        List<AlluxioURI> result = master.getStartupConsistencyCheck().getInconsistentUris();
        Collections.sort(expected);
        Collections.sort(result);
        Assert.assertEquals(expected, result);
        registry.stop();
    }

    @Test
    public void disabled() throws Exception {
        mCluster.stopFS();
        ServerConfiguration.set(MASTER_STARTUP_CONSISTENCY_CHECK_ENABLED, false);
        MasterRegistry registry = MasterTestUtils.createLeaderFileSystemMasterFromJournal();
        FileSystemMaster master = registry.get(FileSystemMaster.class);
        MasterTestUtils.waitForStartupConsistencyCheck(master);
        Assert.assertTrue(master.getStartupConsistencyCheck().getInconsistentUris().isEmpty());
        Assert.assertTrue(master.getStartupConsistencyCheck().getStatus().equals(DISABLED));
        registry.stop();
    }
}
