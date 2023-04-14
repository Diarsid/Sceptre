package diarsid.sceptre.impl.collections.impl;

import java.util.Arrays;

import diarsid.sceptre.impl.collections.SetInt;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;
import static diarsid.sceptre.impl.collections.impl.Constants.INT_NOT_SET;

public class SetIntImpl implements SetInt {

    private int[] array;
    private int size;

    public SetIntImpl() {
        this.array = new int[DEFAULT_ARRAY_SIZE];
        Arrays.fill(this.array, INT_NOT_SET);
        this.size = 0;
    }

    private void extend() {
        int[] old = this.array;
        this.array = new int[this.array.length + DEFAULT_ARRAY_SIZE];
        System.arraycopy(old, 0, this.array, 0, old.length);
        Arrays.fill(this.array, this.size, this.array.length, INT_NOT_SET);
    }

    int indexOf(int element) {
        for ( int i = 0; i < this.size; i++ ) {
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
    public Elements elements() {
        return this.elements();
    }

    @Override
    public void add(int element) {
        if ( this.size == this.array.length ) {
            this.extend();
        }

        if ( this.size == 0 ) {
            this.array[0] = element;
            this.size = 1;
            return;
        }

        int ePrev = this.array[0];

        if ( element == ePrev ) {
            return;
        }
        else if ( element < ePrev ) {
            if ( this.size == 1 ) {
                this.array[0] = element;
                this.array[1] = ePrev;
            }
            else {
                System.arraycopy(this.array, 0, this.array, 1, this.size);
                this.array[0] = element;
            }
            this.size++;
            return;
        }

        int eCurr;
        for ( int i = 1; i < this.size; i++ ) {
            eCurr = this.array[i];
            if ( element == eCurr ) {
                return;
            }
            else if ( ePrev < element && element < eCurr ) {
                System.arraycopy(this.array, i, this.array, i+1, this.size-i);
                this.array[i] = element;
                this.size++;
                return;
            }

            ePrev = eCurr;
        }

        this.array[this.size] = element;
        this.size++;
    }

    @Override
    public boolean remove(int element) {
        int index = this.indexOf(element);

        if ( index == -1 ) {
            return false;
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

        return true;
    }

    @Override
    public int lesserThan(int element) {
        int e;
        int result = Integer.MIN_VALUE;
        for ( int i = 0; i < this.size; i++ ) {
            e = this.array[i];
            if ( e < element ) {
                result = e;
            }
            else if ( e > element ) {
                return result;
            }
        }

        return result;
    }

    @Override
    public int lesserThanOrEqual(int element) {
        int e;
        int result = Integer.MIN_VALUE;
        for ( int i = 0; i < this.size; i++ ) {
            e = this.array[i];
            if ( e < element ) {
                result = e;
            }
            else if ( e == element ) {
                return element;
            }
            else if ( e > element ) {
                return result;
            }
        }

        return result;
    }

    @Override
    public int greaterThan(int element) {
        int e;
        int result = Integer.MIN_VALUE;
        for ( int i = 0; i < this.size; i++ ) {
            e = this.array[i];
            if ( e > element ) {
                return e;
            }
        }

        return result;
    }

    @Override
    public int greaterThanOrEqual(int element) {
        int e;
        int result = Integer.MIN_VALUE;
        for ( int i = 0; i < this.size; i++ ) {
            e = this.array[i];
            if ( e >= element ) {
                return e;
            }
        }

        return result;
    }

    @Override
    public int first() {
        if ( this.isEmpty() ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[0];
    }

    @Override
    public int last() {
        if ( this.isEmpty() ) {
            throw new IndexOutOfBoundsException();
        }

        return this.array[this.size-1];
    }

    @Override
    public void clear() {
        Arrays.fill(this.array, INT_NOT_SET);
        this.size = 0;
    }

    @Override
    public String join(String delimiter) {
        StringBuilder sb = new StringBuilder();

        int last = this.size-1;
        for ( int i = 0; i < last; i++ ) {
            sb.append(this.array[i]).append(delimiter);
        }

        sb.append(this.array[last]);

        return sb.toString();
    }
}
