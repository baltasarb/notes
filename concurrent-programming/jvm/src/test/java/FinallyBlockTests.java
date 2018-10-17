import org.junit.Test;

import java.util.LinkedList;

public class FinallyBlockTests {


    @Test
    public void finallyExecutionAfterExceptionTest() {

        LinkedList<String> list = null;

        try {
            list.addLast("one");
        } catch (NullPointerException e) {

        } finally {
            list = new LinkedList<>();
            list.addLast("second");
        }

        list.forEach(System.out::println);
    }

    @Test
    public void finallyExecutionWithoutException() {

        LinkedList<String> list = null;

        try {
            list = new LinkedList<>();
            list.addLast("one");
        } catch (NullPointerException e) {

        } finally {
            list = new LinkedList<>();
            list.addLast("second");
        }

        list.forEach(System.out::println);
    }

    @Test
    public void finallyExecutionAfterReturn() {

        LinkedList<String> list = null;

        try {
            list = new LinkedList<>();
            list.addLast("one");
            return;
        } catch (NullPointerException e) {

        } finally {
            list = new LinkedList<>();
            list.addLast("second");
            list.forEach(System.out::println);
        }

    }

    @Test
    public void outterExceptionAfterInnerFinally(){

        try{
            System.out.println("outter try");
            try{
                throw new IllegalStateException();
            }finally {
                System.out.println("inner finally");
            }

        }catch (Exception e){
            System.out.println("exception caught");
        }finally {
            System.out.println("outter finally");
        }


    }
}
