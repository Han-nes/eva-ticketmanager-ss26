package rest.client;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import core.models.exceptions.TicketException;
import core.interfaces.TicketShopInterface;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import rest.client.commandHandler.CommunicationHandler;
import rest.client.commandHandler.RestTicketShopCustomerHandler;
import rest.client.commandHandler.RestTicketShopEventHandler;
import rest.client.commandHandler.RestTicketShopTicketHandler;

public class RestTicketShop implements TicketShopInterface {
    private final RestTicketShopEventHandler eventHandler;
    private final RestTicketShopTicketHandler ticketHandler;
    private  final RestTicketShopCustomerHandler customerHandler;

    public RestTicketShop(HttpClient httpClient, String baseUrl) {
        CommunicationHandler communicationHandler = new CommunicationHandler(httpClient);
        this.eventHandler = new RestTicketShopEventHandler(communicationHandler, baseUrl);
        this.customerHandler = new RestTicketShopCustomerHandler(communicationHandler, baseUrl);
        this.ticketHandler = new RestTicketShopTicketHandler(communicationHandler, baseUrl);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventHandler.getAllEvents();
    }

    @Override
    public Event createEvent(String name, String location, LocalDateTime time, int ticketsAvailable) {
        return eventHandler.createEvent(name, location, time, ticketsAvailable);
    }

    @Override
    public Event getEventById(long id) {
        return eventHandler.getEventById(id);
    }

    @Override
    public void updateEvent(Event event) {
        eventHandler.updateEvent(event);
    }

    @Override
    public void deleteEvent(long id) {
        eventHandler.deleteEvent(id);
    }

    @Override
    public void deleteAllEvents() {
        eventHandler.deleteAllEvents();
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerHandler.getAllCustomers();
    }

    @Override
    public Customer createCustomer(String username, String email, LocalDate dateOfBirth) {
        return customerHandler.createCustomer(username, email, dateOfBirth);
    }

    @Override
    public Customer getCustomerById(long id) {
        return customerHandler.getCustomerById(id);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerHandler.updateCustomer(customer);
    }

    @Override
    public void deleteCustomer(long id) {
        customerHandler.deleteCustomer(id);
    }

    @Override
    public void deleteAllCustomers() {
        customerHandler.deleteAllCustomers();
    }

    @Override
    public List<Ticket> getAllTickets() {
        return ticketHandler.getAllTickets();
    }

    @Override
    public Ticket createTicket(long customerId, long eventId) throws TicketException {
        return ticketHandler.createTicket(customerId, eventId);
    }

    @Override
    public Ticket getTicketById(long id) {
        return ticketHandler.getTicketById(id);
    }

    @Override
    public void deleteTicket(long id) {
        ticketHandler.deleteTicket(id);
    }

    @Override
    public void deleteAllTickets() {
        ticketHandler.deleteAllTickets();
    }
}
