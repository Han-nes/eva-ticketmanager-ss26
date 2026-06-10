package concurrence.performanceClients;

import concurrence.performanceTests.ThreadingCorrectnessTest;
import core.models.Customer;
import core.models.Event;
import core.models.exceptions.TicketException;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDServiceInterface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PerformanceClientThreads {

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final IDServiceInterface idService;
    private final ThreadingCorrectnessTest threadingCorrectnessTest;

    private final int amountEventsToBeCreated = 100;
    private final int amountCustomersToBeCreated = 1000;
    private final int maximumAvailableTicketsPerEvent = 10;
    private final int amountTicketsToBeCreated;
    private final int threadCount = 8;


    public PerformanceClientThreads(IDServiceInterface idService) {
        this.idService = idService;
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);

        amountTicketsToBeCreated = Math.min(amountCustomersToBeCreated * amountEventsToBeCreated, amountEventsToBeCreated * maximumAvailableTicketsPerEvent);

        this.threadingCorrectnessTest = new ThreadingCorrectnessTest(this.eventService, this.ticketService, this.customerService);
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

        threadingCorrectnessTest.testEverything(maximumAvailableTicketsPerEvent, amountCustomersToBeCreated, amountEventsToBeCreated, amountTicketsToBeCreated);

        ticketService.deleteAllTickets();
        eventService.deleteAllEvents();
        customerService.deleteAllCustomers();
    }


    private void sequentialExecution() {
        System.out.println("Test Sequential:");
        long startTime = System.currentTimeMillis();
        createEvents();
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        createCustomers();
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        createTickets(eventService.getAllEvents(), customerService.getAllCustomers());
        endTime = System.currentTimeMillis();
        System.out.println("Time to buy " + amountTicketsToBeCreated + " tickets: " + (endTime - startTime) + "ms");
    }

    private void createEvents() {
        for (int i = 0; i < amountEventsToBeCreated; i++) {
            eventService.createEvent(
                    "Event" + i,
                    "Location" + i,
                    LocalDateTime.now().plusDays(7 + i),
                    maximumAvailableTicketsPerEvent
            );
        }
    }


    private void parallelExecution() {
        System.out.println("\nTest parallel");
        System.out.println("Threads used: " + threadCount);
        try {
            long startTime = System.currentTimeMillis();
            createEventsInParallel();
            long endTime = System.currentTimeMillis();
            System.out.println("Time to create " + amountEventsToBeCreated + " events: " + (endTime - startTime) + "ms");

            startTime = System.currentTimeMillis();
            createCustomersInParallel();
            endTime = System.currentTimeMillis();
            System.out.println("Time to create " + amountCustomersToBeCreated + " customers: " + (endTime - startTime) + "ms");

            startTime = System.currentTimeMillis();
            createTicketsInParallel();
            endTime = System.currentTimeMillis();
            System.out.println("Time to buy " + amountTicketsToBeCreated+ " tickets: " + (endTime - startTime) + "ms");
        } catch (Exception ignored){}
    }

    private void createCustomers() {
        for (int i = 0; i < amountCustomersToBeCreated; i++) {
            customerService.createCustomer(
                    "User" + i,
                    "user" + i + "@test.org",
                    LocalDate.now().minusYears(18 + i)
            );
        }
    }

    private void createTickets(List<Event> events, List<Customer> customers) {
        for (Event event : events) {
            for (Customer customer : customers) {
                try {
                    ticketService.createTicket(customer.getId(), event.getId());
                } catch (TicketException e) {
                    break;
                }
            }
        }
    }

    private void createEventsInParallel() {
        List<EventThread> threadPool = new ArrayList<>();
        int amountEventsToBeCreatedPerThread = amountEventsToBeCreated / threadCount;
        int amountEventsToBeCreatedPerThreadModulo = amountEventsToBeCreated % threadCount;
        for(int i = 0; i < threadCount; i++){
            EventThread eventThread;
            if(i == 0) {
                eventThread = new EventThread(eventService, amountEventsToBeCreatedPerThread + amountEventsToBeCreatedPerThreadModulo, i, maximumAvailableTicketsPerEvent);
            } else{
                eventThread = new EventThread(eventService, amountEventsToBeCreatedPerThread, i, maximumAvailableTicketsPerEvent);
            }
            threadPool.add(eventThread);
            eventThread.start();
        }

        for(EventThread eventThread : threadPool){
            try {
                eventThread.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void createCustomersInParallel() {
        List<CustomerThread> threadPool = new ArrayList<>();
        int amountCustomersToBeCreatedPerThread = amountCustomersToBeCreated / threadCount;
        int amountCustomersToBeCreatedPerThreadModulo = amountCustomersToBeCreated % threadCount;
        for(int i = 0; i < threadCount; i++){
            CustomerThread customerThread;
            if(i == 0) {
                customerThread = new CustomerThread(customerService, amountCustomersToBeCreatedPerThread + amountCustomersToBeCreatedPerThreadModulo, i);
            } else {
                customerThread = new CustomerThread(customerService, amountCustomersToBeCreatedPerThread, i);
            }
            threadPool.add(customerThread);
            customerThread.start();
        }

        for(CustomerThread customerThread : threadPool){
            try {
                customerThread.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private void createTicketsInParallel(){
        List<TicketThread> threadPool = new ArrayList<>();
        int partitionSize = eventService.getAllEvents().size() / threadCount;

        for(int i = 0; i < threadCount; i++){
            List<Event> eventSubList;
            if(i == threadCount - 1){
                eventSubList = eventService.getAllEvents().subList(i * partitionSize, eventService.getAllEvents().size());
            } else {
                eventSubList = eventService.getAllEvents().subList(i * partitionSize, (i + 1) * partitionSize);
            }
            //TicketThread ticketThread = new TicketThread(ticketService,customerService.getAllCustomers(), eventSubList);
            TicketThread ticketThread = new TicketThread(ticketService,customerService.getAllCustomers(), eventService.getAllEvents());
            threadPool.add(ticketThread);
            ticketThread.start();
        }

        for(TicketThread ticketThread : threadPool){
            try {
                ticketThread.join();
            } catch (InterruptedException ignored) {}
        }
    }


    private class EventThread extends Thread{
        private final int amountEventsToBeCreatedPerThread;
        private final EventService eventService;
        private final int threadNumber;
        private final int maximumAvailableTicketsPerEvent;

        public EventThread(EventService eventService, int amountEventsToBeCreatedPerThread, int threadNumber, int maximumAvailableTicketsPerEvent){
            this.amountEventsToBeCreatedPerThread = amountEventsToBeCreatedPerThread;
            this.eventService = eventService;
            this.threadNumber = threadNumber;
            this.maximumAvailableTicketsPerEvent = maximumAvailableTicketsPerEvent;
        }

        public void run(){
            for(int i = 0; i < amountEventsToBeCreatedPerThread; i++){
                eventService.createEvent(
                        "event" + threadNumber + "_" + i,
                        "location" + threadNumber + "_" + i,
                        LocalDateTime.now().plusDays(20),
                        maximumAvailableTicketsPerEvent
                );
            }
        }
    }

    private class TicketThread extends Thread{
        private final TicketService ticketService;
        private final List<Customer> customers;
        private final List<Event> events;

        public TicketThread(TicketService ticketService, List<Customer> customers, List<Event> events){
            this.ticketService = ticketService;
            this.customers = customers;
            this.events = events;
        }

        public void run(){
            for(Event event : events){
                for(Customer customer : customers){
                    try {
                        ticketService.createTicket(customer.getId(), event.getId());
                    } catch (TicketException ticketException) {
                        if(Objects.equals(ticketException.getMessage(), TicketException.noTicketsAvailable)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private class CustomerThread extends Thread{
        private final int amountCustomersToBeCreatedPerThread;
        private final CustomerService customerService;
        private final int threadNumber;

        public CustomerThread(CustomerService customerService, int amountCustomersToBeCreatedPerThread, int threadNumber){
            this.amountCustomersToBeCreatedPerThread = amountCustomersToBeCreatedPerThread;
            this.customerService = customerService;
            this.threadNumber = threadNumber;
        }

        public void run(){
            for(int i = 0; i < amountCustomersToBeCreatedPerThread; i++){
                customerService.createCustomer(
                        "cusomter" + threadNumber + "_" + i,
                        "custumoer" + threadNumber + "_" + i + "@mail.com",
                        LocalDate.now().minusYears(20)
                );
            }
        }
    }

}


