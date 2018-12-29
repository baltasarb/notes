package synchronizationWithMonitors.readWriteSemaphore;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class ReadWriteSemaphore {

    private final ReentrantLock monitor;
    private LinkedList<Request> queue;

    public ReadWriteSemaphore(){
        monitor = new ReentrantLock();
        queue = new LinkedList<>();
    }

    public void DownRead() {

    }

    public void DownWrite() {

    }

    public void UpRead() {
    }

    public void UpWrite() {
    }

    public void DowngradeWriter() {
    }

}
