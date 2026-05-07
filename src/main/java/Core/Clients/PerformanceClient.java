package Core.Clients;

import Core.Models.exceptions.TicketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.Ticket;
import Core.Services.CustomerService;
import Core.Services.EventService;
import Core.Services.TicketService;
import IDGenerator.IDService.IDServiceInterface;

public class PerformanceClient {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;

    private final int amountEventsToBeCreated = 100;
    private final int amountCustomersToBeCreated = 100;


    public PerformanceClient(IDServiceInterface idService) {
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);
    }

    public void run() {
        System.out.println("Test consecutive");
        testConsecutive();

        testIDs();
    }

    private List<Long> getIdsFromEvents(List<Event> events){
        List<Long> idsFromEvents = new ArrayList<>();
        for(Event event : events){
            idsFromEvents.add(event.getId());
        }
        return idsFromEvents;
    }

    private List<Long> getIdsFromCustomer(List<Customer> customers){
        List<Long> idsFromCustomers = new ArrayList<>();
        for(Customer customer : customers){
            idsFromCustomers.add(customer.getId());
        }
        return idsFromCustomers;
    }

    private List<Long> getIdsFromTickets(List<Ticket> tickets){
        List<Long> idsFromTickets = new ArrayList<>();
        for(Ticket ticket : tickets){
            idsFromTickets.add(ticket.getId());
        }
        return idsFromTickets;
    }

    private void testIDs() {
        boolean allIDsArePrimeNumbers = true;
        boolean allIDsAreUnique = true;
        List<Long> alreadyCheckedIDs = new ArrayList<>();

        List<Long> idList = getIdsFromEvents(eventService.getAllEvents());
        idList.addAll(getIdsFromCustomer(customerService.getAllCustomers()));
        idList.addAll(getIdsFromTickets(ticketService.getAllTickets()));

        for(Long id : idList){
            if(!isPrime(id)){
                System.out.println(id + " is not a prime number!");
                allIDsArePrimeNumbers = false;
            }
            if(alreadyCheckedIDs.contains(id)){
                System.out.println(id + " is already used!");
                allIDsAreUnique = false;
            }
            alreadyCheckedIDs.add(id);
        }

        if(allIDsArePrimeNumbers){
            System.out.println("all IDs are prime numbers :)");
        } else {
            System.out.println("NOT all IDs are prime numbers!");
        }

        if(allIDsAreUnique){
            System.out.println("all IDs are unique :)");
        } else {
            System.out.println("NOT all IDs are unique!");
        }
    }

    private boolean isPrime(long number) {
        if (number <= 1) return false;
        for (long i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) return false;
        }
        return true;
    }

    private void testConsecutive() {
        long startTime = System.currentTimeMillis();
        List<Event> events = createEvents(amountEventsToBeCreated);
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        List<Customer> customers = createCustomers(amountCustomersToBeCreated);
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        buyTickets(events, customers);
        endTime = System.currentTimeMillis();
        System.out.println(
            "Time to buy tickets: " + (endTime - startTime) + "ms"
        );
    }

    private List<Event> createEvents(int amount) {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Event new_event = eventService.createEvent(
                "Event" + i,
                "Location" + i,
                LocalDateTime.now().plusDays(7 + i),
                1000
            );
            events.add(new_event);
        }

        return events;
    }

    private List<Customer> createCustomers(int amount) {
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Customer newCustomer = customerService.createCustomer(
                "User" + i,
                "user" + i + "@test.org",
                LocalDate.now().minusYears(18 + i)
            );
            customers.add(newCustomer);
        }
        return customers;
    }

    private List<Ticket> buyTickets(
        List<Event> events,
        List<Customer> customers
    ) {
        List<Ticket> tickets = new ArrayList<>();
        for (Event event : events) {
            for (Customer customer : customers) {
                try {
                    tickets.add(ticketService.createTicket(customer.getId(), event.getId()));
                } catch (TicketException e) {
                    break;
                }
            }
        }
        return tickets;
    }


    private List<Ticket> buyTicketsForEventSequential(
        Event event,
        List<Customer> customers
    ) {
        List<Ticket> tickets = new ArrayList<>();
        for (Customer customer : customers) {
            try {
                Ticket newTicket = ticketService.createTicket(customer.getId(), event.getId());
                tickets.add(newTicket);
            } catch (TicketException e) {
                // Stop when event is sold out
                break;
            }
        }
        return tickets;
    }

}
