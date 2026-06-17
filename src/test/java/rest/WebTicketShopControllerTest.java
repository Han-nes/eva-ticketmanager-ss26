package rest;

import rest.server.createRecords.CreateCustomerRequest;
import rest.server.createRecords.CreateEventRequest;
import rest.server.createRecords.CreateTicketRequest;
import rest.server.WebTicketShopService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = WebTicketShopService.class)
@AutoConfigureMockMvc
class WebTicketShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper methods to reduce boilerplate in tests
    private String createEventJson(String name, String location, LocalDateTime time, int tickets){
        return objectMapper.writeValueAsString(
            new CreateEventRequest(name, location, time, tickets)
        );
    }

    private String createCustomerJson(String username, String email, LocalDate dob){
        return objectMapper.writeValueAsString(
            new CreateCustomerRequest(username, email, dob)
        );
    }

    private String performCreateEvent(String name, String location, LocalDateTime time, int tickets) throws Exception {
        return mockMvc.perform(post("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createEventJson(name, location, time, tickets)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    private String performCreateCustomer(String username, String email, LocalDate dob) throws Exception {
        return mockMvc.perform(post("/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createCustomerJson(username, email, dob)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    @Nested
    @DisplayName("Unknown Endpoints")
    class BadEndpointsTests {
        @Test
        @DisplayName("GET /unknown should return 404")
        void shouldReturn404ForUnknownEndpoint() throws Exception {
            mockMvc.perform(get("/unknown")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /ticket should return 405")
        void shouldReturn405ForUnsupportedMethodOnExistingEndpoint() throws Exception {
            mockMvc.perform(put("/ticket")).andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("Event Endpoints")
    class EventEndpointTests {

        @Test
        @DisplayName("POST /event should create and return event")
        void shouldCreateEvent() throws Exception {
            mockMvc.perform(post("/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createEventJson("Test Concert", "Test Arena", LocalDateTime.now().plusDays(7), 100)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Concert"))
                .andExpect(jsonPath("$.location").value("Test Arena"))
                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("GET /event should return list containing created event")
        void shouldGetAllEvents() throws Exception {
            performCreateEvent("ListEvent", "ListVenue", LocalDateTime.now().plusDays(1), 50);

            mockMvc.perform(get("/event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'ListEvent')]").exists());
        }

        @Test
        @DisplayName("GET /event/{id} should return event by id")
        void shouldGetEventById() throws Exception {
            String response = performCreateEvent("ByIdEvent", "ByIdVenue", LocalDateTime.now().plusDays(1), 50);
            String id = objectMapper.readTree(response).get("id").asString();

            mockMvc.perform(get("/event/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ByIdEvent"))
                .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        @DisplayName("GET /event/{id} with unknown id should return 404")
        void shouldReturn404ForUnknownEvent() throws Exception {
            mockMvc.perform(get("/event/000000000000000000"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /event should update event")
        void shouldUpdateEvent() throws Exception {
            String response = performCreateEvent("OldName", "OldVenue", LocalDateTime.now().plusDays(1), 50);
            String updated = response
                .replace("\"name\":\"OldName\"", "\"name\":\"NewName\"")
                .replace("\"location\":\"OldVenue\"", "\"location\":\"NewVenue\"");

            mockMvc.perform(put("/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updated))
                .andExpect(status().isOk());

            String id = objectMapper.readTree(response).get("id").asString();
            mockMvc.perform(get("/event/" + id))
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.location").value("NewVenue"));
        }

        @Test
        @DisplayName("PUT /event reducing ticket count should return 422")
        void shouldRejectReducingTicketCount() throws Exception {
            String response = performCreateEvent("FullEvent", "Venue", LocalDateTime.now().plusDays(1), 100);
            String reduced = response.replace("\"ticketsAvailable\":100", "\"ticketsAvailable\":50");

            mockMvc.perform(put("/event")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reduced))
                .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("DELETE /event/{id} should delete event")
        void shouldDeleteEvent() throws Exception {
            String response = performCreateEvent("DeleteMe", "Venue", LocalDateTime.now().plusDays(1), 10);
            String id = objectMapper.readTree(response).get("id").asString();

            mockMvc.perform(delete("/event/" + id))
                .andExpect(status().isOk());

            mockMvc.perform(get("/event/" + id))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /event should delete all events")
        void shouldDeleteAllEvents() throws Exception {
            performCreateEvent("Event1", "Venue1", LocalDateTime.now().plusDays(1), 10);
            performCreateEvent("Event2", "Venue2", LocalDateTime.now().plusDays(2), 20);

            mockMvc.perform(delete("/event"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/event"))
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Customer Endpoints")
    class CustomerEndpointTests {

        @Test
        @DisplayName("POST /customer should create and return customer")
        void shouldCreateCustomer() throws Exception {
            mockMvc.perform(post("/customer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createCustomerJson("testuser", "test@example.com", LocalDate.now().minusYears(25))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("POST /customer with underage customer should return 422")
        void shouldRejectUnderageCustomer() throws Exception {
            mockMvc.perform(post("/customer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createCustomerJson("younguser", "young@example.com", LocalDate.now().minusYears(16))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("User has to be 18 years old"));
        }

        @Test
        @DisplayName("POST /customer with invalid email should return 422")
        void shouldRejectInvalidEmail() throws Exception {
            mockMvc.perform(post("/customer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createCustomerJson("user", "notanemail", LocalDate.now().minusYears(25))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Invalid email"));
        }

        @Test
        @DisplayName("GET /customer/{id} should return customer by id")
        void shouldGetCustomerById() throws Exception {
            String response = performCreateCustomer("getbyid", "getbyid@example.com", LocalDate.now().minusYears(25));
            String id = objectMapper.readTree(response).get("id").asString();

            mockMvc.perform(get("/customer/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("getbyid"))
                .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        @DisplayName("GET /customer/{id} with unknown id should return 404")
        void shouldReturn404ForUnknownCustomer() throws Exception {
            mockMvc.perform(get("/customer/000000000000000000"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /customer/{id} should delete customer")
        void shouldDeleteCustomer() throws Exception {
            String response = performCreateCustomer("deleteme", "deleteme@example.com", LocalDate.now().minusYears(25));
            String id = objectMapper.readTree(response).get("id").asString();

            mockMvc.perform(delete("/customer/" + id))
                .andExpect(status().isOk());

            mockMvc.perform(get("/customer/" + id))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Ticket Endpoints")
    class TicketEndpointTests {

        @Test
        @DisplayName("POST /ticket should create and return ticket")
        void shouldCreateTicket() throws Exception {
            String customerResponse = performCreateCustomer("ticketuser", "ticket@example.com", LocalDate.now().minusYears(25));
            String customerId = objectMapper.readTree(customerResponse).get("id").asString();

            String eventResponse = performCreateEvent("TicketEvent", "TicketVenue", LocalDateTime.now().plusDays(1), 100);
            String eventId = objectMapper.readTree(eventResponse).get("id").asString();

            String ticketRequest = objectMapper.writeValueAsString(new CreateTicketRequest(
                Long.parseLong(customerId),
                Long.parseLong(eventId)
            ));

            mockMvc.perform(post("/ticket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        @DisplayName("POST /ticket for sold out event should return 422")
        void shouldRejectTicketForSoldOutEvent() throws Exception {
            String customerResponse = performCreateCustomer("soldout", "soldout@example.com", LocalDate.now().minusYears(25));
            String customerId = objectMapper.readTree(customerResponse).get("id").asString();

            String eventResponse = performCreateEvent("SoldOutEvent", "Venue", LocalDateTime.now().plusDays(1), 0);
            String eventId = objectMapper.readTree(eventResponse).get("id").asString();

            String ticketRequest = objectMapper.writeValueAsString(new CreateTicketRequest(
                Long.parseLong(customerId),
                Long.parseLong(eventId)
            ));

            mockMvc.perform(post("/ticket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketRequest))
                .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("POST /ticket with unknown customer should return 422")
        void shouldRejectTicketWithUnknownCustomer() throws Exception {
            String eventResponse = performCreateEvent("SomeEvent", "Venue", LocalDateTime.now().plusDays(1), 100);
            String eventId = objectMapper.readTree(eventResponse).get("id").asString();

            String ticketRequest = objectMapper.writeValueAsString(new CreateTicketRequest(
                //Long.parseLong("00000000-0000-0000-0000-000000000000"),
                0,
                Long.parseLong(eventId)
            ));

            mockMvc.perform(post("/ticket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketRequest))
                .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("GET /ticket/{id} should return ticket by id")
        void shouldGetTicketById() throws Exception {
            String customerResponse = performCreateCustomer("getticket", "getticket@example.com", LocalDate.now().minusYears(25));
            String customerId = objectMapper.readTree(customerResponse).get("id").asString();

            String eventResponse = performCreateEvent("GetTicketEvent", "Venue", LocalDateTime.now().plusDays(1), 100);
            String eventId = objectMapper.readTree(eventResponse).get("id").asString();

            String ticketRequest = objectMapper.writeValueAsString(new CreateTicketRequest(
                Long.parseLong(customerId),
                Long.parseLong(eventId)
            ));

            String ticketResponse = mockMvc.perform(post("/ticket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketRequest))
                .andReturn().getResponse().getContentAsString();

            String ticketId = objectMapper.readTree(ticketResponse).get("id").asString();

            mockMvc.perform(get("/ticket/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId));
        }

        @Test
        @DisplayName("DELETE /ticket/{id} should delete ticket")
        void shouldDeleteTicket() throws Exception {
            String customerResponse = performCreateCustomer("delticket", "delticket@example.com", LocalDate.now().minusYears(25));
            String customerId = objectMapper.readTree(customerResponse).get("id").asString();

            String eventResponse = performCreateEvent("DelTicketEvent", "Venue", LocalDateTime.now().plusDays(1), 100);
            String eventId = objectMapper.readTree(eventResponse).get("id").asString();

            String ticketRequest = objectMapper.writeValueAsString(new CreateTicketRequest(
                Long.parseLong(customerId),
                Long.parseLong(eventId)
            ));

            String ticketResponse = mockMvc.perform(post("/ticket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ticketRequest))
                .andReturn().getResponse().getContentAsString();

            String ticketId = objectMapper.readTree(ticketResponse).get("id").asString();

            mockMvc.perform(delete("/ticket/" + ticketId))
                .andExpect(status().isOk());

            mockMvc.perform(get("/ticket/" + ticketId))
                .andExpect(status().isUnprocessableContent());
        }
    }
}
