package synchronizationWithMonitors;

import utils.Timer;

import java.util.LinkedList;
import java.util.List;

public class BatchBuilder {

    private final Object monitor;
    private final int MAXIMUM_CAPACITY;

    private List<Object> batch;

    public BatchBuilder(int batchSize) {
        this.monitor = new Object();
        this.MAXIMUM_CAPACITY = batchSize;
        batch = new LinkedList<>();
    }

    public List<Object> await(Object value, int timeout) throws InterruptedException {
        synchronized (monitor){

            if(batch.size() == MAXIMUM_CAPACITY - 1){
                batch.add(value);
                List<Object> batchToReturn = batch;
                batch = new LinkedList<>();
                monitor.notifyAll();
                return batchToReturn;
            }

            if(timeout <= 0){
                return null;
            }

            Timer timer = new Timer(timeout);
            long timeLeftToWait = timer.getTimeLeftToWait();

            batch.add(value);

            List<Object> currentBatch = batch;

            try{
                while (true){
                    monitor.wait(timeLeftToWait);

                    if(currentBatch.size() == MAXIMUM_CAPACITY){
                        return currentBatch;
                    }

                    if(timer.timeExpired()){
                        batch.remove(value);
                        return null;
                    }

                    timeLeftToWait = timer.getTimeLeftToWait();
                }
            }catch (InterruptedException e){
                if(currentBatch.size() == MAXIMUM_CAPACITY){
                    Thread.currentThread().interrupt();
                    return currentBatch;
                }
                batch.remove(value);
                throw e;
            }
        }
    }

}
