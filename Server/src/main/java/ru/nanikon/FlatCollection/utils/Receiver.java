package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Receiver {
    private ObjectInputStream is;
    public Receiver(Socket socket) throws IOException {
        this.is = new ObjectInputStream(socket.getInputStream());
    }

    public String receiveString() throws ClassNotFoundException, IOException {
        /*int a = is.read();
        StringBuilder result = new StringBuilder();
        result.append((char) a);
        while (is.available() > 0) {
            a = is.read();
            result.append((char) a);
        }
        return result.toString();*/
        String result = (String) is.readObject();
        return result;
    }

    public Command receiveCommand() throws IOException, ClassNotFoundException {
        Command command = (Command) is.readObject();
        return command;
    }

    public void close() {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
