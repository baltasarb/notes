package Utils;

public class Timer {

    private final long expirationTime;

    public Timer(int timeout) {
        expirationTime = System.currentTimeMillis() + timeout;
    }

    public long getTimeLeftToWait() {
        long timeLeftToWait = expirationTime - System.currentTimeMillis();
        return timeLeftToWait <= 0 ? 0 : timeLeftToWait;
    }

    public boolean timeExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

}