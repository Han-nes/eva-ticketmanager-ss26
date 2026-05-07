package Core.Clients.CommandHandler;

import Core.Interfaces.TicketShopInterface;
import Core.Models.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleClientEventCommandHandler {

    private final TicketShopInterface shop;
    private final Scanner scanner;

    public ConsoleClientEventCommandHandler( TicketShopInterface shop ){
        this.scanner = new Scanner(System.in);
        this.shop = shop;
    }

    public void handleEventCommands() {
        System.out.println("=== Event Management ===");
        System.out.println(
                "Commands: list (l), create (c), read (r), update (u), delete (d), clear (cl), back (b)"
        );

        while (true) {
            System.out.print("events> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("back") || input.equals("b")) break;

            try {
                switch (input) {
                    case "list", "l":
                        listEvents();
                        break;
                    case "create", "c":
                        createEvent();
                        break;
                    case "read", "r":
                        readEvent();
                        break;
                    case "update", "u":
                        updateEvent();
                        break;
                    case "delete", "d":
                        deleteEvent();
                        break;
                    case "clear", "cl":
                        deleteAllEvents();
                        break;
                    default:
                        System.out.println(
                                "Unknown command. Available: list (l), create (c), read (r), update (u), delete (d), clear (cl), back (b)"
                        );
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void listEvents() {
        List<Event> events = shop.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        for (int i = 0; i < events.size(); i++) {
            System.out.println((i + 1) + ". " + ConsoleFormatter.formatEvent(events.get(i)));
        }
    }

    private void createEvent() {
        System.out.print("Event name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Location: ");
        String location = scanner.nextLine().trim();
        System.out.print("Date/time (yyyy-MM-dd HH:mm): ");
        String timeStr = scanner.nextLine().trim();
        System.out.print("Available tickets: ");
        String ticketsStr = scanner.nextLine().trim();

        try {
            LocalDateTime time = LocalDateTime.parse(
                    timeStr,
                    ConsoleFormatter.DATE_TIME_FORMATTER
            );
            int tickets = Integer.parseInt(ticketsStr);
            Event event = shop.createEvent(name, location, time, tickets);
            System.out.println("Event created: " + event.getId());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket number");
        }
    }

    private void readEvent() {
        System.out.print("Event ID: ");
        String idStr = scanner.nextLine().trim();
        try {
            long id = Long.getLong(idStr);
            Event event = shop.getEventById(id);
            if (event == null) {
                System.out.println("Event not found");
            } else {
                System.out.println(ConsoleFormatter.formatEvent(event));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid event ID");
        }
    }

    private void updateEvent() {
        System.out.print("Event ID: ");
        String idStr = scanner.nextLine().trim();
        try {
            long id = Long.getLong(idStr);
            Event event = shop.getEventById(id);
            if (event == null) {
                System.out.println("Event not found");
            } else {
                System.out.print("New name: ");
                String name = scanner.nextLine().trim();
                System.out.print("New location: ");
                String location = scanner.nextLine().trim();
                System.out.print("New date (yyyy-MM-dd HH:mm): ");
                String dateStr = scanner.nextLine().trim();
                System.out.print("New tickets available: ");
                String ticketsAvailableString = scanner.nextLine().trim();
                try {
                    LocalDateTime date = LocalDateTime.parse(
                            dateStr,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    );
                    int ticketsAvailable = Integer.parseInt(
                            ticketsAvailableString
                    );
                    event.setName(name);
                    event.setLocation(location);
                    event.setTime(date);
                    event.setTicketsAvailable(ticketsAvailable);
                    shop.updateEvent(event);
                    System.out.println("Event updated");
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid event ID");
        }
    }

    private void deleteEvent() {
        System.out.print("Enter event ID: ");
        String idStr = scanner.nextLine().trim();
        try {
            Long id = Long.getLong(idStr);
            shop.deleteEvent(id);
            System.out.println("Event deleted");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid event ID");
        }
    }

    private void deleteAllEvents() {
        shop.deleteAllEvents();
        System.out.println("All events deleted");
    }
}
