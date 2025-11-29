package org.example;

import java.util.ArrayList;
import java.util.List;

public class ArrayListSorter {
    final Object []elemKey;
    List<Node> list;

    ArrayListSorter(List<Node> list, int cntElem) {
        elemKey = new Object[cntElem];
        for (int i = 0; i < cntElem; i++) {
            elemKey[i] = new Object();
        }
        this.list = list;
    }

    void start(int cntThread) {
        ArrayListSorter.Stat stat = new ArrayListSorter.Stat(cntThread);
        for (int i = 0; i < cntThread; i++) {
            new Thread(new ArrayListSorter.SorterThread(list, elemKey, elemKey.length, stat)).start();
        }

        try {
            synchronized (stat.wakeMain) {
                if (stat.cntThread > 0) {
                    stat.wakeMain.wait();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(stat.cntSw);
        for (Node node : list) {
            System.out.println(node.str);
        }
    }

    static class Stat {
        private volatile int cntSw = 0;
        public volatile int cntThread;
        public final Object wakeMain = new Object();

        Stat(int cntThread) {
            this.cntThread = cntThread;
        }

        synchronized void incCnt() {
            cntSw++;
        }

        int getCntSw() {
            return cntSw;
        }
    }

    class SorterThread implements Runnable {
        final List<Node> list;
        final int cntElem;
        final ArrayListSorter.Stat stat;
        int timeWait = 1000;
        final Object []elemKey;

        SorterThread(List<Node> list, Object[] keys, int cnt, ArrayListSorter.Stat stat) {
            this.list = list;
            this.cntElem = cnt;
            this.stat = stat;
            this.elemKey = keys;
        }

        @Override
        public void run() {
            boolean isSorted = false;
            int it = 0;
            while(!isSorted) {
                isSorted = checkAndDoSwap();
            }

            synchronized (stat.wakeMain) {
                stat.cntThread--;
                if (stat.cntThread == 0) {
                    stat.wakeMain.notify();
                }
            }

        }

        boolean checkAndDoSwap() {
            Node prev = null;
            Node cur = null;
            Node next = null;
            boolean isSorted = true;
            boolean isSwaped = false;
            for (int i = 0; i < cntElem - 1; i++) {

                if (i == 0) {

                    synchronized (elemKey[i]) {

                        synchronized (elemKey[i + 1]) {

                            cur = list.getFirst();
                            next = list.get(i + 1);
                            if (cur.str.compareTo(next.str) > 0) {
                                Node temp = cur;
                                list.set(i, next);
                                list.set(i + 1, cur);
                                isSorted = false;
                                stat.incCnt();
                                isSwaped = true;
                            }
                        }
                    }
                } else {


                    synchronized (elemKey[i - 1]) {

                        synchronized (elemKey[i]) {

                            synchronized (elemKey[i + 1]) {


                                cur = list.get(i);
                                next = list.get(i + 1);

                                if (cur.str.compareTo(next.str) > 0) {
                                    list.set(i, next);
                                    list.set(i+1, cur);
                                    stat.incCnt();
                                    isSorted = false;

                                }
                            }
                        }
                    }
                }
            }

            return isSorted;
        }


        private void print() {

            for (int i = 0; i < cntElem; i++) {
                synchronized (elemKey[i]) {
                    System.out.println(list.get(i).str);
                }
            }
        }

    }

}
