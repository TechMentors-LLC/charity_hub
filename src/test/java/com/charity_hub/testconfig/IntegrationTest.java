package com.charity_hub.testconfig;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for integration tests that require MongoDB.
 * This annotation sets up the test with:
 * - Full Spring Boot context
 * - Test profile
 * 
 * Integration tests should extend MongoTestContainerConfig to get MongoDB support.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
public @interface IntegrationTest {
}
