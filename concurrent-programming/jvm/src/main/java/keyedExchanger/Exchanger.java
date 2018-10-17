package keyedExchanger;

import Utils.Timer;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class Exchanger<T> {

    private final Object monitor;
    private Request currentRequest;

    private class Request{
        private T data;
        private Request(T data){this.data = data;}
    }

    public Exchanger() {
        monitor = new Object();
        currentRequest = null;
    }

    public Optional<T> exchange(T myData, int timeout) throws InterruptedException {
        synchronized (monitor) {
            if (currentRequest != null) {
                Optional<T> dataToReturn = Optional.of(currentRequest.data);
                currentRequest.data = myData;

                //reference kept by the waiter, can be removed to allow new entries
                currentRequest = null;
                monitor.notify();
                return dataToReturn;
            }

            Timer timer = new Timer(timeout);

            if (timer.timeExpired()) {
                return Optional.empty();
            }

            Request request = currentRequest = new Request(myData);
            long timeLeftToWait = timer.getTimeLeftToWait();

            try {
                while (true) {
                    monitor.wait(timeLeftToWait);
                    if (request.data != myData) {
                        return Optional.of(request.data);
                    }
                    if (timer.timeExpired()) {
                        currentRequest = null;
                        return Optional.empty();
                    }

                    timeLeftToWait = timer.getTimeLeftToWait();
                }
            } catch (InterruptedException e) {
                if (request.data != myData) {
                    Thread.currentThread().interrupt();
                    return Optional.of(request.data);
                }
                currentRequest = null;
                throw e;
            }
        }
    }
}
