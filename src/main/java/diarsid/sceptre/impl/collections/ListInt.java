package diarsid.sceptre.impl.collections;

import diarsid.sceptre.impl.collections.impl.Sort;

public interface ListInt extends Ints {

    void clear();

    int get(int i);

    int last();

    int min();

    void add(int element);

    void add(int index, int element);

    void addAll(ListInt list);

    void set(int index, int element);

    int remove(int index);

    int removeElement(int element);

    int indexOf(int element);

    int lastIndexOf(int element);

    void fill(int element);

    void sort(Sort sort);
}
