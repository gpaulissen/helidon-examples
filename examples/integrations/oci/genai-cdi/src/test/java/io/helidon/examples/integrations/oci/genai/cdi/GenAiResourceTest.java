package io.helidon.examples.integrations.oci.genai.cdi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;

@HelidonTest
public class GenAiResourceTest {

    private static final String APP_URL = "/genai/";

    @Inject
    private WebTarget target;

    private static String appUrl(String path) {
        return APP_URL + path;
    }

    @Test
    public void testChatModelAsk() {
        String answer = target.path(appUrl("chat"))
                .queryParam("userMessage", "Which are the most used Large Language Models?")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertTrue(answer.contains("BERT"), "actual: " + answer);
    }

}
