package tcp.client.ticketShop;

import core.models.Customer;
import core.models.Event;
import core.models.Ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TicketShopStringFormatter {

    public static Event eventFromString(String string) {
        String[] parts = string.split(",");
        long id = Long.parseLong(parts[0].split("=")[1].trim());
        String name = parts[1].split("=")[1].trim().replace("'", "");
        String location = parts[2].split("=")[1].trim().replace("'", "");
        LocalDateTime time = LocalDateTime.parse(parts[3].split("=")[1].trim());
        int ticketsAvailable = Integer.parseInt(parts[4].split("=")[1].trim());
        return new Event(id, name, location, time, ticketsAvailable);
    }

    public static Customer customerFromString(String string) {
        String[] parts = string.split(",");
        long id = Long.parseLong(parts[0].split("=")[1].trim());
        String username = parts[1].split("=")[1].trim().replace("'", "");
        String email = parts[2].split("=")[1].trim().replace("'", "");
        LocalDate dateOfBirth = LocalDate.parse(
                parts[3].split("=")[1].trim().replace("'", "")
        );
        return new Customer(id, username, email, dateOfBirth);
    }

    public static String eventToString(Event event) {
        return String.format(
                "Event{id=%s,name='%s',location='%s',time=%s,ticketsAvailable=%s,ticketsSold=%s}",
                event.getId(),
                event.getName(),
                event.getLocation(),
                event.getTime(),
                event.getTicketsAvailable(),
                event.getTicketsSold()
        );
    }

    public static String ticketToString(Ticket ticket) {
        return String.format(
                "id=%s,dateOfPurchase=%s,customerId=%s,eventId=%s",
                ticket.getId(),
                ticket.getDateOfPurchase(),
                ticket.getCustomerId(),
                ticket.getEventId()
        );
    }

    public static String customerToString(Customer customer) {
        return String.format(
                "Customer{id=%s, username='%s', email='%s', dateOfBirth='%s', ticketsBought='%s'}",
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getDateOfBirth(),
                customer.getTicketsBought()
        );
    }
}
