import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorTests {

    private class OuterMonitor {
        private ReentrantLock outerMonitor;
        private InnerMonitor innerMonitor;

        OuterMonitor(InnerMonitor innerMonitor) {
            this.outerMonitor = new ReentrantLock();
            this.innerMonitor = innerMonitor;
        }

        void enterMonitor() throws InterruptedException {
            outerMonitor.lock();
            System.out.println("In outter, before inner");
            innerMonitor.enterInnerMonitor();
            System.out.println("In outter, after inner");
            outerMonitor.unlock();
        }
        void notifyOutterMonitor(){
            outerMonitor.lock();
            outerMonitor.notify();
            outerMonitor.unlock();
        }
    }

    private class InnerMonitor{
        private final Object innerMonitor;

        InnerMonitor(){
            this.innerMonitor = new Object();
        }

        void enterInnerMonitor() throws InterruptedException {
            synchronized (innerMonitor){

                try{
                    System.out.println("before waiting in inner");
                    innerMonitor.wait();
                    System.out.println("after waiting in inner");
                }catch (InterruptedException e){
                    throw e;
                }

            }
        }

        void notifyInnerMonitor() {
            synchronized (innerMonitor){
                innerMonitor.notify();
            }
        }
    }

    @Test
    public void innerMonitorHoldsOuterMonitorTest() throws InterruptedException {
        InnerMonitor innerMonitor = new InnerMonitor();
        OuterMonitor outerMonitor = new OuterMonitor(innerMonitor);

        outerMonitor.enterMonitor();

        Thread.sleep(100);

        //this should not happen as outer is held by inner
        outerMonitor.enterMonitor();
    }
}
