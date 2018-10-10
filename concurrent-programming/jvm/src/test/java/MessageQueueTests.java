import messageQueue.MessageQueue;
import messageQueue.SendStatus;
import org.junit.Test;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Optional;

public class MessageQueueTests {

    @Test
    public void SendBeforeReceiveTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable sendTask = () -> messageQueue.send(messageToSend);

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                assert result.equals(expectedResult);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(100);
        receiver.start();

        Thread.sleep(500);
    }

    @Test
    public void ReceiveBeforeSendTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String messageToSend = "message to send";
        Optional<String> expectedResult = Optional.of(messageToSend);

        Runnable receiveTask = () -> {
            try {
                Optional<String> result = messageQueue.receive(1000);
                assert result.equals(expectedResult);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable sendTask = () -> messageQueue.send(messageToSend);

        Thread receiver = new Thread(receiveTask);
        Thread sender = new Thread(sendTask);

        receiver.start();
        Thread.sleep(100);
        sender.start();

        Thread.sleep(500);
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

        int numberOfMessages = 100;
        ArrayList<Optional<Integer>> results = new ArrayList<>();

        Runnable sendTask = () -> messageQueue.send(messageId[0]++);

        Runnable receiveTask = () -> {
            try {
                Optional<Integer> result = messageQueue.receive(500);
                results.add(result);
            } catch (InterruptedException e) {
                System.out.println("Receiver : unexpected interrupted exception on wait.");
            }
        };

        Thread [] senders = new Thread[numberOfMessages];
        Thread [] receivers = new Thread[numberOfMessages];

        for(int i = 0; i < numberOfMessages; i++){
            senders[i] = new Thread(sendTask);
            senders[i].start();
            receivers[i] = new Thread(receiveTask);
            receivers[i].start();
        }

        Thread.sleep(500);

        ArrayList<Optional<Integer>> expectedResults = new ArrayList<>();
        for(int i = 0; i < numberOfMessages; i++)
            expectedResults.add(Optional.of(i));

        results.forEach(result -> {
            assert expectedResults.contains(result);
            //each key can only be found once
            expectedResults.remove(result);
        });
    }

}
