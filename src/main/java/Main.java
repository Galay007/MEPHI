//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.core.Observer;
//import io.reactivex.rxjava3.disposables.Disposable;
//
//import java.util.List;

//import Observation.MyObservable;
//import Observation.MyObserver;
//import ThreadControl.IOThreadScheduler;
//import ThreadControl.MyThreadControl;
//import ThreadControl.Scheduler;
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.schedulers.Schedulers;
//
//import java.util.concurrent.TimeUnit;
//
//public class Main {
//    public static void main(String[] args) {
//        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
//        Observable<Integer> mapObservable = Observable.fromIterable(numbers);
//
//        // Преобразование каждого числа в строку
//        Observable<String> stringObservable = mapObservable.map(number -> "Number: " + number);
//
//        // Подписка на Observable и вывод элементов
//        stringObservable.subscribe(
//                item -> System.out.println(item), // onNext: Обработка каждого элемента
//                error -> System.err.println("Error: " + error), // onError: Обработка ошибок
//                () -> System.out.println("Completed") // onComplete: Завершение потока
//        );
//
//
//        Observable<String> observable = Observable.create(observer -> {
//            try {
//                observer.onNext("Hello");
//                Thread.sleep(100); // Имитация работы
//                observer.onNext("World");
//                Thread.sleep(100);
//                //observer.onComplete();
//            } catch (Exception e) {
//                observer.onError(e);
//            }
//        });
//
//        Observer<String> observer1 = new Observer<String>() {
//            private Disposable disposable; // Сохраняем Disposable
//
//            @Override
//            public void onSubscribe(Disposable d) {
//                System.out.println("Observer 1: Subscribed");
//                this.disposable = d; // Сохраняем Disposable
//            }
//
//            @Override
//            public void onNext(String s) {
//                System.out.println("Observer 1: Received: " + s);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                System.err.println("Observer 1: Error: " + e.getMessage());
//            }
//
//            @Override
//            public void onComplete() {
//                System.out.println("Observer 1: Completed");
//            }
//
//            public void dispose() {
//                if (disposable != null && !disposable.isDisposed()) {
//                    disposable.dispose();
//                    System.out.println("Observer 1: Disposed");
//                }
//            }
//        };
//
//        observable.subscribe(observer1);
//        MyObservable<String> observable = MyObservable.create(observer -> {
//            try {
//                observer.onNext("Hello");
//                observer.onNext("World");
//                observer.onComplete();
//            } catch (Exception e) {
//                observer.onError(e);
//            }
//        });
//
//        MyObserver<String> observer1 = new MyObserver<>("observer1");
//
//        observable.subscribe(observer1);
//        MyObservable<String> observable2 = MyObservable.create(item -> item.onNext("Hi created observer"));
//
//        observable2.subscribe(observer1);
//
//        System.out.println("Schedulers.io() example:");
//        Observable.just("Data IO")
//                .subscribeOn(Schedulers.io()) // Подписка выполняется в io()
//                .observeOn(Schedulers.io()) // Обработка элемента выполняется в single()
//                .doOnNext(data -> System.out.println("Thread in doOnNext after observeOn: " + Thread.currentThread().getName()))
//                .doOnSubscribe(disposable -> System.out.println("Thread in doOnSubscribe: " + Thread.currentThread().getName()))
//                .subscribe(data -> System.out.println("Received data (IO): " + data + " - Thread: " + Thread.currentThread().getName()));
//        try {
//            TimeUnit.MILLISECONDS.sleep(50); // Даем время выполниться
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println("\nSchedulers.computation() example:");
//        Observable.just("Data Computation")
//                .subscribeOn(Schedulers.computation()) // Подписка выполняется в computation()
//                .observeOn(Schedulers.computation()) // Обработка элемента выполняется в io()
//                .doOnNext(data -> System.out.println("Thread in doOnNext after observeOn: " + Thread.currentThread().getName()))
//                .doOnSubscribe(disposable -> System.out.println("Thread in doOnSubscribe: " + Thread.currentThread().getName()))
//                .subscribe(data -> System.out.println("Received data (Computation): " + data + " - Thread: " + Thread.currentThread().getName()));
//
//        try {
//            TimeUnit.MILLISECONDS.sleep(50); // Даем время выполниться
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println("\nSchedulers.single() example:");
//        Observable.just("Data Single")
//                .subscribeOn(Schedulers.single()) // Подписка выполняется в single()
//                .observeOn(Schedulers.single()) // Обработка элемента выполняется в computation()
//                .doOnNext(data -> System.out.println("Thread in doOnNext after observeOn: " + Thread.currentThread().getName()))
//                .doOnSubscribe(disposable -> System.out.println("Thread in doOnSubscribe: " + Thread.currentThread().getName()))
//                .subscribe(data -> System.out.println("Received data (Single): " + data + " - Thread: " + Thread.currentThread().getName()));
//
//        try {
//            TimeUnit.MILLISECONDS.sleep(50); // Даем время выполниться
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        Scheduler ioScheduler = new IOThreadScheduler();
//        System.out.println();
//        MyThreadControl<String> observable3 = new MyThreadControl<>("Data IO");
//        observable3.subscribeOn(ioScheduler)
//                .observeOn(ioScheduler)
//                .doOnNext(data -> System.out.println("Processing: " + data + " Thread: " + Thread.currentThread().getName()))
//                .subscribe(data -> System.out.println("Received: " + data + " Thread: " + Thread.currentThread().getName()));
//
//        try {
//            TimeUnit.MILLISECONDS.sleep(50); // Даем время выполниться
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        ioScheduler.shutdown();
//
//    }
//
//
//}
