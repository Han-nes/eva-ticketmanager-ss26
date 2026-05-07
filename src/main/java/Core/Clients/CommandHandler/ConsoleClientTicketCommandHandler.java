package Core.Clients.CommandHandler;

import Core.Interfaces.TicketShopInterface;
import Core.Models.Customer;
import Core.Models.Event;
import Core.Models.Ticket;
import Core.Models.exceptions.TicketException;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleClientTicketCommandHandler {

    private final TicketShopInterface shop;
    private final Scanner scanner;

    public ConsoleClientTicketCommandHandler( TicketShopInterface shop ){
        this.scanner = new Scanner(System.in);
        this.shop = shop;
    }

    public void handleTicketCommands() {
        System.out.println("=== Ticket Management ===");
        System.out.println(
                "Commands: list (l), create (c), read (r), delete (d), clear (cl), back (b)"
        );

        while (true) {
            System.out.print("tickets> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("back") || input.equals("b")) break;

            try {
                switch (input) {
                    case "list", "l":
                        listTickets();
                        break;
                    case "create", "c":
                        createTicket();
                        break;
                    case "read", "r":
                        readTicket();
                        break;
                    case "delete", "d":
                        deleteTicket();
                        break;
                    case "clear", "cl":
                        deleteAllTickets();
                        break;
                    case "validate", "v":
                        verifyTicket();
                        break;
                    default:
                        System.out.println(
                                "Unknown command. Available: list (l), create (c), read (r), delete (d), clear (cl), back (b)"
                        );
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void listTickets() {
        List<Ticket> tickets = shop.getAllTickets();
        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }
        for (int i = 0; i < tickets.size(); i++) {
            System.out.println((i + 1) + ". " + ConsoleFormatter.formatTicket(tickets.get(i)));
        }
    }

    private void createTicket() {
        // List available events
        List<Event> events = shop.getAllEvents();
        System.out.println("Available Events:");
        if (events.isEmpty()) {
            System.out.println("No events available.");
        } else {
            for (int i = 0; i < events.size(); i++) {
                System.out.println(
                        (i + 1) +
                                ". " +
                                ConsoleFormatter.formatEvent(events.get(i)) +
                                " [ID: " +
                                events.get(i).getId() +
                                "]"
                );
            }
        }

        // Get event selection (index or UUID)
        long eventId = 0;
        while (eventId == 0) {
            System.out.print(
                    "Select event (number 1-" +
                            events.size() +
                            ", ID, or 'cancel'): "
            );
            String eventInput = scanner.nextLine().trim();

            if (eventInput.equalsIgnoreCase("cancel")) {
                System.out.println("Ticket creation cancelled.");
                return;
            }

            // Try parsing as index first
            try {
                int eventIndex = Integer.parseInt(eventInput) - 1;
                if (eventIndex >= 0 && eventIndex < events.size()) {
                    eventId = events.get(eventIndex).getId();
                    continue;
                } else if (!events.isEmpty()) {
                    System.out.println(
                            "Invalid index. Please enter a number between 1 and " +
                                    events.size() +
                                    " or a UUID."
                    );
                    continue;
                }
            } catch (NumberFormatException e) {
                // Not a number, try parsing as UUID
            }

            // Try parsing as UUID
            try {
//                eventId = UUID.fromString(eventInput);
                eventId = Long.getLong(eventInput);
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Invalid input. Please enter an index number, valid UUID, or 'cancel'."
                );
            }
        }

        // List available customers
        List<Customer> customers = shop.getAllCustomers();
        System.out.println("\nAvailable Customers:");
        if (customers.isEmpty()) {
            System.out.println("No customers available.");
        } else {
            for (int i = 0; i < customers.size(); i++) {
                System.out.println(
                        (i + 1) +
                                ". " +
                                ConsoleFormatter.formatCustomer(customers.get(i)) +
                                " [ID: " +
                                customers.get(i).getId() +
                                "]"
                );
            }
        }

        // Get customer selection (index or UUID)
        long customerId = 0;
        while (customerId == 0) {
            System.out.print(
                    "Select customer (number 1-" +
                            customers.size() +
                            ", ID, or 'cancel'): "
            );
            String customerInput = scanner.nextLine().trim();

            if (customerInput.equalsIgnoreCase("cancel")) {
                System.out.println("Ticket creation cancelled.");
                return;
            }

            // Try parsing as index first
            try {
                int customerIndex = Integer.parseInt(customerInput) - 1;
                if (customerIndex >= 0 && customerIndex < customers.size()) {
                    customerId = customers.get(customerIndex).getId();
                    continue;
                } else if (!customers.isEmpty()) {
                    System.out.println(
                            "Invalid index. Please enter a number between 1 and " +
                                    customers.size() +
                                    " or a UUID."
                    );
                    continue;
                }
            } catch (NumberFormatException e) {
                // Not a number, try parsing as UUID
            }

            // Try parsing as UUID
            try {
                customerId = Long.getLong(customerInput);
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Invalid input. Please enter an index number, valid UUID, or 'cancel'."
                );
            }
        }

        // Create the ticket using IDs (this allows testing with invalid IDs)
        try {
            Customer customer = shop.getCustomerById(customerId);
            Event event = shop.getEventById(eventId);

            shop.createTicket(customerId, eventId);

            String customerName = (customer != null)
                    ? customer.getUsername()
                    : "Unknown Customer (" + customerId + ")";
            String eventName = (event != null)
                    ? event.getName()
                    : "Unknown Event (" + eventId + ")";
            System.out.println(
                    "Ticket created for " +
                            customerName +
                            " for event \"" +
                            eventName +
                            "\""
            );
        } catch (Exception e) {
            System.out.println("Ticket creation failed: " + e.getMessage());
        }
    }

    private void readTicket() throws TicketException {
        System.out.print("Ticket ID: ");
        long ticketId = Long.getLong(scanner.nextLine().trim());
        Ticket ticket = shop.getTicketById(ticketId);
        if (ticket == null) {
            System.out.println("Ticket not found.");
        } else {
            System.out.println(ConsoleFormatter.formatTicket(ticket));
        }
    }

    private void deleteTicket() throws TicketException {
        System.out.print("Ticket ID: ");
        long ticketId = Long.getLong(scanner.nextLine().trim());
        Ticket ticket = shop.getTicketById(ticketId);
        if (ticket == null) {
            System.out.println("Ticket not found.");
        } else {
            shop.deleteTicket(ticketId);
            System.out.println("Ticket deleted successfully.");
        }
    }

    private void deleteAllTickets() {
        shop.deleteAllTickets();
        System.out.println("All tickets deleted successfully.");
    }

    private void verifyTicket() {
        System.out.print("Ticket ID: ");
        long ticketId = Long.getLong(scanner.nextLine().trim());
        if (shop.verifyTicket(ticketId)) {
            System.out.println("Ticket not found.");
        } else {
            System.out.println("Ticket is valid.");
        }
    }


}
