import messageQueue.MessageQueue;
import messageQueue.SendStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageQueueTests {

    @Test
    public void sendBeforeReceiveTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<Optional<String>> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();
        String messageToSend = "message to send";

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> messageQueue.send(messageToSend);

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                synchronized (resultSynchronization) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submit the work to the pool
        completion.submit(() -> {
            sendTask.run();
            return null;
        });
        completion.submit(() -> {
            receiveTask.run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        Optional<String> expectedResult = Optional.of(messageToSend);
        Assert.assertTrue(results.contains(expectedResult));
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void runAllNTimes() throws InterruptedException {
        for (int i = 0; i < 5000; i++) {
            sendBeforeReceiveTest();
        }
    }

    @Test
    public void ReceiveBeforeSendTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<Optional<String>> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(10);
                synchronized (resultSynchronization) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submit the work to the pool
        completion.submit(() -> {
            receiveTask.run();
            return null;
        });

        // wait for all tasks to complete.
        completion.take(); // will block until the next sub task has completed.

        executor.shutdown();

        Optional<String> expectedResult = Optional.empty();
        System.out.println(results);
        Assert.assertTrue(results.contains(expectedResult));
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void isSentTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);
            assert !status.isSent();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Sender : unexpected interrupted exception on thread sleep.");
            }
            assert status.isSent();
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                assert result.equals(expectedResult);
            } catch (InterruptedException e) {
                System.out.println("Receiver : unexpected interrupted exception on thread sleep.");
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(200);
        receiver.start();

        Thread.sleep(500);
    }

    @Test
    public void isNotSentTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);
            assert !status.isSent();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Sender : unexpected interrupted exception on thread sleep.");
            }
            assert !status.isSent();
        };

        Thread sender = new Thread(sendTask);

        sender.start();

        Thread.sleep(500);
    }

    @Test
    public void awaitSuccessTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);

            try {
                boolean success = status.await(1000);
                assert success;
            } catch (InterruptedException e) {
                System.out.println("Sender await : Unexpected Interruption Occurred. ");
            }
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                assert result.equals(expectedResult);
            } catch (InterruptedException e) {
                System.out.println("Receiver : unexpected interrupted exception on thread sleep.");
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(200);
        receiver.start();

        Thread.sleep(500);
    }

    @Test
    public void awaitFailureTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);

            try {
                boolean success = status.await(1000);
                assert !success;
            } catch (InterruptedException e) {
                System.out.println("Sender await : Unexpected Interruption Occurred. ");
            }
        };

        Thread sender = new Thread(sendTask);

        sender.start();

        Thread.sleep(500);
    }

    @Test
    public void tryCancelSuccessTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);
            boolean success = status.tryCancel();
            assert success;
        };

        Thread sender = new Thread(sendTask);

        sender.start();

        Thread.sleep(500);
    }

    @Test
    public void tryCancelFailureFirstTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable sendTask = () -> {
            SendStatus status = messageQueue.send(messageToSend);
            try {
                Thread.sleep(500);
                boolean success = status.tryCancel();
                assert !success;
            } catch (InterruptedException e) {
                System.out.println("Sender await : Unexpected Interruption Occurred. ");
            }
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                assert result.equals(expectedResult);
            } catch (InterruptedException e) {
                System.out.println("Receiver : unexpected interrupted exception on thread sleep.");
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(200);
        receiver.start();

        Thread.sleep(500);
    }

    @Test
    public void stressTest() throws InterruptedException {
        MessageQueue<Integer> messageQueue = new MessageQueue<>();
        int[] messageId = {0};

        int numberOfMessages = 1000;
        ArrayList<Optional<Integer>> results = new ArrayList<>();

        Runnable sendTask = () -> messageQueue.send(messageId[0]++);

        Runnable receiveTask = () -> {
            try {
                Optional<Integer> result = messageQueue.receive(10000);
                results.add(result);
            } catch (InterruptedException e) {
                System.out.println("Receiver : unexpected interrupted exception on wait.");
            }
        };

        Thread[] senders = new Thread[numberOfMessages];
        Thread[] receivers = new Thread[numberOfMessages];

        for (int i = 0; i < numberOfMessages; i++) {
            int randomNumber = new Random().nextInt();

            if (randomNumber % 2 == 0) {
                senders[i] = new Thread(sendTask);
                senders[i].start();
                receivers[i] = new Thread(receiveTask);
                receivers[i].start();
            } else {
                receivers[i] = new Thread(receiveTask);
                receivers[i].start();
                senders[i] = new Thread(sendTask);
                senders[i].start();
            }
        }

        //time given for the work to be complete
        Thread.sleep(2500);

        ArrayList<Optional<Integer>> expectedResults = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++)
            expectedResults.add(Optional.of(i));

        for (int i = 0; i < results.size(); i++) {
            Optional<Integer> result = results.get(i);
            boolean res = expectedResults.contains(result);
            if (!res)
                System.out.println(result + ": " + res);
            Assert.assertTrue(res);
            //each key can only be found once
            expectedResults.remove(result);
        }

    }

    @Test
    public void stressTestNTimes() throws InterruptedException {
        for (int i = 0; i < 25; i++) {
            stressTest();
        }
    }

}
