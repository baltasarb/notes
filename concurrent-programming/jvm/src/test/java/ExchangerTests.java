import keyedExchanger.Exchanger;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExchangerTests {

    @Test
    public void simpleExchangeTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        Exchanger<Integer> exchanger = new Exchanger<>();

        ArrayList<Boolean> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        // returns a runnable when called with the arguments of message to send and expected to receive
        BiFunction<Integer, Integer, Runnable> exchange = (messageToSend, expectedMessageToReceive) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(messageToSend, 5000);
                        synchronized (resultSynchronization){
                            results.add(Optional.of(expectedMessageToReceive).equals(result));
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultSynchronization){
                            failedResults.add("failure in : " + messageToSend);
                        }
                    }
                };

        //submitWork the work to the pool
        completion.submit(() -> {
            exchange.apply(0, 1).run();
            return null;
        });
        completion.submit(() -> {
            exchange.apply(1, 0).run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        // assert every result matched the expected
        for (Boolean result : results) {
            Assert.assertTrue(result);
        }
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void timeoutTest() throws InterruptedException {

        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        Exchanger<Integer> exchanger = new Exchanger<>();

        ArrayList<Boolean> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        // returns a runnable when called with the arguments of message to send and expected to receive
        Function<Integer, Runnable> exchange = (messageToSend) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(messageToSend, 0);
                        synchronized (resultSynchronization){
                            results.add(Optional.empty().equals(result));
                        }
                    } catch (InterruptedException e) {
                        synchronized (resultSynchronization){
                            failedResults.add("failure in : " + messageToSend);
                        }
                    }
                };

        //submitWork the work to the pool
        completion.submit(() -> {
            exchange.apply(0).run();
            return null;
        });
        completion.submit(() -> {
            exchange.apply(1).run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        // assert every result matched the expected
        for (Boolean result : results) {
            Assert.assertTrue(result);
        }
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void stressTest() throws InterruptedException {
        int numberOfWorkers = 1000;

        Exchanger<Integer> exchanger = new Exchanger<>();

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
        Function<Integer, Runnable> taskGenerator = (messageId) ->
                () -> {
                    try {
                        Optional<Integer> result = exchanger.exchange(messageId, 5000);
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
            int j = i;
            completion.submit(() -> {
                taskGenerator.apply(j).run();
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
            timeoutTest();
            simpleExchangeTest();
        }

        for (int i = 0; i < 50; i++) {
            stressTest();
        }
    }

}
