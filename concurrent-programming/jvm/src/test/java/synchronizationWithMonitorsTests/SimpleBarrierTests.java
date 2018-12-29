package synchronizationWithMonitorsTests;

import org.junit.Assert;
import org.junit.Test;
import synchronizationWithMonitors.SimpleBarrier;

import java.util.ArrayList;
import java.util.function.Function;

public class SimpleBarrierTests {

    @Test
    public void t() throws InterruptedException {
        int numberOfParticipants = 10;

        SimpleBarrier simpleBarrier = new SimpleBarrier(numberOfParticipants);
        ArrayList<Boolean> results = new ArrayList<>();

        Function<Integer, Runnable> participantTaskGenerator = (threadId) -> () -> {
            try {
                boolean result = simpleBarrier.await(1000);
                synchronized (results){results.add(result);}
                System.out.println("Thread " + threadId + " finished. Success: " + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread [] participants = new Thread[numberOfParticipants];

        for (int i = 0; i < numberOfParticipants; i++) {
            participants[i] = new Thread(participantTaskGenerator.apply(i));
        }

        for (int i = 0; i < numberOfParticipants; i++) {
            participants[i].start();
        }

        for (int i = 0; i < numberOfParticipants; i++) {
            participants[i].join();
        }

        for (Boolean result : results){
            Assert.assertTrue(result);
        }

    }
}
