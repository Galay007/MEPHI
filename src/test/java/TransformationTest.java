import Transformation.Transformation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransformationTest {

    @Test
    void map_integersToStrings_correctMapping() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        Transformation<Integer> observable = Transformation.from(numbers);
        Transformation<String> stringObservable = observable.map(number -> "Number: " + number);
        List<String> stringList = stringObservable.toList();

        List<String> expected = List.of("Number: 1", "Number: 2", "Number: 3", "Number: 4", "Number: 5");
        assertEquals(expected, stringList);
    }

    @Test
    void map_squareAndToString_correctTransformation() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        Transformation<String> complexObservable = Transformation.from(numbers)
                .map(x -> x * x)
                .map(x -> x.toString());
        List<String> complexList = complexObservable.toList();

        List<String> expected = List.of("1", "4", "9", "16", "25");
        assertEquals(expected, complexList);
    }

    @Test
    void filter_oddNumbersToString_correctCombination() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Transformation<String> filterNumbers = Transformation.from(numbers)
                .filter(number -> number % 2 != 0)
                .map(number -> "Odd: " + number);
        List<String> stringList = filterNumbers.toList();

        List<String> expected = List.of("Odd: 1", "Odd: 3", "Odd: 5", "Odd: 7", "Odd: 9");
        assertEquals(expected, stringList);
    }

    @Test
    void filter_greaterThanFive_correctFiltration() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Transformation<Integer> filterNumbers = Transformation.from(numbers);
        Transformation<Integer> greaterThanFiveObservable = filterNumbers.filter(number -> number > 5);
        List<Integer> greaterThanFive = greaterThanFiveObservable.toList();

        List<Integer> expected = List.of(6, 7, 8, 9, 10);
        assertEquals(expected, greaterThanFive);
    }
}