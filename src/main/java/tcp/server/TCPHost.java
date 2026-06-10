package tcp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPHost {

    private ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final int port;

    public TCPHost(int port) {
        this.threadPool = Executors.newCachedThreadPool();
        this.port = port;
    }

    public void start() {
        //todo
    }


}
