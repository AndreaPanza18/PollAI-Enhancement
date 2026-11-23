package com.example.pollai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollAiApplicationTest {

    @Test
    void restTemplateFactoryCreatesRestTemplate() {
        assertInstanceOf(RestTemplate.class, new PollAiApplication().restTemplate());
    }

    @Test
    void mainDelegatesToSpringApplication() {
        try (var springApplication = mockStatic(SpringApplication.class)) {
            PollAiApplication.main(new String[]{"--spring.main.web-application-type=none"});

            springApplication.verify(() -> SpringApplication.run(
                    PollAiApplication.class, "--spring.main.web-application-type=none"));
        }
    }

    @Test
    void servletInitializerRegistersPollAiApplicationSource() {
        ServletInitializer initializer = new ServletInitializer();
        SpringApplicationBuilder builder = new SpringApplicationBuilder()
                .web(WebApplicationType.NONE);

        assertSame(builder, initializer.configure(builder));
    }
}
