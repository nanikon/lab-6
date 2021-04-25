package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Sender {
    private ObjectOutputStream os;

    public Sender(Socket socket) {
        try {
            os = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendString(String message) {
        try {
            os.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(Command command) {
        try {
            os.writeObject(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        os.close();
    }
}