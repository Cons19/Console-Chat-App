package server;

import server.ThreadedServer.Protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.function.Consumer;

//TODO: private messages [Razvan, Paul]
//TODO: online/offline (availability) [Razvan, Paul]
//TODO: block private messages from particular clients
//TODO: filter/censor words [Razvan, Paul]
//TODO: change clientName [Dragos]
//TODO: admin client - can kick/mute/promote other clients
//TODO: login?(MySQL, GearHost)
//TODO: server can send messages [Marius]
//TODO: colored messages  [Marius]
//TODO: /help command

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

    private boolean isAdmin;
    private static boolean firstClient = true;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.maxClientsCount = threads.length;
        isAdmin = false;
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
        if (firstClient){
            this.promote();
            firstClient = false;
        }
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
            //user typed "/exit"
            if (line.substring(1).equals(Protocols.EXIT)) {
                return true;
            } else if (line.substring(1).startsWith(Protocols.PROMOTE)) {
                //1 to get to the char after the slash
                //+ protocol length to get to the char after the protocol word
                //+1 to get the char after the space that follows the protocol
                promoteOther(line.substring(1+Protocols.PROMOTE.length()+1));
            } else if (line.substring(1).startsWith(Protocols.DEPROMOTE)) {
                dePromoteOther(line.substring(1+Protocols.DEPROMOTE.length()+1));
            } else if (line.substring(1).contains("jkl")) {//sampleMethod3();

            } else {//inform the user about the invalid command
                System.out.printf("%s: %s", Protocols.MSG_INVALID, line);
            }
            return false;
        }
        //send the normal message to the chat room
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

    private void getCommands(){}

    //promote or depromote the client
    private void promote() {
        if (!isAdmin) {
            isAdmin = true;
            broadcastMessage(String.format("%s is now an admin.", clientName));
        }
    }
    private void dePromote(){
        if (isAdmin){
            isAdmin = false;
            broadcastMessage(String.format("%s is no longer an admin.", clientName));
        }
    }

    //promote or depromote another client
    private void promoteOther(String clientName){
        ClientThread clientThread = getClient(clientName);
        if(clientThread != null){
            executeAdminCommand(ClientThread::promote, clientThread);
        }
    }
    private void dePromoteOther(String clientName){
        ClientThread clientThread = getClient(clientName);
        if(clientThread != null){
            executeAdminCommand(ClientThread::dePromote, clientThread);
        }
    }

    //executes an admin command on a targeted client
    private void executeAdminCommand(Consumer<ClientThread> command, ClientThread targetClient){
        //checks if the user executing the command is an admin
        if (isAdmin){
            //execute the command (ignore the mindblowing Consumer<T> class)
            command.accept(targetClient);
        }
        else{
            //client is not an admin
            os.println("You don't have permission to do that.");
        }
    }

    //return the clientThread with that name
    //or null if the name is not found
    private ClientThread getClient(String clientName){
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName.equalsIgnoreCase(clientName)){
                return threads[i];
            }
        }
        os.printf("User %s not found.%n", clientName);
        return null;
    }
}
