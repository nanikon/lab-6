package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;

public class Receiver {
    private Socket socket;
    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
    }

    public String receive() {
        /*StringBuilder result = new StringBuilder();
        try {
            int a = is.read();
            result.append((char) a);
            while (is.available() > 0) {
                a = is.read();
                result.append((char) a);
            }
        } catch (IOException e) {
            System.out.println("сервер отвалился");
        }
        return result.toString();*/
        String result = null;
        try {
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            result = (String) is.readObject();
        } catch (IOException e) {
            System.out.println("Сервер отвалился");
        } catch (ClassNotFoundException e) {
            System.out.println("Не смог найти класс");
        }
        return result;
    }

    public HashMap<String, Command> receiveMap()  {
        HashMap<String, Command> commands = null;
        try {
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            commands = (HashMap<String, Command>) is.readObject();
        } catch (IOException e) {
            System.out.println("Что-то не то");
        } catch (ClassNotFoundException e) {
            System.out.println("Не смог найти класс");
        }
        return commands;
    }
}
