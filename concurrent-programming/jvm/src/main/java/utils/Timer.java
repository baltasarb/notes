package utils;

public class Timer {

    private long expirationTime;

    public Timer(int timeout) {
        expirationTime = System.currentTimeMillis() + timeout;
    }

    public long getTimeLeftToWait() {
        long timeLeftToWait = expirationTime - System.currentTimeMillis();
        return timeLeftToWait < 0 ? 0 : timeLeftToWait;
    }

    // the +1 increment guarantees that if the timer initialization is done and a check
    // to timeExpired() is done immediately after the times will not be the same
    public boolean timeExpired() {
        return System.currentTimeMillis() + 1 > expirationTime;
    }

    public void reset(int timeout) {
        expirationTime = System.currentTimeMillis() + timeout;
    }

}