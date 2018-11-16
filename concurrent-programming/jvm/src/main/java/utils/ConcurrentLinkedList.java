package utils;

import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLinkedList<T> {

    private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;

    public ConcurrentLinkedList() {
        //both the tail and the head will point, on an epty list, to the same node
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
            //try to solve before inserting the new node
            if (tailNext != null) {
                tail.compareAndSet(observedTail, tailNext);
                continue;
            }
            //try to insert the new node to the next of tail
            if (observedTail.next.compareAndSet(null, newNode)) {
                //try to move the tail immediately to exit intermediate
                tail.compareAndSet(observedTail, newNode);
                return;
            }
        }
    }

    public T removeFirst() {
        while (true) {
            Node observedHead = head.get();
            Node observedHeadNext = observedHead.next.get();

            //if the current head next is null it means the list is empty
            if (observedHeadNext == null) {
                return null;
            }

            //try to remove the node on head next and return it if removed successfully
            if (head.compareAndSet(observedHead, observedHeadNext)) {
                return observedHeadNext.value;
            }
        }
    }

    public boolean isEmpty() {
        //if the head and tail are pointing to the same node then they are pointing to the dummy, ergo the list is empty
        return head.get() == tail.get();
    }

    private class Node {
        private final T value;
        private final AtomicReference<Node> next;

        private Node(T value, Node next) {
            this.value = value;
            this.next = new AtomicReference<>(next);
        }
    }

}


