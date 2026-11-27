package com.example.pollai;

import com.example.pollai.utente.Utente;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.ui.ExtendedModelMap;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationCoverageTest {

    @Test
    void mainPagesReturnTheirTemplates() {
        MainController controller = new MainController();

        assertEquals("homepage", controller.homepage());
        assertEquals("choose-plan", controller.choosePlan());
        assertEquals("login", controller.loginpage());
        assertEquals("talk-to-us", controller.talkToUs());
        assertEquals("our-story", controller.ourStory());
        assertEquals("our-solutions", controller.ourSolutions());
        assertEquals("privacy", controller.privacy());
    }

    @Test
    void registerPageAddsANewUserToTheModel() {
        MainController controller = new MainController();
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("registeruser", controller.registerPage(model));
        assertInstanceOf(Utente.class, model.getAttribute("user"));
    }

    @Test
    void applicationCreatesRestTemplateBean() {
        PollAiApplication application = new PollAiApplication();

        assertNotNull(application.restTemplate());
    }

    @Test
    void servletInitializerAddsApplicationSource() {
        class TestServletInitializer extends ServletInitializer {
            SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
                return configure(builder);
            }
        }

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        assertSame(builder, new TestServletInitializer().configureApplication(builder));
    }
}
