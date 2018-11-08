package lockFreeSynchronizationTests;

import lockFreeSynchronization.unsafeMessageBox.SafeMessageBox;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class UnsafeMessageBoxTests {

    @Test
    public void tryConsumeOneMessageTest() throws InterruptedException {

        SafeMessageBox<Integer> safeMessageBox = new SafeMessageBox<>();

        final int numberOfLives = 1;
        final int message = 1;

        ArrayList<Integer> results = new ArrayList<>();
        Object monitor = new Object();

        Thread publisher = new Thread(() -> safeMessageBox.publish(message, numberOfLives));

        Thread consumer = new Thread(() -> {
            int result = safeMessageBox.tryConsume();
            synchronized (monitor) {
                results.add(result);
            }
        });

        publisher.run();
        Thread.sleep(100);
        consumer.run();

        publisher.join();
        consumer.join();

        int expectedResult = 1;

        Assert.assertEquals(numberOfLives, results.size());

        for (Integer result : results) {
            int actualResult = result;
            Assert.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    public void tryConsumeNMessagesSameConsumerTest() throws InterruptedException {
        SafeMessageBox<Integer> safeMessageBox = new SafeMessageBox<>();

        final int numberOfLives = 100;

        ArrayList<Integer> results = new ArrayList<>();
        Object monitor = new Object();

        Thread publisher = new Thread(() -> safeMessageBox.publish(1, numberOfLives));

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < numberOfLives; i++) {
                int result = safeMessageBox.tryConsume();
                synchronized (monitor) {
                    results.add(result);
                }
            }
        });

        publisher.run();
        Thread.sleep(100);
        consumer.run();

        publisher.join();
        consumer.join();

        int expectedResult = 1;

        Assert.assertEquals(numberOfLives, results.size());

        for (int i = 0; i < numberOfLives; i++) {
            int actualResult = results.get(i);
            Assert.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    public void tryConsumeNMessagesDifferentConsumersTest() throws InterruptedException {
        SafeMessageBox<Integer> safeMessageBox = new SafeMessageBox<>();

        final int numberOfLives = 100;
        final int message = 1;

        ArrayList<Integer> results = new ArrayList<>();
        Object monitor = new Object();

        Thread publisher = new Thread(() -> safeMessageBox.publish(message, numberOfLives));

        Runnable consumerTask = () -> {
            for (int i = 0; i < numberOfLives / 2; i++) {
                int result = safeMessageBox.tryConsume();
                synchronized (monitor) {
                    results.add(result);
                }
            }
        };
        Thread consumer1 = new Thread(consumerTask);
        Thread consumer2 = new Thread(consumerTask);

        publisher.run();
        Thread.sleep(100);
        consumer1.run();
        consumer2.run();

        publisher.join();
        consumer1.join();
        consumer2.join();

        int expectedResult = 1;

        Assert.assertEquals(numberOfLives, results.size());

        for (Integer result : results) {
            int actualResult = result;
            Assert.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    public void tryConsumeMoreLivesThanTheExistingOnes() throws InterruptedException {
        SafeMessageBox<Integer> safeMessageBox = new SafeMessageBox<>();

        final int numberOfLives = 10;
        final int numberOfConsumes = numberOfLives * 2;
        final int message = 1;

        ArrayList<Integer> results = new ArrayList<>();
        Object monitor = new Object();

        Thread publisher = new Thread(() -> safeMessageBox.publish(message, numberOfLives));

        Runnable consumerTask = () -> {
            for (int i = 0; i < numberOfConsumes; i++) {
                Integer result = safeMessageBox.tryConsume();
                synchronized (monitor) {
                    results.add(result);
                }
            }
        };

        Thread consumer1 = new Thread(consumerTask);

        publisher.run();
        Thread.sleep(100);
        consumer1.run();

        publisher.join();
        consumer1.join();

        int expectedSuccessfulResult = 1;
        int expectedNumberOfSuccessfulConsumes = numberOfLives;
        int expectedNumberOfUnsuccessfulConsumes = numberOfConsumes - numberOfLives;

        Assert.assertEquals(numberOfConsumes, results.size());

        int actualNumberOfSuccessfulConsumes = 0;
        int actualNumberOfUnsuccessfulConsumes = 0;

        //count the number of each type of result occurrences
        for (Integer result : results) {
            if (result == null) {
                actualNumberOfUnsuccessfulConsumes++;
            } else if (result == expectedSuccessfulResult) {
                actualNumberOfSuccessfulConsumes++;
            }
        }

        //assert that each result occurred the correct number of times
        Assert.assertEquals(expectedNumberOfSuccessfulConsumes, actualNumberOfSuccessfulConsumes);
        Assert.assertEquals(expectedNumberOfUnsuccessfulConsumes, actualNumberOfUnsuccessfulConsumes);
    }

    @Test
    public void tryConsumeOnePublisher() {

    }

}
