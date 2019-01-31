package lockFreeSynchronization.unsafeSpinCompletion;

public class UnsafeSpinCompletion {

    private final int OPEN = -1;
    private int state = 0;

    public void waitE() throws InterruptedException {
        if(state == OPEN){
            return;
        }

        while(state == 0){
            Thread.sleep(0);
        }

        if(state!=OPEN){
            state--;
        }
    }

    public void complete(){
        if(state != OPEN){
            state++;
        }

    }

    public void completeAll(){
        state = OPEN;
    }

}
