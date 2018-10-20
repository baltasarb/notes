package synchronizationWithMonitors.eventBus;

import java.util.LinkedList;
import java.util.function.Consumer;

class Subscriber {

    private final Consumer<Object> handler;

    private LinkedList<Object> messages;

    Subscriber(Consumer<Object> handler) {
        this.handler = handler;
        this.messages = new LinkedList<>();
    }

    void addMessage(Object message) {
        messages.addLast(message);
    }

    boolean hasMessages() {
        return !messages.isEmpty();
    }

    LinkedList<Object> getAndClearMessages() {
        LinkedList<Object> messagesToReturn = messages;
        messages = new LinkedList<>();
        return messagesToReturn;
    }

    /**
     * @param exclusionGottenMessages messages obtain in the exclusion of the type manager, no new messages can be added
     *                                while out of this exclusion due to this being a local variable
     * @throws InterruptedException thrown in case of any handler exception
     */
    void handleMessages(LinkedList<Object> exclusionGottenMessages) throws InterruptedException {
        try {
            while (exclusionGottenMessages.size() > 0) {
                Object message = exclusionGottenMessages.removeFirst();
                handler.accept(message);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Handler error : %s", e.getMessage());
            throw new InterruptedException(errorMessage);
        }
    }

    int getNumberOfMessages() {
        return messages.size();
    }

}
