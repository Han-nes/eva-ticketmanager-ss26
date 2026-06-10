package concurrence.performanceClients;

import concurrence.performanceTests.ThreadingCorrectnessTest;
import core.models.Customer;
import core.models.Event;
import core.models.Ticket;
import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDServiceInterface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Erzeugt Testdaten in drei Phasen:
 * 1. Events
 * 2. Customer
 * 3. Tickets
 *
 * Annahme:
 * - Die Services besitzen create-Methoden, die das jeweilige Objekt zurückgeben.
 * - Event, Customer und Ticket besitzen eine getId()-Methode.
 * - Event besitzt eine getAvailableTickets()-Methode.
 *
 * Passe die Methodennamen und Datentypen an dein Projekt an.
 */
public class PerformanceClientThreadsCopilot {

        /* =========================
           Konfiguration
           ========================= */

    private static final int EVENT_COUNT = 20;
    private static final int CUSTOMER_COUNT = 50;
    private static final int TICKET_COUNT = 120;

    /**
     * Wie viele Threads pro Phase gestartet werden sollen.
     */
    private static final int THREAD_COUNT = 8;

    /**
     * Wie viele Tickets jedes neu erstellte Event maximal anbietet.
     * Dieser Wert muss so gewählt werden, dass insgesamt genug Tickets existieren.
     */
    private static final int AVAILABLE_TICKETS_PER_EVENT = 20;

        /* =========================
           Services
           ========================= */

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final ThreadingCorrectnessTest threadingCorrectnessTest;


        /* =========================
           Bereits erstellte Objekte
           ========================= */

    private final List<Event> createdEvents = Collections.synchronizedList(new ArrayList<>());
    private final List<Customer> createdCustomers = Collections.synchronizedList(new ArrayList<>());
    private final List<Ticket> createdTickets = Collections.synchronizedList(new ArrayList<>());

        /* =========================
           Hilfsstrukturen für Ticket-Erstellung
           ========================= */

    /**
     * Merkt sich, wie viele Tickets für ein Event bereits "verplant" wurden,
     * damit die maximale Anzahl verfügbarer Tickets nicht überschritten wird.
     */
    private final Map<Long, Integer> reservedTicketsPerEvent = new HashMap<>();

    /**
     * Merkt sich, wie viele Tickets ein bestimmter Kunde für ein bestimmtes Event hat.
     * Schlüssel: customerId + "|" + eventId
     */
    private final Map<String, Integer> reservedTicketsPerCustomerEvent = new HashMap<>();

    /**
     * Schützt die beiden Maps oben und den Zähler plannedTicketCount.
     */
    private final Object reservationLock = new Object();

    /**
     * Zählt, wie viele Tickets bereits zur Erstellung vorgesehen wurden.
     */
    private int plannedTicketCount = 0;

    public PerformanceClientThreadsCopilot(IDServiceInterface idService) {
        this.ticketService = new TicketService(idService);
        this.eventService = new EventService(ticketService, idService);
        this.customerService = new CustomerService(ticketService, idService);
        ticketService.setCustomerService(this.customerService);
        ticketService.setEventService(this.eventService);

        this.threadingCorrectnessTest = new ThreadingCorrectnessTest(this.eventService, this.ticketService, this.customerService);
    }

    /**
     * Führt alle drei Phasen nacheinander aus.
     */
    public void run() {
        validateConfiguration();
        System.out.println("\nTest parallel");
        System.out.println("Threads used: " + THREAD_COUNT);
        long startTime = System.currentTimeMillis();
        createEventsInParallel();
        long endTime = System.currentTimeMillis();
        System.out.println("Time to create " + EVENT_COUNT + " events: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        createCustomersInParallel();
        endTime = System.currentTimeMillis();
        System.out.println("Time to create " + CUSTOMER_COUNT + " customers: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        createTicketsInParallel();
        endTime = System.currentTimeMillis();
        System.out.println("Time to buy " + TICKET_COUNT + " tickets: " + (endTime - startTime) + "ms");


        threadingCorrectnessTest.testEverything(AVAILABLE_TICKETS_PER_EVENT, CUSTOMER_COUNT, EVENT_COUNT, TICKET_COUNT);
    }

        /* =========================
           Phase 1: Events
           ========================= */

    private void createEventsInParallel() {
        runParallel(EVENT_COUNT, index -> {
            String name = "Event-" + (index + 1);
            String location = "Ort-" + ((index % 5) + 1);
            LocalDateTime dateTime = LocalDateTime.now().plusDays(index + 1);

            // Passe diese Service-Methode an dein Projekt an
            Event event = eventService.createEvent(
                    name,
                    location,
                    dateTime,
                    AVAILABLE_TICKETS_PER_EVENT
            );

            createdEvents.add(event);
        });
    }

        /* =========================
           Phase 2: Customer
           ========================= */

    private void createCustomersInParallel() {
        runParallel(CUSTOMER_COUNT, index -> {
            String name = "Customer-" + (index + 1);
            LocalDate birthDate = LocalDate.of(1990, 1, 1).plusDays(index);
            String mail = "customer" + (index + 1) + "@example.com";

            // Passe diese Service-Methode an dein Projekt an
            Customer customer = customerService.createCustomer(
                    name,
                    mail,
                    birthDate
            );

            createdCustomers.add(customer);
        });
    }

        /* =========================
           Phase 3: Tickets
           ========================= */

    private void createTicketsInParallel() {
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        Reservation reservation = reserveNextTicketSlot();

                        if (reservation == null) {
                            // Keine weitere gültige Ticket-Erstellung möglich
                            break;
                        }

                        try {
                            // Passe diese Service-Methode an dein Projekt an
                            Ticket ticket = ticketService.createTicket(
                                    reservation.customerId,
                                    reservation.eventId
                            );

                            createdTickets.add(ticket);
                        } catch (Exception ex) {
                            // Falls die Service-Prüfung doch ablehnt, Reservierung zurücknehmen
                            rollbackReservation(reservation);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });

            thread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Reserviert unter Schutz eines Locks den nächsten gültigen Platz
     * für ein Ticket. Damit werden die Regeln bereits vorab eingehalten:
     * - Event darf nicht überbucht werden
     * - Kunde darf nicht mehr als 5 Tickets für dasselbe Event haben
     *
     * Gibt null zurück, wenn keine weitere gültige Ticket-Erstellung möglich ist
     * oder das Ziel TICKET_COUNT bereits erreicht wurde.
     */
    private Reservation reserveNextTicketSlot() {
        synchronized (reservationLock) {
            if (plannedTicketCount >= TICKET_COUNT) {
                return null;
            }

            for (Event event : createdEvents) {
                int alreadyReservedForEvent = reservedTicketsPerEvent.getOrDefault(event.getId(), 0);

                if (alreadyReservedForEvent >= event.getTicketsAvailable()) {
                    continue;
                }

                for (Customer customer : createdCustomers) {
                    String pairKey = buildPairKey(customer.getId(), event.getId());

                    int alreadyReservedForPair =
                            reservedTicketsPerCustomerEvent.getOrDefault(pairKey, 0);

                    if (alreadyReservedForPair >= 5) {
                        continue;
                    }

                    reservedTicketsPerEvent.put(event.getId(), alreadyReservedForEvent + 1);
                    reservedTicketsPerCustomerEvent.put(pairKey, alreadyReservedForPair + 1);
                    plannedTicketCount++;

                    return new Reservation(customer.getId(), event.getId(), pairKey);
                }
            }

            return null;
        }
    }

    /**
     * Macht eine vorherige Reservierung rückgängig,
     * falls die echte Ticket-Erstellung im Service fehlschlägt.
     */
    private void rollbackReservation(Reservation reservation) {
        synchronized (reservationLock) {
            plannedTicketCount--;

            Integer eventCount = reservedTicketsPerEvent.get(reservation.eventId);
            if (eventCount != null) {
                if (eventCount <= 1) {
                    reservedTicketsPerEvent.remove(reservation.eventId);
                } else {
                    reservedTicketsPerEvent.put(reservation.eventId, eventCount - 1);
                }
            }

            Integer pairCount = reservedTicketsPerCustomerEvent.get(reservation.pairKey);
            if (pairCount != null) {
                if (pairCount <= 1) {
                    reservedTicketsPerCustomerEvent.remove(reservation.pairKey);
                } else {
                    reservedTicketsPerCustomerEvent.put(reservation.pairKey, pairCount - 1);
                }
            }
        }
    }

        /* =========================
           Allgemeine Thread-Hilfe
           ========================= */

    /**
     * Führt eine Aufgabe für die Zahlen 0 bis totalCount-1 parallel aus.
     */
    private void runParallel(int totalCount, IndexedTask task) {
        int workers = Math.min(THREAD_COUNT, totalCount);
        CountDownLatch latch = new CountDownLatch(workers);

        int chunkSize = (int) Math.ceil((double) totalCount / workers);

        for (int worker = 0; worker < workers; worker++) {
            final int start = worker * chunkSize;
            final int end = Math.min(start + chunkSize, totalCount);

            Thread thread = new Thread(() -> {
                try {
                    for (int i = start; i < end; i++) {
                        task.execute(i);
                    }
                } finally {
                    latch.countDown();
                }
            });

            thread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }

        /* =========================
           Prüfung der Konfiguration
           ========================= */

    private void validateConfiguration() {
        int maxPossibleByEventCapacity = EVENT_COUNT * AVAILABLE_TICKETS_PER_EVENT;
        int maxPossibleByCustomerRule = EVENT_COUNT * CUSTOMER_COUNT * 5;
        int absoluteMaxPossible = Math.min(maxPossibleByEventCapacity, maxPossibleByCustomerRule);

        if (TICKET_COUNT > absoluteMaxPossible) {
            throw new IllegalArgumentException(
                    "TICKET_COUNT ist zu groß. Maximal möglich sind " + absoluteMaxPossible
            );
        }
    }

    private String buildPairKey(long customerId, long eventId) {
        return customerId + "|" + eventId;
    }

        /* =========================
           Hilfstypen
           ========================= */

    @FunctionalInterface
    private interface IndexedTask {
        void execute(int index);
    }

    private static class Reservation {
        private final long customerId;
        private final long eventId;
        private final String pairKey;

        private Reservation(long customerId, long eventId, String pairKey) {
            this.customerId = customerId;
            this.eventId = eventId;
            this.pairKey = pairKey;
        }
    }
}