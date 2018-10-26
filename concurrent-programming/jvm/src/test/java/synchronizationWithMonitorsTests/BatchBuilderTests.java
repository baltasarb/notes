package synchronizationWithMonitorsTests;

import org.junit.Assert;
import org.junit.Test;
import synchronizationWithMonitors.BatchBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class BatchBuilderTests {

    @Test
    public void testSimpleBatch() throws InterruptedException {

        BatchBuilder batchBuilder = new BatchBuilder(2);

        final int batchTimeout = 500;

        Object thread1Object = new Object();
        Object thread2Object = new Object();
        Object thread3Object = new Object();

        ArrayList<List<Object>> actualResults = new ArrayList<>();

        Object monitor = new Object();

        Function<Object, Runnable> taskGenerator = (value) ->
                () -> {
                    try {
                        List<Object> result = batchBuilder.await(value, batchTimeout);
                        synchronized (monitor) {
                            if (result != null)
                                actualResults.add(result);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };

        Thread thread1 = new Thread(taskGenerator.apply(thread1Object));
        Thread thread2 = new Thread(taskGenerator.apply(thread2Object));
        Thread thread3 = new Thread(taskGenerator.apply(thread3Object));

        thread1.start();
        thread2.start();
        Thread.sleep(100);
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        for (List<Object> list : actualResults) {
            Assert.assertTrue(list.contains(thread1Object));
            Assert.assertTrue(list.contains(thread2Object));
        }
        Assert.assertEquals(2, actualResults.size());
    }

    @Test
    public void nTimes() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            testSimpleBatch();
        }
    }
}
