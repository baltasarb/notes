package lockFreeSynchronization.unsafeSpinWindowsMutex;

public class UnsafeSpinWindowsMutex {
    private int signalState = 1;  //1 when the mutex is free, <= 0 when the mutex is busy
    private Thread owner; // null or the mutex's owner thread

    public void Acquire() throws InterruptedException {
        do {
            if (signalState > 0 || owner == Thread.currentThread()) {
                if (signalState == Integer.MIN_VALUE)
                    throw new IllegalStateException("Mutex not owned");//MutexNotOwnedException();
                if (--signalState == 0) owner = Thread.currentThread();
                return;
            }
            Thread.sleep(0);//spin once
        } while (true);
    }

    public void Release() {
        if (owner != Thread.currentThread())
            throw new IllegalStateException("Mutex not owned");//MutexNotOwnedException();
        if (signalState == 0) // if the mutex becomes free, first, reset the owner
            owner = null;
        signalState += 1;
    }
}
