import sun.plugin.dom.exception.InvalidStateException;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventBus {

    private final int MAX_PENDING;
    private final ReentrantLock monitor;
    private LinkedList<Subscriber> subscribers;
    private boolean shutdown;

    public EventBus(int maxPending) {
        MAX_PENDING = maxPending;
        monitor = new ReentrantLock();
        subscribers = new LinkedList<>();
        shutdown = false;
    }

    public <T> void SubscribeEvent(Consumer<T> handler) throws InterruptedException {
        monitor.lock();

        Subscriber subscriber = new Subscriber(monitor.newCondition(), handler.getClass());
        subscribers.add(subscriber);

        try{

            while(true){
                subscriber.condition.await();
                //even if spurious awakening, should the list be checked for handling?
                if(subscriber.messages.size() > 0){
                    //How to know type of handler is same as of message?
                    for (int i = 0; i < subscriber.messages.size(); i++)
                    subscriber.messages.forEach(message -> {
                        try{
                            handler.accept((T)message); //different types how to solve?
                        }catch (ClassCastException e){
                            //if its not the same type continue interating
                        }
                    });
                }
            }

        }catch (InterruptedException e){
            subscribers.remove(subscriber);
            throw e;
        }finally {
            monitor.unlock();
        }
    }


    public <E> void PublishEvent(E message) {
        monitor.lock();

        if (shutdown) {
            monitor.unlock();
            throw new InvalidStateException("The subscriber is shutting down and unavailable for further publishing.");
        }

        subscribers.forEach((subscriber) -> {
            if (!subscriber.isFull()) {

                subscriber.type = message.getClass();

                subscriber.addMessage(message);
                if (!subscriber.isHandlingMessage)
                    subscriber.condition.signal();
            }
        });

        monitor.unlock();
    }


    public void Shutdown() {
        monitor.lock();
        shutdown = true;
        monitor.unlock();
    }

    private class Subscriber {
        private final Condition condition;
        private LinkedList<Object> messages;
        private boolean isHandlingMessage;
        public Class type;

        Subscriber(Condition condition, Class type) {
            this.condition = condition;
            messages = new LinkedList<>();
            isHandlingMessage = false;
            this.type = type;
        }

        void addMessage(Object message) {
            messages.add(message);
        }

        boolean isFull() {
            return messages.size() >= MAX_PENDING;
        }
    }
}