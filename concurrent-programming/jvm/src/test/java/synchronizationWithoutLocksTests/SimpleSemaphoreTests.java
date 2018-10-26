package synchronizationWithoutLocksTests;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import synchronizationWithoutLocks.SimpleSemaphore;

import java.util.ArrayList;

public class SimpleSemaphoreTests {

    //will block indefinitely in case of failure
    @Test
    public void simpleAcquireTest() throws InterruptedException {
        int numberOfUnits = 10;
        int numberOfThreads = 10;

        SimpleSemaphore semaphore = new SimpleSemaphore(numberOfUnits);

        ArrayList<Boolean> results = new ArrayList<>();

        Runnable acquireTask = () -> {
            boolean result = false;
            try {
                result = semaphore.acquire(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            results.add(result);
        };

        Thread[] threads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(acquireTask);
            threads[i].start();
            threads[i].join();
        }

        for (Boolean result : results) {
            Assert.assertTrue(result);
        }
    }

    //will block indefinitely in case of failure
    @Test
    public void simpleReleaseTest() throws InterruptedException {
        int numberOfUnits = 10;
        int numberOfThreads = 11;

        SimpleSemaphore semaphore = new SimpleSemaphore(numberOfUnits);

        ArrayList<Boolean> results = new ArrayList<>();

        Object monitor = new Object();

        Runnable acquireTask = () -> {
            boolean result = false;
            try {
                result = semaphore.acquire(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (monitor){
                results.add(result);
            }
        };

        Runnable releaseTask = () -> {
            semaphore.release();
            synchronized (monitor){
                results.add(true);
            }
        };

        //initiate all acquires and the single release
        Thread[] acquireThreads = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            acquireThreads[i] = new Thread(acquireTask);
            acquireThreads[i].start();
        }

        Thread releaseThread = new Thread(releaseTask);
        releaseThread.start();

        //wait for all the threads to finish their work
        for (int i = 0; i < numberOfThreads; i++) {
            acquireThreads[i].join();
        }
        releaseThread.join();

        for (Boolean result : results) {
            Assert.assertTrue(result);
        }
    }

    @Test
    public void nTimes() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            simpleReleaseTest();
        }
    }
}
