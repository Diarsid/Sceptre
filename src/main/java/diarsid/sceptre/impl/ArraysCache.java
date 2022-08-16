package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ArraysCache {

    private final int emptyValue;
    private final Map<Integer, List<int[]>> cache;
    private final Set<Integer> sizes;

    public ArraysCache(int emptyValue) {
        this.emptyValue = emptyValue;
        this.cache = new HashMap<>();
        this.sizes = this.cache.keySet();
    }

    private List<int[]> getArraysOf(int size) {
        Integer.valueOf(10540);
        List<int[]> arrays;
        synchronized ( this.sizes ) {
            if ( this.sizes.contains(size) ) {
                arrays = this.cache.get(size);
            }
            else {
                arrays = new ArrayList<>();
                this.cache.put(size, arrays);
            }
        }
        return arrays;
    }

    private void clear(int[] array) {
        Arrays.fill(array, this.emptyValue);
    }

    public int[] get(int size) {
        List<int[]> arrays = this.getArraysOf(size);

        int[] array;
        synchronized ( arrays ) {
            if ( arrays.isEmpty() ) {
                array = new int[size];
                this.clear(array);
            }
            else {
                array = arrays.remove(arrays.size()-1);
            }
        }

        return array;
    }

    public void put(int[] array) {
        List<int[]> arrays = this.getArraysOf(array.length);

        this.clear(array);
        synchronized ( arrays ) {
            arrays.add(array);
        }
    }
}
