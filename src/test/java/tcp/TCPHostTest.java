package tcp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import tcp.server.TCPHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TCPHostTest {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeEach
    void setUp() throws InterruptedException, IOException {
        // Start the host
        int port = 8081;
        TCPHost host = new TCPHost(port);
        host.start();
        // Give the server time to start
        Thread.sleep(100);

        // Connect to the host
        String hostName = "localhost";
        clientSocket = new Socket(hostName, port);
        in =
            new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
    }

    @Test
    @DisplayName("Should handle two clients with same Services")
    void shouldHandleTwoClientsWithSameServices() throws IOException {
        Socket secondClientSocket = new Socket("localHost2", 8082);
        BufferedReader secondClientIn =
                new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );;
        PrintWriter secondClientOut = new PrintWriter(clientSocket.getOutputStream(), true);

        //Arrange
        String request = "customer;create;handler_user,handler@example.com,2000-01-01";

        // Act
        out.println(request);

        secondClientOut.println("customer;getall;");
        String responseSecondClient = secondClientIn.readLine();

        // Assert
        assertNotNull(responseSecondClient);
        assertTrue(responseSecondClient.contains("handler_user"));
        assertTrue(responseSecondClient.contains("handler@example.com"));


    }

    @Test
    @DisplayName("Should handle customer create request")
    void shouldHandleCustomerCreateRequest() throws IOException {
        // Arrange
        String request = "customer;create;handler_user,handler@example.com,2000-01-01";

        // Act
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("Customer{id="));
        assertTrue(response.contains("username='handler_user'"));
        assertTrue(response.contains("email='handler@example.com'"));
    }

    @Test
    @DisplayName("Should handle customer getall request")
    void shouldHandleCustomerGetAllRequest() throws IOException {
        // Arrange - Create a customer first
        out.println(
            "customer;create;handler_user,handler@example.com,2000-01-01"
        );
        in.readLine(); // Consume create response

        // Act
        out.println("customer;getall;");
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("handler_user"));
        assertTrue(response.contains("handler@example.com"));
    }

    @Test
    @DisplayName("Should handle event create request")
    void shouldHandleEventCreateRequest() throws IOException {
        // Arrange
        String request =
            "event;create;HandlerConcert,HandlerVenue,2025-12-31T20:00,150";

        // Act
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("Event{id="));
        assertTrue(response.contains("name='HandlerConcert'"));
        assertTrue(response.contains("location='HandlerVenue'"));
        assertTrue(response.contains("ticketsAvailable=150"));
    }

    @Test
    @DisplayName("Should handle event getall request")
    void shouldHandleEventGetAllRequest() throws IOException {
        // Arrange - Create an event first
        out.println(
            "event;create;HandlerConcert,HandlerVenue,2025-12-31T20:00,150"
        );
        in.readLine(); // Consume create response

        // Act
        out.println("event;getall;");
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("HandlerConcert"));
        assertTrue(response.contains("HandlerVenue"));
    }

    @Test
    @DisplayName("Should handle ticket create request")
    void shouldHandleTicketCreateRequest() throws IOException {
        // Arrange - Create customer and event first
        out.println(
            "customer;create;ticket_user,ticket@example.com,2000-01-01"
        );
        String customerResponse = in.readLine();
        String customerId = customerResponse.split(",")[0].split("=")[1];

        out.println(
            "event;create;TicketEvent,TicketPlace,2025-12-31T20:00,100"
        );
        String eventResponse = in.readLine();
        String eventId = eventResponse.split(",")[0].split("=")[1];

        // Act
        String request = String.format("ticket;create;%s,%s", customerId, eventId);
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("id="));
        assertTrue(response.contains("dateOfPurchase="));
        assertTrue(response.contains("customerId=" + customerId));
        assertTrue(response.contains("eventId=" + eventId));
    }

    @Test
    @DisplayName("Should handle ticket getall request")
    void shouldHandleTicketGetAllRequest() throws IOException {
        // Arrange - Create customer, event, and ticket
        out.println(
            "customer;create;ticket_user,ticket@example.com,2000-01-01"
        );
        String customerResponse = in.readLine();
        String customerId = customerResponse.split(",")[0].split("=")[1];

        out.println(
            "event;create;TicketEvent,TicketPlace,2025-12-31T20:00,100"
        );
        String eventResponse = in.readLine();
        String eventId = eventResponse.split(",")[0].split("=")[1];

        out.println(String.format("ticket;create;%s,%s", customerId, eventId));
        in.readLine(); // Consume create response

        // Act
        out.println("ticket;getall;");
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("customerId=" + customerId));
        assertTrue(response.contains("eventId=" + eventId));
    }

    @Test
    @DisplayName("Should handle error for invalid email")
    void shouldHandleErrorForInvalidEmail() throws IOException {
        // Arrange
        String request = "customer;create;baduser,invalidemail,2000-01-01";

        // Act
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Invalid email"));
    }

    @Test
    @DisplayName("Should handle error for unknown service")
    void shouldHandleErrorForUnknownService() throws IOException {
        // Arrange
        String request = "invalidservice;getall;";

        // Act
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Unknown Service"));
    }

    @Test
    @DisplayName("Should handle error for missing arguments")
    void shouldHandleErrorForMissingArguments() throws IOException {
        // Arrange
        String request = "customer;create;onlyusername";

        // Act
        out.println(request);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertTrue(
            response.contains("Missing arguments") ||
            response.contains("ArrayIndexOutOfBoundsException")
        );
    }

    @Test
    @DisplayName("Should handle multiple sequential requests")
    void shouldHandleMultipleSequentialRequests() throws IOException {
        // Act & Assert - Send multiple requests
        out.println(
            "customer;create;seq1,seq1@example.com,2000-01-01"
        );
        String response1 = in.readLine();
        assertNotNull(response1);
        assertTrue(response1.contains("username='seq1'"));

        out.println(
            "customer;create;seq2,seq2@example.com,2000-01-01"
        );
        String response2 = in.readLine();
        assertNotNull(response2);
        assertTrue(response2.contains("username='seq2'"));

        out.println("customer;getall;");
        String response3 = in.readLine();
        assertNotNull(response3);
        assertTrue(response3.contains("seq1"));
        assertTrue(response3.contains("seq2"));
    }

    @Test
    @DisplayName("Should handle delete customer request")
    void shouldHandleDeleteCustomerRequest() throws IOException {
        // Arrange - Create a customer first
        out.println(
            "customer;create;deleteuser,delete@example.com,2000-01-01"
        );
        String createResponse = in.readLine();
        String customerId = createResponse.split(",")[0].split("=")[1];

        // Act
        out.println("customer;delete;" + customerId);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertEquals("Success", response);
    }

    @Test
    @DisplayName("Should handle deleteall customers request")
    void shouldHandleDeleteAllCustomersRequest() throws IOException {
        // Arrange - Create some customers
        out.println("customer;create;user1,user1@example.com,2000-01-01");
        in.readLine();
        out.println("customer;create;user2,user2@example.com,2000-01-01");
        in.readLine();

        // Act
        out.println("customer;deleteall;");
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertEquals("Success", response);

        // Verify all customers deleted
        out.println("customer;getall;");
        String getAllResponse = in.readLine();
        assertTrue(getAllResponse.isEmpty());
    }

    @Test
    @DisplayName("Should handle delete event request")
    void shouldHandleDeleteEventRequest() throws IOException {
        // Arrange - Create an event first
        out.println(
            "event;create;DeleteEvent,DeleteVenue,2025-12-31T20:00,100"
        );
        String createResponse = in.readLine();
        String eventId = createResponse.split(",")[0].split("=")[1];

        // Act
        out.println("event;delete;" + eventId);
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertEquals("Success", response);
    }

    @Test
    @DisplayName("Should handle deleteall events request")
    void shouldHandleDeleteAllEventsRequest() throws IOException {
        // Arrange - Create some events
        out.println("event;create;Event1,Venue1,2025-12-31T20:00,100");
        in.readLine();
        out.println("event;create;Event2,Venue2,2026-06-15T14:00,200");
        in.readLine();

        // Act
        out.println("event;deleteall;");
        String response = in.readLine();

        // Assert
        assertNotNull(response);
        assertEquals("All Events deleted", response);

        // Verify all events deleted
        out.println("event;getall;");
        String getAllResponse = in.readLine();
        assertTrue(getAllResponse.isEmpty());
    }
}
