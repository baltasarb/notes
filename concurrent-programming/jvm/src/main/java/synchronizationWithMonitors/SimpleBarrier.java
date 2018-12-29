package synchronizationWithMonitors;

import utils.Timer;

public class SimpleBarrier {

    private final Object monitor;
    private final int MAX_PARTICIPANTS;
    private int currentParticipants;

    public SimpleBarrier(int participants) {
        monitor = new Object();
        MAX_PARTICIPANTS = participants;
        currentParticipants = 0;
    }

    public boolean await(long timeoutMillis) throws InterruptedException {
        synchronized (monitor){

            currentParticipants++;

            if(currentParticipants == MAX_PARTICIPANTS){
                monitor.notifyAll();
                return true;
            }

            Timer timer = new Timer(timeoutMillis);
            long timeLeftToWait = timer.getTimeLeftToWait();

            try{
                while(true){
                    monitor.wait(timeLeftToWait);

                    if(currentParticipants == MAX_PARTICIPANTS){
                        return true;
                    }

                    if(timer.timeExpired()){
                        currentParticipants--;
                        return false;
                    }

                    timeLeftToWait = timer.getTimeLeftToWait();
                }
            }catch (InterruptedException e){
                if(currentParticipants == MAX_PARTICIPANTS){
                    Thread.currentThread().interrupt();
                    return true;
                }
                currentParticipants--;
                throw e;
            }
        }
    }

}
