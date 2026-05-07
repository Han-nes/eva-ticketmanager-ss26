package Core.Interfaces;

import Core.Models.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventServiceInterface {
    Event createEvent(String name, String location, LocalDateTime time, int ticketsAvailable) throws IllegalArgumentException;
    Event getEventById(long id);
    void updateEvent(Event event) throws IllegalArgumentException;
    void deleteEvent(long id) throws IllegalArgumentException;
    List<Event> getAllEvents();
    void deleteAllEvents();
}
