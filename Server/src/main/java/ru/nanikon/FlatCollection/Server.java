package ru.nanikon.FlatCollection;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.SocketException;
import java.util.HashMap;

public class Server {
    private ServerSocket ss;
    private String filename;
    private static Logger logger;

    public Server(int port, Logger logger) {
        this.logger = logger;
        try {
            this.ss = new ServerSocket(port);
            logger.info("Сервер создан");
        } catch (IOException e) {
            //System.out.println("Не смог создать сервер. Попробуйте ещё раз");
            logger.error("Невозможно было создать сервер", e);
            System.exit(0);
        }
    }

    public void run() {
        String message = null;
        offer:
        while (true) {
            try {
                Socket s = ss.accept();
                logger.info("Клиент успешно подключился" + s);
                Sender sender = new Sender(s);
                logger.info("Создан отправитель для клиента");
                Receiver receiver = new Receiver(s);
                logger.info("Создан приёмщик для клиента");
                CollectionManager collectionManager = null;
                try {
                    filename = receiver.receiveString();
                    logger.info("От клиента получено имя файла {}", filename);
                    JsonLinkedListParser parser = new JsonLinkedListParser(filename);
                    collectionManager = new CollectionManager(parser);
                    logger.info("Создан менеджер коллекции");
                    HashMap<String, Command> commands = loadCommand();
                    sender.sendMap(commands);
                    logger.info("Клиенту отправлена мапа доступных ему команд");
                } catch (FileCollectionException | IOException e) {
                    sender.sendString(e.getMessage() + ". Проверьте его и запустите программу ещё раз.");
                    logger.error(e.getMessage() + "Работа завершена");
                    stopConnection(s);
                    continue offer;
                }
                boolean work = true;
                while (work) {
                    try {
                        Command command = receiver.receiveCommand();
                        logger.info("От клинета получена комманда {}", command.getName());
                        String answer = command.execute(collectionManager);
                        logger.info("Команда исполнена и получен ответ {}", answer);
                        sender.sendString(answer);
                        logger.info("Ответ отправлен клиенту");
                    } catch (StopConnectException e) {
                        sender.sendString(e.getAnswer());
                        logger.info("Была получена команда о завершении. Клиенту "+ s +" отправлен сигнал о завершении {}", e.getAnswer());
                        work = false;
                        stopConnection(s);
                        logger.info("Соединение с клиентом завершено");
                    } catch (SocketException e) {
                        work = false;
                        logger.warn("Клиент отвалился" + s);
                        stopConnection(s);
                        logger.info("Соединение с клиентом завершено");
                    }
                }
            } catch (IOException e) {
                logger.warn("Клиент отвалился. Работа с ним завершена");
            } catch (ClassNotFoundException e) {
                logger.error("При десериализации не смог найти класс", e);
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
