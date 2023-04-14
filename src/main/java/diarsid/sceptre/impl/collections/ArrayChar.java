package diarsid.sceptre.impl.collections;

public interface ArrayChar {

    public static interface Elements {

        boolean hasNext();

        void next();

        char current();
    }

    int size();

    boolean contains(char value);

    boolean notContains(char value);

    void setSize(int size);

    char i(int index);

    void set(int index, char element);

    int indexOf(char element);

    void fill(char element);

    void fillFrom(String s);

    void clear();

    Elements elements();
}
