package newEventBus;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventBus {

    private final int MAX_PENDING;

    private final ReentrantLock monitor;
    private Condition shutdown;

    private HashMap<Class, TypeSubscribers> subscribersByType;

    private boolean isShuttingDown;

    //used to shutdown correctly, shutdown waits for this to reach 0
    private int numberOfSubscribers;

    public EventBus(int maxPending) {
        MAX_PENDING = maxPending;
        monitor = new ReentrantLock();
        subscribersByType = new HashMap<>();
        isShuttingDown = false;
        shutdown = monitor.newCondition();
        numberOfSubscribers = 0;
    }

    public <T> void subscribeEvent(Consumer<T> handler, Class consumerType) throws InterruptedException {
        monitor.lock();

        // Prevents new subscribersByType to be added on a system shutdown
        if (isShuttingDown) {
            monitor.unlock();
            throw new InvalidStateException("The bus is shutting down and does not allow new subscriptions.");
        }

        //informs the bus that a new subscriber has arrived, useful for correct shutdown, shutdown waits for this to reach 0
        numberOfSubscribers++;

        //if the type does not exist, create it, then add the subscriber to it
        TypeSubscribers typeSubscribers = getOrCreateTypeSubscribers(consumerType);
        Subscriber subscriber = new Subscriber((Consumer<Object>) handler);
        typeSubscribers.addSubscriber(subscriber);

        try {
            while (true) {
                //  wait for events to be published but releasing the bus monitor in the process
                // reacquire after
                typeSubscribers.subscriberAwaitIfMessagesDoNotExist(subscriber);

                //  on notification, check if there are messages to process and handle them if there are
                typeSubscribers.handleSubscriberMessagesIfTheyExist(subscriber);

                // if a shutdown is occurring guarantee that no more messages are left to publish before exit
                if (isShuttingDown && !subscriber.hasMessages()) {
                    attemptShutdown(typeSubscribers, subscriber);
                    return;
                }
            }
        } catch (InterruptedException e) {
            typeSubscribers.removeSubscriber(subscriber);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    public <E> void publishEvent(E message) {
        monitor.lock();

        if (isShuttingDown) {
            monitor.unlock();
            throw new IllegalStateException("The subscribersByType are shutting down and are unavailable for further publishing.");
        }

        TypeSubscribers typeSubscribers = subscribersByType.get(message.getClass());

        if (typeSubscribers != null) {
            typeSubscribers.addMessageAndNotifySubscribers(message);
        }

        monitor.unlock();
    }

    public void shutdown() throws InterruptedException {
        monitor.lock();

        // set the flag to stop new subscribersByType and publishers to register handlers or publish new messages
        isShuttingDown = true;

        //  awake all subscribersByType that are sleeping so they begin to handle their messages
        //  shutdown will only be possible when all of them are done
        subscribersByType.forEach((aClass, typeSubscribers) -> {
            typeSubscribers.notifySubscribers();
            // because the monitor is unlocked inside notify subscribers
            // it must be locked again before entering this monitor
            //monitor.lock();
        });

        try {
            while (true) {
                //check first to in case shutdown is called with zero subscribers
                // prevents infinite waiting
                if (numberOfSubscribers == 0) {
                    return;
                }
                // wait indefinitely for the subscribersByType to be done
                shutdown.await();
            }
        } catch (InterruptedException e) {
            if (numberOfSubscribers == 0) {
                Thread.currentThread().interrupt();
                return;
            }
            isShuttingDown = false;
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    private TypeSubscribers getOrCreateTypeSubscribers(Class consumerType) {
        TypeSubscribers typeSubscribers = subscribersByType.get(consumerType);

        if (typeSubscribers == null) {
            typeSubscribers = new TypeSubscribers(monitor, MAX_PENDING);
            subscribersByType.put(consumerType, typeSubscribers);
        }
        return typeSubscribers;
    }

    private void attemptShutdown(TypeSubscribers typeSubscribers, Subscriber subscriber) {
        numberOfSubscribers--;
        typeSubscribers.removeSubscriber(subscriber);

        //if this is the last subscriber to exit then notify the shutdown agent
        if (numberOfSubscribers == 0)
            shutdown.signal();
    }


}
