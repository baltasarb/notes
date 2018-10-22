package synchronizationWithMonitors.simpleThreadPoolExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

class Executor {

    //work the executor (execute caller) pretends to give to the worker thread
    private final Runnable work;

    //condition required for when it is needed to wait for a worker
    //upon arrival a worker will notify this executor that the ork is delivered so the
    //executor can exit the execute method
    private final Condition condition;

    //assures this executor is ready to leave it's wait
    private boolean workIsDelivered;

    Executor(Runnable work, Condition condition) {
        this.work = work;
        this.condition = condition;
    }

    void awaitForWorker(long timeout) throws InterruptedException {
        condition.await(timeout, TimeUnit.MILLISECONDS);
    }

    void signalThatWorkCanBeDelivered() {
        workIsDelivered = true;
        condition.signal();
    }

    Runnable getWork() {
        return work;
    }

    boolean workIsDelivered() {
        return workIsDelivered;
    }

}
