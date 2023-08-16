package diarsid.sceptre.impl.collections;

import static java.lang.Math.round;

import static diarsid.support.objects.collections.CollectionUtils.getNearest;

public interface Ints {

    public static interface Elements {

        boolean hasNext();

        Elements next();

        int current();
    }

    int size();

    default boolean hasSize(int size) {
        return this.size() == size;
    }

    boolean isEmpty();

    boolean isNotEmpty();

    boolean contains(int value);

    boolean notContains(int value);

    Elements elements();

    String join(String delimiter);

    public static boolean doesNotExist(int i) {
        return i == Integer.MIN_VALUE;
    }

    public static boolean doesExist(int i) {
        return i != Integer.MIN_VALUE;
    }

    public static int meanSmartIgnoringZeros(ListInt ints) {
        int sum = 0;
        int zeros = 0;
        int size = ints.size();

        int element;
        for (int i = 0; i < size; i++ ) {
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

        return meanSmartConsideringZeros(sum, size, zeros);
    }

    public static int meanSmartConsideringZeros(int sum, int size, int zeros) {
        if ( zeros == 0 ) {
            return round( (float) sum / size);
        }
        else {
            if ( zeros > size / 2 ) {
                return 0;
            }
            else {
                return round( (float) sum / (size - zeros) );
            }
        }
    }

    public static int getNearestToValueFromSetExcluding(int value, Ints keys) {
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

    public static int sumInts(Ints ints) {
        int sum = 0;

        Elements elements = ints.elements();
        while ( elements.hasNext() ) {
            elements.next();
            sum = sum + elements.current();
        }

        return sum;
    }

    private static int getOne(Ints ints) {
        if ( ints.isEmpty() ) {
            throw new IndexOutOfBoundsException();
        }

        return ints.elements().next().current();
    }

    private static boolean hasOne(Ints ints) {
        return ints.size() == 1;
    }

    private static boolean hasMany(Ints ints) {
        return ints.size() > 0;
    }

    public static boolean containsAnyCommonElement(Ints ints1, Ints ints2) {
        if ( ints1.isEmpty() || ints2.isEmpty() ) {
            return false;
        }

        if ( hasOne(ints1) && hasOne(ints2) ) {
            return getOne(ints1) == getOne(ints2);
        }

        if ( hasOne(ints1) && hasMany(ints2) ) {
            int i = getOne(ints1);
            return ints2.contains(i);
        }

        if ( hasOne(ints2) && hasMany(ints1) ) {
            int i = getOne(ints2);
            return ints1.contains(i);
        }

        Elements elements1 = ints1.elements();
        Elements elements2 = ints2.elements();
        while ( elements1.hasNext() ) {
            elements1.next();
            while ( elements2.hasNext() ) {
                elements2.next();
                if ( elements1.current() == elements2.current() ) {
                    return true;
                }
            }
        }

        return false;
    }
}
