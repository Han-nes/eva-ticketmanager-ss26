package Core.Interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import Core.Models.exceptions.TicketException;
import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.Ticket;

public interface TicketShopInterface {
    List<Event> getAllEvents();
    Event createEvent(String name, String location, LocalDateTime time, int ticketsAvailable);
    Event getEventById(long id);
    void updateEvent(Event event);
    void deleteEvent(long id);
    void deleteAllEvents();

    List<Customer> getAllCustomers();
    Customer createCustomer(String username, String email, LocalDate dateOfBirth);
    Customer getCustomerById(long id);
    void updateCustomer(Customer customer);
    void deleteCustomer(long id);
    void deleteAllCustomers();

    List<Ticket> getAllTickets();
    Ticket createTicket(long customerId, long eventId) throws TicketException;
    Ticket getTicketById(long id) throws TicketException;
    void deleteTicket(long id);
    void deleteAllTickets();
    boolean verifyTicket(long id);
}
