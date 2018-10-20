package synchronizationWithMonitorsTests;

import synchronizationWithMonitors.messageQueue.MessageQueue;
import synchronizationWithMonitors.messageQueue.SendStatus;
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

        //submitWork the work to the pool
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
    public void receiveBeforeSendTest() throws InterruptedException {
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

        //submitWork the work to the pool
        completion.submit(() -> {
            receiveTask.run();
            return null;
        });

        // wait for all tasks to complete.
        completion.take(); // will block until the next sub task has completed.

        executor.shutdown();

        Optional<String> expectedResult = Optional.empty();
        Assert.assertTrue(results.contains(expectedResult));
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void isSentTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<SendStatus> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            synchronized (resultSynchronization) {
                results.add(sendStatus);
            }
        };

        Runnable receiveTask = () -> {
            try {
                messageQueue.receive(1000);
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submitWork the work to the pool
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

        Assert.assertTrue(results.get(0).isSent());
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void isNotSentTest() throws InterruptedException {
        int numberOfWorkers = 1;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<SendStatus> results = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            synchronized (resultSynchronization) {
                results.add(sendStatus);
            }
        };

        //submitWork the work to the pool
        completion.submit(() -> {
            sendTask.run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        Assert.assertFalse(results.get(0).isSent());
    }

    @Test
    public void awaitSuccessTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<Boolean> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            boolean result = false;
            try {
                //wait for the message to be sent
                result = sendStatus.await(1000);
            } catch (InterruptedException e) {
                failedResults.add("Error in await.");
            }
            synchronized (resultSynchronization) {
                results.add(result);
            }
        };

        Runnable receiveTask = () -> {
            try {
                messageQueue.receive(1000);
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submitWork the work to the pool
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

        Assert.assertTrue(results.get(0));
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void awaitFailureTest() throws InterruptedException {
        int numberOfWorkers = 1;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<Boolean> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            boolean result = true;
            try {
                //wait for the message to be sent
                result = sendStatus.await(0);
            } catch (InterruptedException e) {
                failedResults.add("Error in await.");
            }
            synchronized (resultSynchronization) {
                results.add(result);
            }
        };

        //submitWork the work to the pool
        completion.submit(() -> {
            sendTask.run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        Assert.assertTrue(!results.get(0));
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void tryCancelSuccessTest() throws InterruptedException {
        int numberOfWorkers = 1;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<Boolean> results = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            //wait for the message to be sent
            boolean result = sendStatus.tryCancel();
            synchronized (resultSynchronization) {
                results.add(result);
            }
        };

        //submitWork the work to the pool
        completion.submit(() -> {
            sendTask.run();
            return null;
        });

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        Assert.assertTrue(results.get(0));
    }

    @Test
    public void tryCancelFailureFirstTest() throws InterruptedException {
        int numberOfWorkers = 2;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<String> messageQueue = new MessageQueue<>();

        ArrayList<SendStatus> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        Runnable sendTask = () -> {
            SendStatus sendStatus = messageQueue.send("someMessage");
            synchronized (resultSynchronization) {
                results.add(sendStatus);
            }
        };

        Runnable receiveTask = () -> {
            try {
                messageQueue.receive(1000);
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submitWork the work to the pool
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

        Assert.assertTrue(!results.get(0).tryCancel());
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void stressTest() throws InterruptedException {
        int numberOfWorkers = 1000;

        //pool to synchronize results with the main thread
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        CompletionService<Void> completion = new ExecutorCompletionService<>(executor);

        MessageQueue<Integer> messageQueue = new MessageQueue<>();

        ArrayList<Optional<Integer>> results = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object resultSynchronization = new Object();

        int[] messageIds = {0};

        Runnable sendTask = () -> {
            int message;
            synchronized (resultSynchronization) {
                message = messageIds[0];
                messageIds[0]++;
            }
            messageQueue.send(message);
        };

        Runnable receiveTask = () -> {
            try {
                Optional<Integer> result = messageQueue.receive(1000);
                synchronized (resultSynchronization) {
                    results.add(result);
                }
            } catch (InterruptedException e) {
                synchronized (resultSynchronization) {
                    failedResults.add("Failure occurred receive.");
                }
            }
        };

        //submitWork the work to the pool
        //number of workers dividing by 2 because only half of them are senders
        //the other half are receivers
        for (int i = 0; i < numberOfWorkers / 2; i++) {
            int randomNumber = new Random().nextInt();

            if (randomNumber % 2 == 0) {
                completion.submit(() -> {
                    sendTask.run();
                    return null;
                });

                completion.submit(() -> {
                    receiveTask.run();
                    return null;
                });
            } else {
                completion.submit(() -> {
                    receiveTask.run();
                    return null;
                });

                completion.submit(() -> {
                    sendTask.run();
                    return null;
                });
            }
        }

        // wait for all tasks to complete.
        for (int i = 0; i < numberOfWorkers; ++i) {
            completion.take(); // will block until the next sub task has completed.
        }
        executor.shutdown();

        //only half of the workers are message receivers
        for (int i = 0; i < numberOfWorkers / 2; i++) {
            Optional<Integer> expectedMessage = Optional.of(i);
            Assert.assertTrue(results.contains(expectedMessage));
            results.remove(expectedMessage);
        }
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void runAllNTimes() throws InterruptedException {
        for (int i = 0; i < 25; i++) {
            sendBeforeReceiveTest();
            receiveBeforeSendTest();
            isSentTest();
            isNotSentTest();
            awaitSuccessTest();
            awaitFailureTest();
            tryCancelSuccessTest();
            tryCancelFailureFirstTest();
            stressTest();
        }
    }

}
