package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The entry point for the server, this class is responsible for
 * keeping references to every client connected to the server,
 * as well as configuring new clients that join the chat room
 */
public class ThreadedServer {
    private static boolean isDone = false;
    //The port number
    private static final int PORT = 2222;
    //Max number of clients connected at one time
    private static final int MAX_CLIENTS = 10;
    //Array of clients; +1 to hold the server's own client thread
    private static final ClientThread[] threads = new ClientThread[MAX_CLIENTS+1];
    //The server socket
    private static ServerSocket serverSocket;
    //Container class for protocol types and response messages
    final class Protocols{
        static final String EXIT = "exit",
                            PROMOTE = "promote",
                            DEPROMOTE = "depromote",
                            KICK = "kick",
                            MUTE = "mute",
                            UNMUTE = "unmute",
                            BYE = "bye",
                            MSG_INVALID = "Invalid command",
                            CURRENT_STATUS = "currentStatus",
                            CHANGE_STATUS = "changeStatus",
                            EMOJI = "emoji",
                            PRIVATE_MESSAGE = "PM";
    }

    //main Server thread
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        threads[0] = new ClientThread(null, threads){
            @Override
            protected void onEnter() throws IOException {
                setIs(System.in);
                setOs(System.out);
                setClientName("Server");
                promote();
                setColor(Colors.PURPLE);
            }
        };

        threads[0].start();

        while (threads[0] != null){
            try {
                Socket clientSocket = serverSocket.accept();
                //loops through the threads array, adding the new client
                //to the first empty element found
                int i;
                for (i = 0; i < MAX_CLIENTS; i++) {
                    if (threads[i] == null){
                        threads[i] = new ClientThread(clientSocket, threads);
                        threads[i].start();
                        break;
                    }
                }
                //if the loop went through the whole array without
                //finding an empty element, this means the array is full of clients,
                //so refuse the new client
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

}
