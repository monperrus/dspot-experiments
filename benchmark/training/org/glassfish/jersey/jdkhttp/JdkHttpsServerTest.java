/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.jdkhttp;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.URI;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;


/**
 * Jdk Https Server tests.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class JdkHttpsServerTest extends AbstractJdkHttpServerTester {
    private static final String TRUSTSTORE_CLIENT_FILE = "./truststore_client";

    private static final String TRUSTSTORE_CLIENT_PWD = "asdfgh";

    private static final String KEYSTORE_CLIENT_FILE = "./keystore_client";

    private static final String KEYSTORE_CLIENT_PWD = "asdfgh";

    private static final String KEYSTORE_SERVER_FILE = "./keystore_server";

    private static final String KEYSTORE_SERVER_PWD = "asdfgh";

    private static final String TRUSTSTORE_SERVER_FILE = "./truststore_server";

    private static final String TRUSTSTORE_SERVER_PWD = "asdfgh";

    private HttpServer server;

    private final URI httpsUri = UriBuilder.fromUri("https://localhost/").port(getPort()).build();

    private final URI httpUri = UriBuilder.fromUri("http://localhost/").port(getPort()).build();

    private final ResourceConfig rc = new ResourceConfig(JdkHttpsServerTest.TestResource.class);

    @Path("/testHttps")
    public static class TestResource {
        @GET
        public String get() {
            return "test";
        }
    }

    /**
     * Test, that {@link HttpsServer} instance is returned when providing empty SSLContext (but not starting).
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testCreateHttpsServerNoSslContext() throws Exception {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
        MatcherAssert.assertThat(server, CoreMatchers.instanceOf(HttpsServer.class));
    }

    /**
     * Test, that exception is thrown when attempting to start a {@link HttpsServer} with empty SSLContext.
     *
     * @throws Exception
     * 		
     */
    @Test(expected = IllegalArgumentException.class)
    public void testStartHttpServerNoSslContext() throws Exception {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, true);
    }

    /**
     * Test, that {@link javax.net.ssl.SSLHandshakeException} is thrown when attepmting to connect to server with client
     * not configured correctly.
     *
     * @throws Exception
     * 		
     */
    @Test(expected = SSLHandshakeException.class)
    public void testCreateHttpsServerDefaultSslContext() throws Throwable {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, SSLContext.getDefault(), true);
        MatcherAssert.assertThat(server, CoreMatchers.instanceOf(HttpsServer.class));
        // access the https server with not configured client
        final Client client = ClientBuilder.newBuilder().newClient();
        try {
            client.target(httpsUri).path("testHttps").request().get(String.class);
        } catch (final ProcessingException e) {
            throw e.getCause();
        }
    }

    /**
     * Test, that {@link HttpsServer} can be manually started even with (empty) SSLContext, but will throw an exception
     * on request.
     *
     * @throws Exception
     * 		
     */
    @Test(expected = IOException.class)
    public void testHttpsServerNoSslContextDelayedStart() throws Throwable {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
        MatcherAssert.assertThat(server, CoreMatchers.instanceOf(HttpsServer.class));
        server.start();
        final Client client = ClientBuilder.newBuilder().newClient();
        try {
            client.target(httpsUri).path("testHttps").request().get(String.class);
        } catch (final ProcessingException e) {
            throw e.getCause();
        }
    }

    /**
     * Test, that {@link HttpsServer} cannot be configured with {@link HttpsConfigurator} after it has started.
     *
     * @throws Exception
     * 		
     */
    @Test(expected = IllegalStateException.class)
    public void testConfigureSslContextAfterStart() throws Throwable {
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, null, false);
        MatcherAssert.assertThat(server, CoreMatchers.instanceOf(HttpsServer.class));
        server.start();
        ((HttpsServer) (server)).setHttpsConfigurator(new HttpsConfigurator(getServerSslContext()));
    }

    /**
     * Tests a client to server roundtrip with correctly configured SSL on both sides.
     *
     * @throws IOException
     * 		
     */
    @Test
    public void testCreateHttpsServerRoundTrip() throws IOException {
        final SSLContext serverSslContext = getServerSslContext();
        server = JdkHttpServerFactory.createHttpServer(httpsUri, rc, serverSslContext, true);
        final SSLContext foundContext = ((HttpsServer) (server)).getHttpsConfigurator().getSSLContext();
        Assert.assertEquals(serverSslContext, foundContext);
        final SSLContext clientSslContext = getClientSslContext();
        final Client client = ClientBuilder.newBuilder().sslContext(clientSslContext).build();
        final String response = client.target(httpsUri).path("testHttps").request().get(String.class);
        Assert.assertEquals("test", response);
    }

    /**
     * Test, that if URI uses http scheme instead of https, SSLContext is ignored.
     *
     * @throws IOException
     * 		
     */
    @Test
    public void testHttpWithSsl() throws IOException {
        server = JdkHttpServerFactory.createHttpServer(httpUri, rc, getServerSslContext(), true);
        MatcherAssert.assertThat(server, CoreMatchers.instanceOf(HttpServer.class));
        MatcherAssert.assertThat(server, CoreMatchers.not(CoreMatchers.instanceOf(HttpsServer.class)));
    }
}
