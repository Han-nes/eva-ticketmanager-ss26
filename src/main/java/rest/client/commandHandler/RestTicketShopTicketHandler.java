package rest.client.commandHandler;

import core.models.Ticket;
import rest.server.createRecords.CreateTicketRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

public class RestTicketShopTicketHandler {
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CommunicationHandler communicationHandler;

    public RestTicketShopTicketHandler(CommunicationHandler communicationHandler, String baseUrl){
        this.baseUrl = baseUrl;
        this.communicationHandler = communicationHandler;
    }

    public List<Ticket> getAllTickets() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket"))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Ticket createTicket(long customerId, long eventId) {
        CreateTicketRequest ticketRequest = new CreateTicketRequest(customerId, eventId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket"))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ticketRequest)))
                .header("Content-Type", "application/json")
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Ticket getTicketById(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket" + id))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public void deleteTicket(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket" + id))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }

    public void deleteAllTickets() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket"))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }

    public boolean verifyTicket(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/ticket/validate/" + id))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }
}
