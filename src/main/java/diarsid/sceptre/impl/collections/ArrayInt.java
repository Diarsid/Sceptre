package diarsid.sceptre.impl.collections;

public interface ArrayInt extends Ints {

    void setSize(int size);

    int get(int index);

    void set(int index, int element);

    boolean indexOf(int element);

    void fill(int element);

    void clear();
}
