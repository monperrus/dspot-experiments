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
package org.apache.hive.service;


import HiveConf.ConfVars.SCRATCHDIR;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.shims.HadoopShims.MiniDFSShim;
import org.apache.hive.jdbc.miniHS2.MiniHS2;
import org.junit.Assert;
import org.junit.Test;


/**
 * If the operation fails because of a DFS error, it used to result in an ugly stack at the client.
 * HIVE-16960 fixes that issue.  This test case checks one DFS error related to sticky bit.  When
 * the sticky bit is set, a user error indicating access denied will the thrown.
 *
 * Setup: HIVE_SERVER2_ENABLE_DOAS set to true:  HS2 performs the operation as connected user.
 * Connect to HS2 as "hive".
 * Create a file and set the sticky bit on the directory.  This will not allow the file to move
 * out of the directory.
 * Perform "LOAD" operation.  This operation will attempt to move the file, resulting in an error
 * from DFS.  The DFS error will translate to an Hive Error with number 20009, that corresponds to
 * "ACCESS DENIED".  The test checks that 20009 is thrown.
 *
 * Additional tests can be added to cover Quota related exceptions.
 */
public class TestDFSErrorHandling {
    private static MiniHS2 miniHS2 = null;

    private static HiveConf hiveConf = null;

    @Test
    public void testAccessDenied() throws Exception {
        Assert.assertTrue("Test setup failed. MiniHS2 is not initialized", (((TestDFSErrorHandling.miniHS2) != null) && (TestDFSErrorHandling.miniHS2.isStarted())));
        Class.forName(MiniHS2.getJdbcDriverName());
        Path scratchDir = new Path(HiveConf.getVar(TestDFSErrorHandling.hiveConf, SCRATCHDIR));
        MiniDFSShim dfs = TestDFSErrorHandling.miniHS2.getDfs();
        FileSystem fs = dfs.getFileSystem();
        Path stickyBitDir = new Path(scratchDir, "stickyBitDir");
        fs.mkdirs(stickyBitDir);
        String dataFileDir = TestDFSErrorHandling.hiveConf.get("test.data.files").replace('\\', '/').replace("c:", "").replace("C:", "").replace("D:", "").replace("d:", "");
        Path dataFilePath = new Path(dataFileDir, "kv1.txt");
        fs.copyFromLocalFile(dataFilePath, stickyBitDir);
        FsPermission fsPermission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL, true);
        // Sets the sticky bit on stickyBitDir - now removing file kv1.txt from stickyBitDir by
        // unprivileged user will result in a DFS error.
        fs.setPermission(stickyBitDir, fsPermission);
        FileStatus[] files = fs.listStatus(stickyBitDir);
        // Connecting to HS2 as foo.
        Connection hs2Conn = DriverManager.getConnection(TestDFSErrorHandling.miniHS2.getJdbcURL(), "foo", "bar");
        Statement stmt = hs2Conn.createStatement();
        String tableName = "stickyBitTable";
        stmt.execute(("drop table if exists " + tableName));
        stmt.execute((("create table " + tableName) + " (foo int, bar string)"));
        try {
            // This statement will attempt to move kv1.txt out of stickyBitDir as user foo.  HS2 is
            // expected to return 20009.
            stmt.execute((((("LOAD DATA INPATH '" + (stickyBitDir.toUri().getPath())) + "/kv1.txt' ") + "OVERWRITE INTO TABLE ") + tableName));
        } catch (Exception e) {
            if (e instanceof SQLException) {
                SQLException se = ((SQLException) (e));
                Assert.assertEquals("Unexpected error code", 20009, se.getErrorCode());
                System.out.println(String.format("Error Message: %s", se.getMessage()));
            } else
                throw e;

        }
        stmt.execute(("drop table if exists " + tableName));
        stmt.close();
        hs2Conn.close();
    }
}
