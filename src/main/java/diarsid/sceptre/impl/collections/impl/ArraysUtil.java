package diarsid.sceptre.impl.collections.impl;

public final class ArraysUtil {

    private ArraysUtil() {}

    public static String arrayToString(int[] array, int size) {
        switch ( size ) {
            case 0 : return "[]";
            case 1 : return "[" + array[0] + "]";
            case 2 : return "[" + array[0] + ", " + array[1] + "]";
            case 3 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + "]";
            case 4 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3] + "]";
            case 5 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3] + ", " + array[4] + "]";
            default: {
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                int last = size-1;
                for ( int i = 0; i < last; i++ ) {
                    sb.append(array[i]).append(", ");
                }
                sb.append(array[last]).append(']');
                return sb.toString();
            }
        }
    }

    public static String arrayToString(char[] array, int size) {
        switch ( size ) {
            case 0 : return "[]";
            case 1 : return "[" + array[0] + "]";
            case 2 : return "[" + array[0] + ", " + array[1] + "]";
            case 3 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + "]";
            case 4 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3] + "]";
            case 5 : return "[" + array[0] + ", " + array[1] + ", " + array[2] + ", " + array[3] + ", " + array[4] + "]";
            default: {
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                int last = size-1;
                for ( int i = 0; i < last; i++ ) {
                    sb.append(array[i]).append(", ");
                }
                sb.append(array[last]).append(']');
                return sb.toString();
            }
        }
    }
}
