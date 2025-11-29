package org.example;

import java.util.Iterator;

public class myList implements Iterable<Node>{
    Node head = null;

    void add(Node node) {
        node.setNext(head);
        head = node;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeIterator(this);
    }

}
