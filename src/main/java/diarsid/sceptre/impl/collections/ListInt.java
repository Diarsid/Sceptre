package diarsid.sceptre.impl.collections;

public interface ListInt extends Ints {

    void clear();

    int get(int i);

    void add(int element);

    void add(int index, int element);

    void set(int index, int element);

    void remove(int index);

    int indexOf(int element);

    int lastIndexOf(int element);

    void fill(int element);
}
