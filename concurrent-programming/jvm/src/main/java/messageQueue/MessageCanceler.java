package messageQueue;

import java.util.concurrent.locks.Condition;

public class MessageCanceler {

    private boolean cancelationRequired;
    private Condition condition;
    private Boolean cancelationSuccessfull;

    public MessageCanceler (){
        cancelationRequired = false;
        cancelationSuccessfull = null;
    }

    public boolean cancelationIsRequired(){
        return cancelationRequired;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void cancelMessage(){
        cancelationRequired = true;
    }

    public Condition getCondition() {
        return condition;
    }

    public Boolean getCancelationSuccessfull() {
        return cancelationSuccessfull;
    }
}