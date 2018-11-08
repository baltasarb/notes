package lockFreeSynchronization.unsafeSemaphore;

public class UnsafeSemaphore {
    private int maxPermits, permits;

    public UnsafeSemaphore(int initial, int maximum) {
        if (initial < 0 || initial > maximum)
            throw new IllegalArgumentException();
        permits = initial;
        maxPermits = maximum;
    }

    public boolean tryAcquire(int acquires) {
        if (permits < acquires)
            return false;
        permits -= acquires;
        return true;
    }

    public void release(int releases) {
        if (permits + releases < permits || permits + releases > maxPermits)
            throw new IllegalArgumentException();
        permits += releases;
    }
}
