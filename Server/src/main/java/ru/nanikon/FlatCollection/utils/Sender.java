package ru.nanikon.FlatCollection.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Sender {
    private ObjectOutputStream os;

    public Sender(Socket socket) throws IOException {
        os = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendString(String message) throws IOException {
        os.writeObject(message);
    }

    public void sendMap(HashMap<?, ?> map) throws IOException {
        os.writeObject(map);
    }

    public void close() throws IOException {
        os.close();
    }
}
