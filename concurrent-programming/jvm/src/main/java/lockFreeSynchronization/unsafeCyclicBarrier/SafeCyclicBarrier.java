package lockFreeSynchronization.unsafeCyclicBarrier;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeCyclicBarrier {
    private final int partners;
    private RemainingHolder remainingHolder;

    public SafeCyclicBarrier(int partners) {
        if (partners <= 0) {
            throw new IllegalArgumentException();
        }
        this.partners = partners;
        remainingHolder = new RemainingHolder(partners);
    }

    public void signalAndAwait() {
        while (true) {
            RemainingHolder observedRemainingHolder = remainingHolder;

            int observedRemaining = observedRemainingHolder.remaining.get();

            if (observedRemaining == 0)
                throw new IllegalStateException();

            if (remainingHolder.remaining.compareAndSet(observedRemaining, observedRemaining - 1)) {
                if (observedRemaining == 1) {
                    remainingHolder = new RemainingHolder(partners);
                }
                return;
            } else {
                while (observedRemainingHolder == remainingHolder) {
                    Thread.yield();
                }
            }
        }

    }

    private class RemainingHolder {
        private final AtomicInteger remaining;

        private RemainingHolder(int remaining) {
            this.remaining = new AtomicInteger(remaining);
        }
    }

}

