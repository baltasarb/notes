import junit.framework.AssertionFailedError;
import keyedExchanger.KeyedExchanger;
import org.junit.Test;

import java.util.Optional;

public class KeyedExchangerTests {

    @Test
    public void exchangeBetweenTwoThreadsTest() throws InterruptedException {
        KeyedExchanger<String> keyedExchanger = new KeyedExchanger<>();
        int pairKey = 1;
        String message1 = "message from exchanger 1";
        String message2 = "message from exchanger 2";

        Thread exchanger1 = new Thread(() -> {
            try {
                Optional<String> result = keyedExchanger.exchange(pairKey, message1, 1000);
                assert result.equals(Optional.of(message2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread exchanger2 = new Thread(() -> {
            try {
                Optional<String> result = keyedExchanger.exchange(pairKey, message2, 1000);
                assert result.equals(Optional.of(message1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        exchanger1.start();
        exchanger2.start();

        Thread.sleep(500);
    }

    @Test
    public void timeoutInExchangeBetweenTwoThreadsTest() throws InterruptedException {
        KeyedExchanger<String> keyedExchanger = new KeyedExchanger<>();
        int pairKey = 1;
        String message1 = "message from exchanger 1";
        String message2 = "message from exchanger 2";

        Thread exchanger1 = new Thread(() -> {
            try {
                Optional<String> result = keyedExchanger.exchange(pairKey, message1, 1);
                assert result.equals(Optional.empty());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread exchanger2 = new Thread(() -> {
            try {
                Optional<String> result = keyedExchanger.exchange(pairKey, message2, 1);
                assert result.equals(Optional.empty());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        exchanger1.start();
        Thread.sleep(100);
        exchanger2.start();

        Thread.sleep(500);
    }

    @Test()
    public void illegalArgumentsTest() throws InterruptedException {
        KeyedExchanger<String> keyedExchanger = new KeyedExchanger<>();
        int pairKey = 1;

        Thread exchanger1 = new Thread(() -> {
            try {
                Optional<String> result = keyedExchanger.exchange(pairKey, null, -1);
                assert result.equals(Optional.empty());
            } catch (Exception e) {
                assert e instanceof IllegalArgumentException;
            }
        });

        exchanger1.start();
        Thread.sleep(100);
    }

    @Test
    public void stressTest() {

    }

}
