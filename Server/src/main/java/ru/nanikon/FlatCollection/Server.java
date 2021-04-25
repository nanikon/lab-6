package ru.nanikon.FlatCollection;


import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.commands.Command;
import ru.nanikon.FlatCollection.commands.HelpCommand;
import ru.nanikon.FlatCollection.commands.ShowCommand;
import ru.nanikon.FlatCollection.exceptions.FileCollectionException;
import ru.nanikon.FlatCollection.utils.CollectionManager;
import ru.nanikon.FlatCollection.utils.JsonLinkedListParser;
import ru.nanikon.FlatCollection.utils.Receiver;
import ru.nanikon.FlatCollection.utils.Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private ServerSocket ss;

    public Server(int port) {
        try {
            this.ss = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Не смог создать сервер");
        }
    }

    public void run() {
        String message = null;
        while (true) {
            try {
                Socket s = ss.accept();
                Sender sender = new Sender(s);
                Receiver receiver = new Receiver(s);
                String filename = receiver.receiveString();
                JsonLinkedListParser parser = new JsonLinkedListParser(filename);
                CollectionManager collectionManager = new CollectionManager(parser);
                HashMap<String, Command> commands = loadCommand();
                sender.sendMap(commands);
                while (true) {
                    Command command = receiver.receiveCommand();
                    String answer = command.execute(collectionManager);
                    sender.sendString(answer);
                }
                /*try {
                    while (true) {
                        message = receiver.receiveString();
                        System.out.println(message);
                        sender.send(message + " принял");
                    }
                } catch (IOException e) {
                    sender.close();
                    receiver.close();
                    s.close();
                    System.out.println("Клиент отвалился");
                } catch (ClassNotFoundException e) {
                    System.out.println("Не смог найти класс");
                }*/
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("Не смог найти класс");
            } catch (FileCollectionException e) {
                System.out.println("Не смог найти файл");
            }
        }
    }

    public HashMap<String, Command> loadCommand() {
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("help", new HelpCommand(commands));
        commands.put("show", new ShowCommand());
        return commands;
    }

}
