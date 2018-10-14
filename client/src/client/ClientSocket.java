package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {
    private Socket clientSocket;

    public ClientSocket(Socket socket){
        this.clientSocket = socket;
    }

    public static void main(String[] args) throws IOException {

        //establishing  a socket connection
        Socket myClientsConnection = new Socket ("localhost",3010);

        //communication between client and server
        SendThread sendThread = new SendThread (myClientsConnection);
        Thread thread = new Thread (sendThread);
        thread.start ();
        ReceiveThread receiveThread = new ReceiveThread (myClientsConnection);
        Thread thread2 = new Thread (receiveThread);
        thread2.start ();





    }

}

class ReceiveThread implements Runnable{

    Socket receiveThreadSocket;
    BufferedReader receive = null;

    public ReceiveThread (Socket myClient){
        this.receiveThreadSocket = myClient;
    }


    @Override
    public void run() {
        try {
            receive = new BufferedReader (new InputStreamReader (this.receiveThreadSocket.getInputStream ()));


            String messageIn;
            while ((messageIn = receive.readLine ()) != null) {

                System.out.println ("From server : " + messageIn);
                System.out.println ("please enter something to send to  server . . ");
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

}


class SendThread implements Runnable{

    Socket sendThreadSocket;
    PrintWriter print = null;
    BufferedReader brinput = null;

    public SendThread (Socket sock){
        this.sendThreadSocket = sock;

    }

    @Override
    public void run() {
        try{
            if(sendThreadSocket.isConnected())
            {
                System.out.println("Client connected to "+sendThreadSocket.getInetAddress() + " on port "+sendThreadSocket.getPort());
                this.print = new PrintWriter(sendThreadSocket.getOutputStream(), true);
                while(true){
                    System.out.println("Type your message to send to server..type 'EXIT' to exit");
                    brinput = new BufferedReader(new InputStreamReader(System.in));
                    String msgtoServerString = null;
                    msgtoServerString = brinput.readLine();
                    this.print.println(msgtoServerString);
                    this.print.flush();

                    if(msgtoServerString.equals("EXIT"))
                        break;
                }//end while
                sendThreadSocket.close();}}catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }//end run method

}