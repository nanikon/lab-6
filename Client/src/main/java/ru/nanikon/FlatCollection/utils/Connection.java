package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Connection {
    private SocketChannel serverSocketChannel;
    private Selector selector;
    private String host;
    private int serverPort;
    private String filename;

    public void startConnection(String host, int serverPort, String filename) {
        try {
            this.host = host;
            this.serverPort = serverPort;
            this.filename = filename;
            selector = Selector.open();
            serverSocketChannel = SocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            serverSocketChannel.connect(new InetSocketAddress(host, serverPort));
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid() && key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        sendString(filename);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Не удается подключится к серверу. Пробуем снова через 5 сек (но возможно вы ошиблись с хостом и портом и наши попытки бесполезны)");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            startConnection(host, serverPort, filename);
            //System.exit(0);
        }
    }

    public void sendString(String message) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(message);
            ByteBuffer outBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid() && key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(outBuffer);
                        if (outBuffer.remaining() < 1) {
                            //System.out.println("сообщение отправлено " + new String(outBuffer.array(), 0, outBuffer.position()));
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Упс, сервер отвалился... Пробую переподключиться");
            startConnection(host, serverPort, filename);
            //System.exit(0);
        }
    }

    public void sendCommand(Command command) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            ByteBuffer outBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid() && key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(outBuffer);
                        if (outBuffer.remaining() < 1) {
                            //System.out.println("Послал команду");
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Упс, сервер отвалился... Пробую переподключиться");
            startConnection(host, serverPort, filename);
            //System.out.println("Переподключился");
            sendCommand(command);
        }
    }

    public HashMap<String, Command> receiveMap() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(10000000);
                        ByteBuffer outBuffer = ByteBuffer.allocate(10000000);
                        try {
                            while (socketChannel.read(byteBuffer) > 0) {
                                byteBuffer.flip();
                                outBuffer.put(byteBuffer);
                                byteBuffer.compact();
                                try {
                                    ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outBuffer.array()));
                                    return (HashMap<String, Command>) objectInputStream.readObject();
                                } catch (StreamCorruptedException ignored) {
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Упс, сервер отвалился... Приходи в следующий раз!");
                            socketChannel.close();
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Упс, сервер отвалился... Приходи в следующий раз!");
            System.exit(0);
            return null;
        }
    }

    public String receive() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(100000);
                        ByteBuffer outBuffer = ByteBuffer.allocate(100000);
                        try {
                            while (socketChannel.read(byteBuffer) > 0) {
                                byteBuffer.flip();
                                outBuffer.put(byteBuffer);
                                byteBuffer.compact();
                                try {
                                    ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outBuffer.array()));
                                    return (String) objectInputStream.readObject();
                                } catch (StreamCorruptedException ignored) {
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Упс, сервер отвалился... Приходи в следующий раз!");
                            socketChannel.close();
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Упс, сервер отвалился... Приходи в следующий раз!");
            System.exit(0);
            return null;
        }
    }

    public void stopConnection() {
        try {
            serverSocketChannel.close();
        } catch (IOException ignored) {
        }
    }
}
