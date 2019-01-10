package lockFreeSynchronization.unsafeLock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/*
public class SafeLock {
    private AtomicInteger state = new AtomicInteger(-1); // state is -1 when the Lock is free
    private AtomicReference<EventWaitHandle> waitEvent; ​// event where waiters are blocked - lazy created

    public EventWaitHandle getEvent(){

        if(waitEvent.get() != null){
            return waitEvent;
        }

        waitEvent.compareAndSet(null, new AutoResetEvent(false));

        return waitEvent.get();
    }

    public void Enter() {
        if (state.incrementAndGet() == 0)
            return; // lock was free, we acquired it
        Event.WaitOne();  ​// lock is busy, block the current thread
    }

    public void Leave() {
        if (state.decrementAndGet() >= 0) Event.set();   ​// at least one thread is waiting, grant ownership to one of them
    }
}
*/
