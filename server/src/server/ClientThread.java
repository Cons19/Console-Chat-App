package server;

import server.ThreadedServer.Protocols;
import server.lang.Languages;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

import static server.lang.Languages.Text.*;

//DONE: private messages [Razvan, Paul]
//DONE: online/offline (availability) [Razvan, Paul]
//DENIED: block private messages from particular clients [Razvan, Paul]
//DENIED: filter/censor words [Razvan, Paul]
//DONE: change clientName [Dragos]
//DONE: admin client - can kick/mute/promote other clients
//DENIED: login?(MySQL, GearHost)
//DONE: server can send messages [Marius]
//DONE: colored messages  [Marius]
//DONE: EMOJI [Razvan, Paul] -
//DENIED: names with country flag [Razvan, Paul] - merge into the it/ro
//DONE: /help command [Dragos]
//DONE: support for Italian [Marius if has time and wants to]
//DONE: support for Romanian [Marius if has time and wants to]
//DONE: Italian welcoming for user name "Andrea" [Razvan, Paul]


/**
 * A thread for a client that joined the server.
 * Broadcasts the user input to all other clients, and processes
 * different commands sent by the user (using the forward slash, '/')
 */
@SuppressWarnings({"RedundantStringFormatCall", "SameParameterValue"})
class ClientThread extends Thread {
    private static final String GOD = "ANDREA";
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
    private Languages.Language ln;
    class Colors {
        static final String RESET = "\u001B[0m";
        static final String PURPLE = "\u001B[35m";
    }

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.maxClientsCount = threads.length;
        this.isAdmin = false;
        this.isJoined = true;
        this.isMuted = false;
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
        //Asks the client for a display language
        langPrompt();
        //Asks the client for a display name
        os.printf(ln.text(S_ENTER_NAME));//S_ENTER_NAME
        setClientName(is.readLine().trim());
        os.printf(ln.text(S_HELLO), clientName);//S_HELLO
        colorPrompt();
        //Informs the room about the new client
        synchronized (this) {
            broadcastStatus(S_HAS_JOINED, clientName);//S_HAS_JOINED
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
        broadcastStatus(S_LEFT, clientName);//S_LEFT
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
                String command = line.substring(1);
                //user typed "/exit"
                if (command.equals(Protocols.EXIT)) {
                    onLeave();
                    return;
                }
                //1 to get to the char after the slash
                //+ protocol length to get to the char after the protocol word
                //+1 to get the char after the space that follows the protocol
                else try {
                    if (command.startsWith(Protocols.LANG)) {
                        changeLang(command.substring(Protocols.LANG.length() + 1));
                    } else if (command.startsWith(Protocols.PROMOTE)) {
                        promoteOther(command.substring(Protocols.PROMOTE.length() + 1), true);
                    } else if (command.startsWith(Protocols.DEPROMOTE)) {
                        promoteOther(command.substring(Protocols.DEPROMOTE.length() + 1), false);
                    } else if (command.startsWith(Protocols.KICK)) {
                        kickOther(command.substring(Protocols.KICK.length() + 1));
                    } else if (command.startsWith(Protocols.MUTE)) {
                        muteOther(command.substring(Protocols.MUTE.length() + 1), true);
                    } else if (command.startsWith(Protocols.UNMUTE)) {
                        muteOther(command.substring(Protocols.UNMUTE.length() + 1), false);
                    } else if (command.startsWith(Protocols.CURRENT_STATUS)) {
                        showStatus();
                    } else if (command.startsWith(Protocols.CHANGE_STATUS)) {
                        changeStatus();
                        showStatus();
                    } else if (command.startsWith(Protocols.EMOJI)) {
                        showEmoji(line);
                    } else if (command.startsWith(Protocols.PM)) {
                        privateMessage(line);
                    } else if (command.startsWith(Protocols.HELP)){
                        getCommands();
                    } else if (command.startsWith(Protocols.NAME)) {
                        String newName = command.substring(5);
                        os.println(getClientName() + " changed to " + newName);
                        setClientName(newName);
                    } else {
                        os.printf(ln.text(S_INVALID_COMMAND), line);//S_INVALID_COMMAND
                    }
                    return;
                } catch (StringIndexOutOfBoundsException ignored) {}
                os.printf(ln.text(S_INVALID_COMMAND), line);//S_INVALID_COMMAND
            } else {
                //send the normal message to the chat room
                broadcastMessage(line);
            }
        }
    }

    //extract parameters (targetClientName, message) to use in broadcastPrivateMessage method
    private void privateMessage(String line) {
        //Example: "/pm R: hello, R"
        boolean client1Available = false;
        boolean client2Available = false;

        os.println(clientName + " - " + line.substring(1+Protocols.PM.length()+1));
        String targetClientName = line.substring(1+Protocols.PM.length()+1, line.indexOf(":"));
        String message = line.substring(line.indexOf(":") + 1);

//      check is both clients have an available status, if so broadcast the message between them.
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] !=null && threads[i].getClientName().equals(clientName) && threads[i].isAvailable()) {
                client1Available = true;
            }
            if (threads[i] !=null && threads[i].getClientName().equals(targetClientName) && threads[i].isAvailable()) {
                client2Available = true;
            }
        }
        if (client1Available) {
            if (client2Available) {
                broadcastPrivateMessage(clientName, targetClientName, message);
            } else {
                os.printf(ln.text(S_PM_TARGET_UNAVAILABLE), targetClientName, targetClientName);//S_PM_TARGET_UNAVAILABLE
            }
        } else {
            os.print(ln.text(S_PM_SELF_UNAVAILABLE)); //S_PM_SELF_UNAVAILABLE
        }
    }

    //Show Current Status of the client
    private void showStatus() {
        if (isAvailable) {
            os.printf(ln.text(S_STATUS_AVAILABLE), clientName);//S_STATUS_AVAILABLE
        } else {
            os.printf(ln.text(S_STATUS_UNAVAILABLE), clientName);//S_STATUS_UNAVAILABLE
        }
    }

    private void showEmoji(String line) {
        // Example: /emoji smile -> message is "smile"
        String message = line.substring(line.indexOf(" ") + 1);

        if (message.equals(":)") || message.equals("clown")){
            broadcastMessage("🤡");
        }else if (message.equals(":D")){
            broadcastMessage("\uD83D\uDE00");
        }else if (message.equals("laugh")){
            broadcastMessage("\uD83D\uDE02");
        }else if (message.equals("sleeping")){
            broadcastMessage("\uD83D\uDE34");
        }else if (message.equals("santa")){
            broadcastMessage("\uD83C\uDF85");
        }else if (message.equals("gandalf")){
            broadcastMessage("\uD83E\uDDD9️");
        }else if (message.equals("genie")){
            broadcastMessage("\uD83E\uDDDE");
        }else if (message.equals("cool")){
            broadcastMessage("\uD83D\uDE0E");
        }else if (message.equals("smiling")){
            broadcastMessage("\uD83D\uDE01️");
        }
    }

    //Sends the message to all clients
    private void broadcastStatus(Languages.Text message, String... elements){
        if (!isMuted){
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null){
                    threads[i].os.printf(threads[i].ln.text(message), (Object[]) elements);
                }
            }
        }
    }
    private void broadcastMessage(String line) {
        if (!isMuted) {
            for (int i = 0; i < maxClientsCount; i++) {
                if (threads[i] != null) {
                    threads[i].os.printf("%s<%s-%s> %s%s%n", color, clientName, ln.name, line, Colors.RESET);
                }
            }
        }
        else{
            os.print(ln.text(S_CURRENTLY_MUTED));//S_CURRENTLY_MUTED
        }
    }

    //Sends the message to Private Client
    private void broadcastPrivateMessage(String clientName, String targetClientName, String line) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] !=null && threads[i].getClientName().equals(targetClientName)) {
                threads[i].os.printf(ln.text(S_PM_FROM), clientName, line); //S_PM_FROM
            }
        }
    }


    private void getCommands(){
        os.println("Chat commands");
        os.println();
        os.println("/exit                       - Quits the chat app");
        os.println("/promote                    - Admin can promote somebody to admin status");
        os.println("/depromote                  - Admin can remove admin rights");
        os.println("/kick                       - Admin can kick somebody from the chat app");
        os.println("/mute                       - Admin can mute somebody in the chat app");
        os.println("/unmute                     - Admin can unmute somebody in the chat app");
        os.println("/PM ClientName:Message      - Send a private message to the ClientName");
        os.println("/color blue/green/red etc   - Changes your font color to blue/green/red etc");
        os.println("/name Newname               - Change current name to Newname");
        os.println("/help                       - Displays chat commands");
    }

    //promote or depromote the client
    void promote() {
        if (!isAdmin) {
            isAdmin = true;
            broadcastStatus(S_IS_NOW_ADMIN, clientName);//S_IS_NOW_ADMIN
        }
    }
    void dePromote(){
        if (isAdmin){
            isAdmin = false;
            broadcastStatus(S_IS_NO_LONGER_ADMIN, clientName);//S_IS_NO_LONGER_ADMIN
        }
    }
    void kick(){
        os.print(ln.text(S_KICKED));//S_KICKED
        onLeave();
    }
    void mute(){
        if (!isMuted) {
            isMuted = true;
            os.print(S_MUTED);//S_MUTED

        }
    }
    void unMute(){
        if (isMuted) {
            isMuted = false;
            os.print(ln.text(S_UNMUTED));//S_UNMUTED
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
            os.print(ln.text(S_NO_PERMISSION));//S_NO_PERMISSION
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
        os.printf(ln.text(S_USER_NOT_FOUND), clientName); //S_USER_NOT_FOUND
        return null;
    }
    private void changeStatus(){
        isAvailable = !isAvailable;
    }
    private void colorPrompt() throws IOException {
        StringBuilder colorMessage = new StringBuilder();
        colorMessage.append(ln.text(S_COLOR_CHOOSE)); //S_COLOR_CHOOSE
        for (Languages.Color colorKey : Languages.Color.values()) {
            colorMessage.append(ln.colorValue(colorKey))
                    .append(ln.color(colorKey).toUpperCase())
                    .append(' ');
        }
        colorMessage.append(Colors.RESET);
        os.println(colorMessage);
        do {
            String input = is.readLine().toLowerCase();
            setColor(ln.colorValue(input));
            if (color == null){
                os.printf(ln.text(S_COLOR_INVALID));//S_COLOR_INVALID
            }
        }while (color == null);
    }
    private void langPrompt() throws IOException{
        StringBuilder langMessage = new StringBuilder();
        for (String langKey : Languages.all.keySet()) {
            langMessage.append(Languages.all.get(langKey).name).append(' ');
        }
        os.println(langMessage);
        do {
            String input = is.readLine().toUpperCase();
            changeLang(input);
        }while (ln == null);
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

        this.clientName = clientName;

        if (this.clientName.equalsIgnoreCase(GOD)) {
            this.clientName = GOD;
            setLang(Languages.it);
            promote();
        }


    }
    void setColor(String color){
        if (color != null)
            this.color = color;
    }
    void setLang(Languages.Language lang){
        if (lang != null && lang != this.ln){
            ln = lang;
            os.printf(ln.text(S_LANG_CHANGED), ln.name); //S_LANG_CHANGED
        }
        else {
            Languages.Language dummyLang = lang;
            if (dummyLang == null) dummyLang = Languages.en;
            os.printf(dummyLang.text(S_LANG_INVALID));//S_LANG_INVALID
        }
    }

    private void changeLang(String lang) {
        setLang(Languages.all.get(lang));
    }

    private String getClientName() {
        return clientName;
    }

    private boolean isAvailable() {
        return isAvailable;
    }
}
