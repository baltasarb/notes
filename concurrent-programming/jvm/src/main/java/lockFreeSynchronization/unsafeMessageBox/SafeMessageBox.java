package lockFreeSynchronization.unsafeMessageBox;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeMessageBox <T>{

    private volatile MessageHolder messageHolder;

    public SafeMessageBox() {
        messageHolder = new MessageHolder(null, 0);
    }

    public void publish(T message, int lives) {
        messageHolder = new MessageHolder(message, lives);
    }

    public T tryConsume() {
        MessageHolder observedMessageHolder = messageHolder;

        while (true) {
            int observedLives = observedMessageHolder.lives.get();

            if (observedLives <= 0) {
                //if the message holder remains the same or has zero lives, return unsuccessfully
                if (observedMessageHolder == messageHolder) {
                    return null;
                }
                //if the message holder has changed, a new attempt can be made to consume the message before returning
                observedMessageHolder = messageHolder;
                continue;
            }

            if (observedMessageHolder.lives.compareAndSet(observedLives, observedLives - 1)) {
                return observedMessageHolder.message;
            }
        }
    }

    private class MessageHolder {
        private final T message;
        private volatile AtomicInteger lives;

        private MessageHolder(T message, int lives) {
            this.message = message;
            this.lives = new AtomicInteger(lives);
        }
    }
}
