package ru.nanikon.FlatCollection.utils;

import ru.nanikon.FlatCollection.commands.Command;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Connection {
    private SocketChannel serverSocketChannel;
    private Selector selector;

    public void startConnection(String host, int serverPort) {
        try {
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
                        return;
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public void sendString(String message) {
        try {
            ByteBuffer outBuffer = ByteBuffer.allocate(10000);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(message);
            outBuffer.put(byteArrayOutputStream.toByteArray());
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
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public void sendCommand(Command command) {
        try {
            ByteBuffer outBuffer = ByteBuffer.allocate(10000);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            outBuffer.put(byteArrayOutputStream.toByteArray());
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
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
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
                        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
                        ByteBuffer outBuffer = ByteBuffer.allocate(2048);
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
                            if (e.getMessage().equals("An established connection was aborted by the software in your host machine")) {
                                socketChannel.close();
                            }
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String receive() {
        try {
            while (true) {
                selector.select();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
                        StringBuilder message = new StringBuilder();
                        while (clientSocketChannel.read(buffer) > 0){
                            message.append(new String(buffer.array(), 0, buffer.position()));
                            buffer.compact();
                        }
                        return message.toString();
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void stopConnection() {
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
