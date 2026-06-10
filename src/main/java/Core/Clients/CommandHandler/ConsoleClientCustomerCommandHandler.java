package core.clients.commandHandler;

import core.interfaces.TicketShopInterface;
import core.models.Customer;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ConsoleClientCustomerCommandHandler {

    private final TicketShopInterface shop;
    private final Scanner scanner;


    public ConsoleClientCustomerCommandHandler( TicketShopInterface shop ){
      this.scanner = new Scanner(System.in);
      this.shop = shop;
    }


    public void handleCustomerCommands() {
        System.out.println("=== Customer Management ===");
        System.out.println(
                "Commands: list (l), create (c), read (r), update (u), delete (d), clear (cl), back (b)"
        );

        while (true) {
            System.out.print("customers> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("back") || input.equals("b")) break;

            try {
                switch (input) {
                    case "list", "l":
                        listCustomers();
                        break;
                    case "create", "c":
                        createCustomer();
                        break;
                    case "read", "r":
                        readCustomer();
                        break;
                    case "update", "u":
                        updateCustomer();
                        break;
                    case "delete", "d":
                        deleteCustomer();
                        break;
                    case "clear", "cl":
                        deleteAllCustomers();
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

    // Customer methods
    private void listCustomers() {
        List<Customer> customers = shop.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (int i = 0; i < customers.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + ConsoleFormatter.formatCustomer(customers.get(i))
            );
        }
    }

    private void createCustomer() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Date of Birth (yyyy-MM-dd): ");
        LocalDate dateOfBirth = LocalDate.parse(scanner.nextLine().trim());

        Customer customer = shop.createCustomer(username, email, dateOfBirth);
        System.out.println("Customer created: " + customer.getId());
    }

    public void readCustomer() {
        System.out.print("Customer ID: ");
        long id = Long.getLong(scanner.nextLine().trim());

        Customer customer = shop.getCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found with ID: " + id);
            return;
        }

        System.out.println("Customer details:");
        System.out.println(ConsoleFormatter.formatCustomer(customer));
    }

    private void updateCustomer() {
        System.out.print("Customer ID: ");
        long id = Long.getLong(scanner.nextLine().trim());

        Customer existingCustomer = shop.getCustomerById(id);
        if (existingCustomer == null) {
            System.out.println("Customer not found with ID: " + id);
            return;
        }

        System.out.print("New username: ");
        String username = scanner.nextLine().trim();
        System.out.print("New email: ");
        String email = scanner.nextLine().trim();
        System.out.print("New date of birth (yyyy-MM-dd): ");
        LocalDate dateOfBirth = LocalDate.parse(scanner.nextLine().trim());

        existingCustomer.setUsername(username);
        existingCustomer.setEmail(email);
        existingCustomer.setDateOfBirth(dateOfBirth);
        shop.updateCustomer(existingCustomer);
        System.out.println("Customer updated: " + existingCustomer);
    }

    private void deleteCustomer() {
        System.out.print("Customer ID: ");
        long id = Long.getLong(scanner.nextLine().trim());

        Customer existingCustomer = shop.getCustomerById(id);
        if (existingCustomer == null) {
            System.out.println("Customer not found with ID: " + id);
            return;
        }

        shop.deleteCustomer(id);
        System.out.println("Customer deleted: " + existingCustomer);
    }

    private void deleteAllCustomers() {
        shop.deleteAllCustomers();
        System.out.println("All customers cleared.");
    }

}
