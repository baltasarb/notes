package list_manipulation.reverse_k_from_k_elements;

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