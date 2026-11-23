package com.example.pollai;

import com.example.pollai.utente.Utente;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MainControllerTest {

    private final MainController controller = new MainController();

    @Test
    void returnsViewNamesForStaticPages() {
        assertEquals("homepage", controller.homepage());
        assertEquals("choose-plan", controller.choosePlan());
        assertEquals("login", controller.loginpage());
        assertEquals("talk-to-us", controller.talkToUs());
        assertEquals("our-story", controller.ourStory());
        assertEquals("our-solutions", controller.ourSolutions());
        assertEquals("privacy", controller.privacy());
    }

    @Test
    void registerPageAddsNewUserToModel() {
        Model model = mock(Model.class);

        assertEquals("registeruser", controller.registerPage(model));

        verify(model).addAttribute(eq("user"), any(Utente.class));
    }
}
