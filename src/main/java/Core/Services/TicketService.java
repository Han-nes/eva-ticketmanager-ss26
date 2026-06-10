package core.services;

import core.models.exceptions.CustomerException;
import core.models.exceptions.EventException;
import core.models.exceptions.TicketException;
import core.interfaces.TicketServiceInterface;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import core.models.Event;
import core.models.Ticket;
import idGenerator.idService.IDServiceInterface;

public class TicketService implements TicketServiceInterface {

    private final Map<Long, Ticket> ticketsById = new ConcurrentHashMap<>();
    private CustomerService customerService;
    private EventService eventService;
    private final IDServiceInterface idService;

    public TicketService(IDServiceInterface idService){
        this.idService = idService;
    }

    public void setCustomerService(CustomerService customerService){
        this.customerService = customerService;
    }

    public void setEventService(EventService eventService){
        this.eventService = eventService;
    }

    @Override
    public Ticket createTicket(long customerId, long eventId) throws TicketException, EventException, CustomerException {
        Ticket ticket = new Ticket(idService.getUnusedId(), LocalDate.now(), customerId, eventId);
        saveTicket(ticket);
        eventService.ticketSoldForEvent(ticket);
        customerService.addTicketToCustomer(ticket);
        return getTicketById(ticket.getId());
    }

    @Override
    public Ticket getTicketById(long id) throws TicketException {
        if(id <= 0 || !ticketsById.containsKey(id)){
            throw TicketException.ticketDoesNotExist();
        }
            return clone(ticketsById.get(id));
    }

    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> allTickets = new ArrayList<>();
        for(long ticketId : ticketsById.keySet()){
            try {
                allTickets.add(getTicketById(ticketId));
            } catch (TicketException ticketException){
                System.out.println("Fehler beim Ziehen eines Tickets");
            }
        }
        return allTickets;
        //return new ArrayList<>(ticketsById.values());
    }

    @Override
    public void deleteTicket(long id) throws IllegalArgumentException {
        if (id <= 0 || !ticketsById.containsKey(id)) {
            throw new IllegalArgumentException("Ticket ID cannot be null");
        }
        Ticket deletedTicket = ticketsById.remove(id);
        if (deletedTicket != null) {
            try {
                eventService.deleteTicketSoldForEvent(deletedTicket);
            } catch (EventException ignored){}
            try {
                customerService.removeTicketFromCustomer(deletedTicket);
            } catch (CustomerException ignored){}
        }
    }

    @Override
    public void deleteAllTickets() {
        for (Ticket ticket : ticketsById.values()) {
            try {
                eventService.deleteTicketSoldForEvent(ticket);
            } catch (EventException ignored){}
            try {
                customerService.removeTicketFromCustomer(ticket);
            } catch (CustomerException ignored){}
        }
        ticketsById.clear();
    }

    private void validateTicket(Ticket ticket) throws TicketException, CustomerException, EventException {
        customerService.getCustomerById(ticket.getCustomerId());
        Event event = eventService.getEventById(ticket.getEventId());

        if (!event.hasAvailableTickets()) {
            throw TicketException.noTicketsAvailable();
        }

        int ticketsBoughtForSameEvent = 0;
        for(long eventTicket : event.getTicketsSold()){
            if(getTicketById(eventTicket).getCustomerId() == ticket.getCustomerId()){
                ticketsBoughtForSameEvent++;
            }
        }
        if(ticketsBoughtForSameEvent >= 5){
            throw TicketException.maximumNumberOfTickets();
        }

    }

    private void saveTicket(Ticket ticket) throws TicketException {
        validateTicket(ticket);
        ticketsById.put(ticket.getId(), clone(ticket));
    }

    private Ticket clone(Ticket ticket){
        return new Ticket(
                ticket.getId(),
                ticket.getDateOfPurchase(),
                ticket.getCustomerId(),
                ticket.getEventId()
        );
    }
}
