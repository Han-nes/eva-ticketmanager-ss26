package core.clients.commandHandler;

import core.models.Customer;
import core.models.Event;
import core.models.Ticket;

import java.time.format.DateTimeFormatter;

public class ConsoleFormatter {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String formatTicket(Ticket ticket) {
        return String.format(
                "Ticket ID: %s\nEvent: \n%s\nCustomer: \n%s\nDate of Purchase: %s",
                ticket.getId(),
                //formatEvent(ticket.getEvent()),
                //formatCustomer(ticket.getCustomer()),
                ticket.getEventId(),
                ticket.getCustomerId(),
                ticket
                        .getDateOfPurchase()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        );
    }

    public static String formatCustomer(Customer customer) {
        return String.format(
                "%s - %s (%s)",
                customer.getUsername(),
                customer.getEmail(),
                customer.getDateOfBirth() != null
                        ? customer.getDateOfBirth()
                        : "No birthday"
        );
    }

    public static String formatEvent(Event event) {
        return String.format(
                "%s - %s (%s) - %d tickets",
                event.getName(),
                event.getLocation(),
                event.getTime().format(DATE_TIME_FORMATTER),
                event.getTicketsAvailable()
        );
    }
}
