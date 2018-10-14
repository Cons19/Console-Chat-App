package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ThreadedServer {
    private static final int PORT = 2222;
    private static final int MAX_CLIENTS = 10;
    private final class Protocols{
        static final String EXIT = "/exit";

    }

    private static ServerSocket serverSocket;
    private static Socket clientSocket;

    private static final ClientThread[] threads = new ClientThread[MAX_CLIENTS];

    public static void main(String[] args) {
        try {
            System.out.println("Creating server socket");
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true){
            try {
                System.out.println("Waiting for a socket");
                clientSocket = serverSocket.accept();
                System.out.println("accepted a socket");
                int i = 0;
                for (i = 0; i < MAX_CLIENTS; i++) {
                    if (threads[i] == null){
                        threads[i] = new ClientThread(clientSocket, threads);
                        threads[i].start();
                        break;
                    }
                }
                System.out.println(Arrays.toString(threads));
                if (i == MAX_CLIENTS){
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server full, try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientThread extends Thread {
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
                        if (line.startsWith(Protocols.EXIT)) {
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
}
