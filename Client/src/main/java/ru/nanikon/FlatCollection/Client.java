package ru.nanikon.FlatCollection;

import ru.nanikon.FlatCollection.commands.Command;
import ru.nanikon.FlatCollection.utils.Receiver;
import ru.nanikon.FlatCollection.utils.Sender;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    private Socket s;
    private Sender sender;
    private Receiver receiver;
    private String filename;

    public Client(String addr, int port, String filename) {
        try {
            s = new Socket(addr, port);
            sender = new Sender(s);
            receiver = new Receiver(s);
            this.filename = filename;
        } catch (IOException e) {
            System.out.println("Не смог подключиться к серверу");
        }
    }

    public void run() {
        sender.sendString(filename);
        HashMap<String, Command> commands = receiver.receiveMap();
        Scanner scr = new Scanner(System.in);
        while (true) {
            String line = scr.nextLine().trim();
            Command command = commands.get(line);
            sender.sendCommand(command);
            String answer = receiver.receive();
            System.out.println(answer);
        }
        /*Scanner scr = new Scanner(System.in);
        String line;
        while (true) {
            line = scr.nextLine();
            sender.send(line);
            line = receiver.receive();
            System.out.println(line);
        }*/
    }
}
