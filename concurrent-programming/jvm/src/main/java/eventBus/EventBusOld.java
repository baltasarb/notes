package eventBus;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventBusOld {

    private final int MAX_PENDING;
    private final ReentrantLock monitor;
    private Condition shutdown;
    private HashMap<Class, LinkedList<SubscriberOld>> subscribers;
    private boolean isShuttingDown;
    private int numberOfSubscribers;

    public EventBusOld(int maxPending) {
        MAX_PENDING = maxPending;
        monitor = new ReentrantLock();
        subscribers = new HashMap<>();
        isShuttingDown = false;
        shutdown = monitor.newCondition();
        numberOfSubscribers = 0;
    }

    public <T> void subscribeEvent(Consumer<T> handler, Class consumerType) throws InterruptedException {
        monitor.lock();

        // TODO: should this be done ?? prevents new subscribers to be added on a system shutdown
        if (isShuttingDown) {
            monitor.unlock();
            throw new InvalidStateException("The bus is shutting down and does not allow new subscriptions.");
        }

        //informs the bus that a new subscriber has arrived, useful for correct shutdown, shutdown waits for this to reach 0
        numberOfSubscribers++;

        SubscriberOld subscriber = new SubscriberOld(monitor.newCondition());

        //if the type does not have a list, create it, then add the subscriber to it
        addNewSubscriberToSubscribersMap(consumerType, subscriber);

        try {
            while (true) {

                //  if publishing has occurred while handling last batch then it should be handled before waiting again
                //  it also helps with message dumping on shutdown
                if (subscriber.getMessages().isEmpty())
                    //  wait for events to be published
                    subscriber.getCondition().await();

                //  on notification, check if there are messages to process and handle them if there are
                if (!subscriber.getMessages().isEmpty()) {

                    //  sets flag to true so that the publisher knows if notification is needed to awake the subscriber
                    subscriber.setHandlingMessages(true);

                    //  grab all messages and empty existing list, avoids further publishing to this list
                    LinkedList<Object> messages = subscriber.getMessages();
                    subscriber.clearMessages();

                    monitor.unlock();
                    //  handling outside the lock to avoid deadlocks: handler work is unknown
                    handleMessages(messages, handler);
                }

                monitor.lock();

                // tell the publisher that notification will be required if further publication is made
                // one exception: if the there is publication while the subscriber is handling messages this will be false and set
                // to true on the next iteration of the loop directly, redundant but necessary.
                // this redundancy avoids a simple if to check list size > 0 here
                subscriber.setHandlingMessages(false);

                // if a shutdown is occurring guarantee that no more messages are left to publish before exit
                if (isShuttingDown && subscriber.getMessages().isEmpty()) {
                    attemptShutdown(subscriber, consumerType);
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

    /**
     * @param subscriberType the key to use on the hash map
     * @param subscriber     the value to add to the key's corresponding list
     */
    private void addNewSubscriberToSubscribersMap(Class subscriberType, SubscriberOld subscriber) {
        if (subscribers.get(subscriberType) == null) {
            LinkedList<SubscriberOld> subscribersByType = new LinkedList<>();
            subscribers.put(subscriberType, subscribersByType);
        }
        subscribers.get(subscriberType).add(subscriber);
    }

    private <T> void handleMessages(LinkedList<Object> messages, Consumer<T> handler) throws InterruptedException {
        try {
            while (messages.size() > 0) {
                T message = (T) messages.removeFirst();
                handler.accept(message);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Handler error : %s", e.getMessage());
            throw new InterruptedException(errorMessage);
        }
    }

    private void attemptShutdown(SubscriberOld subscriber, Class consumerType) {
        numberOfSubscribers--;
        subscribers.get(consumerType).remove(subscriber);

        //if this is the last subscriber to exit then notify the shutdown agent
        if (numberOfSubscribers == 0)
            shutdown.signal();
    }

    public <E> void publishEvent(E message) {
        monitor.lock();

        if (isShuttingDown) {
            monitor.unlock();
            throw new IllegalStateException("The subscribers are shutting down and are unavailable for further publishing.");
        }

        LinkedList<SubscriberOld> eventSubscribers = subscribers.get(message.getClass());

        if (eventSubscribers != null) {
            eventSubscribers.forEach((subscriber) -> {
                if (!subscriber.isFull(MAX_PENDING)) {
                    subscriber.addMessage(message);
                    //notify only if the subscriber is waiting
                    if (!subscriber.isHandlingMessages())
                        subscriber.getCondition().signal();
                }
            });
        }

        monitor.unlock();
    }

    public void shutdown() throws InterruptedException {
        monitor.lock();

        // set the flag to stop new subscribers and publishers to register handlers or publish new messages
        isShuttingDown = true;

        //  awake all subscribers that are sleeping so they begin to handle their messages
        //  shutdown will only be possible when all of them are done
        subscribers.forEach((type, subscriberList) -> {
            subscriberList.forEach(subscriber -> subscriber.getCondition().signal());
        });

        try {
            while (true) {
                //check first to avoid infinite loop in case shutdown is called with no subscribers
                if (numberOfSubscribers == 0) {
                    monitor.unlock();
                    return;
                }

                // wait indefinitely for the subscribers to be done
                shutdown.await();
            }
        } catch (InterruptedException e) {
            isShuttingDown = false;
            throw e;
        } finally {
            monitor.unlock();
        }
    }

}