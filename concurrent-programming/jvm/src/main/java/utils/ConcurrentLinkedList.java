package utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLinkedList<T> {

    private final AtomicReference<Node> head;
    private final AtomicReference<Node> tail;
    private AtomicBoolean isEmpty;

    public ConcurrentLinkedList() {
        Node dummy = new Node(null, null);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
        isEmpty = new AtomicBoolean(true);
    }

    public void addLast(T item) {
        Node newNode = new Node(item, null);

        while (true) {

            Node originalTail = tail.get();
            Node tailNext = originalTail.next.get();

            if (originalTail == tail.get()) {
                if (tailNext != null) {
                    // Queue in intermediate state, advance tail
                    tail.compareAndSet(originalTail, tailNext);
                } else {
                    // In quiescent state, try inserting new node
                    if (originalTail.next.compareAndSet(null, newNode)) {
                        // Insertion succeeded, try advancing tail
                        //if unsuccessful someone already did it
                        tail.compareAndSet(originalTail, newNode);
                        return;
                    }
                }
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

    public void remove(T nodeToRemove) {
        //TODO mark removed nodes?
    }

    public boolean isEmpty() {
       return head.get() == tail.get();
    }

    private class Node {
        final T value;
        final AtomicReference<Node> next;

        Node(T value, Node next) {
            this.value = value;
            this.next = new AtomicReference<>(next);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Node aux = head.get().next.get();

        //if (aux == null)
         //   System.out.println("head next null");

        while (aux != null) {
            stringBuilder.append("[");
            stringBuilder.append(aux.value);
            stringBuilder.append("]");
            aux = aux.next.get();
        }

        return stringBuilder.toString();
    }

}


