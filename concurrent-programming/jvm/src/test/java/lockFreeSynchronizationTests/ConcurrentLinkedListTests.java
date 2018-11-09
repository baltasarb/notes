package lockFreeSynchronizationTests;

import org.junit.Test;
import utils.ConcurrentLinkedList;

public class ConcurrentLinkedListTests {

    @Test
    public void addLastTest() {
        ConcurrentLinkedList<Integer> list = new ConcurrentLinkedList<>();

        list.addLast(1);
        list.addLast(2);


        System.out.println(list);

        System.out.println(list.removeFirst());
        System.out.println(list.removeFirst());

        System.out.println(list);

        list.addLast(1);
        list.addLast(2);

        System.out.println(list);
    }
}
