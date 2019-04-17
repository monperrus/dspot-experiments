/**
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.oauth2.resource;


import HttpHeaders.WWW_AUTHENTICATE;
import SpringBootTest.WebEnvironment;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SampleReactiveOAuth2ResourceServerApplicationTests {
    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer server = new MockWebServer();

    private static final String VALID_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzdWJqZWN0Iiwic2NvcGUiOiJtZXNzYWdlOnJlYWQi" + ((("LCJleHAiOjQ2ODM4MDUxNDF9.h-j6FKRFdnTdmAueTZCdep45e6DPwqM68ZQ8doIJ1exi9YxAlbWzOwId6Bd0L5YmCmp63gGQgsBUBLzwnZQ8kLUgU" + "OBEC3UzSWGRqMskCY9_k9pX0iomX6IfF3N0PaYs0WPC4hO1s8wfZQ-6hKQ4KigFi13G9LMLdH58PRMK0pKEvs3gCbHJuEPw-K5ORlpdnleUTQIwIN") + "afU57cmK3KocTeknPAM_L716sCuSYGvDl6xUTXO7oPdrXhS_EhxLP6KxrpI1uD4Ea_5OWTh7S0Wx5LLDfU6wBG1DowN20d374zepOIEkR-Jnmr_Ql") + "R44vmRqS5ncrF-1R0EGcPX49U6A");

    @Test
    public void getWhenValidTokenShouldBeOk() {
        this.webTestClient.get().uri("/").headers(( headers) -> headers.setBearerAuth(VALID_TOKEN)).exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("Hello, subject!");
    }

    @Test
    public void getWhenNoTokenShouldBeUnauthorized() {
        this.webTestClient.get().uri("/").exchange().expectStatus().isUnauthorized().expectHeader().valueEquals(WWW_AUTHENTICATE, "Bearer");
    }
}
