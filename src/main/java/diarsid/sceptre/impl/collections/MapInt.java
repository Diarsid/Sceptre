package diarsid.sceptre.impl.collections;

public interface MapInt<T> {

    public static interface IntObjectConsumer<T> {

        void accept(int key, T value);
    }

    public static interface Entries<T> {

        boolean hasNext();

        void next();

        int currentKey();

        T currentValue();

        /*
         *  MapInt.Entries<String> entries = map.entries(); <-- call .entries() clears an iterator
         *  while ( entries.hasNext() ) {
         *      entries.next();
         *      int key = entries.currentKey();
         *      String value = entries.currentValue();
         *  }
         */
    }

    public static interface Keys extends Ints {

    }

    public static interface Values<T> {

        int size();

        boolean isEmpty();

        boolean isNotEmpty();

        boolean contains(T value);

        boolean notContains(T value);
    }

    boolean isEmpty();

    boolean isNotEmpty();

    int size();

    Keys keys();

    Values<T> values();

    Entries<T> entries();

    boolean containsKey(int key);

    boolean containsValue(T value);

    void forEach(IntObjectConsumer<T> consumer);

    T get(int key);

    void put(int key, T value);

    void remove(int key);

    void clear();
}
