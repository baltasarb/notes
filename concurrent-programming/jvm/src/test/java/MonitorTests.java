import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorTests {


    private class Object1 {
        boolean bool;
        public ReentrantLock monitor;
        Condition condition;

        public Object1() {
            this.bool = false;
            this.monitor = new ReentrantLock();
            this.condition = monitor.newCondition();
        }


        public void waitInfinitely() throws InterruptedException {
            monitor.lock();

            //System.out.println("bool state ->" + bool);

            Thread.sleep(1000);

            //System.out.println("bool state ->" + bool);

            condition.await(1000, TimeUnit.MILLISECONDS);

            //System.out.println("bool state ->" + bool);

            monitor.unlock();
            System.out.println("bool state ->" + bool);

        }
    }


    private class Object2 {

        ReentrantLock monitor;
        Object1 object1;

        public Object2(Object1 object1) {
            monitor = new ReentrantLock();
            this.object1 = object1;
        }

        public void setBool() {
            monitor.lock();

            object1.bool = true;

            object1.condition.signal();

            monitor.unlock();
        }

    }

    @Test
    public void t() throws InterruptedException {
        Object1 o = new Object1();
        Object2 o2 = new Object2(o);

        o.waitInfinitely();

        Thread.sleep(1111);

        o2.setBool();

        Thread.sleep(1111);
    }

}
