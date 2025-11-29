package org.example;

public class Sorter {
    final Object []elemKey;
    myList list;

    Sorter(myList list, int cntElem) {
        elemKey = new Object[cntElem];
        for (int i = 0; i < cntElem; i++) {
            elemKey[i] = new Object();
        }
        this.list = list;
    }

    void start(int cntThread) {
        Stat stat = new Stat(cntThread);
        for (int i = 0; i < cntThread; i++) {
            new Thread(new SorterThread(list, elemKey, elemKey.length, stat)).start();
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
        final myList list;
        final int cntElem;
        final Stat stat;
        int timeWait = 1000;
        final Object []elemKey;


        SorterThread(myList list, Object[] keys, int cnt, Stat stat) {
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
                        System.out.println(Thread.currentThread() + " i == " + i);

                        synchronized (elemKey[i + 1]) {
                            System.out.println(Thread.currentThread() + " i == " + (i + 1));

                            cur = list.head;
                            next = cur.getNext();
                            if (cur.str.compareTo(next.str) > 0) {
                                cur.setNext(next.getNext());
                                next.setNext(cur);
                                isSorted = false;
                                stat.incCnt();
                                list.head = next;
                                isSwaped = true;
                            }
                        }
                    }
                } else {


                    synchronized (elemKey[i - 1]) {
                        System.out.println(Thread.currentThread() + " i == " + (i - 1));

                        synchronized (elemKey[i]) {
                            System.out.println(Thread.currentThread() + " i == " + i);

                            synchronized (elemKey[i + 1]) {
                                System.out.println(Thread.currentThread() + " i == " + (i + 1));

                                if (!isSwaped) {
                                    prev = cur;
                                    cur = cur.getNext();

                                    isSwaped = false;
                                }
                                else {
                                    prev = next;
                                }
                                next = cur.getNext();


                                if (cur.str.compareTo(next.str) > 0) {
                                    prev.setNext(next);
                                    cur.setNext(next.getNext());
                                    next.setNext(cur);
                                    stat.incCnt();
                                    isSorted = false;
                                    isSwaped = true;
                                }
                            }
                        }
                    }
                }
            }

            return isSorted;
        }


        private void print() {
            Node node = list.head;
            for (int i = 0; i < cntElem; i++) {
                synchronized (elemKey[i]) {
                    System.out.println(node.str);
                    node = node.getNext();
                }

            }
        }

    }
}
