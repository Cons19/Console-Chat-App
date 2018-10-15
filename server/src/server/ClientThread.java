package server;

import server.ThreadedServer.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

//TODO: private messages
//TODO: online/offline (availability)
//TODO: block private messages from particular clients
//TODO: filter/censor words
//TODO: change clientName
//TODO: admin client - can kick/mute/promote other clients
class ClientThread extends Thread {
    private BufferedReader is;
    private PrintStream os;
    private Socket clientSocket;
    private final ClientThread[] threads;
    private int maxClientsCount;

    public String getClientName() {
        return clientName;
    }

    private String clientName;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    @Override
    public void run() {
        try {
            onEnter();
            processInput();
            broadcastMessage("User " + clientName + " left the chat room.");
            onLeave();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onLeave() throws IOException {
        os.println("Bye, " + clientName + "!");

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
    }

    private void processInput() throws IOException {
        synchronized (this) {
            while (true) {
                String line = is.readLine();
                if (parseProtocol(line)) break;
                broadcastMessage("<" + clientName + "> " + line);
            }
        }
    }

    //returns false if the client left the chat
    private boolean parseProtocol(String line) {
        if (line.length() > 0 && line.charAt(0) == '/') {
            switch (line.substring(1)) {
                case Protocols.EXIT:
                    return true;
                case "asd":
                    //sampleMethod1();
                    break;
                case "fgh":
                    //sampleMethod2();
                    break;
                case "jkl":
                    //sampleMethod3();
                    break;
                default:
                    System.out.println(Protocols.MSG_INVALID);
            }
        }
        return false;
    }

    private void broadcastMessage(String line) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null) {
                threads[i].os.println(line);
            }
        }
    }

    private void onEnter() throws IOException {
        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        os = new PrintStream(clientSocket.getOutputStream());
        System.out.println("Asking for name");
        os.println("Enter your name: ");
        clientName = is.readLine().trim();
        os.println("Hello, " + clientName);
        synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null && threads[i] != this) {
                    threads[i].os.println("User " + clientName + " entered the chat room.");
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ClientThread";
    }
}
