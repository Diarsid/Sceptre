package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import diarsid.sceptre.impl.collections.ArrayInt;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_AVAILABLE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;
import static diarsid.sceptre.impl.collections.impl.Sort.REVERSE;

public class ArrayIntImpl implements ArrayInt {

    private static class ElementsImpl implements Elements {

        private final ArrayIntImpl arrayInt;
        private int i;

        public ElementsImpl(ArrayIntImpl arrayInt) {
            this.arrayInt = arrayInt;
            this.i = -1;
        }

        @Override
        public boolean hasNext() {
            return this.i < this.arrayInt.size - 1;
        }

        @Override
        public Elements next() {
            this.i++;
            return this;
        }

        @Override
        public int current() {
            return this.arrayInt.array[this.i];
        }
    }

    private final ElementsImpl elements;
    private int[] array;
    private int size;

    public ArrayIntImpl() {
        this.elements = new ElementsImpl(this);
        this.array = new int[DEFAULT_ARRAY_SIZE];
        this.size = -1;
        Arrays.fill(this.array, INT_NOT_AVAILABLE);
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
    public boolean contains(int value) {
        return this.indexOf(value) != -1;
    }

    @Override
    public boolean notContains(int value) {
        return this.indexOf(value) == -1;
    }

    @Override
    public void setSize(int size) {
        this.size = size;

        if ( size > this.array.length ) {
            this.array = new int[this.array.length + DEFAULT_ARRAY_SIZE];
        }

        Arrays.fill(this.array, 0, this.size, INT_NOT_SET);
        Arrays.fill(this.array, this.size, this.array.length, INT_NOT_AVAILABLE);
    }

    @Override
    public int i(int index) {
        if ( index > this.size ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[index];
    }

    @Override
    public void i(int index, int element) {
        if ( index > this.size ) {
            throw new IndexOutOfBoundsException();
        }

        this.array[index] = element;
    }

    @Override
    public int indexOf(int element) {
        for (int i = 0; i < this.size; i++ ) {
            if ( this.array[i] == element ) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void fill(int element) {
        Arrays.fill(this.array, 0, this.size, element);
    }

    @Override
    public void clear() {
        this.size = -1;
        Arrays.fill(this.array, INT_NOT_AVAILABLE);
    }

    @Override
    public int last() {
        if ( this.size == 0 ) {
            throw new IndexOutOfBoundsException();
        }
        return this.array[this.size-1];
    }

    @Override
    public Elements elements() {
        this.elements.i = -1;
        return this.elements;
    }

    @Override
    public String join(String delimiter) {
        return null;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayIntImpl)) return false;
        ArrayIntImpl other = (ArrayIntImpl) o;
        return this.size == other.size &&
                arrayEquality(this.size, this.array, other.array);
    }

    private static boolean arrayEquality(int size, int[] array0, int[] array1) {
        int i0;
        int i1;
        for ( int i = 0; i < size; i++ ) {
            i0 = array0[i];
            i1 = array1[i];
            if ( i0 != i1 ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }
}
