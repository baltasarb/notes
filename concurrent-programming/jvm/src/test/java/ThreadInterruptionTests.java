import org.junit.Ignore;
import org.junit.Test;

public class ThreadInterruptionTests {

    @Test @Ignore
    public void t() throws InterruptedException {

        Thread t1 = new Thread(() -> {
            Thread.currentThread().interrupt();
            assert Thread.currentThread().isInterrupted();
        });

        t1.start();
        Thread.sleep(1000);
    }

}
