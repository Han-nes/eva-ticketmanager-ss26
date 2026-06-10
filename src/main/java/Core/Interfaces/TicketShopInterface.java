package core.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import core.models.exceptions.TicketException;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;

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
}
