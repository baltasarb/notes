package lockFreeSynchronization.unsafeLock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SafeLock {
    private AtomicInteger state = new AtomicInteger(-1); // state is -1 when the Lock is free
    private AtomicReference<EventWaitHandle> waitEvent = new AtomicReference<>(null); // event where waiters are blocked - lazy created

    private EventWaitHandle getEvent() {
        if (waitEvent.get() == null) {
            waitEvent.compareAndSet(null, new AutoResetEvent(false));
        }

        return waitEvent.get();
    }

    public void Enter() {

        if (state.incrementAndGet() == 0) {
            return;
        }

        getEvent().waitOne();
    }

    public void Leave() {
        if (state.decrementAndGet() >= 0) {
            getEvent().set();
        }
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

