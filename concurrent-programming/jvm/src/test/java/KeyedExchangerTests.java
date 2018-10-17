import keyedExchanger.KeyedExchanger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class KeyedExchangerTests {

    @Test
    public void exchangeBetweenTwoThreadsTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        KeyedExchanger<Integer> exchanger = new KeyedExchanger<>();

        ArrayList<Optional<Integer>> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        // returns a runnable when called with the arguments of message to send and expected to receive
        BiFunction<Integer, Integer, Runnable> exchange = (pairKey, messageToSend) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(pairKey, messageToSend, 5000);
                        synchronized (resultSynchronization) {
                            results.add(result);
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultSynchronization) {
                            failedResults.add("failure in : " + messageToSend);
                        }
                    }
                };

        //submit the work to the pool
        completion.submit(() -> {
            exchange.apply(0, 1).run();
            return null;
        });
        completion.submit(() -> {
            exchange.apply(0, 0).run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        //iterate from 0 to numberOfWorkers checking if that result is present in the results container
        for (int i = 0; i < numberOfWorkers; i++) {
            Optional<Integer> currentValue = Optional.of(i);
            Assert.assertTrue(results.contains(currentValue));
            results.remove(currentValue);
        }

        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void timeoutInExchangeBetweenTwoThreadsTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        KeyedExchanger<Integer> exchanger = new KeyedExchanger<>();

        ArrayList<Optional<Integer>> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        // returns a runnable when called with the arguments of message to send and expected to receive
        BiFunction<Integer, Integer, Runnable> exchange = (pairKey, messageToSend) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(pairKey, messageToSend, 0);
                        synchronized (resultSynchronization) {
                            results.add(result);
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultSynchronization) {
                            failedResults.add("failure in : " + messageToSend);
                        }
                    }
                };

        //submit the work to the pool
        completion.submit(() -> {
            exchange.apply(0, 1).run();
            return null;
        });
        completion.submit(() -> {
            exchange.apply(0, 0).run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        //iterate from 0 to numberOfWorkers checking if that result is present in the results container
        for (Optional<Integer> result : results) {
            Assert.assertEquals(result, Optional.empty());
        }

        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void twoKeysMultipleExchangesEach() throws InterruptedException {
        int numberOfWorkers = 1000;

        KeyedExchanger<Integer> exchanger = new KeyedExchanger<>();

        // used to save the results of each thread in a safe manner
        Object resultAdditionMonitor = new Object();

        //to hold the results of each exchange
        ArrayList<Optional<Integer>> results = new ArrayList<>(numberOfWorkers);

        //hold the values where an exception occurred if any exist
        ArrayList<String> failedResults = new ArrayList<>();

        //pool used to synchronize the results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        //returns a Runnable with the task each exchanger will make
        BiFunction<Integer, Integer, Runnable> taskGenerator = (pairKey, messageId) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(pairKey, messageId, 5000);
                        synchronized (resultAdditionMonitor) {
                            results.add(result);
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultAdditionMonitor) {
                            failedResults.add("result : " + messageId + " failed.");
                        }
                    }
                };

        //submits the work to the thread pool
        for (int i = 0; i < numberOfWorkers; i++) {
            //each pair key is given by the current index
            // each message is the current index on the first worker and current index + 1 on the second worker
            //this guarantees that each value is unique on the results arrayList
            int pairKey = i % 2;
            int message = i;
            completion.submit(() -> {
                taskGenerator.apply(pairKey, message).run();
                return null;
            });
        }

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next task has completed.
        }

        executor.shutdown();

        //iterate from 0 to numberOfWorkers checking if that result is present in the results container
        for (int i = 0; i < numberOfWorkers; i++) {
            Optional<Integer> currentValue = Optional.of(i);
            Assert.assertTrue(results.contains(currentValue));
            results.remove(currentValue);
        }
        //assert that no exceptions happened
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void multipleKeysOneExchangeEach() throws InterruptedException {
        int numberOfWorkers = 1000;

        KeyedExchanger<Integer> exchanger = new KeyedExchanger<>();

        // used to save the results of each thread in a safe manner
        Object resultAdditionMonitor = new Object();

        //to hold the results of each exchange
        ArrayList<Optional<Integer>> results = new ArrayList<>(numberOfWorkers);

        //hold the values where an exception occurred if any exist
        ArrayList<String> failedResults = new ArrayList<>();

        //pool used to synchronize the results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        //returns a Runnable with the task each exchanger will make
        BiFunction<Integer, Integer, Runnable> taskGenerator = (pairKey, messageId) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(pairKey, messageId, 5000);
                        synchronized (resultAdditionMonitor) {
                            results.add(result);
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultAdditionMonitor) {
                            failedResults.add("result : " + messageId + " failed.");
                        }
                    }
                };

        //submits the work to the thread pool
        for (int i = 0; i < numberOfWorkers - 1; i += 2) {
            //each pair key is given by the current index
            // each message is the current index on the first worker and current index + 1 on the second worker
            //this guarantees that each value is unique on the results arrayList
            int j = i;
            completion.submit(() -> {
                taskGenerator.apply(j, j).run();
                return null;
            });
            completion.submit(() -> {
                taskGenerator.apply(j, j + 1).run();
                return null;
            });
        }

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next task has completed.
        }

        executor.shutdown();

        //iterate from 0 to numberOfWorkers checking if that result is present in the results container
        for (int i = 0; i < numberOfWorkers; i++) {
            Optional<Integer> currentValue = Optional.of(i);
            Assert.assertTrue(results.contains(currentValue));
            results.remove(currentValue);
        }
        //assert that no exceptions happened
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void runAllNTimes() throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            exchangeBetweenTwoThreadsTest();
            timeoutInExchangeBetweenTwoThreadsTest();
        }

        for (int i = 0; i < 25; i++) {
            multipleKeysOneExchangeEach();
        }
    }

}
