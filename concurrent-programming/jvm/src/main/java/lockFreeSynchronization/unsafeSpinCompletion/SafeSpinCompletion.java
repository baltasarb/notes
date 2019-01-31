package lockFreeSynchronization.unsafeSpinCompletion;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeSpinCompletion {

    private final int OPEN = -1;
    private AtomicInteger state = new AtomicInteger(0);

    public void Wait() throws InterruptedException {

        int observedState;
        do {
            observedState = state.get();

            if (observedState == OPEN) {
                return;
            }

            while (state.get() == 0) {
                Thread.sleep(0);
            }

            if (observedState != OPEN) {
                state.decrementAndGet();
            }
        } while (true);

    }

    public void complete() {
        if (state.get() == OPEN) return;
        state.incrementAndGet();
    }

    public void completeAll() {
        state.set(OPEN);
    }
}
