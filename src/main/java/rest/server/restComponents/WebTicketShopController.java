package rest.server.restComponents;

import java.util.List;
import java.util.NoSuchElementException;

import idGenerator.idService.IDService;
import rest.server.createRecords.CreateCustomerRequest;
import rest.server.createRecords.CreateEventRequest;
import rest.server.createRecords.CreateTicketRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import core.models.exceptions.TicketException;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;


@RestController
public class WebTicketShopController {
    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;


    public WebTicketShopController() {
        IDService idService = new IDService(10000L, 99999L);
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);
    }

    public Event createEvent(@RequestBody CreateEventRequest request) {
        //todo
        return null;
    }

    public List<Event> getAllEvents() {
        //todo
        return null;
    }

    public Event getEventById(@PathVariable long id) {
        //todo
        return null;
    }


    public void updateEvent(@RequestBody Event event) {
        //todo
    }

    public void deleteEvent(@PathVariable long id) {
        //todo
    }

    public void deleteAllEvents() {
        //todo
    }

    public Customer createCustomer(@RequestBody CreateCustomerRequest request) {
        //todo
        return null;
    }

    public List<Customer> getAllCustomers() {
        //todo
        return null;
    }


    public Customer getCustomerById(@PathVariable long id) {
        //todo
        return null;
    }


    public void updateCustomer(@RequestBody Customer customer) {
        //todo
    }

    public void deleteCustomer(@PathVariable long id) {
        //todo
    }

    public void deleteAllCustomers() {
        //todo
    }

    public Ticket createTicket(@RequestBody CreateTicketRequest request) throws TicketException {
        //todo
        return null;
    }

    public List<Ticket> getAllTickets() {
        //todo
        return null;
    }

    public Ticket getTicketById(@PathVariable long id) throws TicketException {
        //todo
        return null;
    }

    public void deleteTicket(@PathVariable long id) {
        //todo
    }

    public void deleteAllTickets() {
        //todo
    }
}
