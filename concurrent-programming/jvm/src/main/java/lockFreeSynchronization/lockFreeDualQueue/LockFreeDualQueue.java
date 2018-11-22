package lockFreeSynchronization.lockFreeDualQueue;

import java.util.concurrent.atomic.*;

/***
 * Lock-free dualqueue
 * William N. Scherer III and Michael L. Scott
 *
 * from: http://www.cs.rochester.edu/research/synchronization/pseudocode/duals.html
 *
 */

public class LockFreeDualQueue<T> {

    // types of queue nodes
    private enum NodeType {
        DATUM, REQUEST
    }

    // the queue node
    private static class QueueNode<T> {
        NodeType type;
        final T data;
        final AtomicReference<QueueNode<T>> request;
        final AtomicReference<QueueNode<T>> next;

        //  build a datum or request node
        QueueNode(T d, NodeType t) {
            type = t;
            data = d;
            request = new AtomicReference<>(null);
            next = new AtomicReference<>(null);
        }
    }

    // the head and tail references
    private final AtomicReference<QueueNode<T>> head;
    private final AtomicReference<QueueNode<T>> tail;

    public LockFreeDualQueue() {
        QueueNode<T> sentinel = new QueueNode<>(null, NodeType.DATUM);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
    }

    // enqueue a datum
    public void enqueue(T datum) {
        QueueNode<T> newNode = new QueueNode<>(datum, NodeType.DATUM);
        QueueNode<T> observedHead, observedHeadNext, observedTail, observedTailNext;
        while (true) {
            observedHead = head.get();
            observedTail = tail.get();

            if (observedHead == observedTail || observedTail.type != NodeType.REQUEST) {
                // queue empty, tail falling behind, or queue contains data (queue could also
                // contain exactly one outstanding request with tail pointer as yet unswung)
                observedTailNext = observedTail.next.get();

                if (observedTail == tail.get()) {// tail and next are consistent

                    if (observedTailNext != null) {//tail falling behind
                        tail.compareAndSet(observedTail, observedTailNext);
                    } else {// try to link in the new node
                        if (observedTail.next.compareAndSet(null, newNode)) {
                            // linked in request; now try to swing tail pointer
                            tail.compareAndSet(observedTail, newNode);
                            return;
                        }
                    }
                }
            } else {// queue consists of requests
                observedHeadNext = observedHead.next.get();
                if (observedTail == tail.get()) { // tail has not changed
                    QueueNode<T> observedRequest = observedHead.request.get();
                    if (observedHead == head.get()) {// head, next, and req are consistent
                        boolean success = observedRequest == null && observedHead.request.compareAndSet(observedRequest, newNode);
                        // try to remove fulfilled request even if it's not mine
                        head.compareAndSet(observedHead, observedHeadNext);
                        if (success)
                            return;
                    }
                }
            }
        }
    }

    // dequeue a datum - spinning if necessary
    public T dequeue() throws InterruptedException {
        QueueNode<T> observedHead, observedHeadNext, observedTail, observedTailNext, newNode = null;
        do {
            observedHead = head.get();
            observedTail = tail.get();

            if (observedTail == observedHead || observedTail.type == NodeType.REQUEST) {
                // queue empty, tail falling behind, or queue contains data (queue could also
                // contain exactly one outstanding request with tail pointer as yet unswung)
                observedTailNext = observedTail.next.get();

                if (observedTail == tail.get()) {        // tail and next are consistent
                    if (observedTailNext != null) {    // tail falling behind
                        tail.compareAndSet(observedTail, observedTailNext);
                    } else {    // try to link in a request for data
                        if (newNode == null) {
                            newNode = new QueueNode<>(null, NodeType.REQUEST);
                        }
                        if (observedTail.next.compareAndSet(null, newNode)) {
                            // linked in request; now try to swing tail pointer
                            tail.compareAndSet(observedTail, newNode);

                            // help someone else if I need to
                            if (observedHead == head.get() && observedHead.request.get() != null) {
                                head.compareAndSet(observedHead, observedHead.next.get());
                            }

                            // busy waiting for a data done.
                            // we use sleep instead od yield in order to accept interrupts
                            while (observedTail.request.get() == null) {
                                Thread.sleep(0);  // spin accepting interrupts!!!
                            }

                            // help snip my node
                            observedHead = head.get();
                            if (observedHead == observedTail) {
                                head.compareAndSet(observedHead, newNode);
                            }

                            // data is now available; read it out and go home
                            return observedTail.request.get().data;
                        }
                    }
                }
            } else {    // queue consists of real data
                observedHeadNext = observedHead.next.get();
                if (observedTail == tail.get()) {
                    // head and next are consistent; read result *before* swinging head
                    T result = observedHeadNext.data;
                    if (head.compareAndSet(observedHead, observedHeadNext)) {
                        return result;
                    }
                }
            }
        } while (true);
    }

    public boolean isEmpty() {
        QueueNode<T> observedHead = head.get();
        return observedHead.next.get() == null || observedHead.type == NodeType.REQUEST;
    }

}
