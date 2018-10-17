import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExpirableLazyTests {

    public void t() throws InterruptedException {
        int[] counter = {0};
        ExpirableLazy<Integer> expirableLazy = new ExpirableLazy<>(
                () -> counter[0]++, 500);

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("first: " + expirableLazy.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("second: " + expirableLazy.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        Thread.sleep(1000);//guaratees that first executes first
        t2.start();

        Thread.sleep(1000);
    }

    @Test
    public void NoTimeoutTest() throws InterruptedException {
        final int numberOfThreads = 100;
        final int timeToLive = Integer.MAX_VALUE;
        int[] counter = {0};
        int [] actualResults = new int[numberOfThreads];
        int expectedResult = 0;

        Supplier<Integer> supplier = () -> counter[0]++;

        ExpirableLazy<Integer> expirableLazy = new ExpirableLazy<>(supplier, timeToLive);

        Thread [] threads = new Thread[numberOfThreads];

        Consumer<Integer> task = (index) -> {
            try {
                actualResults[index] = expirableLazy.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        for(int i = 0; i < numberOfThreads; i++){
            int [] index = {i};
            threads[i] = new Thread(() -> task.accept(index[0]));
            threads[i].start();
        }

        Thread.sleep(1000);
        for(int i = 0; i < numberOfThreads; i++){
            assert actualResults[i] == expectedResult;
        }
    }

}
