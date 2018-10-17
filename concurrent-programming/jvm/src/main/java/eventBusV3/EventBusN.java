package eventBusV3;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.function.Consumer;

public class EventBusN {

    private final int MAX_PENDING;
    private final Object monitor;
    private HashMap<Class, TypeSubscribersN> subscribersByType;

    private boolean isShuttingDown;

    public EventBusN(int maxPending) {
        this.MAX_PENDING = maxPending;
        this.monitor = new Object();
        subscribersByType = new HashMap<>();
    }

    public <T> void subscribeEvent(Consumer<T> handler, Class consumerType) throws InterruptedException {
        TypeSubscribersN typeSubscribersN;
        synchronized (monitor) {
            // Prevents new subscribersByType to be added on a system shutdown
            if (isShuttingDown) {
                throw new InvalidStateException("The bus is shutting down and does not allow new subscriptions.");
            }
            //if the type does not exist, create it, then add the subscriber to it
            typeSubscribersN = subscribersByType.get(consumerType);
            if (typeSubscribersN == null) {
                typeSubscribersN = new TypeSubscribersN(MAX_PENDING, consumerType, this::removeSubscriptionType);
                subscribersByType.put(consumerType, typeSubscribersN);
            }
        }
        typeSubscribersN.subscribe((Consumer<Object>) handler);
    }

    public <E> void publishEvent(E message) {
        TypeSubscribersN typeSubscribers;

        synchronized (monitor) {
            if (isShuttingDown) {
                throw new IllegalStateException("The subscribersByType are shutting down and are unavailable for further publishing.");
            }
            typeSubscribers = subscribersByType.get(message.getClass());
        }

        if (typeSubscribers != null) {
            typeSubscribers.addMessageAndNotifySubscribers(message);
        }
    }

    public void shutdown() throws InterruptedException {
        synchronized (monitor) {
            // if already shutting down no need to continue
            if (isShuttingDown) {
                return;
            }
            // set the flag to stop new subscribersByType and publishers to register handlers or publish new messages
            isShuttingDown = true;

            //  awake all subscribersByType that are sleeping so they begin to handle their messages
            //  shutdown will only be possible when all of them are done
            subscribersByType.forEach((aClass, typeSubscribers) -> {
                typeSubscribers.shutdownType();
            });

            try {
                while (true) {
                    //check first in case shutdown is called with zero subscribers
                    // prevents infinite waiting
                    if (subscribersByType.isEmpty()) {
                        return;
                    }
                    // wait indefinitely for the subscribersByType to be done
                    monitor.wait();
                }
            } catch (InterruptedException e) {
                if (subscribersByType.isEmpty()) {
                    Thread.currentThread().interrupt();
                    return;
                }
                throw e;
            }
        }
    }

    private void removeSubscriptionType(Class subscribersType) {
        synchronized (monitor) {
            subscribersByType.remove(subscribersType);

            if (subscribersByType.isEmpty()) {
                monitor.notify();
            }
        }
    }

}
