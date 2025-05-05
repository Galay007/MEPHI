package AddOperators;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class MyAddObservable<T> implements Disposable {
    private boolean disposed = false;
    private final List<MyAddObserver<T>> observers = new ArrayList<>();
    private final Consumer<MyAddObserver<T>> onSubscribe;

    private MyAddObservable(Consumer<MyAddObserver<T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public void subscribe(MyAddObserver<T> observer) {
        if (isDisposed()) {
            return;
        }
        observers.add(observer);
        try {
            onSubscribe.accept(observer);
        } catch (Throwable e) {
            if (!isDisposed()) {
                observer.onError(e);
            }
        }
    }

    public static <T> MyAddObservable<T> create(Consumer<MyAddObserver<T>> onSubscribe) {
        return new MyAddObservable<>(onSubscribe);
    }

    public <R> MyAddObservable<R> flatMap(Function<T, MyAddObservable<R>> mapper) {
        return MyAddObservable.create(observer -> {
            MyAddObserver<T> sourceObserver = new MyAddObserver<T>(observer.getName()) {
                @Override
                public void onNext(T value) {
                    try {
                        MyAddObservable<R> mappedObservable = mapper.apply(value);
                        mappedObservable.subscribe(new MyAddObserver<R>(observer.getName()) {
                            @Override
                            public void onNext(R valueR) {
                                observer.onNext(valueR);
                            }

                            @Override
                            public void onError(Throwable error) {
                                observer.onError(error);
                            }

                            @Override
                            public void onComplete() {
                                // Do nothing.  We only want to complete the outer
                                // observable when the source observable completes.
                            }
                        });
                    } catch (Throwable e) {
                        observer.onError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    observer.onError(error);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            };

            this.subscribe(sourceObserver);
        });
    }

    @Override
    public void dispose() {
        disposed = true;
        observers.clear();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}