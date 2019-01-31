package lockFreeSynchronization.unsafeCLHSpinLock;

public class UnsafeCLHSpinLock {

    private class CLHNode {
        private boolean successorMustWait = true; // The default is to wait for a lock
    }

    private CLHNode tail; // the tail of the wait queue; when null the lock is free

    public CLHNode Acquire() throws InterruptedException {
        CLHNode myNode = new CLHNode();    // insert my node at tail of queue and get my predecessor
        CLHNode predecessor = tail;
        tail = myNode;    // If there is a predecessor spin until the lock is free; otherwise we got the lock
        if (predecessor != null) {
            while (predecessor.successorMustWait) Thread.sleep(0);
        }
        return myNode;
    }

    public void Release(CLHNode myNode /* the node returned from corresponding Acquire */) {
        //If we are the last node on the queue, then set tail to null; else release successor
        if (tail == myNode) tail = null;
        else myNode.successorMustWait = false;// Release our successor
    }
}
