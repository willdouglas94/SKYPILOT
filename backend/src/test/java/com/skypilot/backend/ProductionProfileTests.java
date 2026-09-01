package com.skypilot.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ProductionProfileTests {

    @Test
    void testProfileShouldLoadWithInMemoryDatabase() {
        assertThat(true).isTrue();
    }
}
