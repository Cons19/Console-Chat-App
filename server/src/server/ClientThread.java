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
//TODO: login?(MySQL, GearHost)
//TODO: server can send messages
//TODO: colored messages

/**
 * A thread for a client that joined the server.
 * Broadcasts the user input to all other clients, and processes
 * different commands sent by the user (using the forward slash, '/')
 */
class ClientThread extends Thread {
    //input and output streams
    private BufferedReader is;
    private PrintStream os;
    //the socket
    private Socket clientSocket;
    //the clients array populated by the server
    private final ClientThread[] threads;
    //the max number of clients, normally the length of the aforementioned array
    private int maxClientsCount;
    //the display name of the client
    private String clientName;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.maxClientsCount = threads.length;
    }

    @Override
    public void run() {
        try {
            onEnter();
            processInput();
            onLeave();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //actions performed after the client successfully joined the chat room
    private void onEnter() throws IOException {
        //sets the input and output streams
        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        os = new PrintStream(clientSocket.getOutputStream());
        //Asks the client for a display name
        os.println("Enter your name: ");
        clientName = is.readLine().trim();
        os.printf("Hello, %s%n", clientName);
        System.out.printf("%s joined.%n", clientName);
        //Informs the room about the new client
        synchronized (this) {
            broadcastMessage(String.format("User %s entered the chat room.%n", clientName));
        }
    }

    //Processes the protocol of the message
    private void processInput() throws IOException {
        synchronized (this) {
            while (true) {
                String line = is.readLine();
                //parseProtocol returns false if the user typed the exit command
                if (parseProtocol(line)) break;
            }
        }
    }

    private void onLeave() throws IOException {
        broadcastMessage(String.format("User %s left the chat room.", clientName));
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
                    System.out.printf("%s: %s", Protocols.MSG_INVALID, line);
            }
            return false;
        }
        broadcastMessage(String.format("<%s> %s", clientName, line));
        return false;
    }

    //Sends the message to all clients
    private void broadcastMessage(String line) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null) {
                threads[i].os.println(line);
            }
        }
    }

    //Getters, Setters
    public String getClientName() {
        return clientName;
    }
}
