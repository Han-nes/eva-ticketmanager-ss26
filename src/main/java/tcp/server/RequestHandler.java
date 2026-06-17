package tcp.server;

import java.time.LocalDate;
import java.time.LocalDateTime;

import core.models.Customer;
import core.models.Event;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDService;
import tcp.client.ticketShop.TicketShopStringFormatter;

public class RequestHandler {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;

    public RequestHandler(TicketService ticketService, CustomerService customerService, EventService eventService) {
        this.ticketService = ticketService;
        this.customerService = customerService;
        this.eventService = eventService;
    }

    public String callMethodRemotely(String string) throws Exception {
        String[] args = string.split(";");

        if (args.length < 1) {
            return "Error: Missing Service";
        } else if (args.length < 2) {
            return "Error: Missing Method";
        }

        return switch (args[0].toLowerCase()) {
            case "customer" -> callCustomerMethod(args);
            case "event" -> callEventMethod(args);
            case "ticket" -> callTicketMethod(args);
            default -> throw new IllegalArgumentException(
                    "Unknown Service: " + args[0]
            );
        };
    }

    private String callCustomerMethod(String[] args) {
        String[] methodArguments = {};

        if (args.length >= 3) methodArguments = args[2].split(",");

        switch (args[1].toLowerCase()) {
            case "create": {
                if (
                    methodArguments.length < 3
                ) throw new IllegalArgumentException("Missing arguments");
                return TicketShopStringFormatter.customerToString(customerService
                        .createCustomer(
                                methodArguments[0],
                                methodArguments[1],
                                LocalDate.parse(methodArguments[2])
                        ));
            }
            case "getbyid": {
                if (
                    methodArguments.length < 1
                ) throw new IllegalArgumentException("Missing arguments");
                return TicketShopStringFormatter.customerToString(customerService
                    .getCustomerById(Long.parseLong(methodArguments[0])));
            }
            case "update": {
                if (
                    methodArguments.length < 4
                ) throw new IllegalArgumentException("Missing arguments");
                Customer customer = customerService.getCustomerById(Long.parseLong(methodArguments[0]));
                customer.setUsername(methodArguments[1]);
                customer.setEmail(methodArguments[2]);
                customer.setDateOfBirth(LocalDate.parse(methodArguments[3]));
                customerService.updateCustomer(customer);
                return "Success";
            }
            case "delete": {
                if (
                    methodArguments.length < 1
                ) throw new IllegalArgumentException("Missing arguments");
                customerService.deleteCustomer(Long.parseLong(methodArguments[0]));
                return "Success";
            }
            case "getall": {
                StringBuilder output = new StringBuilder();
                customerService
                    .getAllCustomers()
                    .forEach(customer -> output.append(TicketShopStringFormatter.customerToString(customer)).append(";"));
                return output.toString();
            }
            case "deleteall": {
                customerService.deleteAllCustomers();
                return "Success";
            }
            default:
                throw new IllegalArgumentException("Invalid method name");
        }
    }

    private String callEventMethod(String[] args) {
        String[] methodArguments = {};
        if (args.length >= 3) methodArguments = args[2].split(",");

        switch (args[1].toLowerCase()) {
            case "create": {
                if (
                    methodArguments.length < 3
                ) throw new IllegalArgumentException("Missing arguments");
                return TicketShopStringFormatter.eventToString(eventService
                    .createEvent(
                        methodArguments[0],
                        methodArguments[1],
                        LocalDateTime.parse(methodArguments[2]),
                        Integer.parseInt(methodArguments[3])
                    ));
            }
            case "getbyid": {
                if (
                    methodArguments.length < 1
                ) throw new IllegalArgumentException("Missing arguments");
                return TicketShopStringFormatter.eventToString(eventService
                    .getEventById(Long.parseLong(methodArguments[0])));
            }
            case "getall": {
                StringBuilder output = new StringBuilder();
                eventService
                    .getAllEvents()
                    .forEach(event -> output.append(TicketShopStringFormatter.eventToString(event)).append(";"));
                return output.toString();
            }
            case "update": {
                if (
                    methodArguments.length < 5
                ) throw new IllegalArgumentException("Missing arguments");
                Event event = eventService.getEventById(Long.parseLong(methodArguments[0]));
                event.setName(methodArguments[1]);
                event.setLocation(methodArguments[2]);
                event.setTime(LocalDateTime.parse(methodArguments[3]));
                event.setTicketsAvailable(Integer.parseInt(methodArguments[4]));
                eventService.updateEvent(event);
            }
            case "delete": {
                if (
                    methodArguments.length < 1
                ) throw new IllegalArgumentException("Missing arguments");
                eventService.deleteEvent(Long.parseLong(methodArguments[0]));
                return "Success";
            }
            case "deleteall": {
                eventService.deleteAllEvents();
                return "All Events deleted";
            }
            default:
                throw new IllegalArgumentException("Invalid method name");
        }
    }

    private String callTicketMethod(String[] args) throws Exception {
        String[] methodArguments = {};
        if (args.length >= 3) methodArguments = args[2].split(",");

        switch (args[1].toLowerCase()) {
            case "create": {
                if (
                    methodArguments.length < 2
                ) throw new IllegalArgumentException("Missing arguments");

                return TicketShopStringFormatter.ticketToString(ticketService.createTicket(Long.parseLong(methodArguments[0]), Long.parseLong(methodArguments[1])));
            }
            case "getbyid": {
                return TicketShopStringFormatter.ticketToString(ticketService
                    .getTicketById(Long.parseLong(methodArguments[0])));
            }
            case "getall": {
                StringBuilder output = new StringBuilder();
                ticketService
                    .getAllTickets()
                    .forEach(ticket -> output.append(TicketShopStringFormatter.ticketToString(ticket)).append(";"));
                return output.toString();
            }
            case "delete": {
                ticketService.deleteTicket(Long.parseLong(methodArguments[0]));
                return "Success";
            }
            case "deleteall": {
                ticketService.deleteAllTickets();
                return "Success";
            }
            default:
                throw new IllegalArgumentException("Invalid method name");
        }
    }
}
