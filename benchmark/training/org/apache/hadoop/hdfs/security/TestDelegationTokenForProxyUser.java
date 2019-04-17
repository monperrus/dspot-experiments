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
package org.apache.hadoop.hdfs.security;


import WebHdfsConstants.WEBHDFS_SCHEME;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;
import org.apache.hadoop.hdfs.web.WebHdfsTestUtil;
import org.apache.hadoop.security.TestDoAsEffectiveUser;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.test.Whitebox;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestDelegationTokenForProxyUser {
    private static MiniDFSCluster cluster;

    private static Configuration config;

    private static final String GROUP1_NAME = "group1";

    private static final String GROUP2_NAME = "group2";

    private static final String[] GROUP_NAMES = new String[]{ TestDelegationTokenForProxyUser.GROUP1_NAME, TestDelegationTokenForProxyUser.GROUP2_NAME };

    private static final String REAL_USER = "RealUser";

    private static final String PROXY_USER = "ProxyUser";

    private static UserGroupInformation ugi;

    private static UserGroupInformation proxyUgi;

    private static final Logger LOG = LoggerFactory.getLogger(TestDoAsEffectiveUser.class);

    @Test(timeout = 20000)
    public void testDelegationTokenWithRealUser() throws IOException {
        try {
            Token<?>[] tokens = TestDelegationTokenForProxyUser.proxyUgi.doAs(new PrivilegedExceptionAction<Token<?>[]>() {
                @Override
                public Token<?>[] run() throws IOException {
                    return TestDelegationTokenForProxyUser.cluster.getFileSystem().addDelegationTokens("RenewerUser", null);
                }
            });
            DelegationTokenIdentifier identifier = new DelegationTokenIdentifier();
            byte[] tokenId = tokens[0].getIdentifier();
            identifier.readFields(new DataInputStream(new ByteArrayInputStream(tokenId)));
            Assert.assertEquals(identifier.getUser().getUserName(), TestDelegationTokenForProxyUser.PROXY_USER);
            Assert.assertEquals(identifier.getUser().getRealUser().getUserName(), TestDelegationTokenForProxyUser.REAL_USER);
        } catch (InterruptedException e) {
            // Do Nothing
        }
    }

    @Test(timeout = 5000)
    public void testWebHdfsDoAs() throws Exception {
        WebHdfsTestUtil.LOG.info("START: testWebHdfsDoAs()");
        WebHdfsTestUtil.LOG.info(("ugi.getShortUserName()=" + (TestDelegationTokenForProxyUser.ugi.getShortUserName())));
        final WebHdfsFileSystem webhdfs = WebHdfsTestUtil.getWebHdfsFileSystemAs(TestDelegationTokenForProxyUser.ugi, TestDelegationTokenForProxyUser.config, WEBHDFS_SCHEME);
        final Path root = new Path("/");
        TestDelegationTokenForProxyUser.cluster.getFileSystem().setPermission(root, new FsPermission(((short) (511))));
        Whitebox.setInternalState(webhdfs, "ugi", TestDelegationTokenForProxyUser.proxyUgi);
        {
            Path responsePath = webhdfs.getHomeDirectory();
            WebHdfsTestUtil.LOG.info(("responsePath=" + responsePath));
            Assert.assertEquals((((webhdfs.getUri()) + "/user/") + (TestDelegationTokenForProxyUser.PROXY_USER)), responsePath.toString());
        }
        final Path f = new Path("/testWebHdfsDoAs/a.txt");
        {
            FSDataOutputStream out = webhdfs.create(f);
            out.write("Hello, webhdfs user!".getBytes());
            out.close();
            final FileStatus status = webhdfs.getFileStatus(f);
            WebHdfsTestUtil.LOG.info(("status.getOwner()=" + (status.getOwner())));
            Assert.assertEquals(TestDelegationTokenForProxyUser.PROXY_USER, status.getOwner());
        }
        {
            final FSDataOutputStream out = webhdfs.append(f);
            out.write("\nHello again!".getBytes());
            out.close();
            final FileStatus status = webhdfs.getFileStatus(f);
            WebHdfsTestUtil.LOG.info(("status.getOwner()=" + (status.getOwner())));
            WebHdfsTestUtil.LOG.info(("status.getLen()  =" + (status.getLen())));
            Assert.assertEquals(TestDelegationTokenForProxyUser.PROXY_USER, status.getOwner());
        }
    }
}
