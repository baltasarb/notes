import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class KeyedExchangerTests {

    @Test
    public void t() throws InterruptedException {
        KeyedExchanger<String> keyedExchanger = new KeyedExchanger<>();
        int pairKey = 1;
        String thread1Message = "Mensagem da thread 1.";
        String thread2Message = "Mensagem da thread 2.";
        ArrayList<Optional<String>> threadData = new ArrayList<>();

        BiConsumer<Integer, String> task = (key, message) -> {
            try {
                Optional<String> result = keyedExchanger.exchange(key,message, 10000);
                threadData.add(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        };
        Thread t1 = new Thread(() -> task.accept(pairKey, thread1Message));
        Thread t2 = new Thread(() -> task.accept(pairKey, thread2Message));

        t1.start();
        Thread.sleep(2500);//guaratees that first executes first
        t2.start();

        Thread.sleep(1000);

        assert !threadData.get(0).isPresent() || threadData.get(0).get().equals(thread1Message);
        assert !threadData.get(1).isPresent() || threadData.get(1).get().equals(thread2Message);


    }
}
