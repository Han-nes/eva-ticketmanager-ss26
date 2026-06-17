package rest.client.commandHandler;

import core.models.Event;
import rest.server.createRecords.CreateEventRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.List;

public class RestTicketShopEventHandler {
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CommunicationHandler communicationHandler;

    public RestTicketShopEventHandler(CommunicationHandler communicationHandler, String baseUrl){
        this.baseUrl = baseUrl;
        this.communicationHandler = communicationHandler;
    }

    public List<Event> getAllEvents() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event"))
                .GET()
                .header("Content-Type", "application/json")
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Event createEvent(String name, String location, LocalDateTime time, int ticketsAvailable) {
        CreateEventRequest eventRequest = new CreateEventRequest(name, location, time, ticketsAvailable);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event"))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(eventRequest)))
                .header("Content-Type", "application/json")
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Event getEventById(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event/" + id))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public void updateEvent(Event event) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event"))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(event)))
                .header("Content-Type", "application/json")
                .build();
        communicationHandler.send(request);
    }

    public void deleteEvent(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event/" + id))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }

    public void deleteAllEvents() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/event"))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }
}
