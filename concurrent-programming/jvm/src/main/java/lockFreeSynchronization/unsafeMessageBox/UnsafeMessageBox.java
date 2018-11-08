package lockFreeSynchronization.unsafeMessageBox;

public class UnsafeMessageBox<M> {

    private class MsgHolder {
        private final M msg;
        private int lives;

        private MsgHolder(M msg, int lives) {
            this.msg = msg;
            this.lives = lives;
        }
    }

    private MsgHolder msgHolder = null;

    public void Publish(M m, int lvs) {
        msgHolder = new MsgHolder (m, lvs);
    }

    public M TryConsume() {
        if (msgHolder != null && msgHolder.lives > 0) {
            msgHolder.lives -= 1;
            return msgHolder.msg;
        }
        return null;
    }

}
