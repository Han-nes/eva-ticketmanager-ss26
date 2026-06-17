package rest.client.commandHandler;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.NoSuchElementException;

public class CommunicationHandler {
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public CommunicationHandler(HttpClient httpClient){
        this.httpClient = httpClient;
    }

    public <T> T send(HttpRequest request, TypeReference<T> type) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkResponseStatus(response);
            return this.mapper.readValue(response.body(), type);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    public void send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkResponseStatus(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    public void checkResponseStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == 404) {
            throw new NoSuchElementException("Resource not found");
        } else if (status == 422) {
            throw new RuntimeException("Operation not allowed: " + response.body());
        } else if (status >= 400) {
            throw new RuntimeException("Request failed with status " + status + ": " + response.body());
        }
    }
}
