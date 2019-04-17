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
package org.apache.hadoop.hive.ql.security;


import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.ql.IDriver;
import org.apache.hadoop.hive.ql.processors.CommandProcessorResponse;
import org.apache.hadoop.security.UserGroupInformation;


/**
 * TestClientSideAuthorizationProvider : Simple base test for client side
 * Authorization Providers. By default, tests DefaultHiveAuthorizationProvider
 */
public class TestClientSideAuthorizationProvider extends TestCase {
    protected HiveConf clientHiveConf;

    protected HiveMetaStoreClient msc;

    protected IDriver driver;

    protected UserGroupInformation ugi;

    public void testSimplePrivileges() throws Exception {
        String dbName = getTestDbName();
        String tblName = getTestTableName();
        String userName = ugi.getUserName();
        allowCreateDatabase(userName);
        CommandProcessorResponse ret = driver.run(("create database " + dbName));
        TestCase.assertEquals(0, ret.getResponseCode());
        Database db = msc.getDatabase(dbName);
        String dbLocn = db.getLocationUri();
        disallowCreateDatabase(userName);
        validateCreateDb(db, dbName);
        disallowCreateInDb(dbName, userName, dbLocn);
        driver.run(("use " + dbName));
        ret = driver.run(String.format("create table %s (a string) partitioned by (b string)", tblName));
        // failure from not having permissions to create table
        assertNoPrivileges(ret);
        allowCreateInDb(dbName, userName, dbLocn);
        driver.run(("use " + dbName));
        ret = driver.run(String.format("create table %s (a string) partitioned by (b string)", tblName));
        TestCase.assertEquals(0, ret.getResponseCode());// now it succeeds.

        Table tbl = msc.getTable(dbName, tblName);
        validateCreateTable(tbl, tblName, dbName);
        String fakeUser = "mal";
        List<String> fakeGroupNames = new ArrayList<String>();
        fakeGroupNames.add("groupygroup");
        InjectableDummyAuthenticator.injectUserName(fakeUser);
        InjectableDummyAuthenticator.injectGroupNames(fakeGroupNames);
        InjectableDummyAuthenticator.injectMode(true);
        allowSelectOnTable(tbl.getTableName(), fakeUser, tbl.getSd().getLocation());
        ret = driver.run(String.format("select * from %s limit 10", tblName));
        TestCase.assertEquals(0, ret.getResponseCode());
        ret = driver.run(String.format("create table %s (a string) partitioned by (b string)", (tblName + "mal")));
        assertNoPrivileges(ret);
        disallowCreateInTbl(tbl.getTableName(), userName, tbl.getSd().getLocation());
        ret = driver.run((("alter table " + tblName) + " add partition (b='2011')"));
        assertNoPrivileges(ret);
        InjectableDummyAuthenticator.injectMode(false);
        allowCreateInTbl(tbl.getTableName(), userName, tbl.getSd().getLocation());
        ret = driver.run((("alter table " + tblName) + " add partition (b='2011')"));
        TestCase.assertEquals(0, ret.getResponseCode());
        allowDropOnTable(tblName, userName, tbl.getSd().getLocation());
        allowDropOnDb(dbName, userName, db.getLocationUri());
        driver.run((("drop database if exists " + (getTestDbName())) + " cascade"));
    }
}
