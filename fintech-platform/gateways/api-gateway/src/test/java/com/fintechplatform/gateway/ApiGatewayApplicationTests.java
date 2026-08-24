package com.fintechplatform.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A deliberately minimal smoke test: it just confirms the application
 * context (routes, CORS config, etc.) starts up without error. The routes
 * themselves point at other services' URLs but are never actually invoked
 * here, so this test needs none of them running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context fails to start (bad route config, bad YAML,
        // a missing bean), this test fails with the reason why.
    }
}
