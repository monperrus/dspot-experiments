/**
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.microprofile.jwt.auth;


import EndpointConfig.AnnotationScope.APPLICATION;
import JwkEC.ALG_ES256;
import JwkOctet.ALG_HS256;
import JwkRSA.ALG_RS256;
import SecurityResponse.SecurityStatus.FAILURE;
import io.helidon.common.CollectionsHelper;
import io.helidon.common.OptionalHelper;
import io.helidon.config.Config;
import io.helidon.security.AuthenticationResponse;
import io.helidon.security.EndpointConfig;
import io.helidon.security.OutboundSecurityResponse;
import io.helidon.security.Principal;
import io.helidon.security.ProviderRequest;
import io.helidon.security.Role;
import io.helidon.security.SecurityContext;
import io.helidon.security.SecurityEnvironment;
import io.helidon.security.Subject;
import io.helidon.security.jwt.Jwt;
import io.helidon.security.jwt.SignedJwt;
import io.helidon.security.jwt.jwk.JwkKeys;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import org.eclipse.microprofile.auth.LoginConfig;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static JwtAuthAnnotationAnalyzer.LOGIN_CONFIG_METHOD;


/**
 * Unit test for {@link JwtAuthProvider}.
 */
public class JwtAuthProviderTest {
    private static final String WRONG_TOKEN = "yJ4NXQjUzI1NiI6IlZjeXl1TVdxSGp4UjRVNmYzOTV3YmhUZXNZRmFaWXFSbDdBbUxjZE5sNXciLCJ4NXQiOiJTdEZFTlFaM2NMNndQaHFxODZnVmJTTG54TkUiLCJraWQiOiJTSUdOSU5HX0tFWSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJIU01BcHAtY2xpZW50X0FQUElEIiwidXNlci50ZW5hbnQubmFtZSI6ImlkY3MtNzNmYTNlZDY5ZTgxNDFhN2I5MDFmYWY3Zjg3M2U3OGUiLCJzdWJfbWFwcGluZ2F0dHIiOiJ1c2VyTmFtZSIsImlzcyI6Imh0dHBzOlwvXC9pZGVudGl0eS5vcmFjbGVjbG91ZC5jb21cLyIsInRva190eXBlIjoiQVQiLCJjbGllbnRfaWQiOiJIU01BcHAtY2xpZW50X0FQUElEIiwiYXVkIjoiaHR0cDpcL1wvc2NhMDBjangudXMub3JhY2xlLmNvbTo3Nzc3Iiwic3ViX3R5cGUiOiJjbGllbnQiLCJzY29wZSI6InVybjpvcGM6cmVzb3VyY2U6Y29uc3VtZXI6OmFsbCIsImNsaWVudF90ZW5hbnRuYW1lIjoiaWRjcy03M2ZhM2VkNjllODE0MWE3YjkwMWZhZjdmODczZTc4ZSIsImV4cCI6MTU1MDU5NTk0MiwiaWF0IjoxNTUwNTA5NTQyLCJ0ZW5hbnRfaXNzIjoiaHR0cHM6XC9cL2lkY3MtNzNmYTNlZDY5ZTgxNDFhN2I5MDFmYWY3Zjg3M2U3OGUuaWRlbnRpdHkuYzlkZXYxLm9jOXFhZGV2LmNvbSIsImNsaWVudF9ndWlkIjoiN2JmZDM3MjM1ZGY3NDVjNDg5ZjYxZDM1ZTYzZGQ4ZmUiLCJjbGllbnRfbmFtZSI6IkhTTUFwcC1jbGllbnQiLCJ0ZW5hbnQiOiJpZGNzLTczZmEzZWQ2OWU4MTQxYTdiOTAxZmFmN2Y4NzNlNzhlIiwianRpIjoiYzRkNjlhZjUtOGQ4OC00N2Q2LTkzMDctN2RjMmI3NWY4MDQyIn0.ZsngUzzso_sW6rMg3jB-lueiC2sknIDRlgvjumMjp5rRSdLux2X4XZIm2Oa15JbcrnC6I4sgqB0xU1Wte-TW4hbBDLFhaJKYKiNaHBE0L7J73ZK7ITg7dORKkyjLrofGt0m8Rse1OlE9AWevz-l27gtQMO_mctGfHri2BxiMbSN1HwOjWW3kGoqPgCJZJfh2TiFlocEpsXDH4qB1qwhuIoT91gw3kIJlQov0_a9uGEepMU_RWMRjVZCIvuV2hPq_mdeWy2IhkHPxq422CLZ9MDOfbv8F6dY6DralCH4mmKbGM3dbqpZokWQxXG7LG9vWX1PFWw0N9clYHJ4QqBJ4pA";

    private static JwkKeys verifyKeys;

    @Test
    public void testWrongToken() {
        JwtAuthProvider provider = JwtAuthProvider.create(Config.create().get("security.providers.0.mp-jwt-auth"));
        // now we need to use the same token to invoke authentication
        ProviderRequest atnRequest = Mockito.mock(ProviderRequest.class);
        SecurityEnvironment se = SecurityEnvironment.builder().header("Authorization", ("bearer " + (JwtAuthProviderTest.WRONG_TOKEN))).build();
        EndpointConfig ec = Mockito.mock(EndpointConfig.class);
        Mockito.when(ec.combineAnnotations(LoginConfig.class, APPLICATION)).thenReturn(listOf(new LoginConfig() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return LoginConfig.class;
            }

            @Override
            public String authMethod() {
                return LOGIN_CONFIG_METHOD;
            }

            @Override
            public String realmName() {
                return "helidon-realm";
            }
        }));
        Mockito.when(atnRequest.env()).thenReturn(se);
        Mockito.when(atnRequest.endpointConfig()).thenReturn(ec);
        AuthenticationResponse authenticationResponse = provider.syncAuthenticate(atnRequest);
        MatcherAssert.assertThat(authenticationResponse.service(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(authenticationResponse.user(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(authenticationResponse.status(), CoreMatchers.is(FAILURE));
    }

    @Test
    public void testEcBothWays() {
        String username = "user1";
        String userId = "user1-id";
        String email = "user1@example.org";
        String familyName = "Novak";
        String givenName = "Standa";
        String fullName = "Standa Novak";
        Locale locale = Locale.CANADA_FRENCH;
        Principal principal = Principal.builder().name(username).id(userId).addAttribute("email", email).addAttribute("email_verified", true).addAttribute("family_name", familyName).addAttribute("given_name", givenName).addAttribute("full_name", fullName).addAttribute("locale", locale).addAttribute("roles", CollectionsHelper.setOf("role1", "role2")).build();
        Subject subject = Subject.builder().principal(principal).addGrant(Role.create("group1")).addGrant(Role.create("group2")).addGrant(Role.create("group3")).build();
        JwtAuthProvider provider = JwtAuthProvider.create(Config.create().get("security.providers.0.mp-jwt-auth"));
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.user()).thenReturn(Optional.of(subject));
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        SecurityEnvironment outboundEnv = SecurityEnvironment.builder().path("/ec").transport("http").targetUri(URI.create("http://localhost:8080/ec")).build();
        EndpointConfig outboundEp = EndpointConfig.create();
        MatcherAssert.assertThat(provider.isOutboundSupported(request, outboundEnv, outboundEp), CoreMatchers.is(true));
        OutboundSecurityResponse response = provider.syncOutbound(request, outboundEnv, outboundEp);
        String signedToken = response.requestHeaders().get("Authorization").get(0);
        signedToken = signedToken.substring("bearer ".length());
        // now I want to validate it to prove it was correctly signed
        SignedJwt signedJwt = SignedJwt.parseToken(signedToken);
        signedJwt.verifySignature(JwtAuthProviderTest.verifyKeys).checkValid();
        Jwt jwt = signedJwt.getJwt();
        // MP specific additions
        MatcherAssert.assertThat(jwt.payloadClaim("upn"), CoreMatchers.not(Optional.empty()));
        MatcherAssert.assertThat(jwt.payloadClaim("groups"), CoreMatchers.not(Optional.empty()));
        MatcherAssert.assertThat(jwt.userPrincipal(), CoreMatchers.is(Optional.of(username)));
        MatcherAssert.assertThat(jwt.userGroups(), CoreMatchers.not(Optional.empty()));
        MatcherAssert.assertThat(jwt.userGroups().get(), CoreMatchers.hasItems("group1", "group2", "group3"));
        // End of MP specific additions
        MatcherAssert.assertThat(jwt.subject(), CoreMatchers.is(Optional.of(userId)));
        MatcherAssert.assertThat(jwt.preferredUsername(), CoreMatchers.is(Optional.of(username)));
        MatcherAssert.assertThat(jwt.email(), CoreMatchers.is(Optional.of(email)));
        MatcherAssert.assertThat(jwt.emailVerified(), CoreMatchers.is(Optional.of(true)));
        MatcherAssert.assertThat(jwt.familyName(), CoreMatchers.is(Optional.of(familyName)));
        MatcherAssert.assertThat(jwt.givenName(), CoreMatchers.is(Optional.of(givenName)));
        MatcherAssert.assertThat(jwt.fullName(), CoreMatchers.is(Optional.of(fullName)));
        MatcherAssert.assertThat(jwt.locale(), CoreMatchers.is(Optional.of(locale)));
        MatcherAssert.assertThat(jwt.audience(), CoreMatchers.is(Optional.of(listOf("audience.application.id"))));
        MatcherAssert.assertThat(jwt.issuer(), CoreMatchers.is(Optional.of("jwt.example.com")));
        MatcherAssert.assertThat(jwt.algorithm(), CoreMatchers.is(Optional.of(ALG_ES256)));
        Instant instant = jwt.issueTime().get();
        boolean compareResult = (Instant.now().minusSeconds(10).compareTo(instant)) < 0;
        MatcherAssert.assertThat("Issue time must not be older than 10 seconds", compareResult, CoreMatchers.is(true));
        Instant expectedNotBefore = instant.minus(5, ChronoUnit.SECONDS);
        MatcherAssert.assertThat(jwt.notBefore(), CoreMatchers.is(Optional.of(expectedNotBefore)));
        Instant expectedExpiry = instant.plus(((60 * 60) * 24), ChronoUnit.SECONDS);
        MatcherAssert.assertThat(jwt.expirationTime(), CoreMatchers.is(Optional.of(expectedExpiry)));
        // now we need to use the same token to invoke authentication
        ProviderRequest atnRequest = mockRequest(signedToken);
        AuthenticationResponse authenticationResponse = provider.syncAuthenticate(atnRequest);
        OptionalHelper.from(authenticationResponse.user().map(Subject::principal)).ifPresentOrElse(( atnPrincipal) -> {
            assertThat(atnPrincipal, instanceOf(.class));
            JsonWebTokenImpl jsonWebToken = ((JsonWebTokenImpl) (atnPrincipal));
            String upn = jsonWebToken.getClaim(Claims.upn.name());
            assertThat(upn, is(username));
            assertThat(atnPrincipal.id(), is(userId));
            assertThat(atnPrincipal.getName(), is(username));
            assertThat(atnPrincipal.abacAttribute("email"), is(Optional.of(email)));
            assertThat(atnPrincipal.abacAttribute("email_verified"), is(Optional.of(true)));
            assertThat(atnPrincipal.abacAttribute("family_name"), is(Optional.of(familyName)));
            assertThat(atnPrincipal.abacAttribute("given_name"), is(Optional.of(givenName)));
            assertThat(atnPrincipal.abacAttribute("full_name"), is(Optional.of(fullName)));
            assertThat(atnPrincipal.abacAttribute("locale"), is(Optional.of(locale)));
        }, () -> fail("User must be present in response"));
    }

    @Test
    public void testOctBothWays() {
        String userId = "user1-id";
        Principal tp = Principal.create(userId);
        Subject subject = Subject.create(tp);
        JwtAuthProvider provider = JwtAuthProvider.create(Config.create().get("security.providers.0.mp-jwt-auth"));
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.user()).thenReturn(Optional.of(subject));
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        SecurityEnvironment outboundEnv = SecurityEnvironment.builder().path("/oct").transport("http").targetUri(URI.create("http://localhost:8080/oct")).build();
        EndpointConfig outboundEp = EndpointConfig.create();
        MatcherAssert.assertThat(provider.isOutboundSupported(request, outboundEnv, outboundEp), CoreMatchers.is(true));
        OutboundSecurityResponse response = provider.syncOutbound(request, outboundEnv, outboundEp);
        String signedToken = response.requestHeaders().get("Authorization").get(0);
        signedToken = signedToken.substring("bearer ".length());
        // now I want to validate it to prove it was correctly signed
        SignedJwt signedJwt = SignedJwt.parseToken(signedToken);
        signedJwt.verifySignature(JwtAuthProviderTest.verifyKeys).checkValid();
        Jwt jwt = signedJwt.getJwt();
        MatcherAssert.assertThat(jwt.subject(), CoreMatchers.is(Optional.of(userId)));
        MatcherAssert.assertThat(jwt.preferredUsername(), CoreMatchers.is(Optional.of(userId)));
        MatcherAssert.assertThat(jwt.email(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(jwt.emailVerified(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(jwt.familyName(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(jwt.givenName(), CoreMatchers.is(Optional.empty()));
        // stored as "name" attribute on principal, full name is stored as "name" in JWT
        MatcherAssert.assertThat(jwt.fullName(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(jwt.locale(), CoreMatchers.is(Optional.empty()));
        MatcherAssert.assertThat(jwt.audience(), CoreMatchers.is(Optional.of(listOf("audience.application.id"))));
        MatcherAssert.assertThat(jwt.issuer(), CoreMatchers.is(Optional.of("jwt.example.com")));
        MatcherAssert.assertThat(jwt.algorithm(), CoreMatchers.is(Optional.of(ALG_HS256)));
        Instant instant = jwt.issueTime().get();
        boolean compareResult = (Instant.now().minusSeconds(10).compareTo(instant)) < 0;
        MatcherAssert.assertThat("Issue time must not be older than 10 seconds", compareResult, CoreMatchers.is(true));
        Instant expectedNotBefore = instant.minus(5, ChronoUnit.SECONDS);
        MatcherAssert.assertThat(jwt.notBefore(), CoreMatchers.is(Optional.of(expectedNotBefore)));
        Instant expectedExpiry = instant.plus(((60 * 60) * 24), ChronoUnit.SECONDS);
        MatcherAssert.assertThat(jwt.expirationTime(), CoreMatchers.is(Optional.of(expectedExpiry)));
        // now we need to use the same token to invoke authentication
        ProviderRequest atnRequest = mockRequest(signedToken);
        AuthenticationResponse authenticationResponse = provider.syncAuthenticate(atnRequest);
        OptionalHelper.from(authenticationResponse.user().map(Subject::principal)).ifPresentOrElse(( atnPrincipal) -> {
            assertThat(atnPrincipal.id(), is(userId));
            assertThat(atnPrincipal.getName(), is(userId));
            assertThat(atnPrincipal.abacAttribute("email"), is(Optional.empty()));
            assertThat(atnPrincipal.abacAttribute("email_verified"), is(Optional.empty()));
            assertThat(atnPrincipal.abacAttribute("family_name"), is(Optional.empty()));
            assertThat(atnPrincipal.abacAttribute("given_name"), is(Optional.empty()));
            assertThat(atnPrincipal.abacAttribute("full_name"), is(Optional.empty()));
            assertThat(atnPrincipal.abacAttribute("locale"), is(Optional.empty()));
        }, () -> fail("User must be present in response"));
    }

    @Test
    public void testRsaBothWays() {
        String username = "user1";
        String userId = "user1-id";
        String email = "user1@example.org";
        String familyName = "Novak";
        String givenName = "Standa";
        String fullName = "Standa Novak";
        Locale locale = Locale.CANADA_FRENCH;
        Principal principal = Principal.builder().name(username).id(userId).addAttribute("email", email).addAttribute("email_verified", true).addAttribute("family_name", familyName).addAttribute("given_name", givenName).addAttribute("full_name", fullName).addAttribute("locale", locale).build();
        Subject subject = Subject.create(principal);
        JwtAuthProvider provider = JwtAuthProvider.create(Config.create().get("security.providers.0.mp-jwt-auth"));
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.user()).thenReturn(Optional.of(subject));
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        SecurityEnvironment outboundEnv = SecurityEnvironment.builder().path("/rsa").transport("http").targetUri(URI.create("http://localhost:8080/rsa")).build();
        EndpointConfig outboundEp = EndpointConfig.create();
        MatcherAssert.assertThat(provider.isOutboundSupported(request, outboundEnv, outboundEp), CoreMatchers.is(true));
        OutboundSecurityResponse response = provider.syncOutbound(request, outboundEnv, outboundEp);
        String signedToken = response.requestHeaders().get("Authorization").get(0);
        signedToken = signedToken.substring("bearer ".length());
        // now I want to validate it to prove it was correctly signed
        SignedJwt signedJwt = SignedJwt.parseToken(signedToken);
        signedJwt.verifySignature(JwtAuthProviderTest.verifyKeys).checkValid();
        Jwt jwt = signedJwt.getJwt();
        MatcherAssert.assertThat(jwt.subject(), CoreMatchers.is(Optional.of(userId)));
        MatcherAssert.assertThat(jwt.preferredUsername(), CoreMatchers.is(Optional.of(username)));
        MatcherAssert.assertThat(jwt.email(), CoreMatchers.is(Optional.of(email)));
        MatcherAssert.assertThat(jwt.emailVerified(), CoreMatchers.is(Optional.of(true)));
        MatcherAssert.assertThat(jwt.familyName(), CoreMatchers.is(Optional.of(familyName)));
        MatcherAssert.assertThat(jwt.givenName(), CoreMatchers.is(Optional.of(givenName)));
        MatcherAssert.assertThat(jwt.fullName(), CoreMatchers.is(Optional.of(fullName)));
        MatcherAssert.assertThat(jwt.locale(), CoreMatchers.is(Optional.of(locale)));
        MatcherAssert.assertThat(jwt.audience(), CoreMatchers.is(Optional.of(listOf("audience.application.id"))));
        MatcherAssert.assertThat(jwt.issuer(), CoreMatchers.is(Optional.of("jwt.example.com")));
        MatcherAssert.assertThat(jwt.algorithm(), CoreMatchers.is(Optional.of(ALG_RS256)));
        MatcherAssert.assertThat(jwt.issueTime(), CoreMatchers.is(CoreMatchers.not(Optional.empty())));
        jwt.issueTime().ifPresent(( instant) -> {
            boolean compareResult = (Instant.now().minusSeconds(10).compareTo(instant)) < 0;
            assertThat("Issue time must not be older than 10 seconds", compareResult, is(true));
            Instant expectedNotBefore = instant.minus(60, ChronoUnit.SECONDS);
            assertThat(jwt.notBefore(), is(Optional.of(expectedNotBefore)));
            Instant expectedExpiry = instant.plus(3600, ChronoUnit.SECONDS);
            assertThat(jwt.expirationTime(), is(Optional.of(expectedExpiry)));
        });
        // now we need to use the same token to invoke authentication
        ProviderRequest atnRequest = mockRequest(signedToken);
        AuthenticationResponse authenticationResponse = provider.syncAuthenticate(atnRequest);
        OptionalHelper.from(authenticationResponse.user().map(Subject::principal)).ifPresentOrElse(( atnPrincipal) -> {
            assertThat(atnPrincipal.id(), is(userId));
            assertThat(atnPrincipal.getName(), is(username));
            assertThat(atnPrincipal.abacAttribute("email"), is(Optional.of(email)));
            assertThat(atnPrincipal.abacAttribute("email_verified"), is(Optional.of(true)));
            assertThat(atnPrincipal.abacAttribute("family_name"), is(Optional.of(familyName)));
            assertThat(atnPrincipal.abacAttribute("given_name"), is(Optional.of(givenName)));
            assertThat(atnPrincipal.abacAttribute("full_name"), is(Optional.of(fullName)));
            assertThat(atnPrincipal.abacAttribute("locale"), is(Optional.of(locale)));
        }, () -> fail("User must be present in response"));
    }
}
