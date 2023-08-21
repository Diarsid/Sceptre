package diarsid.sceptre.impl.collections;

import java.util.stream.IntStream;

import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.sceptre.impl.collections.impl.Sort;

public interface ListInt extends Ints {

    static ListInt asListInt(int... ints) {
        return new ListIntImpl(ints);
    }

    void clear();

    int get(int i);

    int last();

    int min();

    int sum();

    void add(int element);

    void add(int index, int element);

    void addAll(ListInt list);

    void set(int index, int element);

    int remove(int index);

    int removeElement(int element);

    int removeAll(ListInt elements);

    int indexOf(int element);

    int lastIndexOf(int element);

    void fillWith(int element);

    void addIntsRange(int fromIncl, int toIncl);

    void sort(Sort sort);

    IntStream stream();
}
