package newEventBus;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

class TypeSubscribers {

    private final ReentrantLock busMonitor;
    private final Object monitor;
    private LinkedList<Subscriber> subscribers;
    private final int MAX_PENDING;

    TypeSubscribers(ReentrantLock busMonitor, int maxPendingMessages) {
        this.busMonitor = busMonitor;
        monitor = new Object();
        subscribers = new LinkedList<>();
        this.MAX_PENDING = maxPendingMessages;
    }

    void subscriberAwaitIfMessagesDoNotExist(Subscriber subscriber) throws InterruptedException {
        if(!subscriber.hasMessages()){
            try{
                synchronized (monitor) {
                    // allows new callers to subscribe to the event bus
                    // it's locked again when this method is done on the eventBus side
                    busMonitor.unlock();
                    monitor.wait();
                }
            }finally {
                busMonitor.lock();
            }
        }
    }

    // unlock here guarantees this is first to enter after exit
    void notifySubscribers() {
        synchronized (monitor) {
            busMonitor.unlock();
            // allows new callers to subscribe to the event bus
            // it's locked again when this method is done on the eventBus side
            //busMonitor.unlock();
            monitor.notifyAll();
        }
        busMonitor.lock();
    }

    void addSubscriber(Subscriber subscriber) {
        synchronized (monitor){
            subscribers.addLast(subscriber);
        }
    }

    void removeSubscriber(Subscriber subscriber) {
        synchronized (monitor){
            subscribers.remove(subscriber);
        }
    }

    private void addMessageToSubscribers(Object message) {
        synchronized (monitor) {
            busMonitor.unlock();
            subscribers.forEach(subscriber -> {
                if (subscriber.getNumberOfExistingMessages() < MAX_PENDING) {
                    subscriber.addMessage(message);
                }
            });
        }
        busMonitor.lock();
    }

    void handleSubscriberMessagesIfTheyExist(Subscriber subscriber) throws InterruptedException {
        if (subscriber.hasMessages()) {
            LinkedList<Object> messages;
            synchronized (monitor) {
                busMonitor.unlock();
                //  grab all messages and empty existing list, avoids further publishing to this list
                messages = subscriber.getAndClearMessages();
            }
            //  handling outside the lock to avoid deadlocks: handler work is unknown
            try {
                subscriber.handleMessages(messages);
            } finally {
                busMonitor.lock();
            }
        }
    }

    void addMessageAndNotifySubscribers(Object message) {
        addMessageToSubscribers(message);
        notifySubscribers();
    }
}
