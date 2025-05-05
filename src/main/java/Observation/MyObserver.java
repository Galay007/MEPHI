package Observation;

import java.util.*;

public class MyObserver<T> implements Observer<T> {
    private final String name;
    private final List<T> receivedValues = new ArrayList<>();
    private boolean completed = false;
    private Throwable throwable;

    public MyObserver(String name) {
        this.name = name;
    }

    @Override
    public void onNext(T item) {
        System.out.println(name + ": Received: " + item);
        receivedValues.add(item);
        completed = true;
    }

    @Override
    public void onError(Throwable t) {
        throwable = t;
    }

    @Override
    public void onComplete() {
        System.out.println(name + ": Completed");
    }

    public List<T> getReceivedValues() {
        return receivedValues;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isCompleted() {
        return completed;
    }
}