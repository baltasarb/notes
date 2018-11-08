package lockFreeSynchronization.unsafeCyclicBarrier;

public class UnsafeCyclicBarrier {
    private final int partners;
    private int remaining, currentPhase;

    public UnsafeCyclicBarrier(int partners) {
        if (partners <= 0) throw new IllegalArgumentException();
        this.partners = this.remaining = partners;
    }

    public void signalAndAwait() {
        int phase = currentPhase;
        if (remaining == 0) throw new IllegalStateException();
        if (--remaining == 0) {
            remaining = partners;
            currentPhase++;
        } else {
            while (phase == currentPhase) Thread.yield();
        }
    }
}