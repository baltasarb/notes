package synchronizationWithMonitors.keyedChannel;

import java.util.LinkedList;

class Channel<T> {

    private LinkedList<MessageHolder<T>> producers;
    private LinkedList<MessageHolder<T>> consumers;

    Channel(){
        producers = new LinkedList<>();
        consumers = new LinkedList<>();
    }

    void AddProducer(MessageHolder<T> messageHolder){
        producers.addLast(messageHolder);
    }

    void AddConsumer(MessageHolder<T> waiter){
        consumers.addLast(waiter);
    }

    MessageHolder<T> removeFirstProducer(){
        return producers.removeFirst();
    }

    MessageHolder<T> removeFirstConsumer(){
        return consumers.removeFirst();
    }

    boolean hasConsumers(){
        return !consumers.isEmpty();
    }

    boolean hasProducers(){
        return !producers.isEmpty();
    }

    void removeExpiredConsumer(MessageHolder<T> consumer){
        consumers.remove(consumer);
    }
}
