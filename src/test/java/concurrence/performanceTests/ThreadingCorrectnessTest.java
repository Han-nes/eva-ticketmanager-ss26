package concurrence.performanceTests;

import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static concurrence.performanceTests.ThreadingCorrectnessHelper.*;
import static idGenerator.primeNumberGenerator.PrimeNumberGeneratorOptimized.isPrime;

public class ThreadingCorrectnessTest {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;

    public ThreadingCorrectnessTest(
            EventService eventService,
            TicketService ticketService,
            CustomerService customerService
    ) {
        this.eventService = eventService;
        this.customerService = customerService;
        this.ticketService = ticketService;
    }

    public void testEverything(int maximumAvailableTicketsPerEvent, int amountCustomerToBeCreated, int amountEventsToBeCreated, int amountTicketsToBeCreated){
        testIDs();
        checkAmountCreatedEntites(amountCustomerToBeCreated, amountEventsToBeCreated, amountTicketsToBeCreated);
        testMax5TicketsPerEventPerCustomer();
        testEventTicketContingentViaEvent(maximumAvailableTicketsPerEvent);
        testEventTicketContingentViaTicket(maximumAvailableTicketsPerEvent);
        testEventTicketContingentViaCustomer(maximumAvailableTicketsPerEvent);
    }

    public void testIDs() {
        boolean allIDsArePrimeNumbers = true;
        boolean allIDsAreUnique = true;
        List<Long> alreadyCheckedIDs = new ArrayList<>();

        List<Long> idList = getIdsFromEvents(eventService.getAllEvents());
        idList.addAll(getIdsFromCustomer(customerService.getAllCustomers()));
        idList.addAll(getIdsFromTickets(ticketService.getAllTickets()));

        System.out.println("\nTest der IDs:");

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
            System.out.println("All IDs are prime numbers :)");
        } else {
            System.out.println("ERROR: Not all IDs are prime numbers!");
        }

        if(allIDsAreUnique){
            System.out.println("All IDs are unique :)");
        } else {
            System.out.println("ERROR: Not all IDs are unique!");
        }
    }

    public void testMax5TicketsPerEventPerCustomer(){
        List<Customer> allCustomers = customerService.getAllCustomers();
        boolean moreThan5TicketsBought = false;
        for(Customer customer : allCustomers){
            Map<Long, Integer> ticketsBoughtPerEvent = new HashMap<>();
            for(Long ticketId : customer.getTicketsBought()){

                if(ticketsBoughtPerEvent.containsKey(ticketId)){
                    int ticketAmountPerEvent = ticketsBoughtPerEvent.get(ticketId) + 1;
                    if(ticketAmountPerEvent > 5){
                        System.out.println("Customer " + customer.getUsername() + " has bought more then 5 tickets for one event");
                        moreThan5TicketsBought = true;
                        break;
                    }
                    ticketsBoughtPerEvent.replace(ticketId, ticketAmountPerEvent);
                } else{
                    ticketsBoughtPerEvent.put(ticketId, 1);
                }
            }
        }
        if(!moreThan5TicketsBought) System.out.println("No customer has bought more then 5 tickets for one event");
    }

    public void testEventTicketContingentViaEvent(int maximumAvailableTicketsPerEvent){
        boolean ticketContingentExceeded = false;
        for(Event event : eventService.getAllEvents()){
            if(event.getTicketsSold().size() > maximumAvailableTicketsPerEvent){
                System.out.println("ERROR: Event " + event.getName() + " has sold " + event.getTicketsSold().size() + "Tickets, even though only " + maximumAvailableTicketsPerEvent + " tickets were available.");
            }
        }
        if(!ticketContingentExceeded) System.out.println("According to EventService no event exceeded it's ticket limit");
    }

    public void testEventTicketContingentViaCustomer(int maximumAvailableTicketsPerEvent){
        Map<Long, Integer> ticketsBoughtPerEvent = new HashMap<>();
        for(Customer customer : customerService.getAllCustomers()){
            for(Long ticketId : customer.getTicketsBought()){
                if(ticketsBoughtPerEvent.containsKey(ticketId)){
                    int amountTicketsPerEvent = ticketsBoughtPerEvent.get(ticketId) + 1;
                    ticketsBoughtPerEvent.replace(ticketId, amountTicketsPerEvent);
                } else {
                    ticketsBoughtPerEvent.put(ticketId, 1);
                }
            }
        }

        boolean ticketContingentExceeded = false;
        for(long eventId : ticketsBoughtPerEvent.keySet()){
            if(ticketsBoughtPerEvent.get(eventId) > maximumAvailableTicketsPerEvent){
                System.out.println("Event " + eventId + " has sold " + ticketsBoughtPerEvent.get(eventId) + " ticket, even though only " + maximumAvailableTicketsPerEvent + " tickets were available");
                ticketContingentExceeded = true;
            }
        }
        if(!ticketContingentExceeded) System.out.println("According to CustomerService no event exceeded it's ticket limit");
    }

    public void testEventTicketContingentViaTicket(int maximumAvailableTicketsPerEvent){
        Map<Long, Integer> ticketsPerEvent = new HashMap<>();
        for(Ticket ticket : ticketService.getAllTickets()){
            if(ticketsPerEvent.containsKey(ticket.getEventId())){
                int amountTicketsPerEvent = ticketsPerEvent.get(ticket.getEventId()) + 1;
                ticketsPerEvent.replace(ticket.getEventId(), amountTicketsPerEvent);
            } else {
                ticketsPerEvent.put(ticket.getEventId(), 1);
            }
        }

        boolean ticketContingentExceeded = false;
        for(long eventId : ticketsPerEvent.keySet()){
            if(ticketsPerEvent.get(eventId) > maximumAvailableTicketsPerEvent){
                System.out.println("ERROR: Event " + eventId + " has sold " + ticketsPerEvent.get(eventId) + " tickets, even though only " + maximumAvailableTicketsPerEvent + " tickets were available");
                ticketContingentExceeded = true;
            }
        }
        if(!ticketContingentExceeded) System.out.println("According to TicketService no event exceeded it's ticket limit");
    }

    public void checkAmountCreatedEntites(int amountCustomersToBeCreated, int amountEventsToBeCreated, int amountTicketsToBeCreated){
        System.out.println("\nChecking the amount of created entities:");
        if(customerService.getAllCustomers().size() == amountCustomersToBeCreated){
            System.out.println(customerService.getAllCustomers().size() + " customers of " + amountCustomersToBeCreated + " have been created");
        } else {
            System.out.println("ERROR: " + customerService.getAllCustomers().size() + " customers have been created instead of " + amountCustomersToBeCreated);
        }

        if(eventService.getAllEvents().size() == amountEventsToBeCreated){
            System.out.println(eventService.getAllEvents().size() + " events of " + amountEventsToBeCreated + " have been created");
        } else {
            System.out.println("ERROR: " + eventService.getAllEvents().size() + " events have been created instead of " + amountEventsToBeCreated);
        }

        if(ticketService.getAllTickets().size() == amountTicketsToBeCreated){
            System.out.println(ticketService.getAllTickets().size() + " tickets of " + amountTicketsToBeCreated + " have been created");
        } else {
            System.out.println("ERROR: " + ticketService.getAllTickets().size() + " tickets have been created instead of " + amountTicketsToBeCreated);
        }
    }
}
