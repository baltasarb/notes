package keyedExchangerReentrantLock;

import java.util.concurrent.locks.Condition;

class ExchangerReentrantLock<T> {
    T data;
    Condition condition;

    ExchangerReentrantLock(T data, Condition condition) {
        this.data = data;
        this.condition = condition;
    }
}