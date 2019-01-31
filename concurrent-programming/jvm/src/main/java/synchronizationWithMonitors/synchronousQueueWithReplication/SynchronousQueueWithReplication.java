package synchronizationWithMonitors.synchronousQueueWithReplication;

import utils.Timer;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousQueueWithReplication<T> {

    private final int MAXIMUM_NUMBER_OF_REPLICAS;
    private final ReentrantLock monitor;

    private LinkedList<Producer> producers;
    private LinkedList<Consumer> consumers;

    public SynchronousQueueWithReplication(int numberOfReplicas) {
        MAXIMUM_NUMBER_OF_REPLICAS = numberOfReplicas;
        monitor = new ReentrantLock();
        producers = new LinkedList<>();
        consumers = new LinkedList<>();
    }

    public boolean send(T message, int timeout) {
        monitor.lock();

        if (consumers.size() >= MAXIMUM_NUMBER_OF_REPLICAS) {
            sendMessageToReplicas(message);
            monitor.unlock();
            return true;
        }

        Producer producer = new Producer(monitor.newCondition());
        producers.addLast(producer);

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        try{
            while(true){
                producer.condition.await();

                if(producer.granted){
                    sendMessageToReplicas(message);
                    return true;
                }

                if(timer.timeExpired()){
                    producers.remove(producer);
                    return false;
                }


            }
        }catch (InterruptedException e){

        }

    }

    public boolean receive(int timeout, ArgumentMessageHolder<T> outMessageHolder) {

    }

    private void sendMessageToReplicas(T message){
        for (int i = 0; i < MAXIMUM_NUMBER_OF_REPLICAS; i++) {
            Consumer consumer = consumers.removeFirst();
            consumer.message = message;
            consumer.condition.signal();
        }
    }

    private class Producer {
        private final Condition condition;
        private boolean granted;

        private Producer(Condition condition) {
            this.condition = condition;
            granted = false;
        }
    }

    private class Consumer {
        private T message;
        private final Condition condition;

        private Consumer(T message, Condition condition) {
            this.message = message;
            this.condition = condition;
        }
    }
}
