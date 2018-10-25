package synchronizationWithMonitors.simpleThreadPoolExecutor;

import utils.Timer;

import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor {

    private final int MAX_POOL_SIZE;
    private final int KEEP_ALIVE_TIME;

    private final ReentrantLock monitor;

    private int numberOfExistingWorkers;
    private LinkedList<Worker> idleWorkers;
    private LinkedList<Executor> waitingExecutors;

    private boolean poolIsShuttingDown;

    private Condition awaitTerminationCondition;

    public SimpleThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        this.MAX_POOL_SIZE = maxPoolSize;
        this.KEEP_ALIVE_TIME = keepAliveTime;
        this.monitor = new ReentrantLock();
        this.numberOfExistingWorkers = 0;
        idleWorkers = new LinkedList<>();
        waitingExecutors = new LinkedList<>();
        poolIsShuttingDown = false;
        this.awaitTerminationCondition = null;
    }

    /**
     * @param command the work to be executed
     * @param timeout the maximum waiting time of the execute method
     * @return work assignment success
     * @throws InterruptedException wait interruption
     *                              <p>
     *                              The execute will assign work if an idle worker is available or create one if there is room for it in the pool.
     *                              <p>
     *                              If neither of these cases happen then it will wait for a worker to become idle to assign work to it.
     *                              It is the job of the idle worker to get the work from the executor and notify it that the work has been delivered.
     *                              If no idle worker becomes available in due time
     *                              Waiting executes will be added to a list waitingExecutors and will be attended in a FIFO manner by the
     *                              worker that becomes available.
     *                              As they will be notified by the workers there is no guarantee that
     */
    public boolean execute(Runnable command, int timeout) throws InterruptedException {
        monitor.lock();

        if (poolIsShuttingDown) {
            monitor.unlock();
            throw new RejectedExecutionException("The simple thread pool executor is shutting down.");
        }

        //if there are workers available submit work to one of them
        if (!idleWorkers.isEmpty()) {
            submitWorkToIdleWorker(command);
            monitor.unlock();
            return true;
        }

        //if no workers are available check if there is room for one more to be created
        //and if yes create it and submit work to it
        if (numberOfExistingWorkers < MAX_POOL_SIZE) {
            submitWorkToNewWorker(command);
            monitor.unlock();
            return true;
        }

        Executor executor = new Executor(command, monitor.newCondition());
        waitingExecutors.addLast(executor);

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        //because work could not be given to a worker the execute will wait for a notification from an idle one
        //the worker will be responsible for taking the work out of the executor object and
        //as such, upon wait exit the executor only needs to see if the work is delivered
        try {
            while (true) {
                executor.awaitForWorker(timeLeftToWait);

                if (executor.workIsDelivered()) {
                    return true;
                }

                if (timer.timeExpired()) {
                    waitingExecutors.remove(executor);
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }

        } catch (InterruptedException e) {
            if (executor.workIsDelivered()) {
                Thread.currentThread().interrupt();
                return true;
            }
            waitingExecutors.remove(executor);
            throw e;
        } finally {
            monitor.unlock();
        }
    }

    //notify every idle worker that shutdown is occurring
    //the currently working workers check if a shutdown has been ordered by the pool
    //after their work is complete
    public void shutdown() {
        monitor.lock();

        if (poolIsShuttingDown) {
            monitor.unlock();
            return;
        }

        poolIsShuttingDown = true;
        idleWorkers.forEach(Worker::shutdown);

        monitor.unlock();
    }

    public boolean awaitTermination(int timeout) throws InterruptedException {
        monitor.lock();

        awaitTerminationCondition = monitor.newCondition();

        Timer timer = new Timer(timeout);
        long timeLeftToWait = timer.getTimeLeftToWait();

        try {
            while (true) {
                if (numberOfExistingWorkers == 0 && poolIsShuttingDown) {
                    return true;
                }

                awaitTerminationCondition.await(timeLeftToWait, TimeUnit.MILLISECONDS);
                if (timer.timeExpired()) {
                    return false;
                }

                timeLeftToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            if (numberOfExistingWorkers == 0 && poolIsShuttingDown) {
                Thread.currentThread().interrupt();
                return true;
            }

            throw e;
        } finally {
            awaitTerminationCondition = null;
            monitor.unlock();
        }
    }

    private void submitWorkToIdleWorker(Runnable command) {
        Worker worker = idleWorkers.removeFirst();
        worker.submitWork(command);
    }

    private void submitWorkToNewWorker(Runnable command) {
        new Worker(KEEP_ALIVE_TIME, monitor, new PoolFunctions(), command);
    }

    //this class is used to provide functionality over the pool to each of the workers
    //while limiting its accessibility
    class PoolFunctions {

        Runnable getExecutorWorkNotifyAndRemoveFromWait() {
            Executor executor = waitingExecutors.removeFirst();
            executor.signalThatWorkCanBeDelivered();
            return executor.getWork();
        }

        void incrementWorkerCount() {
            numberOfExistingWorkers++;
        }

        void decrementWorkerCount() {
            numberOfExistingWorkers--;
        }

        void addIdleWorker(Worker worker) {
            idleWorkers.addLast(worker);
        }

        void removeIdleWorker(Worker worker) {
            idleWorkers.remove(worker);
        }

        //used by the workers to know, when a shutdown is occurring, if any executors work is currently pending
        //and needs to be done before the shutdown process is over
        boolean waitingExecutorsIsEmpty() {
            return waitingExecutors.isEmpty();
        }

        void notifyAwaitTermination() {
            if (awaitTerminationCondition != null) {
                awaitTerminationCondition.signal();
            }
        }

        //used by the workers to know if they are the last one, to know when to notify awaitTermination()
        boolean poolIsReadyToShutdown() {
            return numberOfExistingWorkers == 0;
        }

        //used by each worker to check, that if upon work completion the pool is shutting down
        boolean poolIsShuttingDown() {
            return poolIsShuttingDown;
        }

    }

}
