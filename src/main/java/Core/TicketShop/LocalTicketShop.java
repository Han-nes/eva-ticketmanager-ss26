package core.ticketShop;

import core.models.exceptions.TicketException;
import core.interfaces.TicketShopInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDService;

public class LocalTicketShop implements TicketShopInterface {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;


    public LocalTicketShop() {
        IDService idService = new IDService(100000000000000000L, 999999999999999999L);
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);

    }

    // Event operations
    @Override
    public Event createEvent(
        String name,
        String location,
        LocalDateTime time,
        int tickets
    ) {
        return eventService.createEvent(name, location, time, tickets);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @Override
    public Event getEventById(long id) {
        return eventService.getEventById(id);
    }

    @Override
    public void updateEvent(Event event) {
        eventService.updateEvent(event);
    }

    @Override
    public void deleteEvent(long id) {
        eventService.deleteEvent(id);
    }

    @Override
    public void deleteAllEvents() {
        eventService.deleteAllEvents();
    }

    // Customer operations
    @Override
    public Customer createCustomer(
        String username,
        String email,
        LocalDate dateOfBirth
    ) {
        return customerService.createCustomer(username, email, dateOfBirth);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @Override
    public Customer getCustomerById(long id) {
        return customerService.getCustomerById(id);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerService.updateCustomer(customer);
    }

    @Override
    public void deleteCustomer(long id) {
        customerService.deleteCustomer(id);
    }

    @Override
    public void deleteAllCustomers() {
        customerService.deleteAllCustomers();
    }

    // Ticket Operations
    @Override
    public Ticket createTicket(long customerId, long eventId)
        throws TicketException {
        return ticketService.createTicket(customerId, eventId);
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @Override
    public Ticket getTicketById(long id) throws TicketException {
        return ticketService.getTicketById(id);
    }

    @Override
    public void deleteTicket(long id) {
        ticketService.deleteTicket(id);
    }

    @Override
    public void deleteAllTickets() {
        ticketService.deleteAllTickets();
    }
}
