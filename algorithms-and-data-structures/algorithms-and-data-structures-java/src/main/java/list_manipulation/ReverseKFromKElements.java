package list_manipulation;

public class ReverseKFromKElements {

    /*
     * This is the exercise number 1 of List Manipulation, Algorithms and Data Structures.
     * The wiki can be found here: https://github.com/baltasarb/notes/wiki/Algorithms-and-Data-Structures#1
     * The repo can be found here:
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
        int kCounter = 0;

        Node<E> root = list;
        Node<E> currentNode = list;
        Node<E> previous = null;
        Node<E> listHead = null;

        while (currentNode != null) {
            //end of the first sublist
            if (kCounter == k) {
                if (listHead == null) {
                    listHead = root;
                }
                root = currentNode;
                kCounter = 0;
            }

            kCounter++;

            //first element of the list
            if (previous == null) {
                previous = currentNode;
                currentNode = currentNode.getNext();
                continue;
            }

            Node<E> next = currentNode.getNext();

            //last element of the list
            if (next == null) {
                //the last but not reversable node
                if (currentNode.equals(root)) {
                    return listHead;
                }
                previous.setNext(null);
                currentNode.setNext(root);
                return listHead;
            }

            currentNode.setNext(root);
            root = currentNode;
            previous.setNext(next);
            currentNode = next;
        }

        return listHead;
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

    public class Node<E> {
        private Node<E> next;
        private E value;

        public Node(E value) {
            this.value = value;
            next = null;
        }

        public Node<E> getNext() {
            return next;
        }

        public E getValue() {
            return value;
        }

        public void setNext(Node<E> next) {
            this.next = next;
        }
    }

}


