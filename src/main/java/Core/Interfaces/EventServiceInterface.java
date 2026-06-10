package core.interfaces;

import core.models.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventServiceInterface {
    Event createEvent(String name, String location, LocalDateTime time, int ticketsAvailable) throws IllegalArgumentException;
    Event getEventById(long id);
    void updateEvent(Event event) throws IllegalArgumentException;
    void deleteEvent(long id) throws IllegalArgumentException;
    List<Event> getAllEvents();
    void deleteAllEvents();
}
