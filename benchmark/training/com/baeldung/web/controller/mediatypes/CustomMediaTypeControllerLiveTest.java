package com.baeldung.web.controller.mediatypes;


import ContentType.JSON;
import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class }, loader = AnnotationConfigContextLoader.class)
public class CustomMediaTypeControllerLiveTest {
    private static final String URL_PREFIX = "http://localhost:8082/spring-rest";

    @Test
    public void givenServiceEndpoint_whenGetRequestFirstAPIVersion_thenShouldReturn200() {
        RestAssured.given().accept("application/vnd.baeldung.api.v1+json").when().get(((CustomMediaTypeControllerLiveTest.URL_PREFIX) + "/public/api/items/1")).then().contentType(JSON).and().statusCode(200);
    }

    @Test
    public void givenServiceEndpoint_whenGetRequestSecondAPIVersion_thenShouldReturn200() {
        RestAssured.given().accept("application/vnd.baeldung.api.v2+json").when().get(((CustomMediaTypeControllerLiveTest.URL_PREFIX) + "/public/api/items/2")).then().contentType(JSON).and().statusCode(200);
    }
}
