package Core.Interfaces;

import Core.Models.exceptions.TicketException;
import java.util.List;
import java.util.UUID;
import Core.Models.Ticket;

public interface TicketServiceInterface {
    Ticket createTicket(long customerId, long eventId)
        throws IllegalArgumentException, TicketException;
    Ticket getTicketById(long id) throws TicketException;
    List<Ticket> getAllTickets();
    void deleteTicket(long id) throws IllegalArgumentException;
    void deleteAllTickets();
    boolean verifyTicket(long id);
}
