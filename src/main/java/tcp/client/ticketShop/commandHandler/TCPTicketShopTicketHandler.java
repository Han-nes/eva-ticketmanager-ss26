package tcp.client.ticketShop.commandHandler;

import core.models.Ticket;
import tcp.client.TcpClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TCPTicketShopTicketHandler {

    private final TcpClient tcpClient;

    public TCPTicketShopTicketHandler(TcpClient tcpClient){
        this.tcpClient = tcpClient;
    }

    public List<Ticket> getAllTickets() {
        try {
            String response = tcpClient.send("ticket;getall;");
            return parseTickets(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Ticket createTicket(long customerId, long eventId) {
        try {
            String msg = String.format(
                    "ticket;create;%s,%s",
                    customerId,
                    eventId
            );
            String response = tcpClient.send(msg);
            return parseTicket(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Ticket getTicketById(long id) {
        try {
            String msg = String.format("ticket;getbyid;%s", id);
            String response = tcpClient.send(msg);
            return parseTicket(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTicket(long id) {
        try {
            String msg = String.format("ticket;delete;%s", id);
            tcpClient.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllTickets() {
        try {
            tcpClient.send("ticket;deleteall;");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyTicket(long id) {
        // Not implemented in RequestHandler
        throw new UnsupportedOperationException(
                "Remote validateTicket not implemented"
        );
    }

    private Ticket parseTicket(String response) {
        // Parse fields
        String[] parts = response.split(",");
        long id = Long.getLong(parts[0].split("=")[1]);
        LocalDate dateOfPurchase = LocalDate.parse(parts[1].split("=")[1]);

        return new Ticket(id, dateOfPurchase, Long.getLong(parts[2].split("=")[1]), Long.getLong(parts[3].split("=")[1]));
    }

    private List<Ticket> parseTickets(String response) {
        List<Ticket> tickets = new ArrayList<>();
        if (response == null || response.isBlank()) return tickets;
        String[] parts = response.split(";");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                tickets.add(parseTicket(part));
            }
        }
        return tickets;
    }
}
