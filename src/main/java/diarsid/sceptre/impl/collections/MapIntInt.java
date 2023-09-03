package diarsid.sceptre.impl.collections;

public interface MapIntInt {

    public static interface IntIntConsumer {

        void accept(int key, int value);
    }

    public static interface Entries {

        public static interface KeyValueConsumer {

            void accept(int key, int value);
        }

        boolean hasNext();

        void next();

        int currentKey();

        int currentValue();

        default void forEach(KeyValueConsumer consumer) {
            while ( this.hasNext() ) {
                this.next();
                consumer.accept(this.currentKey(), this.currentValue());
            }
        }

        /*
         *  MapIntInt.Entries entries = map.entries(); <-- call .entries() clears an iterator
         *  while ( entries.hasNext() ) {
         *      entries.next();
         *      int key = entries.currentKey();
         *      int value = entries.currentValue();
         *  }
         */
    }

    public static interface Keys extends Ints {

    }

    public static interface Values extends Ints {

    }

    boolean isEmpty();

    boolean isNotEmpty();

    int size();

    Keys keys();

    Values values();

    Entries entries();

    boolean containsKey(int key);

    boolean containsValue(int value);

    void forEach(IntIntConsumer consumer);

    int get(int key);

    void put(int key, int value);

    void putOrIncrementValueIfKeyExists(int key, int value);

    void remove(int key);

    void clear();
}
