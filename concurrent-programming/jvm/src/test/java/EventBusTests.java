import eventBus.EventBus;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Consumer;

public class EventBusTests {

    @Test
    public void subscriptionBeforePublicationTest() throws InterruptedException {
        EventBus eventBus = new EventBus(1);

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        String expectedResult = "string to publish";
        Consumer<String> stringConsumer = result::add;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.subscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                failedResults.add("failed");
            }
        });

        Thread stringPublisher = new Thread(() -> eventBus.publishEvent(expectedResult));

        stringEventSubscriber.start();
        Thread.sleep(100);
        stringPublisher.start();


        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(500);
        eventBus.shutdown();

        Assert.assertTrue(result.contains(expectedResult));
        result.remove(expectedResult);
        Assert.assertTrue(result.isEmpty());
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void publicationBeforeSubscriptionTest() throws InterruptedException {
        EventBus eventBus = new EventBus(1);

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Consumer<String> stringConsumer = result::add;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.subscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                failedResults.add("failed");
            }
        });
        Thread stringPublisher = new Thread(() -> eventBus.publishEvent("message"));

        stringPublisher.start();
        Thread.sleep(100);
        stringEventSubscriber.start();

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(500);
        eventBus.shutdown();

        Assert.assertTrue(result.isEmpty());
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void subscriptionOfDifferedTypePublicationTest() throws InterruptedException {
        EventBus eventBus = new EventBus(1);

        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        int publication = 1;
        Consumer<String> stringConsumer = result::add;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.subscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                failedResults.add("failed");
            }
        });

        Thread intPublisher = new Thread(() -> eventBus.publishEvent(publication));

        stringEventSubscriber.start();
        Thread.sleep(100);
        intPublisher.start();

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(500);
        eventBus.shutdown();

        Assert.assertTrue(result.isEmpty());
        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void oneSubscriptionMultiplePublicationsOfSameTypeTest() throws InterruptedException {
        EventBus eventBus = new EventBus(10);

        ArrayList<Integer> intResults = new ArrayList<>();

        int[] event = {0};

        Object monitor = new Object();

        Consumer<Integer> intConsumer = result -> {
            synchronized (monitor) {
                intResults.add(result);
            }
        };

        Thread intEventSubscriber = new Thread(() -> {
            try {
                eventBus.subscribeEvent(intConsumer, Integer.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread intPublisher1 = new Thread(() -> {
            int message1;
            int message2;
            synchronized (monitor) {
                message1 = event[0];
                message2 = ++event[0];
                event[0]++;
            }
            eventBus.publishEvent(message1);
            eventBus.publishEvent(message2);
        });
        Thread intPublisher2 = new Thread(() -> {
            int message1;
            int message2;
            synchronized (monitor) {
                message1 = event[0];
                message2 = ++event[0];
                event[0]++;
            }
            eventBus.publishEvent(message1);
            eventBus.publishEvent(message2);
        });

        intEventSubscriber.start();
        Thread.sleep(1000);
        intPublisher1.start();
        intPublisher2.start();

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(1000);
        eventBus.shutdown();

        int numberOfMessages = 4;
        for (int i = 0; i < numberOfMessages; i++) {
            Assert.assertTrue(intResults.contains(i));
        }
    }

    @Test
    public void onePublicationMultipleSubscribersOfSameTypeTest() throws InterruptedException {
        EventBus eventBus = new EventBus(10);

        ArrayList<Integer> intSubscriber1Results = new ArrayList<>();
        ArrayList<Integer> intSubscriber2Results = new ArrayList<>();

        int[] event = {0};
        int numberOfMessages = 10;

        Consumer<Integer> intConsumer1 = intSubscriber1Results::add;
        Consumer<Integer> intConsumer2 = intSubscriber2Results::add;

        Thread intEventSubscriber1 = new Thread(() -> {
            try {
                eventBus.subscribeEvent(intConsumer1, Integer.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread intEventSubscriber2 = new Thread(() -> {
            try {
                eventBus.subscribeEvent(intConsumer2, Integer.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //each subscriber must have 10 messages
        Thread intPublisher1 = new Thread(() -> {
            for (int i = 0; i < numberOfMessages; i++)
                eventBus.publishEvent(event[0]++);
        });

        intEventSubscriber1.start();
        intEventSubscriber2.start();
        Thread.sleep(100);
        intPublisher1.start();

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(1000);
        eventBus.shutdown();

        for (int i = 0; i < numberOfMessages; i++) {
            Assert.assertTrue(intSubscriber1Results.contains(i));
            Assert.assertTrue(intSubscriber2Results.contains(i));
        }
    }

    @Test
    public void shutdownTest() throws InterruptedException {
        EventBus eventBus = new EventBus(10);

        int numberOfWorkers = 1;

        int[] intPublicationMessage = {0};

        ArrayList<Integer> intResults = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object monitor = new Object();

        Consumer<Integer> intSubscriberConsumer = result -> {
            synchronized (monitor) {
                intResults.add(result);
            }
        };

        Runnable intSubscriberTask = () -> {
            try {
                eventBus.subscribeEvent(intSubscriberConsumer, Integer.class);
            } catch (InterruptedException e) {
                failedResults.add("Exception in int subscriber");
            }
        };

        Runnable intPublisherTask = () -> {
            int message;
            synchronized (monitor) {
                message = intPublicationMessage[0];
                intPublicationMessage[0]++;
            }
            eventBus.publishEvent(message);
        };

        Thread[] subscribers = new Thread[numberOfWorkers];
        Thread[] publishers = new Thread[numberOfWorkers];

        for (int i = 0; i < numberOfWorkers; i++) {
            subscribers[i] = new Thread(intSubscriberTask);
            subscribers[i].start();
        }

        Thread.sleep(500);

        for (int i = 0; i < numberOfWorkers; i++) {
            publishers[i] = new Thread(intPublisherTask);
            publishers[i].start();
        }

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(1000);
        eventBus.shutdown();

        for (int i = 0; i < numberOfWorkers; i++) {
            Assert.assertTrue(intResults.contains(i));
            intResults.remove(Integer.valueOf(i));
        }

        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void stressTest() throws InterruptedException {
        int numberOfWorkers = 1000;

        EventBus eventBus = new EventBus(numberOfWorkers);

        int[] intPublicationMessage = {0};
        int[] stringPublicationMessageId = {0};

        ArrayList<Integer> intResults = new ArrayList<>();
        ArrayList<String> stringResults = new ArrayList<>();
        ArrayList<String> failedResults = new ArrayList<>();

        Object monitor = new Object();

        Consumer<Integer> intSubscriberConsumer = (result) -> {
            synchronized (monitor) {
                intResults.add(result);
            }
        };

        Consumer<String> stringSubscriberConsumer = result -> {
            synchronized (monitor) {
                stringResults.add(result);
            }
        };

        Runnable intSubscriberTask = () -> {
            try {
                eventBus.subscribeEvent(intSubscriberConsumer, Integer.class);
            } catch (InterruptedException e) {
                failedResults.add("Exception in int subscriber");
            }
        };

        Runnable stringSubscriberTask = () -> {
            try {
                eventBus.subscribeEvent(stringSubscriberConsumer, String.class);
            } catch (InterruptedException e) {
                failedResults.add("Exception in string subscriber");
            }
        };

        Runnable intPublisherTask = () -> {
            int message;
            synchronized (monitor) {
                message = intPublicationMessage[0];
                intPublicationMessage[0]++;
            }
            eventBus.publishEvent(message);
        };

        Runnable stringPublisherTask = () -> {
            int message;
            synchronized (monitor) {
                message = stringPublicationMessageId[0];
                stringPublicationMessageId[0]++;
            }
            eventBus.publishEvent("message " + message);
        };

        Thread[] subscribers = new Thread[numberOfWorkers];
        Thread[] publishers = new Thread[numberOfWorkers];

        for (int i = 0; i < numberOfWorkers; i++) {
            if (i % 2 == 0) {
                subscribers[i] = new Thread(intSubscriberTask);
            } else {
                subscribers[i] = new Thread(stringSubscriberTask);
            }
            subscribers[i].start();
        }

        //enough time for the subscribers to be created
        //if not the message count will not be correct

        Thread.sleep(numberOfWorkers / 2);

        for (int i = 0; i < numberOfWorkers; i++) {
            if (i % 2 == 0) {
                publishers[i] = new Thread(intPublisherTask);
            } else {
                publishers[i] = new Thread(stringPublisherTask);
            }
            publishers[i].start();
        }

        //close the event bus to process all pending messages and be able
        // to assert correctly
        Thread.sleep(1000);
        eventBus.shutdown();

        String message = "message ";
        int numberOfMessageIds = numberOfWorkers / 2;
        for (int i = 0; i < numberOfMessageIds; i++) {
            Assert.assertTrue(intResults.contains(i));
            intResults.remove(Integer.valueOf(i));

            String currentMessage = message + i;
            Assert.assertTrue(stringResults.contains(currentMessage));
            stringResults.remove(currentMessage);
        }

        Assert.assertTrue(failedResults.isEmpty());
    }

    @Test
    public void runAllNTimes() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            subscriptionBeforePublicationTest();
            publicationBeforeSubscriptionTest();
            subscriptionOfDifferedTypePublicationTest();
            oneSubscriptionMultiplePublicationsOfSameTypeTest();
            onePublicationMultipleSubscribersOfSameTypeTest();
            stressTest();
            shutdownTest();
        }
    }

}
