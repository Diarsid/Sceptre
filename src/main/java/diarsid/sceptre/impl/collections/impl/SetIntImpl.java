package diarsid.sceptre.impl.collections.impl;

import diarsid.sceptre.impl.collections.SetInt;

import static diarsid.sceptre.impl.collections.impl.Constants.DEFAULT_ARRAY_SIZE;

public class SetIntImpl implements SetInt {

    int[] array;
    int size;

    public SetIntImpl() {
        this.array = new int[DEFAULT_ARRAY_SIZE];
        this.size = 0;
    }

    int indexOf(int element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isNotEmpty() {
        return false;
    }

    @Override
    public boolean contains(int element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean notContains(int value) {
        return false;
    }

    @Override
    public Elements elements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(int element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(int element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lesserThan(int element) {
        return 0;
    }

    @Override
    public int lesserThanOrEqual(int element) {
        return 0;
    }

    @Override
    public int greaterThan(int element) {
        return 0;
    }

    @Override
    public int greaterThanOrEqual(int element) {
        return 0;
    }

    @Override
    public int first() {
        return 0;
    }

    @Override
    public int last() {
        return 0;
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
