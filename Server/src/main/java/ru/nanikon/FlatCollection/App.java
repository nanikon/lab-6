package ru.nanikon.FlatCollection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static void main(String[] args) {
        //System.out.println(InetAddress.getLocalHost());
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
           logger.error("Ошибка! Порт должен быть числом");
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            logger.error("Ошибка! Вы не ввели порт");
            System.exit(0);
        }
        Server server = new Server(port, logger);
       //Server server = new Server(8881, logger);
        server.run();
        //System.out.println(server.read());
        //server.write("И тебе привет!");
    }
}
