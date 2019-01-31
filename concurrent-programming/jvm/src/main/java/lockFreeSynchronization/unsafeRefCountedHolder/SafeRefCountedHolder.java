package lockFreeSynchronization.unsafeRefCountedHolder;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeRefCountedHolder<T> {

    private T value;
    private AtomicInteger refCount;

    public SafeRefCountedHolder(T v) {
        value = v;
        refCount = new AtomicInteger(1);
    }

    public void AddRef() {
        if (refCount.get() == 0)
            throw new InvalidStateException("");
        refCount.incrementAndGet();
    }

    public void ReleaseRef() {
        int observedRefCount = refCount.get();

        if(observedRefCount == 0){
            throw new InvalidStateException("");
        }

        if(observedRefCount - 1 == 0){
            if(refCount.compareAndSet(observedRefCount, observedRefCount - 1)){
                IDisposable disposable = (IDisposable) value;
                value = null;
                if (disposable != null)
                    disposable.dispose();
            }
        }
    }

    public T getValue() {
        if (refCount.get() == 0)
            throw new InvalidStateException("");
        return value;
    }

    //Placeholder interface to allow compilation
    private interface IDisposable {
        void dispose();
    }
}
