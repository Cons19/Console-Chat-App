package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

class ClientThread extends Thread {
    private BufferedReader is;
    private PrintStream os;
    private Socket clientSocket;
    private final ClientThread[] threads;
    private int maxClientsCount;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    @Override
    public void run() {
        try {
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            os = new PrintStream(clientSocket.getOutputStream());
            System.out.println("Asking for name");
            os.println("Enter your name: ");
            String name = is.readLine().trim();
            os.println("Hello, " + name);
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("User " + name + " entered the chat room.");
                    }
                }
            }
            synchronized (this) {
                while (true) {
                    String line = is.readLine();
                    if (line.startsWith(ThreadedServer.Protocols.EXIT)) {
                        break;
                    }
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null) {
                            threads[i].os.println("<" + name + "> " + line);
                        }
                    }
                }
            }
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println("User " + name + " left the chat room.");
                }
            }

            os.println("Bye, " + name + "!");

            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }

            is.close();
            os.close();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ClientThread";
    }
}
