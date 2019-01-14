package lockFreeSynchronization.unsafeRefCountedHolder;

import sun.plugin.dom.exception.InvalidStateException;

public class UnsafeRefCountedHolder<T> {
    private T value;
    private int refCount;

    public UnsafeRefCountedHolder(T v) {
        value = v;
        refCount = 1;
    }

    public void AddRef() {
        if (refCount == 0)
            throw new InvalidStateException("");
        refCount++;
    }

    public void ReleaseRef() {
        if (refCount == 0)
            throw new InvalidStateException("");
        if (--refCount == 0) {
            /*IDisposable disposable = value as IDisposable;
            value = null;
            if (disposable != null)
                disposable.Dispose();*/
        }
    }

    public T getValue() {
        if (refCount == 0)
            throw new InvalidStateException("");
        return value;
    }
}