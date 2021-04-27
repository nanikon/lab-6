package ru.nanikon.FlatCollection;


import ru.nanikon.FlatCollection.arguments.AbstractArgument;
import ru.nanikon.FlatCollection.commands.*;
import ru.nanikon.FlatCollection.exceptions.FileCollectionException;
import ru.nanikon.FlatCollection.exceptions.StopConnectException;
import ru.nanikon.FlatCollection.utils.CollectionManager;
import ru.nanikon.FlatCollection.utils.JsonLinkedListParser;
import ru.nanikon.FlatCollection.utils.Receiver;
import ru.nanikon.FlatCollection.utils.Sender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
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
                System.out.println("название файла получено");
                JsonLinkedListParser parser = new JsonLinkedListParser(filename);
                CollectionManager collectionManager = new CollectionManager(parser);
                HashMap<String, Command> commands = loadCommand();
                sender.sendMap(commands);
                boolean work = true;
                while (work) {
                    try {
                        Command command = receiver.receiveCommand();
                        String answer = command.execute(collectionManager);
                        sender.sendString(answer);
                    } catch (StopConnectException e) {
                        sender.sendString(e.getAnswer());
                        work = false;
                        stopConnection(s);
                    }
                }
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
        commands.put("add", new AddCommand());
        commands.put("update", new UpdateCommand());
        commands.put("insert_at", new InsertCommand());
        commands.put("remove_by_id", new RemoveCommand());
        commands.put("exit", new ExitCommand());
        commands.put("average_of_number_of_rooms", new AverageOfNumberOfRoomsCommand());
        commands.put("clear", new ClearCommand());
        commands.put("filter_less_than_view", new FilterLessThanViewCommand());
        commands.put("info", new InfoCommand());
        commands.put("remove_any_by_transport", new RemoveAnyByTransportCommand());
        commands.put("sort", new SortCommand());
        commands.put("history", new HistoryCommand());
        return commands;
    }

    public void stopConnection(Socket s) {
        try {
            s.close();
        } catch (IOException ignored) {
        }
    }
}
