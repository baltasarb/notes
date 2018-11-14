package utils;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLinkedList<T> {

    private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;

    public ConcurrentLinkedList() {
        Node dummy = new Node(null, null);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
    }

    public void addLast(T item) {
        Node newNode = new Node(item, null);

        while (true) {
            Node observedTail = tail.get();
            Node tailNext = observedTail.next.get();

            //if tail next is not null, the list is in an intermediate state
            if (tailNext != null) {
                tail.compareAndSet(observedTail, tailNext);
                continue;
            }
            //insert the new node to the next of tail
            if (observedTail.next.compareAndSet(null, newNode)) {
                //try to move the tail immediately
                tail.compareAndSet(observedTail, newNode);
                return;
            }
        }
    }

    public T removeFirst() {
        while (true) {
            Node observedHead = head.get();
            Node observedHeadNext = observedHead.next.get();

            if (observedHeadNext == null) {
                return null;
            }

            if (head.compareAndSet(observedHead, observedHeadNext)) {
                return observedHeadNext.value;
            }
        }
    }

    public boolean isEmpty() {
        return head.get().next == null;
    }

    private class Node {
        final T value;
        final AtomicReference<Node> next;

        Node(T value, Node next) {
            this.value = value;
            this.next = new AtomicReference<>(next);
        }
    }

}


