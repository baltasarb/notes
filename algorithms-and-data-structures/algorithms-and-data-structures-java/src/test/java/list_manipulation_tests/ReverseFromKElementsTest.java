package list_manipulation_tests;

import list_manipulation.ReverseKFromKElements;
import list_manipulation.ReverseKFromKElements.Node;
import org.junit.Assert;
import org.junit.Test;

public class ReverseFromKElementsTest {

    private final static ReverseKFromKElements REVERSE_K_FROM_K_ELEMENTS = new ReverseKFromKElements();

    public void testNullList () {
        Node<Integer> root = null;
        int k = 1;

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(root, k);

        //expect exception
    }

    public void testKEqualToZero(){
        Node<Integer> n1 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(1);
        Node<Integer> n2 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(2);
        Node<Integer> n3 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(3);
        n1.setNext(n2);
        n2.setNext(n3);
        int k = 0;

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, k);
    }

    public void testNegativeK(){
        Node<Integer> n1 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(1);
        Node<Integer> n2 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(2);
        Node<Integer> n3 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(3);
        n1.setNext(n2);
        n2.setNext(n3);
        int k = -1;

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, k);

    }

    @Test
    public void testAlgorithm(){
        Node<Integer> n1 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(1);
        Node<Integer> n2 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(2);
        Node<Integer> n3 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(3);
        Node<Integer> n4 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(4);
        Node<Integer> n5 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(5);
        Node<Integer> n6 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(6);
        Node<Integer> n7 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(7);
        Node<Integer> n8 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(8);

        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
        n4.setNext(n5);
        n5.setNext(n6);
        n6.setNext(n7);
        n7.setNext(n8);

        Node<Integer> result1 = ReverseKFromKElements.reverseKFromKElements(n1, 3);
        int [] expectedValues = {3,2,1,6,5,4,8,7};

        int i = 0;
        while(result1.getNext() != null){
            Assert.assertEquals(expectedValues[i++], (int)result1.getValue());
            result1 = result1.getNext();
        }
    }

    @Test
    public void testReverseList(){
        Node<Integer> n1 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(1);
        Node<Integer> n2 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(2);
        Node<Integer> n3 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(3);
        Node<Integer> n4 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(4);
        Node<Integer> n5 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(5);
        Node<Integer> n6 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(6);
        Node<Integer> n7 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(7);
        Node<Integer> n8 = REVERSE_K_FROM_K_ELEMENTS.new Node<>(8);

        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
        n4.setNext(n5);
        n5.setNext(n6);
        n6.setNext(n7);
        n7.setNext(n8);

        Node<Integer> result1 = ReverseKFromKElements.reverseList(n1);
        int [] expectedValues = {8,7,6,5,4,3,2,1};

        int i = 0;
        while(result1.getNext() != null){
            Assert.assertEquals(expectedValues[i++], (int)result1.getValue());
            result1 = result1.getNext();
        }
    }
}
