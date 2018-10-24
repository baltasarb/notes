package synchronizationWithoutLocksTests;

import org.junit.Assert;
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
            semaphore.acquire();
            results.add(true);
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
        int numberOfUnits = 2;
        int numberOfThreads = 3;

        SimpleSemaphore semaphore = new SimpleSemaphore(numberOfUnits);

        ArrayList<Boolean> results = new ArrayList<>();

        Runnable acquireTask = () -> {
            semaphore.acquire();
            results.add(true);
        };

        Runnable releaseTask = () -> {
            semaphore.release();
            results.add(true);
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
        for (int i = 0; i < 100; i++) {
            simpleReleaseTest();
        }
    }
}
