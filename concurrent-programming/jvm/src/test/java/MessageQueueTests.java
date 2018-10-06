import messageQueue.MessageQueue;
import messageQueue.SendStatus;
import org.junit.Test;

import java.util.Optional;

public class MessageQueueTests {

    @Test
    public void SendBeforeReceiveTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> messageQueue.send("message 1");
        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(1000);
        receiver.start();

        Thread.sleep(1000);
    }

    @Test
    public void ReceiveBeforeSendTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> messageQueue.send("message 1");
        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        receiver.start();
        Thread.sleep(1000);
        sender.start();

        Thread.sleep(1000);
    }

    @Test
    public void isSentTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            boolean received = sendStatus.isSent();
            System.out.println(received);
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        receiver.start();
        Thread.sleep(1000);
        sender.start();

        Thread.sleep(1000);
    }

    @Test
    public void isNotSentTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            boolean received = sendStatus.isSent();
            System.out.println(received);
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(1000);
        receiver.start();

        Thread.sleep(1000);
    }

    @Test
    public void awaitSuccessTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            try {
                boolean received = sendStatus.await(5000);
                System.out.println(received);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(1000);
        receiver.start();

        Thread.sleep(1000);
    }

    @Test
    public void awaitFailureTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            try {
                boolean received = sendStatus.await(100);
                System.out.println(received);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(1000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(1000);
        receiver.start();

        Thread.sleep(1000);
    }

    @Test
    public void tryCancelSendFirstTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            sendStatus.tryCancel();
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(2000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        sender.start();
        Thread.sleep(1000);
        receiver.start();

        Thread.sleep(1000);
    }

    @Test
    public void tryCancelReceiveFirstTest() throws InterruptedException {
        MessageQueue<String> messageQueue = new MessageQueue<>();

        Runnable sendTask = () -> {
            final SendStatus sendStatus = messageQueue.send("message 1");
            sendStatus.tryCancel();
        };

        Runnable receiveTask = () -> {
            try {
                Optional<String> optional = messageQueue.receive(2000);
                System.out.println(optional.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread sender = new Thread(sendTask);
        Thread receiver = new Thread(receiveTask);

        receiver.start();
        Thread.sleep(2000);
        sender.start();

        Thread.sleep(1000);
    }
    
}
