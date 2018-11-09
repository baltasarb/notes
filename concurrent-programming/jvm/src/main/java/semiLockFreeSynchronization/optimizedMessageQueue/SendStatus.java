package semiLockFreeSynchronization.optimizedMessageQueue;

public interface SendStatus {

    boolean isSent();

    boolean await(int timeout) throws InterruptedException;

}
