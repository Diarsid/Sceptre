package diarsid.sceptre.impl.collections.impl;

import diarsid.sceptre.impl.collections.ListInt;

public class PrimitivesUtil {

    static String join(int[] array, int size, String delimiter) {
        StringBuilder sb = new StringBuilder();

        int last = size-1;
        for ( int i = 0; i < last; i++ ) {
            sb.append(array[i]).append(delimiter);
        }

        sb.append(array[last]);

        return sb.toString();
    }

    static String join(char[] array, int size, String delimiter) {
        StringBuilder sb = new StringBuilder();

        int last = size-1;
        for ( int i = 0; i < last; i++ ) {
            sb.append(array[i]).append(delimiter);
        }

        sb.append(array[last]);

        return sb.toString();
    }
}
