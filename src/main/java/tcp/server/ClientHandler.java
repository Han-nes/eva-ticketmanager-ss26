package tcp.server;

import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestHandler requestHandler;




    public ClientHandler(Socket socket, TicketService ticketService, CustomerService customerService, EventService eventService) {
        this.socket = socket;
        this.requestHandler = new RequestHandler(ticketService, customerService, eventService);
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(),
                true
            )
        ) {
            // Handle client communication here
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(
                    "Received message from client " +
                    socket.getRemoteSocketAddress() +
                    ": " +
                    line
                );
                String answer;
                try {
                    answer = requestHandler.callMethodRemotely(line);
                } catch (Exception e) {
                    answer = e.getMessage();
                }
                out.println(answer);
            }
        } catch (IOException e) {
            System.err.println(
                "Caught IOException on client communication: " + e
            );
        }
    }
}
