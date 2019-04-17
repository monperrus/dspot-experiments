package com.baeldung.spring.data.reactive.redis.template;


import SpringBootTest.WebEnvironment;
import com.baeldung.spring.data.reactive.redis.SpringRedisReactiveApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SpringRedisReactiveApplication.class)
public class RedisTemplateListOpsIntegrationTest {
    private static final String LIST_NAME = "demo_list";

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private ReactiveListOperations<String, String> reactiveListOps;

    @Test
    public void givenListAndValues_whenLeftPushAndLeftPop_thenLeftPushAndLeftPop() {
        Mono<Long> lPush = reactiveListOps.leftPushAll(RedisTemplateListOpsIntegrationTest.LIST_NAME, "first", "second").log("Pushed");
        StepVerifier.create(lPush).expectNext(2L).verifyComplete();
        Mono<String> lPop = reactiveListOps.leftPop(RedisTemplateListOpsIntegrationTest.LIST_NAME).log("Popped");
        StepVerifier.create(lPop).expectNext("second").verifyComplete();
    }
}
