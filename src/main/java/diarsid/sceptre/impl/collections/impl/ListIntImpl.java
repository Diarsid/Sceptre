package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;

import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListInt;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;

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
        public void next() {
            this.i++;
        }

        @Override
        public int current() {
            return this.list.array[this.i];
        }
    }

    private int[] array;
    private int size;
    private Elements elements;

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
    public void add(int element) {
        if ( this.array.length == this.size ) {
            int[] old = this.array;
            this.array = new int[this.array.length + DEFAULT_ARRAY_SIZE];
            System.arraycopy(old, 0, this.array, 0, old.length);
        }

        this.array[size] = element;
        this.size++;
    }

    @Override
    public void add(int index, int element) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        if ( this.array.length == this.size ) {
            int[] old = this.array;
            this.array = new int[this.array.length + DEFAULT_ARRAY_SIZE];
            System.arraycopy(old, 0, this.array, 0, old.length);
        }

        System.arraycopy(this.array, index, this.array, index+1, this.size-index);
        this.array[index] = element;

        this.size++;
    }

    @Override
    public void set(int index, int element) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        this.array[index] = element;
    }

    @Override
    public void remove(int index) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        if ( index == 0 ) {
            System.arraycopy(this.array, 1, this.array, 0, this.size-1);
            this.array[this.size-1] = INT_NOT_SET;
        }
        else {
            System.arraycopy(this.array, index+1, this.array, index, this.size-index-1);
            this.array[this.size-1] = INT_NOT_SET;
        }

        this.size--;
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
        return this.elements;
    }

    @Override
    public void fill(int element) {
        Arrays.fill(this.array, 0, this.size, element);
    }

    @Override
    public String join(String delimiter) {
        return IntsUtil.join(this.array, this.size, delimiter);
    }

    @Override
    public String toString() {
        return "[" +
                "size=" + size +
                " " +
                Arrays.toString(array) +
                ']';
    }
}
