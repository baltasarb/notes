package synchronizationWithMonitors.simpleThreadPoolExecutor;

import utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Worker {

    private final ReentrantLock poolMonitor;
    private final Condition workerCondition;
    private final SimpleThreadPoolExecutor.PoolFunctions poolFunctions;

    private Runnable work;

    private boolean workerIsShuttingDown;

    Worker(int keepAliveTime, ReentrantLock poolMonitor, SimpleThreadPoolExecutor.PoolFunctions poolFunctions, Runnable work) {
        this.poolMonitor = poolMonitor;
        this.workerCondition = poolMonitor.newCondition();
        this.poolFunctions = poolFunctions;
        this.work = work;
        this.workerIsShuttingDown = false;

        new Thread(() -> manageWork(keepAliveTime)).start();

        poolFunctions.incrementWorkerCount();
    }

    void submitWork(Runnable command) {
        this.work = command;
        //because work can only be received when a thread is idle (waiting)
        //a signal will always be required
        workerCondition.signal();
    }

    private void manageWork(int keepAliveTime) {
        poolMonitor.lock();

        Timer timer = new Timer(keepAliveTime);
        long timeToWait = timer.getTimeLeftToWait();

        try {
            while (true) {
                if (work == null) {
                    workerCondition.await(timeToWait, TimeUnit.MILLISECONDS);
                }

                if (work != null) {
                    executeWork();
                    //renew waiting time so it doesn't expire after work is done
                    timer.reset(keepAliveTime);
                }

                //if the pool is full and someone is waiting to delegate work
                //grab and execute the work and then notify the waiter
                if (!poolFunctions.waitingExecutorsIsEmpty()) {
                    work = poolFunctions.getExecutorWorkNotifyAndRemoveFromWait();
                    poolFunctions.removeIdleWorker(this);
                } else if (timer.timeExpired() || workerIsShuttingDown) {
                    terminateWorker();
                    return;
                }

                timeToWait = timer.getTimeLeftToWait();
            }
        } catch (InterruptedException e) {
            work = null;
        } finally {
            poolMonitor.unlock();
        }
    }

    //used by the pool to notify a worker that a shutdown is needed
    void shutdown() {
        //if no work is present then the thread is currently waiting
        if (work == null) {
            workerCondition.signal();
        }
        workerIsShuttingDown = true;
    }

    private void executeWork() {
        poolMonitor.unlock();

        try {
            //work done outside the exclusion to allow for parallelism and prevent possible deadlocks
            work.run();
        } catch (Exception e) {
            //exception ignored the pool should not be affected by any exception occurring outside of it
        } finally {
            poolMonitor.lock();
        }

        work = null;

        //when thread is not idle it will not be notified by the shutdown from the pool
        //so a check is needed here to know if this worker should stop working
        if (poolFunctions.poolIsShuttingDown()) {
            workerIsShuttingDown = true;
        }

        poolFunctions.addIdleWorker(this);
    }

    private void terminateWorker() {
        poolFunctions.removeIdleWorker(this);

        poolFunctions.decrementWorkerCount();

        if (poolFunctions.poolIsReadyToShutdown()) {
            poolFunctions.notifyAwaitTermination();
        }
    }

}


