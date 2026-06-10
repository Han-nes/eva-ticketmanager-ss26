package core.interfaces;

import core.models.exceptions.TicketException;
import java.util.List;

import core.models.Ticket;

public interface TicketServiceInterface {
    Ticket createTicket(long customerId, long eventId)
        throws IllegalArgumentException, TicketException;
    Ticket getTicketById(long id) throws TicketException;
    List<Ticket> getAllTickets();
    void deleteTicket(long id) throws IllegalArgumentException;
    void deleteAllTickets();
}
