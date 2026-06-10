package core.services;

import core.models.exceptions.EventException;
import core.interfaces.EventServiceInterface;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import core.models.Event;
import core.models.Ticket;
import idGenerator.idService.IDServiceInterface;

public class EventService implements EventServiceInterface {

    private final Map<Long, Event> eventsById = new ConcurrentHashMap<>();
    private final TicketService ticketService;
    private final IDServiceInterface idService;

    public EventService(TicketService ticketService, IDServiceInterface idService) {
        this.ticketService = ticketService;
        this.idService = idService;
    }

    public Event createEvent(
        String name,
        String location,
        LocalDateTime time,
        int ticketsAvailable
    ) throws EventException {

        Event event = new Event(
            idService.getUnusedId(),
            name,
            location,
            time,
            ticketsAvailable
        );

        saveEvent(event);
        return event;
    }

    public void ticketSoldForEvent(Ticket ticket) {
        Event event = getEventById(ticket.getEventId());
        event.ticketSold(ticket.getId());
        eventsById.put(event.getId(), event);
    }

    public void deleteTicketSoldForEvent(Ticket ticket){
        Event event = getEventById(ticket.getEventId());
        event.ticketDeleted(ticket.getId());
        eventsById.put(event.getId(), event);
    }

    @Override
    public Event getEventById(long id) {
            if(id <= 0 || !eventsById.containsKey(id)){
                throw EventException.eventDoesNotExist();
            }
        return clone(eventsById.get(id));
    }

    @Override
    public void updateEvent(Event event) throws EventException {
        validateUpdatedEvent(event);
        saveEvent(event);
    }

    private void validateUpdatedEvent(Event event){
        Event eventBeforeUpdate = getEventById(event.getId());
        if (event.getTicketsAvailable() < eventBeforeUpdate.getTicketsAvailable()) {
            throw EventException.shouldNotReduceAvailableTicketsWithUpdate();
        }

        if (!event.getTime().isAfter(LocalDateTime.now())) {
            throw EventException.cantSetEventTimeIntoPast();
        }
    }

    @Override
    public void deleteEvent(long id) {
        Event deletedEvent = eventsById.remove(id);
        if (deletedEvent != null) {
            List<Long> ticketIds = new ArrayList<>(deletedEvent.getTicketsSold());
            ticketIds.forEach(ticketService::deleteTicket);
        }
    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> allEvents = new ArrayList<>();
        for(long eventId : eventsById.keySet()){
            try {
                allEvents.add(getEventById(eventId));
            } catch (EventException eventException){
                System.out.println("Fehler beim Ziehen eines Events");
            }
        }
        return allEvents;
        //return new ArrayList<>(eventsById.values());
    }

    @Override
    public void deleteAllEvents() {
        eventsById.clear();
        ticketService.deleteAllTickets();
    }

    private void saveEvent(Event event) throws EventException{
        validateEvent(event);
        eventsById.put(event.getId(), clone(event));
    }

    private void validateEvent(Event event){
        if (event.getTicketsAvailable() < 0) {
            throw EventException.negativeTicketsAvailable();
        }
    }

    private Event clone(Event event){
        Event clonedEvent = new Event(
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getTime(),
                event.getTicketsAvailable()
        );
        clonedEvent.getTicketsSold().addAll(event.getTicketsSold());
        return clonedEvent;
    }
}
