import org.junit.Test;

public class ExpirableLazyTests {

    @Test
    public void t() throws InterruptedException {
        int[] counter = {0};
        ExpirableLazy<Integer> expirableLazy = new ExpirableLazy<>(
                () -> counter[0]++, 500);

        Thread t1 = new Thread(() -> {
            System.out.println("first: " + expirableLazy.getValue());
        });

        Thread t2 = new Thread(() -> {
            System.out.println("second: " + expirableLazy.getValue());
        });

        t1.start();
        Thread.sleep(1000);//guaratees that first executes first
        t2.start();

        Thread.sleep(1000);
    }
}
