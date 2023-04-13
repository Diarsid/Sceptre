package diarsid.sceptre.impl.collections.impl;

public class IntsUtil {

    static String join(int[] array, int size, String delimiter) {
        StringBuilder sb = new StringBuilder();

        int last = size-1;
        for ( int i = 0; i < last; i++ ) {
            sb.append(array[i]).append(delimiter);
        }

        sb.append(array[last]);

        return sb.toString();
    }
}
