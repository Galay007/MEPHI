package ThreadControl;

import java.util.function.Consumer;

public class MyThreadControl<T> {
    private final T data;
    private Consumer<T> onNext;
    private Scheduler subscribeOnScheduler;
    private Scheduler observeOnScheduler;

    public MyThreadControl(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public MyThreadControl<T> subscribeOn(Scheduler scheduler) {
        this.subscribeOnScheduler = scheduler;
        return this;
    }

    public MyThreadControl<T> observeOn(Scheduler scheduler) {
        this.observeOnScheduler = scheduler;
        return this;
    }

    public MyThreadControl<T> doOnNext(Consumer<T> onNext) {
        this.onNext = onNext;
        return this;
    }

    public void subscribe(Consumer<T> consumer) {
        Runnable subscribeTask = () -> {
            try {
                System.out.println("Thread in Subscribe: " + Thread.currentThread().getName()); // Показываем поток subscribe
                if (observeOnScheduler != null) {
                    observeOnScheduler.execute(() -> {
                        System.out.println("Thread in Consumer: " + Thread.currentThread().getName()); // Показываем поток consumer
                        consumer.accept(data);

                        if (onNext != null) {
                            System.out.println("Thread in onNext: " + Thread.currentThread().getName());
                            onNext.accept(data);
                        }
                    });
                } else {
                    System.out.println("Thread in Consumer: " + Thread.currentThread().getName());
                    consumer.accept(data);
                    if (onNext != null) {
                        System.out.println("Thread in onNext: " + Thread.currentThread().getName());
                        onNext.accept(data);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (subscribeOnScheduler != null) {
            subscribeOnScheduler.execute(subscribeTask);
        } else {
            subscribeTask.run(); // Выполняем в текущем потоке, если не указан subscribeOn
        }
    }
}
