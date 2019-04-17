/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.security.authentication;


import AuthConfig.DRUID_AUTHENTICATION_RESULT;
import HttpServletResponse.SC_UNAUTHORIZED;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.metadata.DefaultPasswordProvider;
import org.apache.druid.security.basic.authentication.BasicHTTPAuthenticator;
import org.apache.druid.security.basic.authentication.db.cache.BasicAuthenticatorCacheManager;
import org.apache.druid.security.basic.authentication.entity.BasicAuthenticatorCredentialUpdate;
import org.apache.druid.security.basic.authentication.entity.BasicAuthenticatorCredentials;
import org.apache.druid.security.basic.authentication.entity.BasicAuthenticatorUser;
import org.apache.druid.server.security.AuthenticationResult;
import org.easymock.EasyMock;
import org.junit.Test;


public class BasicHTTPAuthenticatorTest {
    public static BasicAuthenticatorCredentials USER_A_CREDENTIALS = new BasicAuthenticatorCredentials(new BasicAuthenticatorCredentialUpdate("helloworld", 20));

    public static Provider<BasicAuthenticatorCacheManager> CACHE_MANAGER_PROVIDER = Providers.of(new BasicAuthenticatorCacheManager() {
        @Override
        public void handleAuthenticatorUpdate(String authenticatorPrefix, byte[] serializedUserMap) {
        }

        @Override
        public Map<String, BasicAuthenticatorUser> getUserMap(String authenticatorPrefix) {
            return ImmutableMap.of("userA", new BasicAuthenticatorUser("userA", BasicHTTPAuthenticatorTest.USER_A_CREDENTIALS));
        }
    });

    public static BasicHTTPAuthenticator AUTHENTICATOR = new BasicHTTPAuthenticator(BasicHTTPAuthenticatorTest.CACHE_MANAGER_PROVIDER, "basic", "basic", new DefaultPasswordProvider("a"), new DefaultPasswordProvider("a"), false, null, null);

    @Test
    public void testGoodPassword() throws IOException, ServletException {
        String header = StringUtils.utf8Base64("userA:helloworld");
        header = StringUtils.format("Basic %s", header);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        req.setAttribute(DRUID_AUTHENTICATION_RESULT, new AuthenticationResult("userA", "basic", "basic", null));
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        EasyMock.replay(resp);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        filterChain.doFilter(req, resp);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testBadPassword() throws IOException, ServletException {
        String header = StringUtils.utf8Base64("userA:badpassword");
        header = StringUtils.format("Basic %s", header);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        resp.sendError(SC_UNAUTHORIZED);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(resp);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testUnknownUser() throws IOException, ServletException {
        String header = StringUtils.utf8Base64("userB:helloworld");
        header = StringUtils.format("Basic %s", header);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        resp.sendError(SC_UNAUTHORIZED);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(resp);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testRecognizedButMalformedBasicAuthHeader() throws IOException, ServletException {
        String header = StringUtils.utf8Base64("malformed decoded header data");
        header = StringUtils.format("Basic %s", header);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        resp.sendError(SC_UNAUTHORIZED);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(resp);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testRecognizedButNotBase64BasicAuthHeader() throws IOException, ServletException {
        String header = "Basic this_is_not_base64";
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        resp.sendError(SC_UNAUTHORIZED);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(resp);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testUnrecognizedHeader() throws IOException, ServletException {
        String header = StringUtils.utf8Base64("userA:helloworld");
        header = StringUtils.format("NotBasic %s", header);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(header);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        EasyMock.replay(resp);
        // Authentication filter should move on to the next filter in the chain without sending a response
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        filterChain.doFilter(req, resp);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }

    @Test
    public void testMissingHeader() throws IOException, ServletException {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeader("Authorization")).andReturn(null);
        EasyMock.replay(req);
        HttpServletResponse resp = EasyMock.createMock(HttpServletResponse.class);
        EasyMock.replay(resp);
        // Authentication filter should move on to the next filter in the chain without sending a response
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        filterChain.doFilter(req, resp);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(filterChain);
        Filter authenticatorFilter = BasicHTTPAuthenticatorTest.AUTHENTICATOR.getFilter();
        authenticatorFilter.doFilter(req, resp, filterChain);
        EasyMock.verify(req, resp, filterChain);
    }
}
