package synchronizationWithMonitors.simpleThreadPoolExecutor;

import synchronizationWithMonitors.Utils.Timer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Worker {

    private final int INCREMENT_WORKER_COUNT = 1;
    private final int DECREMENT_WORKER_COUNT = -1;

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

        new Thread(() -> executeWork(keepAliveTime)).start();

        poolFunctions.updateWorkerCount(INCREMENT_WORKER_COUNT);
    }

    void submitWork(Runnable command) {
        poolMonitor.lock();

        //if in shutdown should not take further work unless work had been committed to the pool before
        if (workerIsShuttingDown && !poolFunctions.executorsAreWaiting()) {
            return;
        }

        this.work = command;

        workerCondition.signal();

        poolMonitor.unlock();
    }

    private void executeWork(int keepAliveTime) {
        poolMonitor.lock();

        Timer timer = new Timer(keepAliveTime);
        long timeToWait = timer.getTimeLeftToWait();

        try {
            while (true) {
                if (work == null) {
                    workerCondition.await(timeToWait, TimeUnit.MILLISECONDS);
                }

                if (work != null) {
                    poolMonitor.unlock();

                    try {
                        //work done outside the exclusion to allow for parallelism, prevent possible deadlocks
                        work.run();
                    } catch (Exception e) {
                        //exception ignored
                        //the pool should not be affected by any exception occurring outside of it
                    } finally {
                        poolMonitor.lock();
                    }

                    work = null;

                    //renew waiting time so it doesnt expire after work is done
                    timer.reset(keepAliveTime);

                    //when thread is not idle it will not be notified by the shutdown from the pool
                    //so a check is needed here to know if this worker should stop working
                    //in this case the worker wont go to idle list and will get the work
                    //from the executor himself
                    if (poolFunctions.poolIsShuttingDown()) {
                        workerIsShuttingDown = true;
                    }
                    //submitWork -> shutdown (before executeWork continues)
                    else {
                        poolFunctions.addIdleWorker(this);
                    }
                }

                boolean executorsAreWaiting = poolFunctions.executorsAreWaiting();

                if (executorsAreWaiting) {
                    work = poolFunctions.getExecutorWorkNotifyAndRemoveFromWait();
                    //state of executorsAreWaiting() will change here, but because it has been
                    //kept in a local variable that will not alter the functionality yet.
                    //Only on the next iteration
                    poolFunctions.removeIdleWorker(this);
                }

                if (workerIsShuttingDown && !executorsAreWaiting) {
                    //if present remove from idle list
                    poolFunctions.removeIdleWorker(this);

                    poolFunctions.updateWorkerCount(DECREMENT_WORKER_COUNT);
                    if (poolFunctions.poolIsReadyToShutdown()) {
                        poolFunctions.notifyAwaitTermination();
                    }
                    return;
                }

                if (timer.timeExpired()) {
                    poolFunctions.removeIdleWorker(this);
                    poolFunctions.updateWorkerCount(DECREMENT_WORKER_COUNT);
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

    void shutdown() {
        //if no work is present then the thread is currently waiting
        if (work == null) {
            workerCondition.signal();
        }
        workerIsShuttingDown = true;
    }

}
