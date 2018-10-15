package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

//The actual client side that establishes a connection to the server
class ChatClient implements Runnable{

    //Server address details
    private static final int PORT = 2222;
//    private static final String HOST = "25.61.199.172";
    private static final String HOST = "localhost";

    //the socket of the client
    private static Socket clientSocket;
    //the output stream received from the server
    private static PrintStream os;
    //the input stream sent to the server
    private static BufferedReader is;
    //the input entered by the user
    private static BufferedReader inputLine;
    private static boolean closed = false;

    public static void main(String[] args) {
        try {
            //initialisation of the client
            clientSocket = new Socket(HOST, PORT);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + HOST);
        } catch (IOException e){
            e.printStackTrace();
        }

        if (clientSocket != null && os != null && is != null){
            //starts a thread of the client if the initialisation succeeded
            try {
                new Thread(new ChatClient()).start();
                //sent user input while the client is open
                while (!closed){
                    os.println(inputLine.readLine().trim());
                }
                //close the streams and the socket when the client leaves the room
                os.close();
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Thread that processes messages from the server and/or other clients
    @Override
    public void run() {
        try {
            String responseLine;
            while ((responseLine = is.readLine()) != null) {
                if (responseLine.equals("bye")){
                    closed = true;
                    break;
                }
                else {
                    System.out.println(responseLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
