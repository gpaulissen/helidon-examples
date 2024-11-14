package io.helidon.examples.integrations.oci.genai;

import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ServerTest
public class GenAiServiceTest {

    private static HttpClient client;
    private final URI baseUri;

    public GenAiServiceTest(WebServer server) throws Exception {
        baseUri = URI.create("http://localhost:" + server.port());
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Test
    public void testChatModelAsk() throws Exception {
        String userMessage = "Which are the most used Large Language Models?";
            HttpRequest getChatReq = HttpRequest.newBuilder()
                    .uri(baseUri.resolve("/genai/chat?userMessage="+userMessage))
                    .GET()
                    .build();
            var getBooksRes = client.send(getChatReq, HttpResponse.BodyHandlers.ofString());
            assertTrue(getBooksRes.body().contains("BERT"), "actual: " + getBooksRes.body());
    }

}
