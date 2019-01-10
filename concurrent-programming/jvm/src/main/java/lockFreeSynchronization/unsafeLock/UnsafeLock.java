package lockFreeSynchronization.unsafeLock;

public class UnsafeLock {
    /*private int state = -1; // state is -1 when the Lock is free
    private EventWaitHandle waitEvent; ​// event where waiters are blocked - lazy created
    private EventWaitHandle Event;

    public getEvent(){
        if (waitEvent == null) waitEvent = new AutoResetEvent(false);
        return waitEvent;
    }

    public void Enter() {
        if (++state == 0)
            return; // lock was free, we acquired it
        Event.WaitOne();  ​// lock is busy, block the current thread
    }

    public void Leave() {
        if (--state >= 0) Event.Set();   ​// at least one thread is waiting, grant ownership to one of them
    }*/
}