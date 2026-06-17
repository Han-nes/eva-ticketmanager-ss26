package rest.client.commandHandler;

import core.models.Customer;
import rest.server.createRecords.CreateCustomerRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.LocalDate;
import java.util.List;

public class RestTicketShopCustomerHandler {
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CommunicationHandler communicationHandler;

    public RestTicketShopCustomerHandler(CommunicationHandler communicationHandler, String baseUrl){
        this.baseUrl = baseUrl;
        this.communicationHandler = communicationHandler;
    }

    public List<Customer> getAllCustomers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer"))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Customer createCustomer(String username, String email, LocalDate dateOfBirth) {
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(username, email, dateOfBirth);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer"))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(customerRequest)))
                .header("Content-Type", "application/json")
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public Customer getCustomerById(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer/" + id))
                .GET()
                .build();
        return communicationHandler.send(request, new TypeReference<>() {
        });
    }

    public void updateCustomer(Customer customer) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer"))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(customer)))
                .header("Content-Type", "application/json")
                .build();
        communicationHandler.send(request);
    }

    public void deleteCustomer(long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer/" + id))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }

    public void deleteAllCustomers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/customer"))
                .DELETE()
                .build();
        communicationHandler.send(request);
    }
}
