package diarsid.sceptre.impl.collections;

public interface ListChar extends Chars {

    public static interface Elements {

        boolean hasNext();

        void next();

        char current();
    }

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
