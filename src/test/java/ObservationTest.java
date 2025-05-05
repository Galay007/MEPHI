import Observation.MyObservable;
import Observation.MyObserver;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObservationTest {

    @Test
    void testMyObservableEmissions() {
        MyObservable<String> observable = MyObservable.create(observer -> {
            try {
                observer.onNext("Hello");
                observer.onNext("World");
                observer.onComplete();
            } catch (Exception e) {
                observer.onError(e);
            }
        });

        MyObserver<String> observer1 = new MyObserver<>("observer1");

        observable.subscribe(observer1);

        List<String> receivedValues = observer1.getReceivedValues();

        List<String> expectedValues = List.of("Hello", "World");
        assertEquals(expectedValues, receivedValues);

        assertEquals(true, observer1.isCompleted());

    }

    @Test
    void testMyObservableEmissionsSecondObservable() {

        MyObservable<String> observable2 = MyObservable.create(item -> item.onNext("Hi created observer"));

        MyObserver<String> observer2 = new MyObserver<>("observer2");

        observable2.subscribe(observer2);

        List<String> receivedValues = observer2.getReceivedValues();

        List<String> expectedValues = List.of("Hi created observer");
        assertEquals(expectedValues, receivedValues);

        assertEquals(true, observer2.isCompleted());


    }
}