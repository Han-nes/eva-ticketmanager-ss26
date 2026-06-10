package tcp.client.ticketShop.commandHandler;

import core.models.Event;
import tcp.client.ticketShop.TicketShopStringFormatter;
import tcp.client.TcpClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TCPTicketShopEventHandler {

    private final TcpClient tcpClient;


    public TCPTicketShopEventHandler(TcpClient tcpClient){
        this.tcpClient = tcpClient;
    }

    public List<Event> getAllEvents() {
        try {
            String response = tcpClient.send("event;getall;");
            return parseEvents(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Event createEvent(
            String name,
            String location,
            LocalDateTime time,
            int ticketsAvailable
    ) {
        try {
            String msg = String.format(
                    "event;create;%s,%s,%s,%d",
                    name,
                    location,
                    time,
                    ticketsAvailable
            );
            String response = tcpClient.send(msg);
            return parseEvent(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Event getEventById(long id) {
        try {
            String msg = String.format("event;getbyid;%s", id);
            String response = tcpClient.send(msg);
            return parseEvent(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEvent(Event event) {
        try {
            String msg = String.format(
                    "event;update;%s,%s,%s,%d",
                    event.getId(),
                    event.getName(),
                    event.getLocation(),
                    event.getTicketsAvailable()
            );
            tcpClient.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteEvent(long id) {
        try {
            String msg = String.format("event;delete;%s", id);
            tcpClient.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllEvents() {
        try {
            tcpClient.send("event;deleteall;");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Event parseEvent(String response) {
        return TicketShopStringFormatter.eventFromString(response);
    }


    private List<Event> parseEvents(String response) {
        List<Event> events = new ArrayList<>();
        if (response == null || response.isBlank()) return events;
        String[] parts = response.split(";");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    events.add(TicketShopStringFormatter.eventFromString(part));
                } catch (Exception ignored) {}
            }
        }
        return events;
    }
}
