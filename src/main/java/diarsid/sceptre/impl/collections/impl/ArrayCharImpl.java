package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;

import diarsid.sceptre.impl.collections.ArrayChar;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;

public class ArrayCharImpl implements ArrayChar {

    public final static char EMPTY = '=';
    private final static char NOT_AVAILABLE = '#';

    private static class ElementsImpl implements Elements {

        private final ArrayCharImpl arrayChar;
        private int i;

        public ElementsImpl(ArrayCharImpl arrayChar) {
            this.arrayChar = arrayChar;
            this.i = -1;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.arrayChar.size - 1;
        }

        @Override
        public void next() {
            this.i++;
        }

        @Override
        public char current() {
            return this.arrayChar.array[this.i];
        }
    }

    private final ElementsImpl elements;
    private char[] array;
    private int size;

    public ArrayCharImpl() {
        this.elements = new ElementsImpl(this);
        this.array = new char[DEFAULT_ARRAY_SIZE];
        this.size = -1;
        Arrays.fill(this.array, NOT_AVAILABLE);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean contains(char value) {
        return this.indexOf(value) != -1;
    }

    @Override
    public boolean notContains(char value) {
        return this.indexOf(value) == -1;
    }

    @Override
    public void setSize(int size) {
        this.size = size;

        if ( size > this.array.length ) {
            this.array = new char[this.array.length + DEFAULT_ARRAY_SIZE];
        }

        Arrays.fill(this.array, 0, this.size, EMPTY);
        Arrays.fill(this.array, this.size, this.array.length, NOT_AVAILABLE);
    }

    @Override
    public char i(int index) {
        if ( index > this.size ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[index];
    }

    @Override
    public void set(int index, char element) {
        if ( index > this.size ) {
            throw new IndexOutOfBoundsException();
        }

        this.array[index] = element;
    }

    @Override
    public int indexOf(char element) {
        for (int i = 0; i < this.size; i++ ) {
            if ( this.array[i] == element ) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void fill(char element) {
        Arrays.fill(this.array, 0, this.size, element);
    }

    @Override
    public void fillFrom(String s) {
        this.setSize(s.length());
        for ( int i = 0; i < this.size; i++ ) {
            this.array[i] = s.charAt(i);
        }
    }

    @Override
    public void clear() {
        this.size = -1;
        Arrays.fill(this.array, NOT_AVAILABLE);
    }

    @Override
    public Elements elements() {
        this.elements.i = -1;
        return this.elements;
    }
}
