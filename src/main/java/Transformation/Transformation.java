package Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Transformation<T> {

    private List<T> items;

    public Transformation(List<T> items) {
        this.items = items;
    }

    public <R> Transformation<R> map(Function<T, R> mapper) {
        List<R> mappedItems = new ArrayList<>();
        for (T item : items) {
            mappedItems.add(mapper.apply(item));
        }
        return new Transformation<>(mappedItems);
    }

    public List<T> toList() {
        return new ArrayList<>(items);
    }

    public static <T> Transformation<T> from(List<T> items) {
        return new Transformation<>(items);
    }

    public Transformation<T> filter(Predicate<T> predicate) {
        List<T> filteredItems = new ArrayList<>();
        for (T item : items) {
            if (predicate.test(item)) {
                filteredItems.add(item);
            }
        }
        return new Transformation<>(filteredItems);
    }
}