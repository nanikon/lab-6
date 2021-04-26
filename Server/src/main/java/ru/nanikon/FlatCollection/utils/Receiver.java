package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Receiver {
    //private ObjectInputStream is;
    private Socket socket;
    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
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
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
        String result = (String) is.readObject();
        //is.close();
        return result;
    }

    public Command receiveCommand() throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
        Command command = (Command) is.readObject();
        //is.close();
        return command;
    }
}
