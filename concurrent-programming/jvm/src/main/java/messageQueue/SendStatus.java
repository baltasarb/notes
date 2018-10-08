package messageQueue;

public interface SendStatus {

    boolean isSent();

    boolean tryCancel();

    boolean await(int timeout) throws InterruptedException;

}
