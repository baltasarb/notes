package list_manipulation.reverse_k_from_k_elements;

public class Node<E> {
    Node<E> next;
    E value;

    public Node(E value) {
        this.value = value;
        next = null;
    }

    public void setNext(Node<E> next) {
        this.next = next;
    }

    public Node<E> getNext() {
        return next;
    }

    public E getValue() {
        return value;
    }
}