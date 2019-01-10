package synchronizationWithMonitors.windowsKeyedEvent;

import utils.Timer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class WindowsKeyedEvent {

    private final ReentrantLock monitor;

    private HashMap<Object, LinkedList<KeyedEvent>> releases;
    private HashMap<Object, LinkedList<KeyedEvent>> awaits;

    public WindowsKeyedEvent() {
        monitor = new ReentrantLock();
        releases = new HashMap<>();
        awaits = new HashMap<>();
    }

    public boolean release(Object key, int timeout) throws InterruptedException {
        monitor.lock();

        KeyedEvent keyedEvent;
        LinkedList<KeyedEvent> currentAwaits = awaits.get(key);
        if (currentAwaits != null) {
            keyedEvent = currentAwaits.removeFirst();
            return true;
        }

        Timer timer = new Timer(timeout);

        if(timer.timeExpired()){
            return false;
        }

        keyedEvent = new KeyedEvent(monitor.newCondition());
        //put in release list

        try{
            while(true){
                keyedEvent.getCondition().await();

                if(keyedEvent.isGranted()){
                    return true;
                }

                if(timer.timeExpired()){
                    //remove from releases
                    return false;
                }
            }
        }catch (InterruptedException e){
            return false;
        }

    }

    public boolean await(Object key, int timeout) throws InterruptedException {
        return false;
    }

}
