package diarsid.sceptre.impl.collections;

import java.util.Iterator;

import static java.lang.Math.round;

import static diarsid.support.objects.collections.CollectionUtils.getNearest;

public interface Ints {

    public static interface Elements {

        boolean hasNext();

        void next();

        int current();
    }

    int size();

    boolean isEmpty();

    boolean isNotEmpty();

    boolean contains(int value);

    boolean notContains(int value);

    Elements elements();

    String join(String delimiter);

    public static int meanSmartIgnoringZeros(ListInt ints) {
        int sum = 0;
        int zeros = 0;

        int element;
        for (int i = 0; i < ints.size(); i++ ) {
            element = ints.get(i);
            if ( element == 0 ) {
                zeros++;
            } else {
                sum = sum + element;
            }
        }

        if ( sum == 0 ) {
            return 0;
        }

        int size = ints.size();
        if ( zeros == 0 ) {
            return round( (float) sum / size);
        } else {
            if ( zeros > size / 2 ) {
                return 0;
            } else {
                return round( (float) sum / (size - zeros) );
            }
        }
    }

    public static Integer getNearestToValueFromSetExcluding(int value, MapIntInt.Keys keys) {
        if ( keys.isEmpty() ) {
            throw new IllegalArgumentException("Set<Integer> should not be empty!");
        }

        int higher = value;
        int lower = value;
        int current;

        Elements elements = keys.elements();
        while ( elements.hasNext() ) {
            elements.next();
            current = elements.current();
            if ( current == value + 1 ) {
                return current;
            }

            if ( higher == value ) {
                if ( current > higher ) {
                    higher = current;
                }
            } else {
                if ( current > value && current < higher ) {
                    higher = current;
                }
            }

            if ( lower == value ) {
                if ( current < lower ) {
                    lower = current;
                }
            } else {
                if ( current < value && current > lower ) {
                    lower = current;
                }
            }
        }

        if ( higher != value && lower != value ) {
            return getNearest(lower, value, higher);
        } else if ( higher != value ) {
            return higher;
        } else {
            return lower;
        }
    }
}
