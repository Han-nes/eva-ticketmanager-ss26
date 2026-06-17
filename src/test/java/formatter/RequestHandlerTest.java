package formatter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import core.models.Customer;
import core.models.Event;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDService;
import org.checkerframework.checker.units.qual.C;
import tcp.server.RequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tcp.client.ticketShop.TicketShopStringFormatter;

class RequestHandlerTest {

    private RequestHandler requestHandler;

    @BeforeEach
    void setUp() {
        IDService idService = new IDService(10000L, 99999L);
        TicketService ticketService = new TicketService(idService);
        CustomerService customerService = new CustomerService(ticketService, idService);
        EventService eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);
        requestHandler = new RequestHandler(ticketService, customerService, eventService);
    }

    @Nested
    @DisplayName("Customer Method Tests")
    class CustomerMethodTests {

        @Test
        @DisplayName("Should create a customer and return customer string")
        void shouldCreateCustomerAndReturnCustomerString() throws Exception {
            // Arrange
            String username = "testuser";
            String email = "test@example.com";
            LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
            String request = String.format(
                "customer;create;%s,%s,%s",
                username,
                email,
                dateOfBirth
            );

            // Act
            String response = requestHandler.callMethodRemotely(request);

            // Assert
            assertNotNull(response);
            assertTrue(response.startsWith("Customer{id="));
            assertTrue(response.contains("username='" + username + "'"));
            assertTrue(response.contains("email='" + email + "'"));
            assertTrue(response.contains("dateOfBirth='" + dateOfBirth + "'"));
        }

        @Test
        @DisplayName("Should fail to create customer with invalid email")
        void shouldFailToCreateCustomerWithInvalidEmail() {
            // Arrange
            String request = "customer;create;testuser,invalidemail,2000-01-01";

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
            assertTrue(exception.getMessage().contains("Invalid email"));
        }

        @Test
        @DisplayName("Should fail to create customer under 18 years old")
        void shouldFailToCreateCustomerUnder18() {
            // Arrange
            LocalDate recentDate = LocalDate.now().minusYears(15);
            String request = String.format(
                "customer;create;testuser,test@example.com,%s",
                recentDate
            );

            // Act & Assert
            Exception exception = assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
            assertTrue(exception.getMessage().contains("18 years old"));
        }

        @Test
        @DisplayName("Should fail to create customer with missing arguments")
        void shouldFailToCreateCustomerWithMissingArguments() {
            // Arrange
            String request = "customer;create;testuser";

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName(
            "Should get customer by ID and return valid customer string"
        )
        void shouldGetCustomerByIdAndReturnValidCustomerString()
            throws Exception {
            // Arrange - First create a customer
            String createRequest =
                "customer;create;testuser,test@example.com,2000-01-01";
            String createResponse = requestHandler.callMethodRemotely(
                createRequest
            );
            Customer createdCustomer = TicketShopStringFormatter.customerFromString(createResponse);

            // Act - Get the customer by ID
            String getRequest = String.format(
                "customer;getbyid;%s",
                createdCustomer.getId()
            );
            String response = requestHandler.callMethodRemotely(getRequest);

            // Assert
            assertNotNull(response);
            assertTrue(response.startsWith("Customer{id="));
            assertTrue(response.contains("username='testuser'"));
            assertTrue(response.contains("email='test@example.com'"));
            assertTrue(response.contains("dateOfBirth='2000-01-01'"));

            // Verify we can parse the response back to a Customer
            Customer retrievedCustomer = TicketShopStringFormatter.customerFromString(response);
            assertEquals(createdCustomer.getId(), retrievedCustomer.getId());
            assertEquals(
                createdCustomer.getUsername(),
                retrievedCustomer.getUsername()
            );
        }

        @Test
        @DisplayName("Should fail to get customer with missing ID")
        void shouldFailToGetCustomerWithMissingId() {
            // Arrange
            String request = "customer;getbyid;";

            // Act & Assert
            assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName("Should delete customer and return success")
        void shouldDeleteCustomerAndReturnSuccess() throws Exception {
            // Arrange - First create a customer
            String createRequest =
                "customer;create;testuser,test@example.com,2000-01-01";
            String createResponse = requestHandler.callMethodRemotely(createRequest);
            Customer createdCustomer = TicketShopStringFormatter.customerFromString(createResponse);

            // Act - Delete the customer
            String deleteRequest = String.format(
                "customer;delete;%s",
                createdCustomer.getId()
            );
            String response = requestHandler.callMethodRemotely(deleteRequest);

            // Assert
            assertEquals("Success", response);
        }

        @Test
        @DisplayName("Should fail to delete customer with missing ID")
        void shouldFailToDeleteCustomerWithMissingId() {
            // Arrange
            String request = "customer;delete;";

            // Act & Assert
            assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName(
            "Should get all customers and return list of customer strings"
        )
        void shouldGetAllCustomersAndReturnList() throws Exception {
            // Arrange - Create multiple customers
            requestHandler.callMethodRemotely("customer;create;user1,user1@example.com,2000-01-01");
            requestHandler.callMethodRemotely("customer;create;user2,user2@example.com,1995-05-15");

            // Act
            String response = requestHandler.callMethodRemotely("customer;getall;");

            // Assert
            assertNotNull(response);
            assertTrue(response.contains("username='user1'"));
            assertTrue(response.contains("username='user2'"));
            assertTrue(response.contains("user1@example.com"));
            assertTrue(response.contains("user2@example.com"));

            // Verify format - should be semicolon-separated customer strings
            String[] customers = response.split(";");
            assertTrue(customers.length >= 2);
            for (String customerStr : customers) {
                if (!customerStr.isEmpty()) {
                    assertTrue(customerStr.startsWith("Customer{id="));
                }
            }
        }

        @Test
        @DisplayName("Should delete all customers and return success")
        void shouldDeleteAllCustomersAndReturnSuccess() throws Exception {
            // Arrange - Create some customers
            requestHandler.callMethodRemotely(
                "customer;create;user1,user1@example.com,2000-01-01"
            );
            requestHandler.callMethodRemotely(
                "customer;create;user2,user2@example.com,1995-05-15"
            );

            // Act
            String response = requestHandler.callMethodRemotely(
                "customer;deleteall;"
            );

            // Assert
            assertEquals("Success", response);

            // Verify all customers are deleted
            String getAllResponse = requestHandler.callMethodRemotely(
                "customer;getall;"
            );
            assertTrue(getAllResponse.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for invalid customer method")
        void shouldThrowExceptionForInvalidCustomerMethod() {
            // Arrange
            String request = "customer;invalidmethod;";

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }
    }

    @Nested
    @DisplayName("Event Method Tests")
    class EventMethodTests {

        @Test
        @DisplayName("Should create an event and return event string")
        void shouldCreateEventAndReturnEventString() throws Exception {
            // Arrange
            String name = "Concert";
            String location = "Arena";
            LocalDateTime time = LocalDateTime.of(2025, 12, 31, 20, 0);
            int ticketsAvailable = 100;
            String request = String.format(
                "event;create;%s,%s,%s,%d",
                name,
                location,
                time,
                ticketsAvailable
            );

            // Act
            String response = requestHandler.callMethodRemotely(request);

            // Assert
            assertNotNull(response);
            assertTrue(response.startsWith("Event{id="));
            assertTrue(response.contains("name='" + name + "'"));
            assertTrue(response.contains("location='" + location + "'"));
            assertTrue(response.contains("time=" + time));
            assertTrue(
                response.contains("ticketsAvailable=" + ticketsAvailable)
            );
        }

        @Test
        @DisplayName("Should fail to create event with missing arguments")
        void shouldFailToCreateEventWithMissingArguments() {
            // Arrange
            String request = "event;create;Concert,Arena";

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName("Should get event by ID and return valid event string")
        void shouldGetEventByIdAndReturnValidEventString() throws Exception {
            // Arrange - First create an event
            String createRequest =
                "event;create;Concert,Arena,2025-12-31T20:00,100";
            String createResponse = requestHandler.callMethodRemotely(
                createRequest
            );
            Event createdEvent = TicketShopStringFormatter.eventFromString(createResponse);

            // Act - Get the event by ID
            String getRequest = String.format(
                "event;getbyid;%s",
                createdEvent.getId()
            );
            String response = requestHandler.callMethodRemotely(getRequest);

            // Assert
            assertNotNull(response);
            assertTrue(response.startsWith("Event{id="));
            assertTrue(response.contains("name='Concert'"));
            assertTrue(response.contains("location='Arena'"));

            // Verify we can parse the response back to an Event
            Event retrievedEvent = TicketShopStringFormatter.eventFromString(response);
            assertEquals(createdEvent.getId(), retrievedEvent.getId());
            assertEquals(createdEvent.getName(), retrievedEvent.getName());
        }

        @Test
        @DisplayName("Should fail to get event with missing ID")
        void shouldFailToGetEventWithMissingId() {
            // Arrange
            String request = "event;getbyid;";

            // Act & Assert
            assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName("Should get all events and return list of event strings")
        void shouldGetAllEventsAndReturnList() throws Exception {
            // Arrange - Create multiple events
            requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            requestHandler.callMethodRemotely(
                "event;create;Festival,Park,2026-06-15T14:00,500"
            );

            // Act
            String response = requestHandler.callMethodRemotely(
                "event;getall;"
            );

            // Assert
            assertNotNull(response);
            assertTrue(response.contains("name='Concert'"));
            assertTrue(response.contains("name='Festival'"));
            assertTrue(response.contains("location='Arena'"));
            assertTrue(response.contains("location='Park'"));

            // Verify format - should be semicolon-separated event strings
            String[] events = response.split(";");
            assertTrue(events.length >= 2);
            for (String eventStr : events) {
                if (!eventStr.isEmpty()) {
                    assertTrue(eventStr.startsWith("Event{id="));
                }
            }
        }

        @Test
        @DisplayName("Should delete event and return success")
        void shouldDeleteEventAndReturnSuccess() throws Exception {
            // Arrange - First create an event
            String createRequest =
                "event;create;Concert,Arena,2025-12-31T20:00,100";
            String createResponse = requestHandler.callMethodRemotely(
                createRequest
            );
            Event createdEvent = TicketShopStringFormatter.eventFromString(createResponse);

            // Act - Delete the event
            String deleteRequest = String.format(
                "event;delete;%s",
                createdEvent.getId()
            );
            String response = requestHandler.callMethodRemotely(deleteRequest);

            // Assert
            assertEquals("Success", response);
        }

        @Test
        @DisplayName("Should fail to delete event with missing ID")
        void shouldFailToDeleteEventWithMissingId() {
            // Arrange
            String request = "event;delete;";

            // Act & Assert
            assertThrows(Exception.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName("Should delete all events and return success message")
        void shouldDeleteAllEventsAndReturnSuccessMessage() throws Exception {
            // Arrange - Create some events
            requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            requestHandler.callMethodRemotely(
                "event;create;Festival,Park,2026-06-15T14:00,500"
            );

            // Act
            String response = requestHandler.callMethodRemotely(
                "event;deleteall;"
            );

            // Assert
            assertEquals("All Events deleted", response);

            // Verify all events are deleted
            String getAllResponse = requestHandler.callMethodRemotely(
                "event;getall;"
            );
            assertTrue(getAllResponse.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for invalid event method")
        void shouldThrowExceptionForInvalidEventMethod() {
            // Arrange
            String request = "event;invalidmethod;";

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }
    }

    @Nested
    @DisplayName("Ticket Method Tests")
    class TicketMethodTests {

        @Test
        @DisplayName("Should create a ticket and return ticket string")
        void shouldCreateTicketAndReturnTicketString() throws Exception {
            // Arrange - Create a customer and event first
            String customerResponse = requestHandler.callMethodRemotely(
                "customer;create;testuser,test@example.com,2000-01-01"
            );
            Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

            String eventResponse = requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            Event event = TicketShopStringFormatter.eventFromString(eventResponse);

            String request = String.format(
                "ticket;create;%s,%s",
                customer.getId(),
                event.getId()
            );

            // Act
            String response = requestHandler.callMethodRemotely(request);

            // Assert
            assertNotNull(response);
            assertTrue(response.contains("id="));
            assertTrue(response.contains("dateOfPurchase="));
            assertTrue(response.contains("customerId=" + customer.getId()));
            assertTrue(response.contains("eventId=" + event.getId()));
        }

        @Test
        @DisplayName("Should fail to create ticket with missing arguments")
        void shouldFailToCreateTicketWithMissingArguments() {
            // Arrange
            String request = "ticket;create;" + UUID.randomUUID();

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }

        @Test
        @DisplayName("Should get ticket by ID and return valid ticket string")
        void shouldGetTicketByIdAndReturnValidTicketString() throws Exception {
            // Arrange - Create customer, event, and ticket
            String customerResponse = requestHandler.callMethodRemotely(
                "customer;create;testuser,test@example.com,2000-01-01"
            );
            Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

            String eventResponse = requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            Event event = TicketShopStringFormatter.eventFromString(eventResponse);

            String createTicketRequest = String.format(
                "ticket;create;%s,%s",
                customer.getId(),
                event.getId()
            );
            String createTicketResponse = requestHandler.callMethodRemotely(
                createTicketRequest
            );

            // Extract ticket ID from response
            String ticketId = createTicketResponse.split(",")[0].split("=")[1];

            // Act - Get the ticket by ID
            String getRequest = String.format("ticket;getbyid;%s", ticketId);
            String response = requestHandler.callMethodRemotely(getRequest);

            // Assert
            assertNotNull(response);
            assertTrue(response.contains("id=" + ticketId));
            assertTrue(response.contains("customerId=" + customer.getId()));
            assertTrue(response.contains("eventId=" + event.getId()));
        }

        @Test
        @DisplayName("Should get all tickets and return list of ticket strings")
        void shouldGetAllTicketsAndReturnList() throws Exception {
            // Arrange - Create customer, event, and multiple tickets
            String customerResponse = requestHandler.callMethodRemotely(
                "customer;create;testuser,test@example.com,2000-01-01"
            );
            Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

            String eventResponse = requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            Event event = TicketShopStringFormatter.eventFromString(eventResponse);

            requestHandler.callMethodRemotely(
                String.format(
                    "ticket;create;%s,%s",
                    customer.getId(),
                    event.getId()
                )
            );
            requestHandler.callMethodRemotely(
                String.format(
                    "ticket;create;%s,%s",
                    customer.getId(),
                    event.getId()
                )
            );

            // Act
            String response = requestHandler.callMethodRemotely(
                "ticket;getall;"
            );

            // Assert
            assertNotNull(response);
            assertTrue(response.contains("customerId=" + customer.getId()));
            assertTrue(response.contains("eventId=" + event.getId()));

            // Verify format - should be semicolon-separated ticket strings
            String[] tickets = response.split(";");
            assertTrue(tickets.length >= 2);
            for (String ticketStr : tickets) {
                if (!ticketStr.isEmpty()) {
                    assertTrue(ticketStr.contains("id="));
                    assertTrue(ticketStr.contains("dateOfPurchase="));
                }
            }
        }

        @Test
        @DisplayName("Should delete ticket and return success")
        void shouldDeleteTicketAndReturnSuccess() throws Exception {
            // Arrange - Create customer, event, and ticket
            String customerResponse = requestHandler.callMethodRemotely(
                "customer;create;testuser,test@example.com,2000-01-01"
            );
            Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

            String eventResponse = requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            Event event = TicketShopStringFormatter.eventFromString(eventResponse);

            String createTicketRequest = String.format(
                "ticket;create;%s,%s",
                customer.getId(),
                event.getId()
            );
            String createTicketResponse = requestHandler.callMethodRemotely(
                createTicketRequest
            );

            // Extract ticket ID
            String ticketId = createTicketResponse.split(",")[0].split("=")[1];

            // Act - Delete the ticket
            String deleteRequest = String.format("ticket;delete;%s", ticketId);
            String response = requestHandler.callMethodRemotely(deleteRequest);

            // Assert
            assertEquals("Success", response);
        }

        @Test
        @DisplayName("Should delete all tickets and return success")
        void shouldDeleteAllTicketsAndReturnSuccess() throws Exception {
            // Arrange - Create customer, event, and tickets
            String customerResponse = requestHandler.callMethodRemotely(
                "customer;create;testuser,test@example.com,2000-01-01"
            );
            Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

            String eventResponse = requestHandler.callMethodRemotely(
                "event;create;Concert,Arena,2025-12-31T20:00,100"
            );
            Event event = TicketShopStringFormatter.eventFromString(eventResponse);

            requestHandler.callMethodRemotely(
                String.format(
                    "ticket;create;%s,%s",
                    customer.getId(),
                    event.getId()
                )
            );

            // Act
            String response = requestHandler.callMethodRemotely(
                "ticket;deleteall;"
            );

            // Assert
            assertEquals("Success", response);

            // Verify all tickets are deleted
            String getAllResponse = requestHandler.callMethodRemotely(
                "ticket;getall;"
            );
            assertTrue(getAllResponse.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for invalid ticket method")
        void shouldThrowExceptionForInvalidTicketMethod() {
            // Arrange
            String request = "ticket;invalidmethod;";

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                requestHandler.callMethodRemotely(request)
            );
        }
    }

    @Nested
    @DisplayName("General Request Handling Tests")
    class GeneralRequestHandlingTests {

        @Test
        @DisplayName("Should throw exception for unknown service")
        void shouldThrowExceptionForUnknownService() {
            // Arrange
            String request = "unknown;getall;";

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestHandler.callMethodRemotely(request)
            );
            assertEquals("Unknown Service: unknown", exception.getMessage());
        }

        @Test
        @DisplayName("Should return error for missing service")
        void shouldReturnErrorForMissingService() throws Exception {
            // Arrange
            String request = "";

            // Act
            String response = requestHandler.callMethodRemotely(request);

            // Assert
            assertEquals("Error: Missing Method", response);
        }

        @Test
        @DisplayName("Should return error for missing method")
        void shouldReturnErrorForMissingMethod() throws Exception {
            // Arrange
            String request = "customer";

            // Act
            String response = requestHandler.callMethodRemotely(request);

            // Assert
            assertEquals("Error: Missing Method", response);
        }
    }
}
