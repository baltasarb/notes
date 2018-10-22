package synchronizationWithMonitors.eventBus;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Manager of the subscribers of the same type
 */
class TypeSubscribers {

    private final int MAX_PENDING;

    private final Class type;
    private final Consumer<Class> removeTypeFromBus;

    //monitor shared by all the subscribers of this type
    //this allows for the type managers to run in parallel fashion
    private final ReentrantLock monitor;

    //condition shared by all the subscribers of this type, a signalAll call is sufficient to notify them all
    private final Condition typeCondition;

    //container of all the subscribers associated with this particular type
    private LinkedList<Subscriber> subscribers;

    private boolean busIsShuttingDown;

    TypeSubscribers(int maxPending, Class type, Consumer<Class> removeTypeFromBus) {
        this.MAX_PENDING = maxPending;
        this.type = type;
        this.removeTypeFromBus = removeTypeFromBus;
        this.monitor = new ReentrantLock();
        this.typeCondition = monitor.newCondition();
        this.subscribers = new LinkedList<>();
        this.busIsShuttingDown = false;
    }

    //creates a new subscriber and associates it with this type performing every action outside the bus exclusion
    //only subscribers of this type have to wait for exclusion access, the subscribers of different types
    //can subscribe to their type in parallel due to the utilization of a specific monitor per type manager
    void subscribe(Consumer<Object> handler) throws InterruptedException {
        monitor.lock();

        Subscriber subscriber = new Subscriber(handler);
        subscribers.addLast(subscriber);

        try {
            while (true) {

                if (!subscriber.hasMessages()) {
                    typeCondition.await();
                }

                if (subscriber.hasMessages()) {
                    //to handle messages outside exclusion a fixed size list has to be obtained beforehand
                    LinkedList<Object> messages = subscriber.getAndClearMessages();

                    monitor.unlock();

                    try {
                        //handled outside exclusion to allow for parallel
                        // execution between subscribers of the same type and to avoid potential deadlocks
                        subscriber.handleMessages(messages);
                    } finally {
                        //guarantee that if an exception occurs the monitor is still
                        // returned to it's normal state
                        monitor.lock();
                    }
                }

                if (busIsShuttingDown && !subscriber.hasMessages()) {
                    //if no more messages to handle are present and the bus is shutting down the subscriber is removed from
                    //this type manager
                    subscribers.remove(subscriber);

                    //if this type haves no more subscribers it can remove itself from the event bus
                    //if this is the last one it will notify the shutdown that the bus shutdown is complete
                    if (subscribers.isEmpty()) {
                        removeTypeFromBus.accept(type);
                    }
                    return;
                }
            }
        } catch (InterruptedException e) {
            subscribers.remove(subscriber);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    //used by a publisher to send messages to a type and signal all the subscribers that are waiting
    void addMessageAndNotifySubscribers(Object message) {
        monitor.lock();

        subscribers.forEach(subscriber -> {
            if (subscriber.getNumberOfMessages() <= MAX_PENDING) {
                subscriber.addMessage(message);
            }
        });

        typeCondition.signalAll();

        monitor.unlock();
    }

    //used by the shutdown of the bus in each of the type managers
    //the subscribers belonging to the manager type will be awoke of the wait and execute their respective
    //handlers immediatly to prepare for bus shutdown
    void shutdownType() {
        monitor.lock();

        busIsShuttingDown = true;

        typeCondition.signalAll();

        monitor.unlock();
    }

}
