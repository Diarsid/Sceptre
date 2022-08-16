package diarsid.sceptre.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import diarsid.support.objects.CommonEnum;
import diarsid.support.objects.PooledReusable;

public class WordInVariant extends PooledReusable {

    public static enum Placing implements CommonEnum<Placing> {
        INDEPENDENT,
        DEPENDENT
    }

    public final static int INITIAL_LENGTH = 10;
    public final static int NOT_SET = -1;
    public final static int ZERO = 0;
    public final static char EMPTY = '_';

    char[] chars;
    char[] charsInVariant;
    int index;
    int startIndex;
    int endIndex;
    int length;
    boolean completed;
    Placing placing;

    WordInVariant() {
        chars = new char[INITIAL_LENGTH];
        Arrays.fill(chars, EMPTY);
        charsInVariant = new char[INITIAL_LENGTH];
        Arrays.fill(charsInVariant, EMPTY);
        startIndex = NOT_SET;
        endIndex = NOT_SET;
        length = ZERO;
        completed = false;
        placing = null;
        index = NOT_SET;
    }

    public void set(int i, char c) {
        if ( completed ) {
            throw new IllegalArgumentException();
        }

        if ( length == 0 ) {
            startIndex = i;
        }

        if ( length == chars.length ) {
            char[] swap = chars;
            chars = new char[swap.length + INITIAL_LENGTH];

            Arrays.fill(chars, EMPTY);
            System.arraycopy(swap, 0, chars, 0, length);
        }

        if ( i >= charsInVariant.length ) {
            if ( length == 0 ) {
                int size = charsInVariant.length + INITIAL_LENGTH;

                while ( size <= i ) {
                    size = size + INITIAL_LENGTH;
                }

                charsInVariant = new char[size];

                Arrays.fill(charsInVariant, EMPTY);
            }
            else {
                char[] swap = charsInVariant;

                int size = swap.length + INITIAL_LENGTH;

                while ( size <= i ) {
                    size = size + INITIAL_LENGTH;
                }

                charsInVariant = new char[size];

                Arrays.fill(charsInVariant, EMPTY);
                System.arraycopy(swap, startIndex, charsInVariant, startIndex, length);
            }

        }

        endIndex = i;
        charsInVariant[i] = c;
        chars[length] = c;
        length++;
    }

    public void complete() {
        completed = true;
    }

    public boolean hasSameWord(WordInVariant other) {
        return Arrays.equals(this.chars, other.chars);
    }

    public boolean hasIndex(int index) {
        return index >= startIndex && index <= endIndex;
    }

    public int intersectsWithRange(int otherStartIndex, int otherLength) {
        int otherEndIndex = otherStartIndex + otherLength - 1;

        if ( otherStartIndex > this.endIndex ) {
            return 0;
        }

        if ( otherEndIndex < this.startIndex ) {
            return 0;
        }

        int matches = 0;
        for ( int i = otherStartIndex; i <= otherEndIndex; i++) {
            if ( i >= this.startIndex && i <= this.endIndex ) {
                matches++;
            }
        }

        return matches;
    }

    public boolean containsRange(int rangeStartIndex, int rangeLength) {
        int rangeEndIndex = rangeStartIndex + rangeLength - 1;

        return this.startIndex <= rangeStartIndex && this.endIndex >= rangeEndIndex;
    }

    public boolean containsStartEndOrEnclosed(int rangeStartIndex, int rangeLength) {
        int rangeEndIndex = rangeStartIndex + rangeLength - 1;

        boolean containsStart = this.startIndex <= rangeStartIndex && this.endIndex >= rangeStartIndex;
        boolean containsEnd = this.startIndex <= rangeEndIndex && this.endIndex >= rangeEndIndex;
        boolean isEnclosedByRange = this.startIndex >= rangeStartIndex && this.endIndex<= rangeEndIndex;

        return containsStart || containsEnd || isEnclosedByRange;
    }

    public int countContains(List<Integer> indexes) {
        int count = 0;
        for ( int index : indexes ) {
            if ( index < 0 ) {
                continue;
            }
            if ( this.startIndex <= index && this.endIndex >= index ) {
                count++;
            }
        }
        return count;
    }

    public int countContains(int[] indexes) {
        int count = 0;
        for ( int index : indexes ) {
            if ( index < 0 ) {
                continue;
            }
            if ( this.startIndex <= index && this.endIndex >= index ) {
                count++;
            }
        }
        return count;
    }

    public boolean hasStartIn(Set<Integer> indexes) {
        return indexes.contains(this.startIndex);
    }

    public int intersections(Set<Integer> indexes) {
        int matches = 0;
        for ( int index : indexes ) {
            if ( index >= startIndex && index <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(Set<Integer> indexes, int excludingPosition) {
        int matches = 0;
        for ( int index : indexes ) {
            if ( index == excludingPosition ) {
                continue;
            }
            if ( index >= startIndex && index <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(int afterPosition, int[] positions) {
        int positionInVariant;
        int matches = 0;
        for ( int i = 0; i < positions.length; i++) {
            positionInVariant = positions[i];
            if ( positionInVariant < 0 ) {
                continue;
            }
            if ( afterPosition >= positionInVariant ) {
                continue;
            }
            if ( positionInVariant >= startIndex && positionInVariant <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(int[] positions, int excludingPosition) {
        int positionInVariant;
        int matches = 0;
        for ( int i = 0; i < positions.length; i++) {
            positionInVariant = positions[i];
            if ( positionInVariant < 0 ) {
                continue;
            }
            if ( excludingPosition == positionInVariant ) {
                continue;
            }
            if ( positionInVariant >= startIndex && positionInVariant <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(int[] positions, int startExcludingPosition, int length) {
        int positionInVariant;
        int matches = 0;
        int endExcludingPosition = startExcludingPosition + length - 1;
        for ( int i = 0; i < positions.length; i++) {
            positionInVariant = positions[i];
            if ( positionInVariant < 0 ) {
                continue;
            }
            if ( positionInVariant >= startExcludingPosition && positionInVariant <= endExcludingPosition ) {
                continue;
            }
            if ( positionInVariant >= startIndex && positionInVariant <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(int[] positions) {
        int positionInVariant;
        int matches = 0;
        for ( int i = 0; i < positions.length; i++) {
            positionInVariant = positions[i];
            if ( positionInVariant < 0 ) {
                continue;
            }
            if ( positionInVariant >= startIndex && positionInVariant <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    @Override
    protected void clearForReuse() {
        Arrays.fill(chars, EMPTY);
        Arrays.fill(charsInVariant, EMPTY);
        startIndex = NOT_SET;
        endIndex = NOT_SET;
        length = ZERO;
        completed = false;
        placing = null;
        index = NOT_SET;
    }

    private static String string(char[] chars) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for ( int i = 0; i < chars.length - 1; i++ ) {
            sb.append(chars[i]).append(' ');
        }
        sb.append(chars[chars.length-1]).append(']');
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Word{" + string(chars) + " " + string(charsInVariant) +
                ", start=" + startIndex +
                ", end=" + endIndex +
                ", length=" + length +
                ", completed=" + completed +
                '}';
    }
}
