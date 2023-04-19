package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;
import java.util.stream.IntStream;

import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListInt;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;
import static diarsid.sceptre.impl.collections.impl.Sort.REVERSE;
import static diarsid.sceptre.impl.collections.impl.Sort.STRAIGHT;

public class ListIntImpl implements ListInt {

    private static class Elements implements Ints.Elements {

        private final ListIntImpl list;
        private int i;

        public Elements(ListIntImpl list) {
            this.list = list;
            this.i = -1;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.list.size - 1;
        }

        @Override
        public Ints.Elements next() {
            this.i++;
            return this;
        }

        @Override
        public int current() {
            return this.list.array[this.i];
        }
    }

    private final Elements elements;
    private int[] array;
    private int size;

    public ListIntImpl() {
        this.array = new int[DEFAULT_ARRAY_SIZE];
        this.size = 0;
        Arrays.fill(this.array, INT_NOT_SET);
        this.elements = new Elements(this);
    }

    public ListIntImpl(int size) {
        this.array = new int[size];
        this.size = 0;
        Arrays.fill(this.array, INT_NOT_SET);
        this.elements = new Elements(this);
    }

    @Override
    public void clear() {
        Arrays.fill(this.array, INT_NOT_SET);
        this.size = 0;
    }

    @Override
    public int get(int i) {
        if ( i >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[i];
    }

    @Override
    public int last() {
        return this.array[this.size-1];
    }

    @Override
    public int min() {
        if ( this.size == 0 ) {
            throw new IndexOutOfBoundsException();
        }

        if ( this.size == 1 ) {
            return this.array[0];
        }

        int min = Integer.MAX_VALUE;
        int element;
        for ( int i = 0; i < this.size; i++ ) {
            element = this.array[i];
            if ( element < min ) {
                min = element;
            }
        }

        return min;
    }

    @Override
    public void add(int element) {
        if ( this.array.length == this.size ) {
            extend();
        }

        this.array[this.size] = element;
        this.size++;
    }

    private void extend() {
        int[] old = this.array;
        this.array = new int[this.array.length + DEFAULT_ARRAY_SIZE];
        System.arraycopy(old, 0, this.array, 0, old.length);
        Arrays.fill(this.array, this.size, this.array.length, INT_NOT_SET);
    }

    @Override
    public void add(int index, int element) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        if ( this.array.length == this.size ) {
            extend();
        }

        System.arraycopy(this.array, index, this.array, index+1, this.size-index);
        this.array[index] = element;

        this.size++;
    }

    @Override
    public void addAll(ListInt list) {
        if ( list.isEmpty() ) {
            return;
        }

        int newSize = this.size + list.size();

        while ( newSize > this.array.length ) {
            extend();
        }

        ListIntImpl listImpl = (ListIntImpl) list;

        System.arraycopy(listImpl.array, 0, this.array, this.size, listImpl.size);

        this.size = newSize;
    }

    @Override
    public void set(int index, int element) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        this.array[index] = element;
    }

    @Override
    public int remove(int index) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        int removed;

        if ( index == 0 ) {
            removed = this.array[0];
            System.arraycopy(this.array, 1, this.array, 0, this.size-1);
            this.array[this.size-1] = INT_NOT_SET;
        }
        else {
            removed = this.array[index];
            System.arraycopy(this.array, index+1, this.array, index, this.size-index-1);
            this.array[this.size-1] = INT_NOT_SET;
        }

        this.size--;

        return removed;
    }

    @Override
    public int removeElement(int element) {
        int index = this.indexOf(element);

        if ( index < 0 ) {
            return Integer.MIN_VALUE;
        }

        return this.remove(index);
    }

    @Override
    public int indexOf(int element) {
        for ( int i = 0; i < this.size; i++ ) {
            if ( this.array[i] == element ) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(int element) {
        for ( int i = this.size-1; i > -1 ; i-- ) {
            if ( this.array[i] == element ) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int size() {
        return this.size;
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
    public boolean contains(int element) {
        return this.indexOf(element) > -1;
    }

    @Override
    public boolean notContains(int element) {
        return this.indexOf(element) == -1;
    }

    @Override
    public Ints.Elements elements() {
        this.elements.i = -1;
        return this.elements;
    }

    @Override
    public void fill(int element) {
        Arrays.fill(this.array, 0, this.size, element);
    }

    @Override
    public String join(String delimiter) {
        return PrimitivesUtil.join(this.array, this.size, delimiter);
    }

    @Override
    public void sort(Sort sort) {
        Arrays.sort(this.array, 0, this.size);

        if ( sort.is(REVERSE) ) {
            int i = 0;
            int j = this.size-1;

            int swap;
            while ( i <= j ) {
                swap = this.array[i];
                this.array[i] = this.array[j];
                this.array[j] = swap;
                i++;
                j--;
            }
        }
    }

    @Override
    public IntStream stream() {
        var stream = IntStream.builder();
        for ( int i = 0; i < this.size; i++ ) {
            stream.accept(this.array[i]);
        }

        return stream.build();
    }

    @Override
    public String toString() {
        return ArraysUtil.arrayToString(this.array, this.size);
    }
}
