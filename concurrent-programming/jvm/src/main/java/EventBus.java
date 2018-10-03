import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventBus {

    private final int MAX_PENDING;
    private final ReentrantLock monitor;
    private Condition shutdown;
    private HashMap<Class, LinkedList<Subscriber>> subscribers;
    private boolean isShuttingDown;
    private int numberOfSubscribers;

    public EventBus(int maxPending) {
        MAX_PENDING = maxPending;
        monitor = new ReentrantLock();
        subscribers = new HashMap<>();
        isShuttingDown = false;
        shutdown = monitor.newCondition();
        numberOfSubscribers = 0;
    }

    public <T> void SubscribeEvent(Consumer<T> handler, Class consumerType) throws InterruptedException {
        monitor.lock();

        // TODO: should this be done ?? prevents new subscribers to be added on a system shutdown
        if (isShuttingDown) {
            monitor.unlock();
            throw new InvalidStateException("The bus is shutting down and does not allow new subscriptions.");
        }

        //informs the bus that a new subscriber has arrived
        //usefull for correct shutdown, shutdown waits for this to reach 0
        numberOfSubscribers++;

        Subscriber subscriber = new Subscriber(monitor.newCondition());

        //if the type does not have a list, create it, then add the subscriber to it
        if (subscribers.get(consumerType) == null) {
            LinkedList<Subscriber> subscribersByType = new LinkedList<>();
            subscribers.put(consumerType, subscribersByType);
        }
        subscribers.get(consumerType).add(subscriber);

        try {
            while (true) {

                //  if publishing has occurred while handling last batch then it should be handled before waiting again
                //  it also helps with message dumping on shutdown
                if (subscriber.messages.isEmpty())
                    //  wait for events to be published
                    subscriber.condition.await();

                //  on notification, check if there are messages to process and handle them if there are
                if (!subscriber.messages.isEmpty()) {

                    //  grab all messages and empty existing list, avoids further publishing to this list
                    LinkedList<Object> messages = subscriber.messages;
                    subscriber.messages = new LinkedList<>();

                    //  sets flag to true so that the publisher knows if notification is needed to awake the subscriber
                    subscriber.isHandlingMessages = true;

                    //  handling outside the lock to avoid deadlocks: handler work is unknown
                    monitor.unlock();

                    handleMessages(messages, handler);
                }

                monitor.lock();

                // tell the publisher that notification will be required if further publication is made
                // one exception: if the there is publication while the subscriber is handling messages this will be false and set
                // to true on the next iteration of the loop directly, redundant but necessary.
                // this redundancy avoids a simple if to check list size > 0 here
                subscriber.isHandlingMessages = false;

                // if a shutdown is occurring guarantee that no more messages are left to publish before exit
                if (isShuttingDown && subscriber.messages.isEmpty()) {
                    numberOfSubscribers--;
                    subscribers.get(consumerType).remove(subscriber);

                    //last subscriber to exit, ready to shutdown completely, notify the shutdown agent
                    if(numberOfSubscribers == 0)
                        shutdown.signal();

                    monitor.unlock();
                    return;
                }
            }
        } catch (InterruptedException e) {
            subscribers.get(consumerType).remove(subscriber);
            throw e;
        } finally {
            monitor.unlock();
        }
    }


    public <E> void PublishEvent(E message) {
        monitor.lock();

        if (isShuttingDown) {
            monitor.unlock();
            throw new InvalidStateException("The subscribers are shutting down and are unavailable for further publishing.");
        }

        LinkedList<Subscriber> eventSubscribers = subscribers.get(message.getClass());

        if (eventSubscribers != null) {
            eventSubscribers.forEach((subscriber) -> {
                if (!subscriber.isFull()) {
                    subscriber.addMessage(message);
                    if (!subscriber.isHandlingMessages)
                        subscriber.condition.signal();
                }
            });
        }

        monitor.unlock();
    }

    public void Shutdown() throws InterruptedException {
        monitor.lock();

        // set the flag to stop new subscribers and publishers to register handlers or publish new messages
        isShuttingDown = true;

        //  awake all subscribers that are sleeping so they begin to handle their messages
        //  shutdown will only be possible when all of them are done
        shutdown.signalAll();

        try {
            while (true) {
                //check first to avoid infinite loop in case shutdown is called with no subscribers
                if(numberOfSubscribers == 0){
                    monitor.unlock();
                    return;
                }

                // wait indefinitely for the subscribers to be done
                shutdown.await(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            isShuttingDown = false;
            throw e;
        }finally {
            monitor.unlock();
        }
    }

    private <T> void handleMessages (LinkedList<Object> messages, Consumer<T> handler) throws InterruptedException {
        while (messages.size() > 0) {
            T message = (T) messages.removeFirst();
            try {
                handler.accept(message);
            } catch (Exception e) {
                String errorMessage = String.format("Handler error : %s", e.getMessage());
                throw new InterruptedException(errorMessage);
            }
        }
    }

    private class Subscriber {
        private final Condition condition;
        private LinkedList<Object> messages;
        private boolean isHandlingMessages;

        Subscriber(Condition condition) {
            this.condition = condition;
            messages = new LinkedList<>();
            isHandlingMessages = false;
        }

        void addMessage(Object message) {
            messages.addLast(message);
        }

        boolean isFull() {
            return messages.size() >= MAX_PENDING;
        }

    }
}