import org.junit.Test;

import java.util.function.Consumer;

public class EventBusTests {

    @Test
    public void SubsriptionBeforePublicationTest() throws InterruptedException {
        EventBus eventBus = new EventBus(5);

        int [] strCounter = {0};
        Consumer<String> stringConsumer = System.out::println;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.SubscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread stringPublisher = new Thread (() -> eventBus.PublishEvent("string to publish : " + strCounter[0]++));

        stringEventSubscriber.start();
        Thread.sleep(1000);
        stringPublisher.start();

        Thread.sleep(1000);
    }

    @Test
    public void PublicationBeforeSubscriptionTest() throws InterruptedException {
        EventBus eventBus = new EventBus(5);

        int [] strCounter = {0};
        Consumer<String> stringConsumer = System.out::println;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.SubscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread stringPublisher = new Thread (() -> eventBus.PublishEvent("string to publish : " + strCounter[0]++));

        stringPublisher.start();
        Thread.sleep(1000);
        stringEventSubscriber.start();

        Thread.sleep(1000);
    }

    @Test
    public void SubscriptionOFDifferenTypePublicationTest() throws InterruptedException {
        EventBus eventBus = new EventBus(5);

        int [] strCounter = {0};
        Consumer<String> stringConsumer = System.out::println;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.SubscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread intPublisher = new Thread (() -> eventBus.PublishEvent(1));

        stringEventSubscriber.start();
        Thread.sleep(1000);
        intPublisher.start();

        Thread.sleep(1000);
    }

    @Test
    public void MutlipleSubscriptionsOFDifferenTypesAndDifferentPublicationTypesTest() throws InterruptedException {
        EventBus eventBus = new EventBus(5);

        int [] strCounter = {0};
        Consumer<String> stringConsumer = System.out::println;
        Consumer<Integer> intConsumer = System.out::println;

        Thread stringEventSubscriber = new Thread(() -> {
            try {
                eventBus.SubscribeEvent(stringConsumer, String.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread intEventSubscriber = new Thread(() -> {
            try {
                eventBus.SubscribeEvent(intConsumer, Integer.class);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread intPublisher = new Thread (() -> eventBus.PublishEvent(1));
        Thread stringPublisher = new Thread (() -> eventBus.PublishEvent("string to publish : " + strCounter[0]++));

        stringEventSubscriber.start();
        intEventSubscriber.start();
        Thread.sleep(1000);
        intPublisher.start();
        stringPublisher.start();

        Thread.sleep(1000);
    }

}
