import org.junit.Test;

import java.util.LinkedList;
import java.util.function.Consumer;

public class GenericTypesTests {

    private final LinkedList<Object> list = new LinkedList<>();

    public <T> void consumeIfSameType(Consumer<T> handler, Class<T> handlerType) {
        list.forEach(item -> {
            if (item.getClass().equals(handlerType))
                handler.accept((T) item);
        });
    }


    public <E> void addMessagesToList(int numberOfMessages, E message) {
        for (int i = 0; i < numberOfMessages; i++) {
            list.add(message);
        }
    }


    @Test
    public void genericEqualityTest() {

        addMessagesToList(10, 1);
        addMessagesToList(10, "String");
        addMessagesToList(10, new Object());

        //prints the entire list because of toString()
        Consumer<String> stringConsumer = System.out::println;
        consumeIfSameType(stringConsumer, String.class);

        //prints integers only
        Consumer<Integer> intConsumer = System.out::println;
        consumeIfSameType(intConsumer, Integer.class);

    }


}