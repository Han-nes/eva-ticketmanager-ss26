package tcp.client.ticketShop;

import core.interfaces.TicketShopInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import tcp.client.ticketShop.commandHandler.TCPTicketShopCustomerHandler;
import tcp.client.ticketShop.commandHandler.TCPTicketShopEventHandler;
import tcp.client.ticketShop.commandHandler.TCPTicketShopTicketHandler;
import tcp.client.TcpClient;

public class TCPTicketShop implements TicketShopInterface {

    private final TCPTicketShopEventHandler eventHandler;
    private final TCPTicketShopCustomerHandler customerHandler;
    private final TCPTicketShopTicketHandler ticketHandler;

    public TCPTicketShop(TcpClient tcpClient) {
        eventHandler = new TCPTicketShopEventHandler(tcpClient);
        customerHandler = new TCPTicketShopCustomerHandler(tcpClient);
        ticketHandler = new TCPTicketShopTicketHandler(tcpClient);
    }

    // --- Event operations ---

    @Override
    public List<Event> getAllEvents() {
        return eventHandler.getAllEvents();
    }

    @Override
    public Event createEvent(
        String name,
        String location,
        LocalDateTime time,
        int ticketsAvailable
    ) {
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

    // --- Customer operations ---

    @Override
    public List<Customer> getAllCustomers() {
        return customerHandler.getAllCustomers();
    }

    @Override
    public Customer createCustomer(
        String username,
        String email,
        LocalDate dateOfBirth
    ) {
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

    // --- Ticket operations ---

    @Override
    public List<Ticket> getAllTickets() {
        return ticketHandler.getAllTickets();
    }

    @Override
    public Ticket createTicket(long customerId, long eventId) {
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
