package lockFreeSynchronization.unsafeRefCountedHolder;

import sun.plugin.dom.exception.InvalidStateException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SafeRefCountedHolder<T> {

    private AtomicReference<T> value;
    private AtomicInteger refCount;

    public SafeRefCountedHolder(T v) {
        value = new AtomicReference<>(v);
        refCount = new AtomicInteger(1);
    }

    public void AddRef() {
        while(true){
            int obs = refCount.get();

            if (obs == 0)
                throw new InvalidStateException("");

            if(refCount.compareAndSet(obs, obs + 1))
                return;
        }
    }

    public void ReleaseRef() {
        if (refCount.get() == 0)
            throw new InvalidStateException("");

        if (refCount.decrementAndGet() == 0) {
            //IDisposable disposable = value as IDisposable;
            value.set(null);
           /* if (disposable != null)
                disposable.Dispose();*/
        }
    }

    public T getValue() {
        if (refCount.get() == 0)
            throw new InvalidStateException("");
        return value.get();
    }

}
