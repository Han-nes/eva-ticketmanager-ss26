package concurrence.performanceTests;

import core.models.Customer;
import core.models.Event;
import core.models.Ticket;

import java.util.ArrayList;
import java.util.List;

public class ThreadingCorrectnessHelper {

    public static List<Long> getIdsFromEvents(List<Event> events){
        List<Long> idsFromEvents = new ArrayList<>();
        for(Event event : events){
            idsFromEvents.add(event.getId());
        }
        return idsFromEvents;
    }

    public static List<Long> getIdsFromCustomer(List<Customer> customers){
        List<Long> idsFromCustomers = new ArrayList<>();
        for(Customer customer : customers){
            idsFromCustomers.add(customer.getId());
        }
        return idsFromCustomers;
    }

    public static List<Long> getIdsFromTickets(List<Ticket> tickets){
        List<Long> idsFromTickets = new ArrayList<>();
        for(Ticket ticket : tickets){
            idsFromTickets.add(ticket.getId());
        }
        return idsFromTickets;
    }
}
