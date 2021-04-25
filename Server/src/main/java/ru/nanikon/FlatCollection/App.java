package ru.nanikon.FlatCollection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class App {
    public static void main(String[] args) throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost());
        Server server = new Server(8881);
        server.run();
        //System.out.println(server.read());
        //server.write("И тебе привет!");
    }
}
