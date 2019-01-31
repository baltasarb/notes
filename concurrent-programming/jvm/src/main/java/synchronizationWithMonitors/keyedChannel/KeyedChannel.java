package synchronizationWithMonitors.keyedChannel;

import utils.Timer;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedChannel<K, T> {

    //this class's monitor, creator of the conditions for each message holder to use
    private final ReentrantLock monitor;
    //contains channels, each one with two lists, a producer list (Put) and a consumer list(Take)
    private final HashMap<K, Channel<T>> channels;

    public KeyedChannel() {
        monitor = new ReentrantLock();
        channels = new HashMap<>();
    }

    public void Put(T message, K key) {
        monitor.lock();

        Channel<T> channel = channels.get(key);
        MessageHolder<T> messageHolder;

        //if channel already exists
        if (channel != null) {
            //try to deliver message if someone is waiting
            if (channel.hasConsumers()) {
                messageHolder = channel.removeFirstConsumer();
                messageHolder.setMessage(message);
                messageHolder.signal();
                monitor.unlock();
                return;
            }
        }

        else {
            //the channel does not exist yet, create it and add it to the channel container
            channel = new Channel<>();
            channels.put(key, channel);
        }

        //no one was waiting, create new producer message holder so that the next Take() call can receive it
        messageHolder = new MessageHolder<>(message);
        channel.AddProducer(messageHolder);

        monitor.unlock();
    }

    public T Take(K key, int timeout) throws InterruptedException {
        if (timeout <= 0) {
            throw new IllegalArgumentException();
        }

        monitor.lock();

        Channel<T> channel = channels.get(key);

        //if a channel exists
        if (channel != null) {
            //check for existing producers
            if (channel.hasProducers()) {
                //remove the first producer and return it's message
                //no wait necessary, the work is done
                MessageHolder<T> firstProducer = channel.removeFirstProducer();
                monitor.unlock();
                return firstProducer.getMessage();
            }
        }
        else {
            //the channel does not exist yet, create it and add it to the channel container
            channel = new Channel<>();
            channels.put(key, channel);
        }

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        //create a consumer and save the reference
        //this way it can be waited upon and be checked for messages on wait() exit
        MessageHolder<T> consumerHolder = new MessageHolder<>(monitor.newCondition());
        channel.AddConsumer(consumerHolder);

        try {
            while (true) {
                consumerHolder.await(timeLeftToWait);

                //if the message is not null then a producer already delivered
                //the message to this Take, ready to leave
                if (consumerHolder.getMessage() != null) {
                    return consumerHolder.getMessage();
                }

                //if the time expired the consumer needs to be removed so no one
                //can deliver messages to it
                if (timer.timeExpired()) {
                    channel.removeExpiredConsumer(consumerHolder);
                    return null;
                }

                //update the waiting time
                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            //if this thread was interrupted there is a chance some producer delivered a message to it
            //if that is the case this can exit with success but still allow for a caller to see
            //if it was interrupted
            T message = consumerHolder.getMessage();
            if (message != null) {
                Thread.currentThread().interrupt();
                channel.removeExpiredConsumer(consumerHolder);
                return message;
            }
            //otherwise simply throw the existing exception
            throw e;
        } finally {
            monitor.unlock();
        }
    }
}
