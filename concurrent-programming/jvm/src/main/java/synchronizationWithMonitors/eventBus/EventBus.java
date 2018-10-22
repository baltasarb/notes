package synchronizationWithMonitors.eventBus;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.function.Consumer;

public class EventBus {

    private final int MAX_PENDING;
    private final Object monitor;

    //grouping of the subscribers by type
    private HashMap<Class, TypeSubscribers> subscribersByType;

    private boolean isShuttingDown;

    public EventBus(int maxPending) {
        this.MAX_PENDING = maxPending;
        this.monitor = new Object();
        subscribersByType = new HashMap<>();
    }

    public <T> void subscribeEvent(Consumer<T> handler, Class consumerType) throws InterruptedException {
        TypeSubscribers typeSubscribers;
        synchronized (monitor) {
            // Prevents new subscribersByType to be added on a system shutdown
            if (isShuttingDown) {
                throw new InvalidStateException("The bus is shutting down and does not allow new subscriptions.");
            }
            //if the type does not exist, create it
            typeSubscribers = subscribersByType.get(consumerType);
            if (typeSubscribers == null) {
                typeSubscribers = new TypeSubscribers(MAX_PENDING, consumerType, this::removeSubscriptionType);
                subscribersByType.put(consumerType, typeSubscribers);
            }
        }
        //subscription to the event of this type
        typeSubscribers.subscribe((Consumer<Object>) handler);
    }

    public <E> void publishEvent(E message) {
        TypeSubscribers typeSubscribers;

        synchronized (monitor) {
            if (isShuttingDown) {
                throw new IllegalStateException("The subscribersByType are shutting down and are unavailable for further publishing.");
            }
            typeSubscribers = subscribersByType.get(message.getClass());
        }

        //if the type subscribers are present, publish the message to all of them and notify them all
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
            subscribersByType.forEach((type, typeSubscribers) -> {
                typeSubscribers.shutdownType();
            });

            try {
                while (true) {
                    //check first in case shutdown is called with zero subscribers
                    // prevents infinite waiting
                    if (subscribersByType.isEmpty()) {
                        return;
                    }
                    // wait indefinitely for the subscribersByType shutdown to be done
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

    //method used by the type to remove himself from the subscribersByType container
    //it also notifies it is the last one to be removed, which happens in case of a shutdown
    private void removeSubscriptionType(Class subscribersType) {
        synchronized (monitor) {
            subscribersByType.remove(subscribersType);

            if (subscribersByType.isEmpty()) {
                monitor.notify();
            }
        }
    }

}
