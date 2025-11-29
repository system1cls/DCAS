package org.example;

import java.util.Iterator;

public class Node {
    private Node next;
    public String str;

    public Node(String str) {
        this.str = str;
        next = null;
    }

    String getStr() {
        return str;
    }

    void setNext(Node next) {
        this.next = next;
    }


    Node getNext() {
        return next;
    }

}
