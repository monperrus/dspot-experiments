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
package org.apache.hadoop.hive.metastore;


import junit.framework.TestCase;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;


public class TestMetaStoreAuthorization extends TestCase {
    protected HiveConf conf = new HiveConf();

    private int port;

    public void testIsWritable() throws Exception {
        setup();
        String testDir = System.getProperty("test.warehouse.dir", "/tmp");
        Path testDirPath = new Path(testDir);
        FileSystem fs = testDirPath.getFileSystem(conf);
        Path top = new Path(testDirPath, "_foobarbaz12_");
        try {
            fs.mkdirs(top);
            Warehouse wh = new Warehouse(conf);
            FsPermission writePerm = FsPermission.createImmutable(((short) (511)));
            FsPermission noWritePerm = FsPermission.createImmutable(((short) (365)));
            fs.setPermission(top, writePerm);
            TestCase.assertTrue((("Expected " + top) + " to be writable"), wh.isWritable(top));
            fs.setPermission(top, noWritePerm);
            TestCase.assertTrue((("Expected " + top) + " to be not writable"), (!(wh.isWritable(top))));
        } finally {
            fs.delete(top, true);
        }
    }

    public void testMetaStoreAuthorization() throws Exception {
        setup();
        MetaStoreTestUtils.startMetaStoreWithRetry(conf);
        HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
        FileSystem fs = null;
        String dbName = "simpdb";
        Database db1 = null;
        Path p = null;
        try {
            try {
                db1 = client.getDatabase(dbName);
                client.dropDatabase(dbName);
            } catch (NoSuchObjectException noe) {
            }
            if (db1 != null) {
                p = new Path(db1.getLocationUri());
                fs = p.getFileSystem(conf);
                fs.delete(p, true);
            }
            db1 = new Database();
            db1.setName(dbName);
            client.createDatabase(db1);
            Database db = client.getDatabase(dbName);
            TestCase.assertTrue("Databases do not match", db1.getName().equals(db.getName()));
            p = new Path(db.getLocationUri());
            if (fs == null) {
                fs = p.getFileSystem(conf);
            }
            fs.setPermission(p.getParent(), FsPermission.createImmutable(((short) (365))));
            try {
                client.dropDatabase(dbName);
                throw new Exception("Expected dropDatabase call to fail");
            } catch (MetaException me) {
            }
            fs.setPermission(p.getParent(), FsPermission.createImmutable(((short) (493))));
            client.dropDatabase(dbName);
        } finally {
            if (p != null) {
                fs.delete(p, true);
            }
        }
    }
}
