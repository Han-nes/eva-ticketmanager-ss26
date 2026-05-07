package services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import Core.Models.exceptions.EventException;
import Core.Models.Event;
import Core.Services.EventService;
import Core.Services.TicketService;
import IDGenerator.IDService.IDService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EventServiceTest {
    private final IDService idService = new IDService(10000L, 99999L);
    private EventService eventService;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        this.eventService = new EventService(new TicketService(idService), idService);
    }

    @Nested
    @DisplayName("Create Event Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event with valid data")
        void shouldCreateEventWithValidData() throws IllegalArgumentException {
            // Arrange
            String name = "Test Concert";
            String location = "Test Arena";
            LocalDateTime time = LocalDateTime.now().plusDays(7);
            int ticketsAvailable = 100;

            // Act
            Event event = eventService.createEvent(
                name,
                location,
                time,
                ticketsAvailable
            );

            // Assert
            assertNotNull(event);
            assertNotNull(event.getId());
            assertEquals(name, event.getName());
            assertEquals(location, event.getLocation());
            assertEquals(time, event.getTime());
            assertEquals(ticketsAvailable, event.getTicketsAvailable().get());
        }

        @Test
        @DisplayName("Should create event with zero tickets available")
        void shouldCreateEventWithZeroTicketsAvailable()
            throws IllegalArgumentException {
            // Arrange
            String name = "Sold Out Event";
            String location = "Test Venue";
            LocalDateTime time = LocalDateTime.now().plusDays(5);
            int ticketsAvailable = 0;

            // Act
            Event event = eventService.createEvent(
                name,
                location,
                time,
                ticketsAvailable
            );

            // Assert
            assertNotNull(event);
            assertEquals(0, event.getTicketsAvailable().get());
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should retrieve event by ID")
        void shouldRetrieveEventById() throws IllegalArgumentException {
            // Arrange
            Event createdEvent = eventService.createEvent(
                "Test Event",
                "Test Location",
                LocalDateTime.now().plusDays(1),
                50
            );

            // Act
            Event retrievedEvent = eventService.getEventById(
                createdEvent.getId()
            );

            // Assert
            assertNotNull(retrievedEvent);
            assertEquals(createdEvent.getId(), retrievedEvent.getId());
            assertEquals(createdEvent.getName(), retrievedEvent.getName());
        }

        @Test
        @DisplayName("Should return Null for non-existent event ID")
        void shouldReturnNullForNonExistentEventId() {
            // Arrange
            long nonExistentId = -1;

            //Act and Assert
            assertThrows(
                    EventException.class,
                    () -> eventService.getEventById(nonExistentId)
            );
        }

        @Test
        @DisplayName("Should update existing event")
        void shouldUpdateExistingEvent() throws IllegalArgumentException {
            // Arrange
            Event event = eventService.createEvent(
                "Old Name",
                "Old Location",
                LocalDateTime.now().plusDays(1),
                50
            );
            event.setName("New Name");
            event.setLocation("New Location");

            // Act
            eventService.updateEvent(event);
            Event updatedEvent = eventService.getEventById(event.getId());

            // Assert
            assertEquals("New Name", updatedEvent.getName());
            assertEquals("New Location", updatedEvent.getLocation());
        }

        @Test
        @DisplayName("Should be unable to reduce number of available event tickets")
        void shouldBeUnableToReduceNumberOfAvailableEventTickets() {
            // Arrange
            int previouslyAvailableTickets = 50;
            Event testEvent = eventService.createEvent(
                "Name",
                "Location",
                LocalDateTime.now().plusDays(1),
                previouslyAvailableTickets
            );
            int newlyAvailableTickets = previouslyAvailableTickets - 1;

            testEvent.setTicketsAvailable(newlyAvailableTickets);

            // Act & Assert
            EventException exception = assertThrows(
                EventException.class,
                () -> eventService.updateEvent(testEvent)
            );
            assertEquals(
                EventException.shouldNotReduceAvailableTicketsWithUpdate,
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should delete event by ID")
        void shouldDeleteEventById() throws IllegalArgumentException {
            // Arrange
            Event event = eventService.createEvent(
                "Test Event",
                "Test Location",
                LocalDateTime.now().plusDays(1),
                50
            );

            // Act
            eventService.deleteEvent(event.getId());

            // Assert
            assertThrows(
                    EventException.class,
                    () -> eventService.getEventById(event.getId())
            );
        }

        @Test
        @DisplayName("Should get all events")
        void shouldGetAllEvents() throws IllegalArgumentException {
            // Arrange
            eventService.createEvent(
                "Event 1",
                "Location 1",
                LocalDateTime.now().plusDays(1),
                50
            );
            eventService.createEvent(
                "Event 2",
                "Location 2",
                LocalDateTime.now().plusDays(2),
                100
            );

            // Act
            List<Event> events = eventService.getAllEvents();

            // Assert
            assertEquals(2, events.size());
        }

        @Test
        @DisplayName("Should delete all events")
        void shouldDeleteAllEvents() throws IllegalArgumentException {
            // Arrange
            eventService.createEvent(
                "Event 1",
                "Location 1",
                LocalDateTime.now().plusDays(1),
                50
            );
            eventService.createEvent(
                "Event 2",
                "Location 2",
                LocalDateTime.now().plusDays(2),
                100
            );

            // Act
            eventService.deleteAllEvents();
            List<Event> events = eventService.getAllEvents();

            // Assert
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Event test")
    class UpdateEventTests {

        @BeforeEach
        void setUp() {
            String name = "testevent";
            String location = "testlocation";
            LocalDateTime date = LocalDateTime.now().plusDays(7);
            int ticketsAvailable = 100;

            testEvent = eventService.createEvent(
                name,
                location,
                date,
                ticketsAvailable
            );
        }

        @Test
        @DisplayName("Should update event with valid data")
        void shouldUpdateEventWithValidData() {
            // Arrange
            String name = "newname";
            String location = "newlocation";
            LocalDateTime time = LocalDateTime.now().plusYears(19);
            int ticketsAvailable = 150;

            // Act
            testEvent.setName(name);
            testEvent.setLocation(location);
            testEvent.setTime(time);
            testEvent.setTicketsAvailable(ticketsAvailable);
            eventService.updateEvent(testEvent);
            Event updatedEvent = eventService.getEventById(testEvent.getId());
            // Assert
            assertNotNull(updatedEvent);
            assertEquals(name, updatedEvent.getName());
            assertEquals(location, updatedEvent.getLocation());
            assertEquals(time, updatedEvent.getTime());
        }

        @Test
        @DisplayName("Should throw exception for past event")
        void shouldThrowExceptionForPastEvent() {
            // Arrange
            LocalDateTime time = LocalDateTime.now().minusSeconds(1);

            // Act & Assert
            EventException exception = assertThrows(
                EventException.class,
                () -> {
                    testEvent.setTime(time);
                    eventService.updateEvent(testEvent);
                }
            );
            assertEquals(EventException.cantSetEventTimeIntoPast, exception.getMessage());
        }

        @Test
        @DisplayName("Shouldn't be able to Insert Ticket via Update")
        void shouldNotInsertTicketViaUpdate(){
            // Arrange
            Event newEvent = new Event(idService.getUnusedId(), "testEvent" , "testLocation", LocalDateTime.now(), 100);

            // Act

            // Act + Assert
            EventException exception = assertThrows(
                    EventException.class,
                    () -> eventService.updateEvent(newEvent)
            );
            assertEquals(EventException.eventDoesNotExist, exception.getMessage());
        }
    }
}
