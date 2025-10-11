package org.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class IOThread extends Thread {

    private final Selector selector;
    private boolean isAdding = false;
    private final Map<String, ClientData> clientTable;
    private final Set<String> clientsAdded;
    private final Queue<String> queue;
    private final Object selectorKey;
    private final List<SocketChannel> channelToAdd;

    IOThread(Map<String, ClientData> clientTable, Set<String> addedClients, Queue<String> queue) {
        this.clientsAdded = addedClients;
        channelToAdd = new ArrayList<>();
        this.clientTable = clientTable;
        selectorKey = new Object();
        this.queue = queue;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToSelector(SocketChannel channel) {

        synchronized (selectorKey) {
            isAdding = true;

            channelToAdd.addLast(channel);

            selector.wakeup();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {

                selector.select();

                synchronized (selectorKey) {
                    if (isAdding) {
                        isAdding = false;

                        for (var channel : channelToAdd) {
                            channel.register(selector, channel.validOps());
                        }

                        channelToAdd.clear();
                        continue;
                    }


                }

                Set<SelectionKey> selectionKeySet = selector.selectedKeys();

                for (SelectionKey key : selectionKeySet) {
                    if (key.isReadable()) {
                        handleClinet(key);
                    }

                }

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void handleClinet(SelectionKey key) {

        try {
            SocketChannel channel = (SocketChannel) key.channel();

            ByteBuffer lengthOfName = ByteBuffer.allocate(4);

            while(lengthOfName.hasRemaining()) {
                channel.read(lengthOfName);
            }

            lengthOfName.flip();
            int length = lengthOfName.getInt();

            ByteBuffer nameBuffer = ByteBuffer.allocate(length);

            while(nameBuffer.hasRemaining()) {
                channel.read(nameBuffer);
            }

            nameBuffer.flip();
            String name = new String(nameBuffer.array());

            synchronized (clientsAdded) {
                if (!clientsAdded.contains(name)) {

                    clientsAdded.add(name);
                    System.out.println("Client added: " + name);

                    synchronized (queue) {
                        queue.add(name);
                        queue.notify();
                    }

                    channel.close();
                    return;
                }
            }

            ClientData data;
            synchronized (clientTable) {
                if (clientTable.containsKey(name)) {
                    data = clientTable.get(name);
                }
                else {
                    channel.close();
                    return;
                }
            }

            System.out.println("Send data to " + name + "\n");
            writeData(channel, data);
            channel.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void writeData(SocketChannel channel, ClientData data) {
        ByteBuffer buffer;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(data);
            byte[] bytes = bos.toByteArray();


            buffer = ByteBuffer.allocate(bytes.length + 4);
            buffer.putInt(bytes.length);
            buffer.put(bytes);

            buffer.flip();
            while(buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
