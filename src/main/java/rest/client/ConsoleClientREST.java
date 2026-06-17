package rest.client;

import core.clients.commandHandler.ConsoleClientCustomerCommandHandler;
import core.clients.commandHandler.ConsoleClientEventCommandHandler;
import core.clients.commandHandler.ConsoleClientTicketCommandHandler;
import core.interfaces.TicketShopInterface;

import java.net.http.HttpClient;
import java.util.Scanner;

public class ConsoleClientREST {

    private final Scanner scanner;

    private final ConsoleClientEventCommandHandler eventCommandHandler;
    private final ConsoleClientTicketCommandHandler ticketCommandHandler;
    private final ConsoleClientCustomerCommandHandler customerCommandHandler;

    public ConsoleClientREST() {
        this.scanner = new Scanner(System.in);

        String baseUrl = "http://localhost:8080";
        HttpClient httpClient = HttpClient.newHttpClient();
        TicketShopInterface shop = new RestTicketShop(httpClient, baseUrl);

        this.customerCommandHandler = new ConsoleClientCustomerCommandHandler(shop);
        this.eventCommandHandler = new ConsoleClientEventCommandHandler(shop);
        this.ticketCommandHandler = new ConsoleClientTicketCommandHandler(shop);
    }

    public void start() {
        System.out.println("=== Welcome to the REST-Ticket-shop ===");
        System.out.println(
                "Type 'help' to see available commands or 'exit' to quit.\n"
        );

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) continue;

            try {
                if (handleCommand(input)) break; // Exit if true
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private boolean handleCommand(String input) {
        return switch (input) {
            case "help" -> {
                showHelp();
                yield false;
            }
            case "events", "e" -> {
                eventCommandHandler.handleEventCommands();
                yield false;
            }
            case "customers", "c" -> {
                customerCommandHandler.handleCustomerCommands();
                yield false;
            }
            case "tickets", "t" -> {
                ticketCommandHandler.handleTicketCommands();
                yield false;
            }
            case "exit", "quit", "q" -> {
                System.out.println("Thank you for using the Ticket-shop!");
                yield true;
            }
            default -> {
                System.out.println(
                        "Unknown command: '" +
                                input +
                                "'. Type 'help' for available commands."
                );
                yield false;
            }
        };
    }

    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  events, e     - Enter event management mode");
        System.out.println("  customers, c  - Enter customer management mode");
        System.out.println("  tickets, t    - Enter ticket management mode");
        System.out.println("  help          - Show this help message");
        System.out.println("  exit, quit, q - Exit the application");
    }

}

