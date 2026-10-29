package dev.frizio.lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JavaArconiaLabApplicationTests {

    @Value("${local.server.port}")
    private int port;

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    void booksCanBeCreatedReadUpdatedAndDeleted() throws Exception {
        String title = "Book " + UUID.randomUUID();
        String author = "Author " + UUID.randomUUID();

        HttpResponse<String> createResponse = send("POST", "/books",
            "{\"title\":\"" + title + "\",\"author\":\"" + author + "\"}");
        assertEquals(201, createResponse.statusCode());

        Matcher idMatcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(createResponse.body());
        assertTrue(idMatcher.find());
        long id = Long.parseLong(idMatcher.group(1));
        assertTrue(createResponse.headers().firstValue("Location").orElseThrow().endsWith("/books/" + id));
        assertTrue(createResponse.body().contains("\"title\":\"" + title + "\""));

        HttpResponse<String> readResponse = send("GET", "/books/" + id, null);
        assertEquals(200, readResponse.statusCode());
        assertTrue(readResponse.body().contains("\"author\":\"" + author + "\""));

        HttpResponse<String> listResponse = send("GET", "/books", null);
        assertEquals(200, listResponse.statusCode());
        assertTrue(listResponse.body().contains("\"id\":" + id));

        String updatedTitle = title + " updated";
        HttpResponse<String> updateResponse = send("PUT", "/books/" + id,
            "{\"title\":\"" + updatedTitle + "\",\"author\":\"" + author + "\"}");
        assertEquals(200, updateResponse.statusCode());
        assertTrue(updateResponse.body().contains("\"title\":\"" + updatedTitle + "\""));

        HttpResponse<String> deleteResponse = send("DELETE", "/books/" + id, null);
        assertEquals(204, deleteResponse.statusCode());

        assertEquals(404, send("GET", "/books/" + id, null).statusCode());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher bodyPublisher = body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .method(method, bodyPublisher)
            .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
