package server;

import org.junit.Test;

import java.io.IOException;

public class ClientThreadTest {

    @Test
    public void setColor() throws IOException {
//        new ClientThread(null, new ClientThread[0]).colorPrompt();
    }

    @Test
    public void emoji(){
        StringBuffer sb = new StringBuffer();
        sb.append(Character.toChars(127467));
        sb.append(Character.toChars(127479));
        System.out.println("👍");
        System.out.println("\uD83C\uDDEE\uD83C\uDDF9");
    }
}