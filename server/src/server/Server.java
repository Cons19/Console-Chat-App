package server;
import client.ClientSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {


    public static void main(String[] args) throws IOException, InterruptedException {

        ServerSocket serverSocket = new ServerSocket (3010);
        ArrayList <ClientSocket> clientSockets = new ArrayList <> ();

        while (true)
        {
            Socket socket = serverSocket.accept ();
            System.out.println ("Someone connected");
            ClientSocket cs = new ClientSocket(socket);
            clientSockets.add (cs);

            ReceiveFromClientThread receive = new ReceiveFromClientThread (socket);
            Thread thread = new Thread (receive);
            thread.start ();

            SendToClientThread send = new SendToClientThread (socket);
            Thread thread2 = new Thread (send);
            thread2.start ();

        }
    }
}

class ReceiveFromClientThread implements Runnable{

    Socket clientSocket = null;
    BufferedReader brBufferedReader = null;

    public ReceiveFromClientThread(Socket clientSocket){
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        try {
            brBufferedReader = new BufferedReader (new InputStreamReader (this.clientSocket.getInputStream ()));

            String messageString;

            while (true){

                //assign message from client to messageString
                while ((messageString = brBufferedReader.readLine ()) !=null){
                    if (messageString.equals ("EXIT")){
                        break;
                    }

                    //printing the message from client
                    System.out.println ("From client: " + messageString);

                }

                this.clientSocket.close ();
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }
}


class SendToClientThread implements Runnable{

    PrintWriter pwPrintWriter;
    Socket clientSock ;

    public SendToClientThread (Socket clientSock){
        this.clientSock = clientSock;
    }


    @Override
    public void run() {

        try {
            pwPrintWriter = new PrintWriter (new OutputStreamWriter (this.clientSock.getOutputStream ()));

            while (true){
                String msgToClientString = null;
                BufferedReader input = new BufferedReader (new InputStreamReader (System.in));

                msgToClientString = input.readLine (); //get message to sent to client

                pwPrintWriter.println (msgToClientString);
                pwPrintWriter.flush ();
                System.out.println ("please enter something to send back to client");
            }

        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}