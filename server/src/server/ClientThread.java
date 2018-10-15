package server;

import server.ThreadedServer.Protocols;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

//TODO: private messages [Razvan, Paul]
//TODO: online/offline (availability) [Razvan, Paul] [DONE]
//TODO: block private messages from particular clients [Razvan, Paul]
//TODO: filter/censor words [Razvan, Paul]
//TODO: change clientName [Dragos]
//DONE: admin client - can kick/mute/promote other clients
//TODO: login?(MySQL, GearHost)
//DONE: server can send messages [Marius]
//DONE: colored messages  [Marius]
//TODO: EMOJI
//TODO: names with country flag
//TODO: /help command
//TODO: support for Italian

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
    //the status (available/unavailable) of the client
    private boolean isAvailable;
    private boolean isAdmin;
    private volatile boolean isJoined;
    private boolean isMuted;
    private String color;
    static class Colors {
        static final String RESET = "\u001B[0m";
        static final String PURPLE = "\u001B[35m";
        static Map<String, String> colorMap = new HashMap<>();
        static {
            colorMap.put("black", "\u001B[30m");
            colorMap.put("red", "\u001B[31m");
            colorMap.put("green", "\u001B[32m");
            colorMap.put("yellow", "\u001B[33m");
            colorMap.put("blue", "\u001B[34m");
//            colorMap.put("purple", "\u001B[35m");
            colorMap.put("cyan", "\u001B[36m");
            colorMap.put("white", "\u001B[37m");
        }
        static String get(String colorName){
            return colorMap.get(colorName);
        }
    }

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.maxClientsCount = threads.length;
        this.isAdmin = false;
        this.isJoined = true;
        //change client status to available
        this.isAvailable = true;
    }

    @Override
    public void run() {
        try {
            onEnter();
            processInput();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //actions performed after the client successfully joined the chat room
    protected void onEnter() throws IOException {
        //sets the input and output streams
        is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        os = new PrintStream(clientSocket.getOutputStream());
        //Asks the client for a display name
        os.println("Enter your name: ");
        clientName = is.readLine().trim();
        os.printf("Hello, %s%n", clientName);
        colorPrompt();
        //Informs the room about the new client
        synchronized (this) {
            broadcastMessage(String.format("%s joined.", clientName));
        }
    }

    //Processes the protocol of the message
    private void processInput() throws IOException {
        synchronized (this) {
            while (isJoined) {
                String line = is.readLine();
                parseProtocol(line);
            }
        }
    }

    private void onLeave() {
        isJoined = false;
        broadcastMessage(String.format("%s left.", clientName));
        os.println(Protocols.BYE);

        synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
            }
        }

        try {
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ignored){}
    }

    //returns false if the client left the chat
    private void parseProtocol(String line) {
        if (line.length() > 0) {
            if (line.charAt(0) == '/') {
                //user typed "/exit"
                if (line.substring(1).equals(Protocols.EXIT)) {
                    onLeave();
                    return;
                }
                //1 to get to the char after the slash
                //+ protocol length to get to the char after the protocol word
                //+1 to get the char after the space that follows the protocol
                else try {
                    if (line.substring(1).startsWith(Protocols.PROMOTE)) {
                        promoteOther(line.substring(1 + Protocols.PROMOTE.length() + 1), true);
                    } else if (line.substring(1).startsWith(Protocols.DEPROMOTE)) {
                        promoteOther(line.substring(1 + Protocols.DEPROMOTE.length() + 1), false);
                    } else if (line.substring(1).startsWith(Protocols.KICK)) {
                        kickOther(line.substring(1 + Protocols.KICK.length() + 1));
                    } else if (line.substring(1).startsWith(Protocols.MUTE)) {
                        muteOther(line.substring(1 + Protocols.MUTE.length() + 1), true);
                    } else if (line.substring(1).startsWith(Protocols.UNMUTE)) {
                        muteOther(line.substring(1 + Protocols.UNMUTE.length() + 1), false);
                    } else if (line.substring(1).contains("currentStatus")) {
                        if (isAvailable) {
                            os.println(clientName + " - Current Status is available.");
                        } else {
                            os.println(clientName + " - Current Status is unavailable.");
                        }
                    } else if (line.substring(1).contains("changeStatus")) {
                        changeStatus();
                        if (isAvailable) {
                            os.println(clientName + " - Status changed to available.");
                        } else {
                            os.println(clientName + " - Status changed to unavailable.");
                        }
                    } else if (line.substring(1).contains("emoji")) {
                        os.println(clientName + " - \uD83C\uDDEE\uD83C\uDDF9");
                    }
                } catch (StringIndexOutOfBoundsException ignored) {}
                os.printf("%s: \"%s\"%n", Protocols.MSG_INVALID, line);
            } else {
                //send the normal message to the chat room
                broadcastMessage(String.format("%s<%s> %s%s", color, clientName, line, Colors.RESET));
            }
        }
    }

    //Sends the message to all clients
    private void broadcastMessage(String line) {
        if (!isMuted) {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null) {
                    threads[i].os.println(line);
                }
            }
        }
        else{
            os.println("You are muted, you cannot do that.");
        }
    }

    private void getCommands(){}

    //promote or depromote the client
    void promote() {
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
    private void kick(){
        os.println("You have been kicked from the room.");
        onLeave();
    }
    private void mute(){
        if (!isMuted) {
            isMuted = true;
            os.println("You have been muted.");

        }
    }
    private void unMute(){
        if (isMuted) {
            isMuted = false;
            os.println("You are no longer muted.");

        }
    }
    //promote or depromote another client
    private void promoteOther(String clientName, boolean asAdmin){
        ClientThread clientThread = getClient(clientName);
        if(clientThread != null){
            if (asAdmin) {
                executeAdminCommand(ClientThread::promote, clientThread);
            }
            else {
                executeAdminCommand(ClientThread::dePromote, clientThread);
            }
        }
    }
    private void kickOther(String clientName){
        ClientThread clientThread = getClient(clientName);
        if (clientThread != null && clientThread != this){
            executeAdminCommand(ClientThread::kick, clientThread);
        }
    }
    private void muteOther(String clientName, boolean mute){
        ClientThread clientThread = getClient(clientName);
        if (clientThread != null && clientThread != this){
            if (mute)
                executeAdminCommand(ClientThread::mute, clientThread);
            else
                executeAdminCommand(ClientThread::unMute, clientThread);
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
    private ClientThread getClient(String clientName) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].clientName.equalsIgnoreCase(clientName)) {
                return threads[i];
            }
        }
        os.printf("User %s not found.%n", clientName);
        return null;
    }
    private boolean changeStatus(){
        if (isAvailable) {
            isAvailable = false;
        } else {
            isAvailable = true;
        }
        return isAvailable;
    }

    //Getters, Setters
    void setIs(InputStream is) {
        if (this.is == null)
            this.is = new BufferedReader(new InputStreamReader(is));
    }
    void setOs(PrintStream os) {
        if (this.os == null)
            this.os = os;
    }
    void setClientName(String clientName) {
        if (this.clientName == null)
            this.clientName = clientName;
    }
    void setColor(String color){
        if (color != null)
            this.color = color;
    }
    void colorPrompt() throws IOException {
        //for testing only
//        os = System.out;
//        is = new BufferedReader(new InputStreamReader(System.in));
        //
        StringBuilder colorMessage = new StringBuilder();
        colorMessage.append("Choose your color: ");
        for (String colorKey : Colors.colorMap.keySet()) {
            colorMessage.append(Colors.colorMap.get(colorKey)).append(colorKey.toUpperCase()).append(' ');
        }
        colorMessage.append(Colors.RESET);
        os.println(colorMessage);
        do {
            String input = is.readLine().toLowerCase();
            color = Colors.get(input);
            if (color == null){
                os.println("Invalid option");
            }
        }while (color == null);
    }

    public String getClientName() {
        return clientName;
    }
}
