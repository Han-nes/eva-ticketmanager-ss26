package tcp.server;

import core.services.CustomerService;
import core.services.EventService;
import core.services.TicketService;
import idGenerator.idService.IDService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPHost {

    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final int port;

    private final EventService eventService;
    private final CustomerService customerService;
    private final TicketService ticketService;

    public TCPHost(int port) {
        this.threadPool = Executors.newCachedThreadPool();
        this.port = port;

        IDService idService = new IDService(10000L, 99999L);
        this.ticketService = new TicketService(idService);
        this.customerService = new CustomerService(ticketService, idService);
        this.eventService = new EventService(ticketService, idService);
        ticketService.setCustomerService(customerService);
        ticketService.setEventService(eventService);
    }

    public void start() {
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(
                    "Server started. Listening on port " +
                    serverSocket.getLocalPort()
                );
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println(
                        "New client connected: " +
                        clientSocket.getRemoteSocketAddress()
                    );
                    threadPool.submit(new ClientHandler(clientSocket, ticketService, customerService, eventService));
                }
                serverSocket.close();
            } catch (IOException e) {
                System.err.println(
                    "Caught IOException on ServerSocket creation: " +
                    e.getMessage()
                );
            }
        });
        serverThread.start();
    }


}
