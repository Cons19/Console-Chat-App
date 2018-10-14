package client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

class ChatClient implements Runnable{

    private static final int PORT = 2222;
    private static final String HOST = "localhost";

    private static Socket clientSocket;
    private static PrintStream os;
    private static DataInputStream is;

    private static BufferedReader inputLine;
    private static boolean closed = false;

    public static void main(String[] args) {
        try {
            clientSocket = new Socket(HOST, PORT);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + HOST);
        } catch (IOException e){
            e.printStackTrace();
        }

        if (clientSocket != null && os != null && is != null){
            try {
                new Thread(new ChatClient()).start();
                while (!closed){
                    os.println(inputLine.readLine().trim());
                }
                os.close();
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        String responseLine;

        try {
            while ((responseLine = is.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.contains("Bye")){
                    closed = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
