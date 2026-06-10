package core.models.exceptions;

public class TicketException extends Exception {
    public static final String noTicketsAvailable = "No tickets available for event";
    public static final String maximumNumberOfTickets =  "Maximum Number of Tickets one Customer can aquire per Event reached";
    public static final String ticketDoesNotExist = "Ticket does not exist." ;

    public TicketException(String message) {
        super(message);
    }

    public static TicketException noTicketsAvailable() {
        return new TicketException(noTicketsAvailable);
    }

    public static TicketException maximumNumberOfTickets() {
        return new TicketException(maximumNumberOfTickets);
    }

    public static TicketException ticketDoesNotExist() {
        return new TicketException(ticketDoesNotExist);
    }

}
