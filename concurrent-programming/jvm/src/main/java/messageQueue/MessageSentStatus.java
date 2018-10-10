package messageQueue;

public class MessageSentStatus implements SendStatus {

    @Override
    public boolean isSent() {
        return true;
    }

    @Override
    public boolean tryCancel() {
        return false;
    }

    @Override
    public boolean await(int timeout) {
        return true;
    }

}
