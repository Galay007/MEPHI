package Observation;
import io.reactivex.rxjava3.disposables.Disposable;

import java.util.*;
import java.util.function.Consumer;


public class MyObservable<T> implements Disposable {

    private final List<MyObserver<T>> observers = new ArrayList<>();
    private final Consumer<MyObserver<T>> onSubscribe;

    private MyObservable(Consumer<MyObserver<T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public void subscribe(MyObserver<T> observer) {
        observers.add(observer);
        try {
            onSubscribe.accept(observer);
        } catch (Throwable e) {
            observer.onError(e);
        }
    }

    public static <T> MyObservable<T> create(Consumer<MyObserver<T>> onSubscribe) {
        return new MyObservable<>(onSubscribe);
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isDisposed() {
        return false;
    }
}