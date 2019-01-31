package lockFreeSynchronization.unsafeWindowsCriticalSection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SafeWindowsCriticalSection {
    private AtomicInteger state = new AtomicInteger(-1);     // -1 when the CS is free
    private volatile long owner;  // identifier of the CS’ owner thread
    private AtomicInteger enterCount = new AtomicInteger(0); //number of times the CS was entered by the current owner
    private AtomicReference<EventWaitHandle> waitEvent;// lazy created, event where waiters are blocked

    private EventWaitHandle getWaitEvent() {
        EventWaitHandle currentEvent;
        do {
            currentEvent = waitEvent.get();

            if (currentEvent != null) {
                return currentEvent;
            }

            if (waitEvent.compareAndSet(null, new AutoResetEvent(false))) {
                return waitEvent.get();
            }
        } while (true);
    }

    public void Enter() {
        long threadId = Thread.currentThread().getId();
        if (state.incrementAndGet() == 0) { // CS was free, we acquired it
            owner = threadId;
            enterCount.set(1);
        } else if (owner == threadId)
            enterCount.incrementAndGet(); // recursive enter
        else { // the CS is owned by another thread, so the current thread must wait
            getWaitEvent().waitOne();
            owner = threadId;
            enterCount.set(1);
        }
    }

    public void Leave() {
        if (owner != Thread.currentThread().getId()) throw new IllegalStateException();
        if (enterCount.decrementAndGet() > 0) // recursive leave
            state.decrementAndGet();
        else { // last leave: clear the owner thread
            owner = 0;
            if (state.decrementAndGet() >= 0) //At least one thread is waiting, wake one of the waiting threads
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