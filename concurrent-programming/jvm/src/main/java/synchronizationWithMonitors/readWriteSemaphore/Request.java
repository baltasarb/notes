package synchronizationWithMonitors.readWriteSemaphore;

import java.util.concurrent.locks.ReentrantLock;

public class Request {

    private enum State {READ, WRITE}

    private final State state;

    public Request(State state){
        this.state = state;
    }
}
