package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;
import java.util.Objects;

import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.MapIntInt;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;

public class MapIntIntImpl implements MapIntInt {

    private static class EntriesIterator implements Entries {

        private final MapIntIntImpl map;
        private int i;

        public EntriesIterator(MapIntIntImpl map) {
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
        public int currentValue() {
            return this.map.entries[this.i].value;
        }

        void clear() {
            this.i = -1;
        }
    }

    private static class Entry extends PooledReusable {

        private int key;
        private int value;

        private Entry() {
            this.key = INT_NOT_SET;
            this.value = INT_NOT_SET;
        }

        void set(int key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        protected void clearForReuse() {
            this.key = INT_NOT_SET;
            this.value = INT_NOT_SET;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry entry = (Entry) o;
            return key == entry.key &&
                    value == entry.value;
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

    private static abstract class AbstractInts implements Ints {

        protected final MapIntIntImpl map;

        public AbstractInts(MapIntIntImpl map) {
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
    }

    private static class MapKeys extends AbstractInts implements Keys {

        private static class Elements implements Ints.Elements {

            private final MapIntIntImpl map;
            private int i;

            public Elements(MapIntIntImpl map) {
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

        private final Elements elements;

        public MapKeys(MapIntIntImpl map) {
            super(map);
            this.elements = new Elements(map);
        }

        public boolean contains(int key) {
            if ( this.map.size == 0 ) {
                return false;
            }
            else if ( this.map.size == 1 ) {
                return this.map.entries[0].key == key;
            }

            Entry entry;
            for ( int i = 0; i < super.map.size; i++ ) {
                entry = super.map.entries[i];
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
            for ( int i = 0; i < super.map.size; i++ ) {
                entry = super.map.entries[i];
                if ( entry.key == key ) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Ints.Elements elements() {
            this.elements.i = -1;
            return this.elements;
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

    private static class MapValues extends AbstractInts implements Values {

        private static class Elements implements Ints.Elements {

            private final MapIntIntImpl map;
            private int i;

            public Elements(MapIntIntImpl map) {
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
                return this.map.entries[this.i].value;
            }
        }

        private final Elements elements;

        public MapValues(MapIntIntImpl map) {
            super(map);
            this.elements = new Elements(map);
        }

        @Override
        public Ints.Elements elements() {
            this.elements.i = -1;
            return this.elements;
        }

        @Override
        public boolean contains(int value) {
            if ( this.map.size == 0 ) {
                return false;
            }
            else if ( this.map.size == 1 ) {
                return this.map.entries[0].value == value;
            }

            Entry entry;
            for ( int i = 0; i < super.map.size; i++ ) {
                entry = super.map.entries[i];
                if ( entry.value == value ) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean notContains(int value) {
            if ( this.map.size == 0 ) {
                return true;
            }
            else if ( this.map.size == 1 ) {
                return ! (this.map.entries[0].value == value);
            }

            Entry entry;
            for ( int i = 0; i < super.map.size; i++ ) {
                entry = super.map.entries[i];
                if ( entry.value == value ) {
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
                sb.append(this.map.entries[i].value).append(delimiter);
            }

            sb.append(this.map.entries[last].value);

            return sb.toString();
        }
    }

    private final static GuardedPool<Entry> ENTRIES_POOL;

    static {
        ENTRIES_POOL = new GuardedPool<>(Entry::new);
    }

    private final EntriesIterator iterator;
    private final MapKeys keys;
    private final MapValues values;
    private Entry[] entries;
    private int size;

    public MapIntIntImpl() {
        this.entries = new Entry[DEFAULT_ARRAY_SIZE];
        this.size = 0;
        this.iterator = new EntriesIterator(this);
        this.keys = new MapKeys(this);
        this.values = new MapValues(this);
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
    public Values values() {
        return this.values;
    }

    @Override
    public Entries entries() {
        this.iterator.clear();
        return this.iterator;
    }

    @Override
    public boolean containsKey(int key) {
        return this.keys.contains(key);
    }

    @Override
    public boolean containsValue(int value) {
        return this.values.contains(value);
    }

    @Override
    public void forEach(IntIntConsumer consumer) {
        if ( this.size == 0 ) {
            return;
        }
        else if ( this.size == 1 ) {
            Entry entry = this.entries[0];
            consumer.accept(entry.key, entry.value);
            return;
        }

        Entry entry;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            consumer.accept(entry.key, entry.value);
        }
    }

    @Override
    public int get(int key) {
        if ( this.size == 0 ) {
            return Integer.MIN_VALUE;
        }
        else if ( this.size == 1 ) {
            Entry entry = this.entries[0];
            if ( entry.key == key ) {
                return entry.value;
            }
        }

        Entry entry;
        for ( int i = 0; i < this.size; i++ ) {
            entry = this.entries[i];
            if ( entry.key == key ) {
                return entry.value;
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public void put(int key, int value) {
        if ( this.size == 0 ) {
            Entry next = ENTRIES_POOL.give();
            next.set(key, value);
            this.entries[0] = next;
            this.size++;
            return;
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

    @Override
    public void putOrIncrementValueIfKeyExists(int key, int value) {
        if ( this.size == 0 ) {
            Entry next = ENTRIES_POOL.give();
            next.set(key, value);
            this.entries[0] = next;
            this.size++;
            return;
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
            entry.value = entry.value + 1;
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
