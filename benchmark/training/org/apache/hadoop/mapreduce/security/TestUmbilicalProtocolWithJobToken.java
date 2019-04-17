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
package org.apache.hadoop.mapreduce.security;


import Client.LOG;
import TaskUmbilicalProtocol.versionID;
import java.net.InetSocketAddress;
import java.security.PrivilegedExceptionAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.mapred.TaskUmbilicalProtocol;
import org.apache.hadoop.mapreduce.security.token.JobTokenIdentifier;
import org.apache.hadoop.mapreduce.security.token.JobTokenSecretManager;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.test.GenericTestUtils;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


/**
 * Unit tests for using Job Token over RPC.
 *
 * System properties required:
 * -Djava.security.krb5.conf=.../hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/target/test-classes/krb5.conf
 * -Djava.net.preferIPv4Stack=true
 */
public class TestUmbilicalProtocolWithJobToken {
    private static final String ADDRESS = "0.0.0.0";

    public static final Logger LOG = LoggerFactory.getLogger(TestUmbilicalProtocolWithJobToken.class);

    private static Configuration conf;

    static {
        TestUmbilicalProtocolWithJobToken.conf = new Configuration();
        TestUmbilicalProtocolWithJobToken.conf.set(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
        UserGroupInformation.setConfiguration(TestUmbilicalProtocolWithJobToken.conf);
    }

    static {
        GenericTestUtils.setLogLevel(Client.LOG, Level.TRACE);
        GenericTestUtils.setLogLevel(Server.LOG, Level.TRACE);
        GenericTestUtils.setLogLevel(SaslRpcClient.LOG, Level.TRACE);
        GenericTestUtils.setLogLevel(SaslRpcServer.LOG, Level.TRACE);
        GenericTestUtils.setLogLevel(SaslInputStream.LOG, Level.TRACE);
    }

    @Test
    public void testJobTokenRpc() throws Exception {
        TaskUmbilicalProtocol mockTT = Mockito.mock(TaskUmbilicalProtocol.class);
        Mockito.doReturn(versionID).when(mockTT).getProtocolVersion(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
        Mockito.doReturn(ProtocolSignature.getProtocolSignature(mockTT, TaskUmbilicalProtocol.class.getName(), versionID, 0)).when(mockTT).getProtocolSignature(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyInt());
        JobTokenSecretManager sm = new JobTokenSecretManager();
        final Server server = setProtocol(TaskUmbilicalProtocol.class).setInstance(mockTT).setBindAddress(TestUmbilicalProtocolWithJobToken.ADDRESS).setPort(0).setNumHandlers(5).setVerbose(true).setSecretManager(sm).build();
        server.start();
        final UserGroupInformation current = UserGroupInformation.getCurrentUser();
        final InetSocketAddress addr = NetUtils.getConnectAddress(server);
        String jobId = current.getUserName();
        JobTokenIdentifier tokenId = new JobTokenIdentifier(new Text(jobId));
        Token<JobTokenIdentifier> token = new Token<JobTokenIdentifier>(tokenId, sm);
        sm.addTokenForJob(jobId, token);
        SecurityUtil.setTokenService(token, addr);
        TestUmbilicalProtocolWithJobToken.LOG.info(("Service address for token is " + (token.getService())));
        current.addToken(token);
        current.doAs(new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                TaskUmbilicalProtocol proxy = null;
                try {
                    proxy = ((TaskUmbilicalProtocol) (RPC.getProxy(TaskUmbilicalProtocol.class, versionID, addr, TestUmbilicalProtocolWithJobToken.conf)));
                    proxy.statusUpdate(null, null);
                } finally {
                    server.stop();
                    if (proxy != null) {
                        RPC.stopProxy(proxy);
                    }
                }
                return null;
            }
        });
    }
}
