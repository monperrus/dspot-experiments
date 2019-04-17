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
package alluxio.server.auth;


import FileSystemMasterClient.Factory;
import PropertyKey.Name;
import alluxio.ClientContext;
import alluxio.client.file.FileSystemMasterClient;
import alluxio.conf.ServerConfiguration;
import alluxio.exception.status.UnavailableException;
import alluxio.master.MasterClientContext;
import alluxio.security.LoginUserTestUtils;
import alluxio.security.authentication.AuthenticationProvider;
import alluxio.testutils.BaseIntegrationTest;
import alluxio.testutils.LocalAlluxioClusterResource;
import java.net.URLClassLoader;
import javax.security.sasl.AuthenticationException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Though its name indicates that it provides the tests for Alluxio authentication. This class is
 * likely to test four authentication modes: NOSASL, SIMPLE, CUSTOM, KERBEROS.
 */
// TODO(bin): add tests for {@link MultiMasterLocalAlluxioCluster} in fault tolerant mode
// TODO(bin): improve the way to set and isolate MasterContext/WorkerContext across test cases
public final class MasterClientAuthenticationIntegrationTest extends BaseIntegrationTest {
    @Rule
    public LocalAlluxioClusterResource mLocalAlluxioClusterResource = new LocalAlluxioClusterResource.Builder().build();

    @Rule
    public ExpectedException mThrown = ExpectedException.none();

    @Test
    @LocalAlluxioClusterResource.Config(confParams = { Name.SECURITY_AUTHENTICATION_TYPE, "NOSASL", Name.SECURITY_AUTHORIZATION_PERMISSION_ENABLED, "false" })
    public void noAuthenticationOpenClose() throws Exception {
        authenticationOperationTest("/file-nosasl");
    }

    @Test
    @LocalAlluxioClusterResource.Config(confParams = { Name.SECURITY_AUTHENTICATION_TYPE, "SIMPLE" })
    public void simpleAuthenticationOpenClose() throws Exception {
        authenticationOperationTest("/file-simple");
    }

    @Test
    @LocalAlluxioClusterResource.Config(confParams = { Name.SECURITY_AUTHENTICATION_TYPE, "CUSTOM", Name.SECURITY_AUTHENTICATION_CUSTOM_PROVIDER_CLASS, MasterClientAuthenticationIntegrationTest.NameMatchAuthenticationProvider.FULL_CLASS_NAME, Name.SECURITY_LOGIN_USERNAME, "alluxio" })
    public void customAuthenticationOpenClose() throws Exception {
        authenticationOperationTest("/file-custom");
    }

    @Test
    @LocalAlluxioClusterResource.Config(confParams = { Name.SECURITY_AUTHENTICATION_TYPE, "CUSTOM", Name.SECURITY_AUTHENTICATION_CUSTOM_PROVIDER_CLASS, MasterClientAuthenticationIntegrationTest.NameMatchAuthenticationProvider.FULL_CLASS_NAME, Name.SECURITY_LOGIN_USERNAME, "alluxio" })
    public void customAuthenticationDenyConnect() throws Exception {
        try (FileSystemMasterClient masterClient = Factory.create(MasterClientContext.newBuilder(ClientContext.create(ServerConfiguration.global())).build())) {
            Assert.assertFalse(masterClient.isConnected());
            // Using no-alluxio as loginUser to connect to Master, the IOException will be thrown
            LoginUserTestUtils.resetLoginUser("no-alluxio");
            mThrown.expect(UnavailableException.class);
            masterClient.connect();
        }
    }

    @Test
    @LocalAlluxioClusterResource.Config(confParams = { Name.SECURITY_AUTHENTICATION_TYPE, "SIMPLE" })
    public void simpleAuthenticationIsolatedClassLoader() throws Exception {
        FileSystemMasterClient masterClient = Factory.create(MasterClientContext.newBuilder(ClientContext.create(ServerConfiguration.global())).build());
        Assert.assertFalse(masterClient.isConnected());
        // Get the current context class loader to retrieve the classpath URLs.
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertTrue((contextClassLoader instanceof URLClassLoader));
        // Set the context class loader to an isolated class loader.
        ClassLoader isolatedClassLoader = new URLClassLoader(((URLClassLoader) (contextClassLoader)).getURLs(), null);
        Thread.currentThread().setContextClassLoader(isolatedClassLoader);
        try {
            masterClient.connect();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        Assert.assertTrue(masterClient.isConnected());
        masterClient.close();
    }

    /**
     * An authentication provider for {@link AuthType#CUSTOM}.
     */
    public static class NameMatchAuthenticationProvider implements AuthenticationProvider {
        // The fullly qualified class name of this authentication provider. This is needed to configure
        // the alluxio cluster
        public static final String FULL_CLASS_NAME = "alluxio.server.auth.MasterClientAuthenticationIntegrationTest$" + "NameMatchAuthenticationProvider";

        @Override
        public void authenticate(String user, String password) throws AuthenticationException {
            if (!(user.equals("alluxio"))) {
                throw new AuthenticationException("Only allow the user alluxio to connect");
            }
        }
    }
}
