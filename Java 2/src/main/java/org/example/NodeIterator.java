package org.example;

import java.util.Iterator;

public class NodeIterator implements Iterator<Node> {
    Node cur;

    public NodeIterator(myList list) {
        cur = list.head;
    }

    @Override
    public boolean hasNext() {
        return cur != null;
    }

    @Override
    public Node next() {
        Node node = cur;
        cur = cur.getNext();
        return node;
    }
}
