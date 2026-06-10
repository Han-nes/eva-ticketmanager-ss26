package tcp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        //todo
    }

    public String send(String message) throws IOException {
        //todo
        return "";
    }


    public void close() {
        //todo
    }
}
