package list_manipulation_tests;

import list_manipulation.reverse_k_from_k_elements.Node;
import list_manipulation.reverse_k_from_k_elements.ReverseKFromKElements;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidParameterException;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Node;

public class ReverseFromKElementsTest {

    @Test(expected = InvalidParameterException.class)
    public void testNullList() {
        Node<Integer> root = null;
        int k = 1;

        ReverseKFromKElements.reverseKFromKElements(root, k);
    }

    @Test(expected = InvalidParameterException.class)
    public void testNegativeK() {
        Node<Integer> n1 = new Node<>(1);
        int k = -1;

        ReverseKFromKElements.reverseKFromKElements(n1, k);
    }

    @Test
    public void testKEqualToZero() {
        Node<Integer> n1 = new Node<>(1);
        Node<Integer> n2 = new Node<>(2);
        Node<Integer> n3 = new Node<>(3);
        n1.setNext (n2);
        n2.setNext(n3);
        int k = 0;

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, k);

        int[] expectedValues = {1, 2, 3};
        assertResult(result, expectedValues);
    }

    @Test
    public void testSingleElementList() {
        Node<Integer> n1 = new Node<>(1);

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, 3);

        int[] expectedValues = {1};
        assertResult(result, expectedValues);
    }

    @Test
    public void testTwoElementList() {
        Node<Integer> n1 = new Node<>(1);
        Node<Integer> n2 = new Node<>(2);
        n1.setNext(n2);

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, 3);

        int[] expectedValues = {2, 1};
        assertResult(result, expectedValues);
    }

    @Test
    public void testSeveralElementsList() {
        Node<Integer> n1 = new Node<>(1);
        Node<Integer> n2 = new Node<>(2);
        Node<Integer> n3 = new Node<>(3);
        Node<Integer> n4 = new Node<>(4);
        Node<Integer> n5 = new Node<>(5);
        Node<Integer> n6 = new Node<>(6);
        Node<Integer> n7 = new Node<>(7);
        Node<Integer> n8 = new Node<>(8);

        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
        n4.setNext(n5);
        n5.setNext(n6);
        n6.setNext(n7);
        n7.setNext(n8);

        Node<Integer> result = ReverseKFromKElements.reverseKFromKElements(n1, 3);

        int[] expectedValues = {3, 2, 1, 6, 5, 4, 8, 7};
        assertResult(result, expectedValues);
    }

    @Test
    public void testReverseList() {
        Node<Integer> n1 = new Node<>(1);
        Node<Integer> n2 = new Node<>(2);
        Node<Integer> n3 = new Node<>(3);
        Node<Integer> n4 = new Node<>(4);
        Node<Integer> n5 = new Node<>(5);
        Node<Integer> n6 = new Node<>(6);
        Node<Integer> n7 = new Node<>(7);
        Node<Integer> n8 = new Node<>(8);

        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
        n4.setNext(n5);
        n5.setNext(n6);
        n6.setNext(n7);
        n7.setNext(n8);

        Node<Integer> result = ReverseKFromKElements.reverseList(n1);

        int[] expectedValues = {8, 7, 6, 5, 4, 3, 2, 1};
        assertResult(result, expectedValues);
    }

    /**
     * @param result         should not be null
     * @param expectedValues the expected values to be within the list after the method execution
     */
    private void assertResult(Node<Integer> result, int[] expectedValues) {
        int i = 0;
        Assert.assertNotEquals(null, result);
        while (result != null) {
            Assert.assertEquals(expectedValues[i++], (int) result.getValue());
            result = result.getNext();
        }
    }
}
