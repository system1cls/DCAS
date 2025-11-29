package org.example;

import com.google.common.collect.TreeMultiset;

import java.util.*;
import java.util.concurrent.*;


public class Main {
    public static void main(String[] args) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> set = new HashSet<>();
        List<String> list = new ArrayList<>();
        queue.add("");
        ExecutorService service = Executors.newFixedThreadPool(20);
        List<Future<String>> futures = new ArrayList<>();

        try {

                synchronized (queue) {
                    set.add(queue.peek());
                    Sender sender = new Sender(queue, queue.peek());
                    futures.add(service.submit(sender));
                    queue.poll();
                }

                while (!futures.isEmpty()) {
                    String str = futures.getFirst().get();
                    futures.removeFirst();
                    list.add(str);

                    synchronized (queue) {
                        while (queue.size() > 0) {
                            String host = queue.peek();
                            if (!set.contains(host)) {
                                Sender sender = new Sender(queue, queue.peek());
                                futures.add(service.submit(sender));
                                set.add(host);
                            }
                            queue.poll();
                        }
                    }
                }


        } catch (ExecutionException | InterruptedException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        service.close();

        list.sort(String::compareTo);
        for (var str : list) {
            System.out.println(str);
        }

    }
}