package tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RequestHandler requestHandler;


    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.requestHandler = new RequestHandler();
    }

    @Override
    public void run() {
        //todo
    }
}
