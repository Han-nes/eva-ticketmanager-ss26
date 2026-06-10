package tcp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;
import core.models.Customer;
import core.models.Event;
import tcp.client.TcpClient;
import tcp.server.TCPHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tcp.client.ticketShop.TicketShopStringFormatter;

class TCPClientTest {

    private TcpClient client;

    @BeforeEach
    void setUp() throws InterruptedException {
        int port = 8080;
        TCPHost host = new TCPHost(port);
        String hostName = "localhost";
        client = new TcpClient(hostName, port);
        host.start();
        // Give the server time to start
        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    @DisplayName("Should connect to the host")
    void shouldConnectToTheHost() {
        assertDoesNotThrow(() -> client.connect());
    }

    @Test
    @DisplayName(
        "Should create a customer via TCP and receive valid customer response"
    )
    void shouldCreateCustomerViaTcpAndReceiveValidResponse()
        throws IOException {
        // Arrange
        String username = "tcpuser";
        String email = "tcp@example.com";
        LocalDate dateOfBirth = LocalDate.of(1995, 6, 15);
        String message = String.format(
            "customer;create;%s,%s,%s",
            username,
            email,
            dateOfBirth
        );

        // Act
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("Customer{id="));
        assertTrue(response.contains("username='" + username + "'"));
        assertTrue(response.contains("email='" + email + "'"));
        assertTrue(response.contains("dateOfBirth='" + dateOfBirth + "'"));

        // Verify response can be parsed back to Customer
        Customer customer = TicketShopStringFormatter.customerFromString(response);
        assertEquals(username, customer.getUsername());
        assertEquals(email, customer.getEmail());
        assertEquals(dateOfBirth, customer.getDateOfBirth());
    }

    @Test
    @DisplayName(
        "Should get customer by ID via TCP and receive valid customer response"
    )
    void shouldGetCustomerByIdViaTcpAndReceiveValidResponse()
        throws IOException {
        // Arrange - First create a customer
        String createMessage =
            "customer;create;tcpuser,tcp@example.com,1995-06-15";
        String createResponse = client.send(createMessage);
        Customer createdCustomer = TicketShopStringFormatter.customerFromString(createResponse);

        // Act - Get customer by ID
        String getMessage = String.format(
            "customer;getbyid;%s",
            createdCustomer.getId()
        );
        String response = client.send(getMessage);

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("Customer{id="));
        assertTrue(response.contains("username='tcpuser'"));
        assertTrue(response.contains("email='tcp@example.com'"));

        // Verify response matches created customer
        Customer retrievedCustomer = TicketShopStringFormatter.customerFromString(response);
        assertEquals(createdCustomer.getId(), retrievedCustomer.getId());
        assertEquals(
            createdCustomer.getUsername(),
            retrievedCustomer.getUsername()
        );
    }

    @Test
    @DisplayName(
        "Should get all customers via TCP and receive valid list response"
    )
    void shouldGetAllCustomersViaTcpAndReceiveValidListResponse()
        throws IOException {
        // Arrange - Create multiple customers
        client.send("customer;create;user1,user1@example.com,2000-01-01");
        client.send("customer;create;user2,user2@example.com,1990-12-25");

        // Act
        String message = "customer;getall;";
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("username='user1'"));
        assertTrue(response.contains("username='user2'"));
        assertTrue(response.contains("user1@example.com"));
        assertTrue(response.contains("user2@example.com"));

        // Verify format - should contain multiple customer strings
        String[] customers = response.split(";");
        assertTrue(customers.length >= 2);
    }

    @Test
    @DisplayName("Should delete customer via TCP and receive success response")
    void shouldDeleteCustomerViaTcpAndReceiveSuccessResponse()
        throws IOException {
        // Arrange - Create a customer first
        String createMessage =
            "customer;create;deleteuser,delete@example.com,2000-01-01";
        String createResponse = client.send(createMessage);
        Customer customer = TicketShopStringFormatter.customerFromString(createResponse);

        // Act
        String deleteMessage = String.format(
            "customer;delete;%s",
            customer.getId()
        );
        String response = client.send(deleteMessage);

        // Assert
        assertNotNull(response);
        assertEquals("Success", response);
    }

    @Test
    @DisplayName(
        "Should create an event via TCP and receive valid event response"
    )
    void shouldCreateEventViaTcpAndReceiveValidResponse() throws IOException {
        // Arrange
        String message = "event;create;TCPConcert,Stadium,2025-12-31T20:00,200";

        // Act
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("Event{id="));
        assertTrue(response.contains("name='TCPConcert'"));
        assertTrue(response.contains("location='Stadium'"));
        assertTrue(response.contains("ticketsAvailable=200"));

        // Verify response can be parsed back to Event
        Event event = TicketShopStringFormatter.eventFromString(response);
        assertEquals("TCPConcert", event.getName());
        assertEquals("Stadium", event.getLocation());
        assertEquals(200, event.getTicketsAvailable());
    }

    @Test
    @DisplayName(
        "Should get all events via TCP and receive valid list response"
    )
    void shouldGetAllEventsViaTcpAndReceiveValidListResponse()
        throws IOException {
        // Arrange - Create multiple events
        client.send("event;create;Event1,Venue1,2025-12-31T20:00,100");
        client.send("event;create;Event2,Venue2,2026-06-15T14:00,200");

        // Act
        String message = "event;getall;";
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("name='Event1'"));
        assertTrue(response.contains("name='Event2'"));
        assertTrue(response.contains("location='Venue1'"));
        assertTrue(response.contains("location='Venue2'"));

        // Verify format
        String[] events = response.split(";");
        assertTrue(events.length >= 2);
    }

    @Test
    @DisplayName(
        "Should create a ticket via TCP and receive valid ticket response"
    )
    void shouldCreateTicketViaTcpAndReceiveValidResponse() throws IOException {
        // Arrange - Create customer and event first
        String customerResponse = client.send(
            "customer;create;ticketuser,ticket@example.com,2000-01-01"
        );
        Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

        String eventResponse = client.send(
            "event;create;TicketEvent,TicketVenue,2025-12-31T20:00,50"
        );
        Event event = TicketShopStringFormatter.eventFromString(eventResponse);

        // Act
        String message = String.format(
            "ticket;create;%s,%s",
            customer.getId(),
            event.getId()
        );
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("id="));
        assertTrue(response.contains("dateOfPurchase="));
        assertTrue(response.contains("customerId=" + customer.getId()));
        assertTrue(response.contains("eventId=" + event.getId()));
    }

    @Test
    @DisplayName(
        "Should get all tickets via TCP and receive valid list response"
    )
    void shouldGetAllTicketsViaTcpAndReceiveValidListResponse()
        throws IOException {
        // Arrange - Create customer, event, and tickets
        String customerResponse = client.send(
            "customer;create;ticketuser,ticket@example.com,2000-01-01"
        );
        Customer customer = TicketShopStringFormatter.customerFromString(customerResponse);

        String eventResponse = client.send(
            "event;create;TicketEvent,TicketVenue,2025-12-31T20:00,50"
        );
        Event event = TicketShopStringFormatter.eventFromString(eventResponse);

        client.send(
            String.format(
                "ticket;create;%s,%s",
                customer.getId(),
                event.getId()
            )
        );
        client.send(
            String.format(
                "ticket;create;%s,%s",
                customer.getId(),
                event.getId()
            )
        );

        // Act
        String message = "ticket;getall;";
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("customerId=" + customer.getId()));
        assertTrue(response.contains("eventId=" + event.getId()));

        // Verify format
        String[] tickets = response.split(";");
        assertTrue(tickets.length >= 2);
    }

    @Test
    @DisplayName("Should handle invalid email error via TCP")
    void shouldHandleInvalidEmailErrorViaTcp() throws IOException {
        // Arrange
        String message = "customer;create;baduser,bademail,2000-01-01";

        // Act
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Invalid email"));
    }

    @Test
    @DisplayName("Should handle unknown service error via TCP")
    void shouldHandleUnknownServiceErrorViaTcp() throws IOException {
        // Arrange
        String message = "unknown;getall;";

        // Act
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Unknown Service"));
    }

    @Test
    @DisplayName("Should handle missing arguments error via TCP")
    void shouldHandleMissingArgumentsErrorViaTcp() throws IOException {
        // Arrange
        String message = "customer;create;onlyusername";

        // Act
        String response = client.send(message);

        // Assert
        assertNotNull(response);
        assertTrue(
            response.contains("Missing arguments") ||
                response.contains("ArrayIndexOutOfBoundsException")
        );
    }

    @Test
    @DisplayName("Should maintain connection for multiple requests")
    void shouldMaintainConnectionForMultipleRequests() throws IOException {
        // Act & Assert - Send multiple requests using same connection
        String response1 = client.send(
            "customer;create;multi1,multi1@example.com,2000-01-01"
        );
        assertNotNull(response1);
        assertTrue(response1.contains("username='multi1'"));

        String response2 = client.send(
            "customer;create;multi2,multi2@example.com,2000-01-01"
        );
        assertNotNull(response2);
        assertTrue(response2.contains("username='multi2'"));

        String response3 = client.send("customer;getall;");
        assertNotNull(response3);
        assertTrue(response3.contains("multi1"));
        assertTrue(response3.contains("multi2"));
    }
}
