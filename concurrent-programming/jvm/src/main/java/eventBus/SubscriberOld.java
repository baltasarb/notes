package eventBus;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;

/**
 * This class has two purposes:
 * save messages published by the publishEvent method
 * save the condition for each subscriber, allowing for specific notification
 */
class SubscriberOld {
    private final Condition condition;
    private LinkedList<Object> messages;
    private boolean isHandlingMessages;

    SubscriberOld(Condition condition) {
        this.condition = condition;
        messages = new LinkedList<>();
        isHandlingMessages = false;
    }

    void addMessage(Object message) {
        messages.addLast(message);
    }

    boolean isFull(int maxPending) {
        return messages.size() >= maxPending;
    }

    Condition getCondition() {
        return condition;
    }

    LinkedList<Object> getMessages() {
        return messages;
    }

    boolean isHandlingMessages() {
        return isHandlingMessages;
    }

    void setHandlingMessages(boolean handlingMessages) {
        isHandlingMessages = handlingMessages;
    }

    void clearMessages() {
        this.messages = new LinkedList<>();
    }
}