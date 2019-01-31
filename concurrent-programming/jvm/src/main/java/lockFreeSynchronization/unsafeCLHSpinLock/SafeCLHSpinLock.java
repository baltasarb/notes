package lockFreeSynchronization.unsafeCLHSpinLock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SafeCLHSpinLock {

    private AtomicReference<CLHNode> tail; // the tail of the wait queue; when null the lock is free

    public CLHNode Acquire() throws InterruptedException {

        CLHNode myNode = new CLHNode();    // insert my node at tail of queue and get my predecessor
        CLHNode observedTail;
        CLHNode predecessor;

        do {
            observedTail = tail.get();
            predecessor = observedTail;
        } while (!tail.compareAndSet(observedTail, myNode));

        // If there is a predecessor spin until the lock is free; otherwise we got the lock
        if (predecessor != null) {
            while (predecessor.successorMustWait.get()) Thread.sleep(0);
        }
        return myNode;
    }

    public void Release(CLHNode myNode /* the node returned from corresponding Acquire */) {
        //If we are the last node on the queue, then set tail to null; else release successor
        CLHNode observedTail;

        do {
            observedTail = tail.get();

            if (observedTail == myNode) {
                if (tail.compareAndSet(observedTail, null))
                    return;
            }
            else {
                if (myNode.successorMustWait.compareAndSet(true, false)) {// Release our successor
                    return;
                }
            }

        } while (true);
    }

    private class CLHNode {
        private AtomicBoolean successorMustWait = new AtomicBoolean(true); // The default is to wait for a lock
    }
}
