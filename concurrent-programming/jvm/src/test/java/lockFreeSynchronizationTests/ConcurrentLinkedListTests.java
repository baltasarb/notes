package lockFreeSynchronizationTests;

import org.junit.Assert;
import org.junit.Test;
import utils.ConcurrentLinkedList;

import java.util.LinkedList;
import java.util.function.Function;

public class ConcurrentLinkedListTests {

    @Test
    public void addLastTest() throws InterruptedException {
        int numberOfItemsToAdd = 1000;
        int numberOfWorkers = 1000;

        ConcurrentLinkedList<Integer> list = new ConcurrentLinkedList<>();

        Thread[] workers = new Thread[numberOfWorkers];

        Function<Integer, Runnable> workerTaskGenerator = (numberToAdd) -> () -> list.addLast(numberToAdd);

        //assign work to every worker
        for (int i = 0; i < numberOfItemsToAdd; i++) {
            workers[i] = new Thread(workerTaskGenerator.apply(i));
        }

        //start every worker
        for (Thread worker : workers) {
            worker.start();
        }

        //wait for every worker to complete
        for (Thread worker : workers) {
            worker.join();
        }


        //extract every result from the list
        LinkedList<Integer> results = new LinkedList<>();
        for (int i = 0; i < numberOfItemsToAdd; i++) {
            Integer currentResult = list.removeFirst();
            results.addLast(currentResult);
        }

        //remove every expected value
        int expectedValue = 0;
        while (expectedValue < numberOfItemsToAdd)
            results.remove(Integer.valueOf(expectedValue++));

        //if any value was repeated or was not removed assert will fail
        Assert.assertTrue(results.isEmpty());
    }

    @Test
    public void removeFirstTest() throws InterruptedException {
        int numberOfItemsToAdd = 1000;
        int numberOfWorkers = 1000;

        Object monitor = new Object();

        ConcurrentLinkedList<Integer> list = new ConcurrentLinkedList<>();

        //add some values to the list
        for (int i = 0; i < numberOfItemsToAdd; i++) {
            list.addLast(i);
        }

        Thread[] workers = new Thread[numberOfWorkers];

        LinkedList<Integer> actualResults = new LinkedList<>();
        Runnable removeFirstTask = () -> {
            Integer currentElement = list.removeFirst();
            synchronized (monitor) {
                actualResults.addLast(currentElement);
            }
        };

        //assign work to every worker
        for (int i = 0; i < numberOfItemsToAdd; i++) {
            workers[i] = new Thread(removeFirstTask);
        }

        //start every worker
        for (Thread worker : workers) {
            worker.start();
        }

        //wait for every worker to complete
        for (Thread worker : workers) {
            worker.join();
        }

        //remove every expected value
        int expectedValue = 0;
        while (expectedValue < numberOfItemsToAdd)
            actualResults.remove(Integer.valueOf(expectedValue++));

        //if any value was repeated or was not removed assert will fail
        Assert.assertTrue(actualResults.isEmpty());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void isEmptyTest() throws InterruptedException {
        ConcurrentLinkedList<Integer> list = new ConcurrentLinkedList<>();

        Integer[] actualResult = new Integer[1];
        Runnable removeFirstTask = () -> {
            Integer currentElement = list.removeFirst();
            actualResult[0] = currentElement;
        };

        Thread worker = new Thread(removeFirstTask);
        worker.start();
        worker.join();

        //if any value was repeated or was not removed assert will fail
        Assert.assertEquals(null, actualResult[0]);
        Assert.assertTrue(list.isEmpty());
    }

}
