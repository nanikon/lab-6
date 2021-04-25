package ru.nanikon.FlatCollection;

public class App {
    public static void main(String[] args) {
        Client client = new Client("localhost", 8881, "example.json");
        client.run();
    }
}
