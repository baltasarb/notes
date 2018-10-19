import org.junit.Assert;
import org.junit.Test;
import simpleThreadPoolExecutor.SimpleThreadPoolExecutor;

import java.util.ArrayList;

public class SimpleThreadPoolExecutorTests {

    @Test
    public void executeInEmptyPoolTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //work to be done by the pool
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
    }

    @Test
    public void executeInPoolWithIdleWorkerTest() throws InterruptedException {
        int maxPoolSize = 2;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //doing nothing in order to go straight to the idle worker queue
        Runnable idleWorkerWork = () -> {
        };
        //work to be done by the pool, should use the idle worker already created
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(idleWorkerWork, 1000);
            Thread.sleep(50);
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
    }

    @Test
    public void executeInPoolWithNoAvailableWorkersButWithCapacityForMore() throws InterruptedException {
        int maxPoolSize = 2;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //blocking work to force another worker to be created
        Runnable unavailableWorkerWork = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                failedResults.add("Unavailable worker interrupted.");
            }
        };
        //work to be done by the pool, should use a new worker
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(unavailableWorkerWork, 1000);
            Thread.sleep(50);
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
    }

    @Test
    public void executeInPoolAtMaxCapacityTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //block this worker so the next call to execute will have to wait
        Runnable workingWorker = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                failedResults.add("Working worker interrupted.");
            }
        };
        //work to be done by the pool, should use the worker created before
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(workingWorker, 1000);
            Thread.sleep(50);
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
    }

    @Test
    public void workExceptionDoesNotAffectThePoolTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //work to be done by the pool, the exception should be ignored by it
        Runnable work = () -> {
            int i = 1 / 0;
        };

        try {
            pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void shutdownTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //work to be done by the pool, should be finalized before shutdown
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //give time for the worker to be initiated before shutdown
        Thread.sleep(10);
        pool.shutdown();

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
    }

    @Test
    public void awaitTerminationSuccessTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //work to be done by the pool, should be finalized before shutdown
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //give time for the worker to be initiated before shutdown
        Thread.sleep(10);
        pool.shutdown();

        //give time for shutdown to execute before await
        Thread.sleep(10);
        boolean awaitSuccess = pool.awaitTermination(1000);

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
        Assert.assertTrue(awaitSuccess);
    }

    @Test
    public void awaitTerminationTimeoutTest() throws InterruptedException {
        int maxPoolSize = 1;
        int keepAlive = 1000;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //value expected after work is done by execute
        int expectedValue = 1;

        //work to be done by the pool, should be finalized before shutdown
        Runnable work = () -> valueToBeIncremented[0]++;

        boolean executeSuccess = false;

        try {
            executeSuccess = pool.execute(work, 1000);
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        Thread.sleep(10);
        boolean awaitSuccess = pool.awaitTermination(500);

        //wait for the work to be completed in main thread
        Thread.sleep(50);

        Assert.assertTrue(failedResults.isEmpty());
        Assert.assertEquals(expectedValue, valueToBeIncremented[0]);
        Assert.assertTrue(executeSuccess);
        Assert.assertFalse(awaitSuccess);
    }

    @Test
    public void stressTest() throws InterruptedException {
        int maxPoolSize = 50;
        int keepAlive = 1000;
        int numberOfExecutes = maxPoolSize * 2;

        SimpleThreadPoolExecutor pool = new SimpleThreadPoolExecutor(maxPoolSize, keepAlive);

        int[] valueToBeIncremented = {0};

        //to synchronize value incrementation
        Object monitor = new Object();

        //result repository
        ArrayList<Integer> results = new ArrayList<>();
        ArrayList<Boolean> executeSuccessResults = new ArrayList<>();

        //error repository
        ArrayList<String> failedResults = new ArrayList<>();

        //work to be done by the pool, should all be finalized
        Runnable work = () -> {
            synchronized (monitor) {
                int value = valueToBeIncremented[0]++;
                System.out.println(value);
                results.add(value);
            }
        };

        try {

            for (int i = 0; i < numberOfExecutes; i++) {
                boolean executeSuccess = pool.execute(work, 10000);
                synchronized (monitor) {
                    executeSuccessResults.add(executeSuccess);
                }
            }
        } catch (InterruptedException e) {
            failedResults.add("Exception while executing." + e.getMessage());
        }

        //wait for the work to be completed in main thread
        Thread.sleep(numberOfExecutes * 25);

        Assert.assertTrue(failedResults.isEmpty());
        for (int i = 0; i < numberOfExecutes; i++) {
            Assert.assertTrue(results.contains(i));
            results.remove(Integer.valueOf(i));

            Assert.assertTrue(executeSuccessResults.get(i));
        }
    }

    @Test
    public void runAllNTimes() throws InterruptedException {
        //due to thread sleeps to synchronize with the main thread some tests take some time
        //therefore only 5 times each
        for (int i = 0; i < 5; i++) {
            executeInEmptyPoolTest();
            executeInPoolWithIdleWorkerTest();
            executeInPoolWithNoAvailableWorkersButWithCapacityForMore();
            executeInPoolAtMaxCapacityTest();
            workExceptionDoesNotAffectThePoolTest();
            shutdownTest();
            awaitTerminationSuccessTest();
            awaitTerminationTimeoutTest();
            stressTest();
        }
    }

}
