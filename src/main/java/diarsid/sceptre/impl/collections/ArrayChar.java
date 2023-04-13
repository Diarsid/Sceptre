package diarsid.sceptre.impl.collections;

public interface ArrayChar {

    int size();

    boolean contains(char value);

    boolean notContains(char value);

    void setSize(int size);

    char get(int index);

    void set(int index, char element);

    int indexOf(char element);

    void fill(char element);

    void clear();
}
