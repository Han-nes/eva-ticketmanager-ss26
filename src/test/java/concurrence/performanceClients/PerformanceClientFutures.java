package concurrence.performanceClients;

import concurrence.performanceTests.ThreadingCorrectnessTest;
import core.models.exceptions.TicketException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDServiceInterface;

public class PerformanceClientFutures {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final ExecutorService executorService;
    private final IDServiceInterface idService;
    private final ThreadingCorrectnessTest threadingCorrectnessTest;

    private final int amountEventsToBeCreated = 100;
    private final int amountCustomersToBeCreated = 100;
    private final int maximumAvailableTicketsPerEvent = 100;
    private final int threadCount = 8;


    public PerformanceClientFutures(IDServiceInterface idService) {
        this.idService = idService;
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);

        this.threadingCorrectnessTest = new ThreadingCorrectnessTest(this.eventService, this.ticketService, this.customerService);

        //this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public void run() {
//        sequentialExecution();
//
//        threadingCorrectnessTest.testEverything(maximumAvailableTicketsPerEvent, amountCustomersToBeCreated, amountEventsToBeCreated, amountCustomersToBeCreated*amountEventsToBeCreated);
//
//        ticketService.deleteAllTickets();
//        eventService.deleteAllEvents();
//        customerService.deleteAllCustomers();
//        idService.clearIdStore();

        parallelExecution();

        threadingCorrectnessTest.testEverything(maximumAvailableTicketsPerEvent, amountCustomersToBeCreated, amountEventsToBeCreated, amountCustomersToBeCreated*amountEventsToBeCreated);

        ticketService.deleteAllTickets();
        eventService.deleteAllEvents();
        customerService.deleteAllCustomers();
    }


    private void sequentialExecution() {
        System.out.println("Test consecutive:");
        long startTime = System.currentTimeMillis();
        List<Event> events = createEvents();
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        List<Customer> customers = createCustomers();
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        //List<Ticket> tickets = buyTickets(events, customers);
        List<Ticket> tickets = buyTickets(eventService.getAllEvents(), customerService.getAllCustomers());
        endTime = System.currentTimeMillis();
        System.out.println("Time to buy " + amountCustomersToBeCreated * amountEventsToBeCreated + " tickets: " + (endTime - startTime) + "ms");
    }

    private List<Event> createEvents() {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < amountEventsToBeCreated; i++) {
            Event new_event = eventService.createEvent(
                "Event" + i,
                "Location" + i,
                LocalDateTime.now().plusDays(7 + i),
                    maximumAvailableTicketsPerEvent
            );
            events.add(new_event);
        }
        return events;
    }


    private void parallelExecution() {
        System.out.println("\nTest parallel");
        System.out.println("Threads used: " + threadCount);
        try {
            long startTime = System.currentTimeMillis();
            List<Event> events = createEventsInParallel();
            long endTime = System.currentTimeMillis();
            System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

            startTime = System.currentTimeMillis();
            List<Customer> customers = createCustomersInParallel();
            endTime = System.currentTimeMillis();
            System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

            startTime = System.currentTimeMillis();
//            buyTicketsInParallelSequential(events, customers);
            List<Ticket> tickets = buyTicketsInParallelSequential(eventService.getAllEvents(), customerService.getAllCustomers());
            endTime = System.currentTimeMillis();
            System.out.println("Time to buy " + amountEventsToBeCreated * amountCustomersToBeCreated + " tickets: " + (endTime - startTime) + "ms");
        } finally {
            shutdownExecutor();
        }
    }

    private List<Customer> createCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < amountCustomersToBeCreated; i++) {
            Customer newCustomer = customerService.createCustomer(
                "User" + i,
                "user" + i + "@test.org",
                LocalDate.now().minusYears(18 + i)
            );
            customers.add(newCustomer);
        }
        return customers;
    }

    private List<Ticket> buyTickets(List<Event> events, List<Customer> customers
    ) {
        List<Ticket> tickets = new ArrayList<>();
        for (Event event : events) {
            for (Customer customer : customers) {
                try {
                    tickets.add(ticketService.createTicket(customer.getId(), event.getId()));
                } catch (TicketException e) {
                    break;
                }
            }
        }
        return tickets;
    }

    private List<Event> createEventsInParallel() {
        List<CompletableFuture<Event>> eventFutures = IntStream.range(0, amountEventsToBeCreated)
            .mapToObj(i -> CompletableFuture.supplyAsync(           //Hier wird die Erstellung der Events per Stream auf die einzelnen Threads aufgeteilt
                    () -> eventService.createEvent(
                            "Event" + i,
                            "Location" + i,
                            LocalDateTime.now().plusDays(7 + i),
                            maximumAvailableTicketsPerEvent),
                    executorService                                     //Hier wird auf den Thread-Pool zugegriffen
                    )
            ).toList();                             //Hier werden die Ergebnisse der einzelnen Threads gesammelt

         return eventFutures
            .stream()
            .map(CompletableFuture::join)
            .toList();
    }

    private List<Customer> createCustomersInParallel() {
        List<CompletableFuture<Customer>> customerFutures = IntStream.range(0, amountCustomersToBeCreated)
            .mapToObj(i -> CompletableFuture.supplyAsync(
                    () -> customerService.createCustomer(
                            "User" + i,
                            "user" + i + "@test.org",
                            LocalDate.now().minusYears(18 + i)
                        ),
                    executorService
                )
            ).toList();

         return customerFutures
            .stream()
            .map(CompletableFuture::join)
            .toList();
    }

    private List<Ticket> buyTicketsInParallelSequential(List<Event> events, List<Customer> customers) {
        List<CompletableFuture<List<Ticket>>> eventFutures = new ArrayList<>();

        for (Event event : events) {
            CompletableFuture<List<Ticket>> eventFuture =
                CompletableFuture.supplyAsync(
                    () -> buyTicketsForEventSequential(event, customers),
                    executorService
                );
            eventFutures.add(eventFuture);
        }

        List<Ticket> createdTickets = eventFutures
                .stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
        System.out.println(createdTickets.size() + " Tickets have been created");
        return createdTickets;
    }

    private List<Ticket> buyTicketsForEventSequential(
        Event event,
        List<Customer> customers
    ) {
        List<Ticket> tickets = new ArrayList<>();
        for (Customer customer : customers) {
            try {
                Ticket newTicket = ticketService.createTicket(customer.getId(), event.getId());
                tickets.add(newTicket);
            } catch (TicketException e) {
                // Stop when event is sold out
                break;
            }
        }
        return tickets;
    }

    private void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                System.out.println("Executor service was forcibly shut down");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.out.println("Executor service shutdown was interrupted");
        }
    }


}
