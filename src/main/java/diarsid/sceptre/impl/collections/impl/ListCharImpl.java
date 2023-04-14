package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;

import diarsid.sceptre.impl.collections.ListChar;

import static diarsid.sceptre.impl.collections.impl.Constants.CHAR_NOT_SET;
import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;

public class ListCharImpl implements ListChar {

    private static class Elements implements ListChar.Elements {

        private final ListCharImpl list;
        private int i;

        public Elements(ListCharImpl list) {
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
        public char current() {
            return this.list.array[this.i];
        }
    }

    private final Elements elements;
    private char[] array;
    private int size;

    public ListCharImpl() {
        this.array = new char[DEFAULT_ARRAY_SIZE];
        this.size = 0;
        Arrays.fill(this.array, CHAR_NOT_SET);
        this.elements = new Elements(this);
    }

    public ListCharImpl(int size) {
        this.array = new char[size];
        this.size = 0;
        Arrays.fill(this.array, CHAR_NOT_SET);
        this.elements = new Elements(this);
    }

    @Override
    public void clear() {
        Arrays.fill(this.array, CHAR_NOT_SET);
        this.size = 0;
    }

    @Override
    public char get(int i) {
        if ( i >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[i];
    }

    @Override
    public void add(char element) {
        if ( this.array.length == this.size ) {
            char[] old = this.array;
            this.array = new char[this.array.length + DEFAULT_ARRAY_SIZE];
            System.arraycopy(old, 0, this.array, 0, old.length);
        }

        this.array[size] = element;
        this.size++;
    }

    @Override
    public void add(int index, char element) {
        if ( index >= this.size ) {
            throw new IndexOutOfBoundsException();
        }

        if ( this.array.length == this.size ) {
            char[] old = this.array;
            this.array = new char[this.array.length + DEFAULT_ARRAY_SIZE];
            System.arraycopy(old, 0, this.array, 0, old.length);
        }

        System.arraycopy(this.array, index, this.array, index+1, this.size-index);
        this.array[index] = element;

        this.size++;
    }

    @Override
    public void set(int index, char element) {
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
            this.array[this.size-1] = CHAR_NOT_SET;
        }
        else {
            System.arraycopy(this.array, index+1, this.array, index, this.size-index-1);
            this.array[this.size-1] = CHAR_NOT_SET;
        }

        this.size--;
    }

    @Override
    public void remove(char element) {
        int index = this.indexOf(element);
        if ( index < 0 ) {
            return;
        }

        this.remove(index);
    }

    @Override
    public int indexOf(char element) {
        for ( int i = 0; i < this.size; i++ ) {
            if ( this.array[i] == element ) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(char element) {
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
    public boolean contains(char element) {
        return this.indexOf(element) > -1;
    }

    @Override
    public boolean notContains(char element) {
        return this.indexOf(element) == -1;
    }

    @Override
    public Elements elements() {
        return this.elements;
    }

    @Override
    public void fill(char element) {
        Arrays.fill(this.array, 0, this.size, element);
    }

    @Override
    public String join(String delimiter) {
        return PrimitivesUtil.join(this.array, this.size, delimiter);
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
