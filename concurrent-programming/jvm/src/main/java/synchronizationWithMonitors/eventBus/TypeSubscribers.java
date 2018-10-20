package synchronizationWithMonitors.eventBus;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

class TypeSubscribers {

    private final int MAX_PENDING;
    private final Class type;
    private final Consumer<Class> removeTypeFromBus;
    private final ReentrantLock monitor;
    private final Condition typeCondition;

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
                        // execution and avoid potential deadlocks
                        subscriber.handleMessages(messages);
                    } finally {
                        //guarantee that if an exception occurs the monitor is still
                        // returned to it's normal state
                        monitor.lock();
                    }
                }

                if (busIsShuttingDown && !subscriber.hasMessages()) {
                    subscribers.remove(subscriber);
                    //if there are no more subscribers of this type then
                    //an attempt is made at shutting down the bus
                    //in case of this not being the last type left
                    //then it is only removed from the bus
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

    void shutdownType() {
        monitor.lock();

        busIsShuttingDown = true;

        typeCondition.signalAll();

        monitor.unlock();
    }

}
