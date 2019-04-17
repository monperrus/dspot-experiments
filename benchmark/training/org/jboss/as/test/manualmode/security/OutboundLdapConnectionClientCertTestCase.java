/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.manualmode.security;


import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.AnnotationUtils;
import org.apache.directory.server.core.annotations.ContextEntry;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreateIndex;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.factory.DSAnnotationProcessor;
import org.apache.directory.server.core.kerberos.KeyDerivationInterceptor;
import org.apache.directory.server.factory.ServerAnnotationProcessor;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.ssl.LdapsInitializer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.protocol.shared.transport.Transport;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.integration.ldap.InMemoryDirectoryServiceFactory;
import org.jboss.as.test.integration.security.common.AbstractSecurityDomainsServerSetupTask;
import org.jboss.as.test.integration.security.common.AbstractSecurityRealmsServerSetupTask;
import org.jboss.as.test.integration.security.common.AbstractSystemPropertiesServerSetupTask;
import org.jboss.as.test.integration.security.common.ManagedCreateLdapServer;
import org.jboss.as.test.integration.security.common.ManagedCreateTransport;
import org.jboss.as.test.integration.security.common.Utils;
import org.jboss.as.test.integration.security.common.config.SecurityDomain;
import org.jboss.as.test.integration.security.common.config.SecurityModule;
import org.jboss.as.test.integration.security.common.config.SecurityModule.Builder;
import org.jboss.as.test.integration.security.common.config.realm.Authentication;
import org.jboss.as.test.integration.security.common.config.realm.Authorization;
import org.jboss.as.test.integration.security.common.config.realm.LdapAuthentication;
import org.jboss.as.test.integration.security.common.config.realm.RealmKeystore;
import org.jboss.as.test.integration.security.common.config.realm.SecurityRealm;
import org.jboss.as.test.integration.security.common.config.realm.ServerIdentity;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * A testcase which tests a SecurityRealm used as a SSL configuration source for LDAPs, asserts that
 * {@code always-send-client-cert} (see <a href="https://issues.jboss.org/browse/WFCORE-2647"></a>) attribute of an outbound
 * LDAP connection works properly.
 * <p>
 * This test uses a simple re-implementation of ApacheDS {@link LdapsInitializer} class, which enables to set our own
 * TrustManager and require the client authentication.<br/>
 * Test scenario:
 * <ol>
 * <li>start container</li>
 * <li>Start LDAP server with LDAPs protocol - use {@link TrustAndStoreTrustManager} as a TrustManager for incoming connections.
 * </li>
 * <li>configure two security realms and two separate LDAP outbound connections for each realm: one of those connections has {@code alwaysSendClientCert(true)} and the other has alwaysSendClientCert(false)</li>
 * <li>configure two security domains which point to the two security realms respectively</li>
 * <li>deploy two web applications, which use the two security domains respectively</li>
 * <li>test access to the web-apps</li>
 * <li>test if the server certificate configured in the security realm was used for client authentication on LDAP server side
 * (use {@link TrustAndStoreTrustManager#isSubjectInClientCertChain(String)})</li>
 * <li>undo the changes</li>
 * </ol>
 *
 * @author Josef Cacek
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class OutboundLdapConnectionClientCertTestCase {
    private static final String KEYSTORE_PASSWORD = "123456";

    private static final String KEYSTORE_FILENAME_LDAPS = "ldaps.keystore";

    private static final String KEYSTORE_FILENAME_JBAS = "jbas.keystore";

    private static final String TRUSTSTORE_FILENAME_JBAS = "jbas.truststore";

    private static final File KEYSTORE_FILE_LDAPS = new File(OutboundLdapConnectionClientCertTestCase.KEYSTORE_FILENAME_LDAPS);

    private static final File KEYSTORE_FILE_JBAS = new File(OutboundLdapConnectionClientCertTestCase.KEYSTORE_FILENAME_JBAS);

    private static final File TRUSTSTORE_FILE_JBAS = new File(OutboundLdapConnectionClientCertTestCase.TRUSTSTORE_FILENAME_JBAS);

    private static final int LDAPS_PORT = 10636;

    private static final String CONTAINER = "default-jbossas";

    private static final String TEST_FILE = "test.txt";

    private static final String TEST_FILE_CONTENT = "OK";

    private static final String LDAPS_AUTHN_REALM_ALWAYS = "ldaps-authn-realm-always";

    private static final String LDAPS_AUTHN_REALM_SOMETIMES = "ldaps-authn-realm-sometimes";

    private static final String LDAPS_AUTHN_SD_ALWAYS = "ldaps-authn-sd-always";

    private static final String LDAPS_AUTHN_SD_SOMETIMES = "ldaps-authn-sd-sometimes";

    private static final String SSL_CONF_REALM = "ssl-conf-realm";

    private static final String LDAPS_CONNECTION_ALWAYS = "test-ldaps-always";

    private static final String LDAPS_CONNECTION_SOMETIMES = "test-ldaps-sometimes";

    private static final String SECURITY_CREDENTIALS = "secret";

    private static final String SECURITY_PRINCIPAL = "uid=admin,ou=system";

    private final OutboundLdapConnectionClientCertTestCase.LDAPServerSetupTask ldapsSetup = new OutboundLdapConnectionClientCertTestCase.LDAPServerSetupTask();

    private final OutboundLdapConnectionClientCertTestCase.SecurityRealmsSetup realmsSetup = new OutboundLdapConnectionClientCertTestCase.SecurityRealmsSetup();

    private final OutboundLdapConnectionClientCertTestCase.SecurityDomainsSetup domainsSetup = new OutboundLdapConnectionClientCertTestCase.SecurityDomainsSetup();

    private final OutboundLdapConnectionClientCertTestCase.SystemPropertiesSetup systemPropertiesSetup = new OutboundLdapConnectionClientCertTestCase.SystemPropertiesSetup();

    private static boolean serverConfigured = false;

    @ArquillianResource
    private static ContainerController containerController;

    @ArquillianResource
    Deployer deployer;

    @Test
    @InSequence(-2)
    public void startContainer() throws Exception {
        OutboundLdapConnectionClientCertTestCase.containerController.start(OutboundLdapConnectionClientCertTestCase.CONTAINER);
    }

    @Test
    @InSequence(2)
    public void stopContainer() throws Exception {
        OutboundLdapConnectionClientCertTestCase.containerController.stop(OutboundLdapConnectionClientCertTestCase.CONTAINER);
    }

    /**
     * A {@link ServerSetupTask} instance which creates security domains for this test case.
     *
     * @author Josef Cacek
     */
    static class SecurityDomainsSetup extends AbstractSecurityDomainsServerSetupTask {
        /**
         * Returns SecurityDomains configuration for this testcase.
         *
         * @see org.jboss.as.test.integration.security.common.AbstractSecurityDomainsServerSetupTask#getSecurityDomains()
         */
        @Override
        protected SecurityDomain[] getSecurityDomains() {
            final Builder realmDirectLMBuilder = new SecurityModule.Builder().name("RealmDirect");
            final SecurityModule mappingModule = new SecurityModule.Builder().name("SimpleRoles").putOption("jduke", "Admin").build();
            final SecurityDomain sdAlways = new SecurityDomain.Builder().name(OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_SD_ALWAYS).loginModules(realmDirectLMBuilder.putOption("realm", OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_REALM_ALWAYS).build()).mappingModules(mappingModule).build();
            final SecurityDomain sdSometimes = new SecurityDomain.Builder().name(OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_SD_SOMETIMES).loginModules(realmDirectLMBuilder.putOption("realm", OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_REALM_SOMETIMES).build()).mappingModules(mappingModule).build();
            return new SecurityDomain[]{ sdAlways, sdSometimes };
        }
    }

    /**
     * A {@link ServerSetupTask} instance which creates security realms for this test case.
     *
     * @author Josef Cacek
     */
    static class SecurityRealmsSetup extends AbstractSecurityRealmsServerSetupTask {
        /**
         * Returns SecurityRealms configuration for this testcase.
         */
        @Override
        protected SecurityRealm[] getSecurityRealms() {
            final RealmKeystore.Builder keyStoreBuilder = new RealmKeystore.Builder().keystorePassword(OutboundLdapConnectionClientCertTestCase.KEYSTORE_PASSWORD);
            final String ldapsUrl = (("ldaps://" + (Utils.getSecondaryTestAddress(managementClient))) + ":") + (OutboundLdapConnectionClientCertTestCase.LDAPS_PORT);
            final SecurityRealm sslConfRealm = new SecurityRealm.Builder().name(OutboundLdapConnectionClientCertTestCase.SSL_CONF_REALM).authentication(new Authentication.Builder().truststore(keyStoreBuilder.keystorePath(OutboundLdapConnectionClientCertTestCase.TRUSTSTORE_FILE_JBAS.getAbsolutePath()).build()).build()).serverIdentity(new ServerIdentity.Builder().ssl(keyStoreBuilder.keystorePath(OutboundLdapConnectionClientCertTestCase.KEYSTORE_FILE_JBAS.getAbsolutePath()).build()).build()).build();
            final SecurityRealm ldapsAuthRealmAlways = new SecurityRealm.Builder().name(OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_REALM_ALWAYS).authentication(new Authentication.Builder().ldap(// ldap authentication
            // ldap-connection
            // shared attributes
            new LdapAuthentication.Builder().connection(OutboundLdapConnectionClientCertTestCase.LDAPS_CONNECTION_ALWAYS).url(ldapsUrl).searchDn(OutboundLdapConnectionClientCertTestCase.SECURITY_PRINCIPAL).searchCredential(OutboundLdapConnectionClientCertTestCase.SECURITY_CREDENTIALS).securityRealm(OutboundLdapConnectionClientCertTestCase.SSL_CONF_REALM).alwaysSendClientCert(true).baseDn("ou=People,dc=jboss,dc=org").recursive(Boolean.TRUE).usernameAttribute("uid").build()).build()).authorization(new Authorization.Builder().path("application-roles.properties").relativeTo("jboss.server.config.dir").build()).build();
            final SecurityRealm ldapsAuthRealmSometimes = new SecurityRealm.Builder().name(OutboundLdapConnectionClientCertTestCase.LDAPS_AUTHN_REALM_SOMETIMES).authentication(new Authentication.Builder().ldap(// ldap authentication
            // ldap-connection
            // shared attributes
            new LdapAuthentication.Builder().connection(OutboundLdapConnectionClientCertTestCase.LDAPS_CONNECTION_SOMETIMES).url(ldapsUrl).searchDn(OutboundLdapConnectionClientCertTestCase.SECURITY_PRINCIPAL).searchCredential(OutboundLdapConnectionClientCertTestCase.SECURITY_CREDENTIALS).securityRealm(OutboundLdapConnectionClientCertTestCase.SSL_CONF_REALM).baseDn("ou=People,dc=jboss,dc=org").recursive(Boolean.TRUE).usernameAttribute("uid").build()).build()).authorization(new Authorization.Builder().path("application-roles.properties").relativeTo("jboss.server.config.dir").build()).build();
            return new SecurityRealm[]{ sslConfRealm, ldapsAuthRealmAlways, ldapsAuthRealmSometimes };
        }
    }

    /**
     * This setup task disables hostname verification truststore file.
     */
    static class SystemPropertiesSetup extends AbstractSystemPropertiesServerSetupTask {
        /**
         *
         *
         * @see org.jboss.as.test.integration.security.common.AbstractSystemPropertiesServerSetupTask#getSystemProperties()
         */
        @Override
        protected SystemProperty[] getSystemProperties() {
            return new SystemProperty[]{ new DefaultSystemProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "") };
        }
    }

    /**
     * A server setup task which configures and starts LDAP server.
     */
    // @formatter:off
    // @formatter:on
    @CreateDS(name = "JBossDS-OutboundLdapConnectionClientCertTestCase", factory = InMemoryDirectoryServiceFactory.class, partitions = { @CreatePartition(name = "jboss", suffix = "dc=jboss,dc=org", contextEntry = @ContextEntry(entryLdif = "dn: dc=jboss,dc=org\n" + (("dc: jboss\n" + "objectClass: top\n") + "objectClass: domain\n\n")), indexes = { @CreateIndex(attribute = "objectClass"), @CreateIndex(attribute = "dc"), @CreateIndex(attribute = "ou") }) }, additionalInterceptors = { KeyDerivationInterceptor.class })
    @CreateLdapServer(transports = { @CreateTransport(protocol = "LDAPS", port = OutboundLdapConnectionClientCertTestCase.LDAPS_PORT, address = "0.0.0.0") }, certificatePassword = OutboundLdapConnectionClientCertTestCase.KEYSTORE_PASSWORD)
    static class LDAPServerSetupTask {
        private DirectoryService directoryService;

        private LdapServer ldapServer;

        private final SslCertChainRecorder recorder = new SslCertChainRecorder();

        /**
         * Creates directory services, starts LDAP server and KDCServer.
         */
        public void startLdapServer() throws Exception {
            LdapsInitializer.setAndLockRecorder(recorder);
            directoryService = DSAnnotationProcessor.getDirectoryService();
            final SchemaManager schemaManager = directoryService.getSchemaManager();
            try (LdifReader ldifReader = new LdifReader(OutboundLdapConnectionClientCertTestCase.class.getResourceAsStream("OutboundLdapConnectionTestCase.ldif"))) {
                for (LdifEntry ldifEntry : ldifReader) {
                    directoryService.getAdminSession().add(new org.apache.directory.api.ldap.model.entry.DefaultEntry(schemaManager, ldifEntry.getEntry()));
                }
            }
            final ManagedCreateLdapServer createLdapServer = new ManagedCreateLdapServer(((CreateLdapServer) (AnnotationUtils.getInstance(CreateLdapServer.class))));
            createLdapServer.setKeyStore(OutboundLdapConnectionClientCertTestCase.KEYSTORE_FILE_LDAPS.getAbsolutePath());
            fixTransportAddress(createLdapServer, StringUtils.strip(TestSuiteEnvironment.getSecondaryTestAddress(false)));
            ldapServer = ServerAnnotationProcessor.instantiateLdapServer(createLdapServer, directoryService);
            /* set setNeedClientAuth(true) and setWantClientAuth(true) manually as there is no way to do this via annotation */
            Transport[] transports = ldapServer.getTransports();
            Assert.assertTrue("The LDAP server configured via annotations should have just one transport", ((transports.length) == 1));
            final TcpTransport transport = ((TcpTransport) (transports[0]));
            transport.setNeedClientAuth(true);
            transport.setWantClientAuth(true);
            TcpTransport newTransport = new InitializedTcpTransport(transport);
            ldapServer.setTransports(newTransport);
            Assert.assertEquals(ldapServer.getCertificatePassword(), OutboundLdapConnectionClientCertTestCase.KEYSTORE_PASSWORD);
            ldapServer.start();
        }

        /**
         * Fixes bind address in the CreateTransport annotation.
         *
         * @param createLdapServer
         * 		
         */
        private void fixTransportAddress(ManagedCreateLdapServer createLdapServer, String address) {
            final CreateTransport[] createTransports = createLdapServer.transports();
            for (int i = 0; i < (createTransports.length); i++) {
                final ManagedCreateTransport mgCreateTransport = new ManagedCreateTransport(createTransports[i]);
                mgCreateTransport.setAddress(address);
                createTransports[i] = mgCreateTransport;
            }
        }

        /**
         * Stops LDAP server and KDCServer and shuts down the directory service.
         */
        public void shutdownLdapServer() throws Exception {
            ldapServer.stop();
            directoryService.shutdown();
            FileUtils.deleteDirectory(directoryService.getInstanceLayout().getInstanceDirectory());
            LdapsInitializer.unsetAndUnlockRecorder();
        }
    }
}
