package tcp.server;

public class ServerMain {

    public static void main(String[] args) {
        int port = 12345;
        TCPHost server = new TCPHost(port);
        server.start();
    }
}
