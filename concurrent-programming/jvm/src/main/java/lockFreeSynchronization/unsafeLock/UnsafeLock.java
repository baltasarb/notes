package lockFreeSynchronization.unsafeLock;

public class UnsafeLock {
    private int state = -1; // state is -1 when the Lock is free
    private EventWaitHandle waitEvent; // event where waiters are blocked - lazy created

    private EventWaitHandle getEvent() {
        if (waitEvent == null) waitEvent = new AutoResetEvent(false);
        return waitEvent;
    }

    public void Enter() {
        if (++state == 0)
            return; // lock was free, we acquired it
        getEvent().waitOne(); // lock is busy, block the current thread
    }

    public void Leave() {
        if (--state >= 0) getEvent().set();  // at least one thread is waiting, grant ownership to one of them
    }

    //Placeholder classes so it compiles
    private class EventWaitHandle {
        private void set() {
        }

        private void waitOne() {
        }
    }

    private class AutoResetEvent extends EventWaitHandle {
        private AutoResetEvent(boolean b) {
        }
    }

}