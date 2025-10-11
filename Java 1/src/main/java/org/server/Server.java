package org.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.security.PrivateKey;
import java.util.*;


public class Server {

    private final int cntThreadsForIO;
    private final IOThread[] IOThreads;
    private final int port;

    private int itOfIO = 0;

    Server(int port, int cntThreadsForIO, int cntThreadsForCreating, PrivateKey pairForCert, String issuer) {
        this.cntThreadsForIO = cntThreadsForIO;
        this.port = port;
        Set<String> clentsAdded = new HashSet<>();
        HashMap<String, ClientData> clientTable = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();

        this.IOThreads = new IOThread[cntThreadsForIO];
        for (int i = 0; i < this.cntThreadsForIO; i++) {
            IOThreads[i] = new IOThread(clientTable, clentsAdded, queue);
            IOThreads[i].start();
        }

        Thread[] keyGens = new Thread[cntThreadsForCreating];
        for (int i = 0; i < cntThreadsForCreating; i++) {
            keyGens[i] = new Thread(new KeyGen("SHA256withRSA", pairForCert, queue, clientTable, issuer));
            keyGens[i].start();
        }
    }

    public void startServer() {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            ServerSocket serverSocket = serverSocketChannel.socket();

            serverSocket.bind(new InetSocketAddress("localhost", port));

            while(true) {

                SocketChannel channel = serverSocket.accept().getChannel();
                channel.configureBlocking(false);


                IOThreads[itOfIO].addToSelector(channel);
                itOfIO = (itOfIO + 1) % cntThreadsForIO;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
