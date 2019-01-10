package synchronizationWithMonitors.windowsKeyedEvent;

import java.util.concurrent.locks.Condition;

public class KeyedEvent {

    private final Condition condition;
    private boolean granted;

    public KeyedEvent(Condition condition){
        this.condition = condition;
        granted = false;
    }

    public Condition getCondition(){
        return condition;
    }

    public boolean isGranted(){
        return granted;
    }

}
