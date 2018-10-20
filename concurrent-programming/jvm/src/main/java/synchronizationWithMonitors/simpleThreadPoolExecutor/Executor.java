package synchronizationWithMonitors.simpleThreadPoolExecutor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

class Executor {

    private final Runnable work;
    private final Condition condition;

    //assures this executor should assign work next
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
