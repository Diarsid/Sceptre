package diarsid.sceptre.impl.collections;

public interface ListChar {

    public static interface Elements {

        boolean hasNext();

        void next();

        char current();
    }

    int size();

    boolean isEmpty();

    boolean isNotEmpty();

    boolean contains(char value);

    boolean notContains(char value);

    Elements elements();

    String join(String delimiter);

    void clear();

    char get(int i);

    void add(char element);

    void add(int index, char element);

    void set(int index, char element);

    char remove(int index);

    void remove(char c);

    int indexOf(char element);

    int lastIndexOf(char element);

    void fill(char element);
}
