package list_manipulation.reverse_k_from_k_elements;

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
      return null;
    }

    public static <E> Node<E> reverseKFromKElementsWithStack(Node<E> list, int k) {
        return null;
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


