package services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.exceptions.CustomerException;
import Core.Models.exceptions.EventException;
import Core.Models.exceptions.TicketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.Ticket;
import Core.Services.CustomerService;
import Core.Services.EventService;
import Core.Services.TicketService;
import IDGenerator.IDService.IDService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sound.midi.MidiSystem;

class TicketServiceTest {

    private final IDService idService = new IDService(10000L, 99999L);
    private TicketService ticketService;
    private CustomerService customerService;
    private EventService eventService;
    private Customer testCustomer;
    private Customer testCustomer2;
    private Event testEvent;
    private Event testEvent2;

    @BeforeEach
    void setUp() {
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);

        testCustomer = customerService.createCustomer(
            "testuser",
            "test@example.com",
            LocalDate.now().minusYears(25)
        );

        testCustomer2 = customerService.createCustomer(
                "testuser2",
                "test2@example.com",
                LocalDate.now().minusYears(25)
        );

        testEvent = eventService.createEvent(
            "Test Concert",
            "Test Arena",
            LocalDateTime.now().plusDays(7),
            100
        );

        testEvent2 = eventService.createEvent(
                "Test Concert 2",
                "Test Arena 2",
                LocalDateTime.now().plusDays(7),
                100
        );
    }

    @Nested
    @DisplayName("Create Ticket Tests")
    class CreateTicketTests {

        @Test
        @DisplayName("Should create ticket with valid customer and event")
        void shouldCreateTicketWithValidCustomerAndEvent() throws TicketException {
            // Act
            Ticket ticket = ticketService.createTicket(testCustomer.getId(), testEvent.getId());

            // Assert
            assertNotNull(ticket);
            assertNotNull(ticket.getId());
            assertEquals(testCustomer.getId(), ticket.getCustomerId());
            assertEquals(testEvent.getId(), ticket.getEventId());
            assertEquals(LocalDate.now(), ticket.getDateOfPurchase());
        }

        @Test
        @DisplayName("Should throw exception when customer is null")
        void shouldThrowExceptionWhenCustomerIsNull() {
            // Act & Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () -> ticketService.createTicket(-1, testEvent.getId())
            );
            assertEquals(
                CustomerException.customerDoesNotExist,
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should throw exception when event is null")
        void shouldThrowExceptionWhenEventIsNull() {
            // Act & Assert
            EventException exception = assertThrows(
                EventException.class,
                () -> ticketService.createTicket(testCustomer.getId(), -1)
            );
            assertEquals(
                EventException.eventDoesNotExist,
                exception.getMessage()
            );
        }

        @Test
        @DisplayName(
            "Should throw exception when both customer and event are null"
        )
        void shouldThrowExceptionWhenBothCustomerAndEventAreNull() {
            // Act & Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () -> ticketService.createTicket(-1, -1)
            );

            assertEquals(
                    CustomerException.customerDoesNotExist,
                    exception.getMessage()
            );

        }

        @Test
        @DisplayName("Should generate unique IDs for multiple tickets")
        void shouldGenerateUniqueIdsForMultipleTickets()
            throws TicketException {
            // Act
            Ticket ticket1 = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );
            Ticket ticket2 = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            // Assert
            assertNotEquals(ticket1.getId(), ticket2.getId());
        }
    }

    @Nested
    @DisplayName("Mapping to events and customers")
    class MappingTests {

        @Test
        @DisplayName("Should map ticket to event")
        void shouldMapTicketToEvent() throws TicketException {
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            long eventId = createdTicket.getEventId();
            Event mappedEvent = eventService.getEventById(eventId);

            assert mappedEvent.getTicketsSold().contains(createdTicket.getId());
        }

        @Test
        @DisplayName("Should not map ticket to event")
        void shouldNotMapTicketToEvent() throws TicketException {
            Ticket createdTicketEvent1 = ticketService.createTicket(
                    testCustomer.getId(),
                    testEvent.getId()
            );

            Ticket createdTicketEvent2 = ticketService.createTicket(
                    testCustomer.getId(),
                    testEvent2.getId()
            );

            long eventId = createdTicketEvent1.getEventId();

            Event mappedEvent = eventService.getEventById(eventId);

            assertFalse(mappedEvent.getTicketsSold().contains(createdTicketEvent2.getId()));
        }

        @Test
        @DisplayName("Should map ticket to customer")
        void shouldMapTicketToCustomer() throws TicketException {
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            Customer mappedCustomer = customerService.getCustomerById(
                    createdTicket.getCustomerId()
            );

            assert mappedCustomer.getTicketsBought().contains(ticketService.getTicketById(createdTicket.getId()).getId());
        }

        @Test
        @DisplayName("Should not map ticket to customer")
        void shouldNotMapTicketToCustomer() throws TicketException {
            Ticket createdTicketCustomer1 = ticketService.createTicket(
                    testCustomer.getId(),
                    testEvent.getId()
            );

            Ticket createdTicketCustomer2 = ticketService.createTicket(
                    testCustomer2.getId(),
                    testEvent.getId()
            );

            long customerId = createdTicketCustomer1.getCustomerId();

            Customer mappedCustomer = customerService.getCustomerById(customerId);

            assertFalse(mappedCustomer.getTicketsBought().contains(createdTicketCustomer2.getId()));
        }

        @Test
        @DisplayName("Should delete tickets of deleted customers")
        void shouldDeleteTicketsOfDeletedCustomers() throws TicketException {
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            customerService.deleteCustomer(testCustomer.getId());

            TicketException exception = assertThrows(
                    TicketException.class,
                    () -> ticketService.getTicketById(createdTicket.getId())
            );

            assertEquals(TicketException.ticketDoesNotExist, exception.getMessage());
        }

        @Test
        @DisplayName("Should delete tickets of deleted events")
        void shouldDeleteTicketsOfDeletedEvents() throws TicketException {
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            long eventId = createdTicket.getEventId();

            eventService.deleteEvent(eventId);

            TicketException exception = assertThrows(
                    TicketException.class,
                    () -> ticketService.getTicketById(createdTicket.getId())
            );
            assertEquals(TicketException.ticketDoesNotExist, exception.getMessage());
        }

        @Test
        @DisplayName("Should delete all tickets when deleting all customers")
        void shouldDeleteAllTicketsWhenDeletingAllCustomers()
            throws TicketException {
            Customer newTestCustomer = customerService.createCustomer(
                "newUser",
                "test@example.org",
                LocalDate.now().minusYears(19)
            );

            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(newTestCustomer.getId(), testEvent.getId());

            customerService.deleteAllCustomers();
            assertTrue(ticketService.getAllTickets().isEmpty());
        }

        @Test
        @DisplayName("Should delete all tickets when deleting all events")
        void shouldDeleteAllTicketsWhenDeletingAllEvents()
            throws TicketException {
            Event newTestEvent = eventService.createEvent(
                "Test Event",
                "Test Location",
                LocalDateTime.now().plusHours(24),
                10
            );

            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(testCustomer.getId(), newTestEvent.getId());

            customerService.deleteAllCustomers();
            assertTrue(ticketService.getAllTickets().isEmpty());
        }

        @Test
        @DisplayName(
            "Should delete all ticket associations in customers when deleting all events"
        )
        void shouldDeleteAllTicketAssociationsInCustomersWhenDeletingAllEvents()
            throws TicketException {
            Event newTestEvent = eventService.createEvent(
                "Test Event",
                "Test Location",
                LocalDateTime.now().plusHours(24),
                10
            );

            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(testCustomer.getId(), newTestEvent.getId());

            eventService.deleteAllEvents();
            assertTrue(testCustomer.getTicketsBought().isEmpty());
        }

        @Test
        @DisplayName("Should delete all ticket associations in events when deleting all customers")
        void shouldDeleteAllTicketAssociationsInEventsWhenDeletingAllCustomers()
            throws TicketException {
            Customer newTestCustomer = customerService.createCustomer(
                "newUser",
                "test@example.org",
                LocalDate.now().minusYears(19)
            );

            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(newTestCustomer.getId(), testEvent.getId());

            customerService.deleteAllCustomers();
            assertEquals(0, testEvent.getTicketsSold().size());
        }

        @Test
        @DisplayName(
            "Should delete corresponding ticketBought by customer when deleting ticket"
        )
        void shouldDeleteCorrespondingTicketBoughtByCustomerWhenDeletingTicket()
            throws TicketException {
            // Arrange
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            // Act
            ticketService.deleteTicket(createdTicket.getId());

            // Assert
            assertFalse(
                testCustomer.getTicketsBought().contains(createdTicket.getId())
            );
        }

        @Test
        @DisplayName(
            "Should delete corresponding ticketSold of event for deleted ticket"
        )
        void shouldDeleteCorrespondingTicketSoldOfEventWhenDeletingTicket()
            throws TicketException {
            // Arrange
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            // Act
            ticketService.deleteTicket(createdTicket.getId());

            // Assert
            assertFalse(testEvent.getTicketsSold().contains(createdTicket.getId()));
        }

        @Test
        @DisplayName("Bought tickets should reduce available ticket amount")
        void shouldReduceAvailableTicketAmountWhenBuyingNewTickets()
            throws TicketException {
            // Arrange
            int initialTicketCount = testEvent.getTicketsAvailable().get();

            // Act
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());

            // Assert
            assertTrue(eventService.getEventById(testEvent.getId()).getTicketsAvailable().get() < initialTicketCount);
        }

        @Test
        @DisplayName("Should be unable to buy tickets for unavailable event")
        void shouldBeUnableToBuyTicketsForUnavailableEvent()
            throws TicketException {
            // Arrange
            Event unavailableTestEvent = eventService.createEvent(
                "Test Event",
                "Test Location",
                LocalDateTime.now().plusDays(7),
                0
            );

            // Act
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            // Assert
            // This should fail
            assertThrows(TicketException.class, () ->
                ticketService.createTicket(testCustomer.getId(), unavailableTestEvent.getId())
            );
        }

        @Test
        @DisplayName("Should be unable to buy more than five tickets for event per customer")
        void shouldBeUnableToBuyMoreThanFiveTicketsForEventPerCustomer()
            throws TicketException {
            for (int i = 0; i < 5; i++) {
                ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            }
            TicketException exception = assertThrows(TicketException.class, () ->
                ticketService.createTicket(testCustomer.getId(), testEvent.getId())
            );
            assertEquals(TicketException.maximumNumberOfTickets, exception.getMessage());
        }

        @Test
        @DisplayName(
            "Should increase availableTicketAmount when deleting ticket for event"
        )
        void shouldIncreaseAvailableTicketAmountWhenDeletingTicketForEvent()
            throws TicketException {
            // Arrange
            Ticket newTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );
            int availableTickets = eventService.getEventById(testEvent.getId()).getTicketsAvailable().get();

            // Act
            ticketService.deleteTicket(newTicket.getId());

            // Assert
            assertEquals(testEvent.getTicketsAvailable().get(), availableTickets + 1);
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should retrieve ticket by ID")
        void shouldRetrieveTicketById() throws TicketException {
            // Arrange
            Ticket createdTicket = ticketService.createTicket(
                testCustomer.getId(),
                testEvent.getId()
            );

            // Act
            Ticket retrievedTicket = ticketService.getTicketById(
                createdTicket.getId()
            );

            // Assert
            assertNotNull(retrievedTicket);
            assertEquals(createdTicket.getId(), retrievedTicket.getId());
            assertEquals(
                createdTicket.getCustomerId(),
                retrievedTicket.getCustomerId()
            );
            assertEquals(createdTicket.getEventId(), retrievedTicket.getEventId());
        }

        @Test
        @DisplayName("Should return Null for non-existent ticket ID")
        void shouldReturnNullForNonExistentTicketId() {
            // Arrange
            long nonExistentId = idService.getUnusedId();

            // Assert
            assertThrows(
                    EventException.class,
                    () -> eventService.getEventById(nonExistentId)
            );
        }

        @Test
        @DisplayName("Should delete ticket by ID")
        void shouldDeleteTicketById() throws TicketException {
            // Arrange
            Ticket ticket = ticketService.createTicket(testCustomer.getId(), testEvent.getId());

            // Act
            ticketService.deleteTicket(ticket.getId());

            // Assert
            assertThrows(
                    TicketException.class,
                    () -> ticketService.getTicketById(ticket.getId())
            );
        }

        @Test
        @DisplayName("Should throw exception when deleting with null ID")
        void shouldThrowExceptionWhenDeletingWithNullId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ticketService.deleteTicket(-1)
            );
            assertEquals("Ticket ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should get all tickets")
        void shouldGetAllTickets() throws TicketException {
            // Arrange
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());

            // Act
            List<Ticket> tickets = ticketService.getAllTickets();

            // Assert
            assertEquals(2, tickets.size());
        }

        @Test
        @DisplayName("Should return empty list when no tickets exist")
        void shouldReturnEmptyListWhenNoTicketsExist() {
            // Act
            List<Ticket> tickets = ticketService.getAllTickets();

            // Assert
            assertTrue(tickets.isEmpty());
        }

        @Test
        @DisplayName("Should delete all tickets")
        void shouldDeleteAllTickets() throws TicketException {
            // Arrange
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());
            ticketService.createTicket(testCustomer.getId(), testEvent.getId());

            // Act
            ticketService.deleteAllTickets();
            List<Ticket> tickets = ticketService.getAllTickets();

            // Assert
            assertTrue(tickets.isEmpty());
        }
    }
}
