import AddOperators.MyAddObservable;
import AddOperators.MyAddObserver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AddOperatorsTest {

    @Test
    void testFlatMap() {
        MyAddObservable<Integer> numbers = MyAddObservable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        MyAddObservable<String> strings = numbers.flatMap(number -> MyAddObservable.create(observer -> {
            observer.onNext("Number: " + number);
            observer.onComplete();
        }));

        MyAddObserver<String> myObserver = new MyAddObserver<>("TestObserver");
        strings.subscribe(myObserver);

        List<String> expected = List.of("Number: 1", "Number: 2", "Number: 3");
        assertEquals(expected, myObserver.getReceivedValues());
        assertTrue(myObserver.isCompleted());
    }

    @Test
    void testDispose() {
        MyAddObservable<Integer> observable = MyAddObservable.create(observer -> {
            observer.onNext(1);
            observer.onComplete();
        });

        MyAddObserver<Integer> observer = new MyAddObserver<>("TestObserver");
        MyAddObservable<Integer> disposable = observable;
        disposable.subscribe(observer);
        disposable.dispose();

        assertTrue(disposable.isDisposed());
    }

    @Test
    void testOnError() {
        MyAddObservable<Integer> observable = MyAddObservable.create(observer -> {
            Throwable error = new RuntimeException("Test Exception");
            observer.onError(error);
        });

        MyAddObserver<Integer> observer = new MyAddObserver<>("TestObserver");

        observable.subscribe(observer);

        assertNotNull(observer.getThrowable());
        assertEquals("Test Exception", observer.getThrowable().getMessage());
    }
}
