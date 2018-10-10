package messageQueue;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import Utils.Timer;

public class MessageQueue<T> {

    private ReentrantLock monitor;
    private LinkedList<PendingMessage<T>> pendingSenders;
    private LinkedList<PendingMessage<T>> pendingReceivers;

    public MessageQueue() {
        this.monitor = new ReentrantLock();
        this.pendingSenders = new LinkedList<>();
        this.pendingReceivers = new LinkedList<>();
    }

    public SendStatus send(T sentMsg) {
        monitor.lock();

        //  Send handles everything on arrival because someone is already waiting.
        // Status cannot be canceled or awaited on, MessageSentStatus is returned.
        if (!pendingReceivers.isEmpty()) {
            PendingMessage<T> pendingMessage = pendingReceivers.removeFirst();
            pendingMessage.sendAndSignal(sentMsg);
            monitor.unlock();
            return pendingMessage.messageSentStatus();
        }

        //As someone will need to arrive to handle this message a MessagePendingStatus will be associated with it.
        PendingMessage<T> pendingMessage = new PendingMessage<>(monitor.newCondition(), sentMsg, monitor, this::cancelMessage);
        pendingSenders.addLast(pendingMessage);

        monitor.unlock();
        return pendingMessage.getStatus();
    }

    public Optional<T> receive(int timeout) throws InterruptedException {
        if (timeout <= 0) {
            return Optional.empty();
        }

        monitor.lock();

        Timer timer = new Timer(timeout);

        if (!pendingSenders.isEmpty()) {
            PendingMessage<T> pendingMessage = pendingSenders.removeFirst();
            pendingMessage.setSentAndSignalStatus();
            monitor.unlock();
            return Optional.of(pendingMessage.getMessage());
        }

        if (timer.timeExpired()) {
            monitor.unlock();
            return Optional.empty();
        }

        long timeLeft = timer.getTimeLeftToWait();

        PendingMessage<T> pendingMessage = new PendingMessage<>(monitor.newCondition());
        pendingReceivers.addLast(pendingMessage);

        try {
            while (true) {
                pendingMessage.getCondition().await(timeLeft, TimeUnit.MILLISECONDS);

                T currentPendingMessage = pendingMessage.getMessage();
                if (currentPendingMessage != null) {
                    return Optional.of(currentPendingMessage);
                }

                if (timer.timeExpired()) {
                    pendingReceivers.remove(pendingMessage);
                    return Optional.empty();
                }
            }
        } catch (InterruptedException e) {
            T currentPendingMessage = pendingMessage.getMessage();
            if (currentPendingMessage != null) {
                Thread.currentThread().interrupt();
                return Optional.of(currentPendingMessage);
            }
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    // !!! needs to be called inside a monitor lock
    private Supplier<Boolean> cancelMessage(PendingMessage<T> pendingMessage) {
        return () -> pendingSenders.remove(pendingMessage);
    }
}
