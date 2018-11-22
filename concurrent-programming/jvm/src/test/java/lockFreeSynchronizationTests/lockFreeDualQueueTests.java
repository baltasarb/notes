package lockFreeSynchronizationTests;

import lockFreeSynchronization.lockFreeDualQueue.LockFreeDualQueue;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class lockFreeDualQueueTests {

    @Test
    public void testLockFreeDualQueue() throws Exception {
        final int CONSUMER_THREADS = 2;
        final int PRODUCER_THREADS = 1;
        final int MAX_PRODUCE_INTERVAL = 25;
        final int MAX_CONSUME_TIME = 25;
        final int FAILURE_PERCENT = 5;
        final int JOIN_TIMEOUT = 100;
        final int RUN_TIME = 10 * 1000;
        final int POLL_INTERVAL = 20;

        Thread[] consumers = new Thread[CONSUMER_THREADS];
        Thread[] producers = new Thread[PRODUCER_THREADS];
        final LockFreeDualQueue<String> dqueue = new LockFreeDualQueue<>();
        final int[] productions = new int[PRODUCER_THREADS];
        final int[] consumptions = new int[CONSUMER_THREADS];
        final int[] failuresInjected = new int[PRODUCER_THREADS];
        final int[] failuresDetected = new int[CONSUMER_THREADS];

        // create and start the consumer threads.
        for (int i = 0; i < CONSUMER_THREADS; i++) {
            final int tid = i;
            consumers[i] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                int count = 0;

                System.out.printf("-->c#%02d starts...%n", tid);
                do {
                    try {
                        String data = dqueue.dequeue();
                        if (!data.equals("hello")) {
                            failuresDetected[tid]++;
                            System.out.printf("[f#%d]", tid);
                        }

                        if (++count % 100 == 0)
                            System.out.printf("[c#%02d]", tid);

                        // simulate the time needed to process the data.
                        Thread.sleep(rnd.nextInt(MAX_CONSUME_TIME + 1));

                    } catch (InterruptedException ie) {
                        //do {} while (tid == 0);
                        break;
                    }
                } while (true);

                // display consumer thread's results.
                System.out.printf("%n<--c#%02d exits, consumed: %d, failures: %d",
                        tid, count, failuresDetected[tid]);
                consumptions[tid] = count;
            });
            consumers[i].setDaemon(true);
            consumers[i].start();
        }

        // create and start the producer threads.
        for (int i = 0; i < PRODUCER_THREADS; i++) {
            final int tid = i;
            producers[i] = new Thread(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                int count = 0;

                System.out.printf("-->p#%02d starts...%n", tid);
                do {
                    String data;

                    if (rnd.nextInt(100) >= FAILURE_PERCENT) {
                        data = "hello";
                    } else {
                        data = "HELLO";
                        failuresInjected[tid]++;
                    }

                    // enqueue a data item
                    dqueue.enqueue(data);

                    // Increment request count and periodically display the "alive" menssage.
                    if (++count % 100 == 0)
                        System.out.printf("[p#%02d]", tid);

                    // production interval.
                    try {
                        Thread.sleep(rnd.nextInt(MAX_PRODUCE_INTERVAL));
                    } catch (InterruptedException ie) {
                        //do {} while (tid == 0);
                        break;
                    }
                } while (true);
                System.out.printf("%n<--p#%02d exits, produced: %d, failures: %d",
                        tid, count, failuresInjected[tid]);
                productions[tid] = count;
            });
            producers[i].setDaemon(true);
            producers[i].start();
        }

        // run the test RUN_TIME milliseconds
        Thread.sleep(RUN_TIME);

        // interrupt all producer threads and wait for until each one finished.
        int stillRunning = 0;
        for (int i = 0; i < PRODUCER_THREADS; i++) {
            producers[i].interrupt();
            producers[i].join(JOIN_TIMEOUT);
            if (producers[i].isAlive())
                stillRunning++;
        }

        // wait until the queue is empty
        while (!dqueue.isEmpty())
            Thread.sleep(POLL_INTERVAL);

        // Interrupt each consumer thread and wait for a while until each one finished.
        for (int i = 0; i < CONSUMER_THREADS; i++) {
            consumers[i].interrupt();
            consumers[i].join(JOIN_TIMEOUT);
            if (consumers[i].isAlive())
                stillRunning++;
        }

        // If any thread failed to finish, something is wrong.
        if (stillRunning > 0) {
            String error = String.format("%n<--*** failure: %d thread(s) did answer to interrupt%n", stillRunning);
            throw new Exception(error);
        }

        // Compute and display the results.

        long sumProductions = 0, sumFailuresInjected = 0;
        for (int i = 0; i < PRODUCER_THREADS; i++) {
            sumProductions += productions[i];
            sumFailuresInjected += failuresInjected[i];
        }
        long sumConsumptions = 0, sumFailuresDetected = 0;
        for (int i = 0; i < CONSUMER_THREADS; i++) {
            sumConsumptions += consumptions[i];
            sumFailuresDetected += failuresDetected[i];
        }
        System.out.printf("%n<-- successful: %d/%d, failed: %d/%d%n",
                sumProductions, sumConsumptions, sumFailuresInjected, sumFailuresDetected);

        Assert.assertTrue(sumProductions == sumConsumptions && sumFailuresInjected == sumFailuresDetected);
    }
}
