package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;
import java.util.Objects;

import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.MapInt;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;

import static java.util.Objects.requireNonNull;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;

public class MapIntImpl<T> implements MapInt<T> {

    private static class EntriesIterator<T> implements Entries<T> {

        private final MapIntImpl<T> map;
        private int i;

        public EntriesIterator(MapIntImpl<T> map) {
            this.map = map;
            this.i = -1;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.map.size - 1;
        }

        @Override
        public void next() {
            this.i++;
        }

        @Override
        public int currentKey() {
            return this.map.entries[this.i].key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T currentValue() {
            return (T) this.map.entries[this.i].value;
        }

        void clear() {
            this.i = -1;
        }
    }

    private static class Entry extends PooledReusable {

        private int key;
        private Object value;

        private Entry() {
            this.key = INT_NOT_SET;
            this.value = null;
        }

        void set(int key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        protected void clearForReuse() {
            this.key = INT_NOT_SET;
            this.value = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return key == entry.key &&
                    Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "[" +
                    "key=" + key +
                    ", value=" + value +
                    ']';
        }
    }

    private static class MapKeys<T> implements Keys {

        private static class Elements implements Ints.Elements {

            private final MapIntImpl<?> map;
            private int i;

            public Elements(MapIntImpl<?> map) {
                this.map = map;
                this.i = -1;
            }

            @Override
            public boolean hasNext() {
                return this.i < this.map.size - 1;
            }

            @Override
            public Ints.Elements next() {
                this.i++;
                return this;
            }

            @Override
            public int current() {
                return this.map.entries[this.i].key;
            }
        }

        private final MapIntImpl<T> map;
        private final Elements elements;

        public MapKeys(MapIntImpl<T> map) {
            this.map = map;
            this.elements = new Elements(this.map);
        }

        @Override
        public int size() {
            return this.map.size;
        }

        @Override
        public Ints.Elements elements() {
            this.elements.i = -1;
            return this.elements;
        }

        @Override
        public boolean isEmpty() {
            return this.map.size == 0;
        }

        @Override
        public boolean isNotEmpty() {
            return this.map.size > 0;
        }

        public boolean contains(int key) {
            if ( this.map.size == 0 ) {
                return false;
            }
            else if ( this.map.size == 1 ) {
                return this.map.entries[0].key == key;
            }

            Entry entry;
            for ( int i = 0; i < this.map.size; i++ ) {
                entry = this.map.entries[i];
                if ( entry.key == key ) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean notContains(int key) {
            if ( this.map.size == 0 ) {
                return true;
            }
            else if ( this.map.size == 1 ) {
                return ! (this.map.entries[0].key == key);
            }

            Entry entry;
            for ( int i = 0; i < this.map.size; i++ ) {
                entry = this.map.entries[i];
                if ( entry.key == key ) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String join(String delimiter) {
            StringBuilder sb = new StringBuilder();

            int last = this.map.size-1;
            for ( int i = 0; i < last; i++ ) {
                sb.append(this.map.entries[i].key).append(delimiter);
            }

            sb.append(this.map.entries[last].key);

            return sb.toString();
        }
    }

    private static class MapValues<T> implements Values<T> {

        private final MapIntImpl<T> map;

        public MapValues(MapIntImpl<T> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return this.map.size;
        }

        @Override
        public boolean isEmpty() {
            return this.map.size == 0;
        }

        @Override
        public boolean isNotEmpty() {
            return this.map.size > 0;
        }

        @Override
        public boolean contains(T value) {
            requireNonNull(value);
            
            if ( this.map.size == 0 ) {
                return false;
            }
            else if ( this.map.size == 1 ) {
                return this.map.entries[0].value.equals(value);
            }

            Entry entry;
            for ( int i = 0; i < this.map.size; i++ ) {
                entry = this.map.entries[i];
                if ( entry.value.equals(value) ) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean notContains(T value) {
            requireNonNull(value);
            
            if ( this.map.size == 0 ) {
                return true;
            }
            else if ( this.map.size == 1 ) {
                return ! (this.map.entries[0].value.equals(value));
            }

            Entry entry;
            for ( int i = 0; i < this.map.size; i++ ) {
                entry = this.map.entries[i];
                if ( entry.value.equals(value) ) {
                    return false;
                }
            }

            return true;
        }
    }

    private final static GuardedPool<Entry> ENTRIES_POOL;

    static {
        ENTRIES_POOL = new GuardedPool<>(Entry::new);
    }

    private final EntriesIterator<T> iterator;
    private final MapKeys<T> keys;
    private final MapValues<T> values;
    private Entry[] entries;
    private int size;

    public MapIntImpl() {
        this.entries = new Entry[DEFAULT_ARRAY_SIZE];
        this.size = 0;
        this.iterator = new EntriesIterator<>(this);
        this.keys = new MapKeys<>(this);
        this.values = new MapValues<>(this);
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean isNotEmpty() {
        return this.size > 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Keys keys() {
        return this.keys;
    }

    @Override
    public Values<T> values() {
        return this.values;
    }

    @Override
    public Entries<T> entries() {
        this.iterator.clear();
        return this.iterator;
    }

    @Override
    public boolean containsKey(int key) {
        return this.keys.contains(key);
    }

    @Override
    public boolean containsValue(T value) {
        return this.values.contains(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(IntObjectConsumer<T> consumer) {
        if ( this.size == 0 ) {
            return;
        }
        else if ( this.size == 1 ) {
            Entry entry = this.entries[0];
            consumer.accept(entry.key, (T) entry.value);
            return;
        }

        Entry entry;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            consumer.accept(entry.key, (T) entry.value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int key) {
        if ( this.size == 0 ) {
            return null;
        }
        else if ( this.size == 1 ) {
            Entry entry = this.entries[0];
            if ( entry.key == key ) {
                return (T) entry.value;
            }
        }

        Entry entry;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            if ( entry.key == key ) {
                return (T) entry.value;
            }
        }

        return null;
    }

    @Override
    public void put(int key, T value) {
        if ( this.size == 0 ) {
            Entry next = ENTRIES_POOL.give();
            next.set(key, value);
            this.entries[0] = next;
            this.size++;
        }

        Entry entry = null;
        boolean exists = false;
        int indexOfLastKeyLessThanGiven = -1;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            if ( entry.key < key ) {
                indexOfLastKeyLessThanGiven = i;
            }
            if ( entry.key == key ) {
                exists = true;
                break;
            }
        }

        if ( exists ) {
            entry.value = value;
        }
        else {
            int indexOfFirstKeyMoreThanGiven = indexOfLastKeyLessThanGiven + 1;

            if ( indexOfFirstKeyMoreThanGiven == this.entries.length ) {
                this.extendArray();

                Entry next = ENTRIES_POOL.give();
                next.set(key, value);
                this.entries[indexOfFirstKeyMoreThanGiven] = next;
            }
            else {
                if ( this.entries.length == this.size ) {
                    this.extendArray();
                }

                Entry insert = ENTRIES_POOL.give();
                insert.set(key, value);

                System.arraycopy(
                        this.entries, indexOfFirstKeyMoreThanGiven,
                        this.entries, indexOfFirstKeyMoreThanGiven + 1,
                        this.size - indexOfFirstKeyMoreThanGiven);

                this.entries[indexOfFirstKeyMoreThanGiven] = insert;
            }

            this.size++;
        }
    }

    private void extendArray() {
        Entry[] old = this.entries;
        this.entries = new Entry[this.entries.length + DEFAULT_ARRAY_SIZE];
        System.arraycopy(old, 0, this.entries, 0, old.length);
    }

    @Override
    public void remove(int key) {
        if ( this.size == 0 ) {
            return;
        }
        else if ( this.size == 1 ) {
            Entry entry = this.entries[0];
            if ( entry.key == key ) {
                this.entries[0] = null;
                ENTRIES_POOL.takeBack(entry);
                this.size--;
            }
            return;
        }

        Entry entry = null;
        int index = -1;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            if ( entry.key == key ) {
                index = i;
                break;
            }
        }

        if ( index == -1 ) {
            return;
        }

        int last = this.size - 1;
        if ( index == last ) {
            this.entries[last] = null;
        }
        else {
            this.entries[index] = null;
            System.arraycopy(
                    this.entries, index + 1,
                    this.entries, index,
                    this.size - index - 1);
            this.entries[last] = null;
        }

        this.size--;

        ENTRIES_POOL.takeBack(entry);
    }

    @Override
    public void clear() {
        if ( this.size == 0 ) {
            return;
        }

        for ( int i = 0; i < this.size; i++ ) {
            ENTRIES_POOL.takeBack(this.entries[i]);
        }
        Arrays.fill(this.entries, null);

        this.size = 0;
    }
}
