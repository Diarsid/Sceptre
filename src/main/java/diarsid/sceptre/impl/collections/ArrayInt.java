package diarsid.sceptre.impl.collections;

import java.util.stream.IntStream;

import diarsid.sceptre.impl.collections.impl.Sort;

public interface ArrayInt extends Ints {

    void setSize(int size);

    int i(int index);

    void i(int index, int element);

    int indexOf(int element);

    void fill(int element);

    void clear();

    int last();

    IntStream stream();

    void sort(Sort sort);

    void copy(ArrayInt other);
}
