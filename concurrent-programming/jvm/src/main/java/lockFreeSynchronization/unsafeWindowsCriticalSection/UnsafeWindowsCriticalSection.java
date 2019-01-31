package lockFreeSynchronization.unsafeWindowsCriticalSection;

public class UnsafeWindowsCriticalSection {
    private int state = -1;     // -1 when the CS is free
    private long owner;  // identifier of the CS’ owner thread
    private int enterCount; //number of times the CS was entered by the current owner
    private EventWaitHandle waitEvent;// lazy created, event where waiters are blocked

    private EventWaitHandle getWaitEvent() {
        if (waitEvent == null)
            waitEvent = new AutoResetEvent(false);
        return waitEvent;
    }

    public void Enter() {
        long tid = Thread.currentThread().getId();
        if (++state == 0) { // CS was free, we acquired it
            owner = tid;
            enterCount = 1;
        } else if (owner == tid)
            enterCount++; // recursive enter
        else { // the CS is owned by another thread, so the current thread must wait
            getWaitEvent().waitOne();
            owner = tid;
            enterCount = 1;
        }
    }

    public void Leave() {
        if (owner != Thread.currentThread().getId()) throw new IllegalStateException();
        if (--enterCount > 0) // recursive leave
            state -= 1;
        else { // last leave: clear the owner thread
            owner = 0;
            if (--state >= 0) //At least one thread is waiting, wake one of the waiting threads
                getWaitEvent().set();
        }
    }

    private class EventWaitHandle {
        private void waitOne() {
        }

        private void set() {
        }
    }

    private class AutoResetEvent extends EventWaitHandle {
        private AutoResetEvent(boolean b) {
        }
    }
}
