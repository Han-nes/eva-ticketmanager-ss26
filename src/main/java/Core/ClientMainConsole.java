package core;

import core.clients.ConsoleClientLocal;

class ClientMainConsole {

    public static void main(String[] args) {
        ConsoleClientLocal consoleClientLocal = new ConsoleClientLocal();
        consoleClientLocal.start();
    }
}
