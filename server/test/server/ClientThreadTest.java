package server;

import org.junit.Test;

import java.io.IOException;

public class ClientThreadTest {

    @Test
    public void setColor() throws IOException {
        new ClientThread(null, new ClientThread[0]).colorPrompt();
    }
}