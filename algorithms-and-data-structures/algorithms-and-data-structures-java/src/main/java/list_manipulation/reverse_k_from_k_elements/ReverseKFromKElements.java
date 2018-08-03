package list_manipulation.reverse_k_from_k_elements;

import java.security.InvalidParameterException;

public class ReverseKFromKElements {

    /*
     * This is the exercise number 1 of List Manipulation, Algorithms and Data Structures.
     * The wiki can be found here: https://github.com/baltasarb/notes/wiki/Algorithms-and-Data-Structures#1
     * The repo can be found here: https://github.com/baltasarb/notes/blob/master/algorithms-and-data-structures/algorithms-and-data-structures-java/src/main/java/list_manipulation/ReverseKFromKElements.java
     *
     * 1) Implement the static method:  public static <E> Node<E> reverseFromKElements(Node<E> list, int k)
     *
     *    That receives a simply linked list named list (non circular, without sentinel) and inverts its order from k to k elements.
     *    Assume that each object of type Node<E> has two fields: a value and a reference called next.
     *
     *    For instance:
     *    With the list  {1,2,3,4,5,6,7,8} and k = 3 the method should transform it to {3,2,1,6,4,8,7}
     * */

    public static <E> Node<E> reverseKFromKElements(Node<E> list, int k) {
        if (list == null) {
            throw new InvalidParameterException("The parameter list cannot be null.");
        }

        if(k < 0){
            throw new InvalidParameterException("The parameter k cannot be less than zero.");
        }

        if(k == 0){
            return list;
        }

        int kCounter = 1;

        Node<E> previousSublistTail = null,
                currentSublistRoot = null,
                listHead = null;
        do {
            //first element of the sublist
            if (kCounter == 1) {
                currentSublistRoot = list;
            }

            Node<E> next = list.getNext();
            if (kCounter++ >= k || next == null) {
                kCounter = 1;

                //last element of the list, before reaching k
                if (next == null) {
                    //single element list
                    if (previousSublistTail == null) {
                        return list;
                    }

                    currentSublistRoot = reverseList(currentSublistRoot);
                    previousSublistTail.setNext(currentSublistRoot);

                    if (listHead == null) {
                        listHead = currentSublistRoot;
                    }
                    return listHead;
                }

                Node<E> sublistTail = currentSublistRoot;
                list.setNext(null);
                Node<E> sublistRoot = reverseList(currentSublistRoot);

                if (listHead == null) {
                    listHead = sublistRoot;
                }

                //first element
                if (previousSublistTail == null) {
                    previousSublistTail = sublistTail;
                    sublistTail.setNext(next);
                } else {
                    previousSublistTail.setNext(sublistRoot);
                    previousSublistTail = sublistTail;
                    sublistTail.setNext(next);
                }
                currentSublistRoot = list;
            }
            list = next;
        } while (true);
    }

    //returns the root of the new list
    public static <E> Node<E> reverseList(Node<E> list) {
        Node<E> root = list;
        list = list.getNext();
        root.setNext(null);

        while (list != null) {
            Node<E> next = list.getNext();
            list.setNext(root);
            root = list;
            list = next;
        }

        return root;
    }

}


