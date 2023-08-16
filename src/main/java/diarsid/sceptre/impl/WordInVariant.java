package diarsid.sceptre.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import diarsid.sceptre.impl.collections.ArrayInt;
import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.SetInt;
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

    public boolean isEqualTo(Cluster cluster) {
        return this.startIndex == cluster.firstPosition() && this.length == cluster.length();
    }

    public boolean hasSameWord(WordInVariant other) {
        if ( this.length == other.length ) {
            for ( int i = 0; i < this.length; i++ ) {
                if ( this.chars[i] != other.chars[i] ) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public int charPositionOf(char c) {
        char wc;
        int wcPosition;
        for ( int i = 0; i < this.length; i++ ) {
            wc = this.chars[i];
            if ( c == wc ) {
                wcPosition = this.startIndex + i;
                return wcPosition;
            }
        }
        return Integer.MIN_VALUE;
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

    public int firstIntersectionInVariant(ListInt candidatePositions) {
        for ( int i = this.startIndex; i <= this.endIndex; i++ ) {
            if ( candidatePositions.contains(i) ) {
                return i;
            }
        }

        return -1;
    }

    public int firstIntersection(Ints ints) {
        Ints.Elements elements = ints.elements();
        int index;
        while ( elements.hasNext() ) {
            elements.next();
            index = elements.current();
            if ( index >= startIndex && index <= endIndex ) {
                return index;
            }
        }

        return -1;
    }

    public int intersections(ListInt indexes) {
        int count = 0;
        int index;
        for ( int i = 0; i < indexes.size(); i++ ) {
            index = indexes.get(i);
            if ( index < 0 ) {
                continue;
            }
            if ( this.startIndex <= index && this.endIndex >= index ) {
                count++;
            }
        }
        return count;
    }

    public void collectIntersections(ListInt indexes, ListInt collector) {
        int index;
        for ( int i = 0; i < indexes.size(); i++ ) {
            index = indexes.get(i);
            if ( index < 0 ) {
                continue;
            }
            if ( this.startIndex <= index && this.endIndex >= index ) {
                collector.add(index);
            }
        }
    }

    public boolean isEnclosedByFound(Ints indexes, int askedIndex) {
        int matchesBefore = 0;
        int matchesAfter = 0;

        boolean matchesStart = false;
        boolean matchesEnd = false;

        Ints.Elements elements = indexes.elements();
        int index;
        while ( elements.hasNext() ) {
            elements.next();
            index = elements.current();

            if ( index == askedIndex ) {
                continue;
            }

            if ( index >= startIndex && index <= endIndex ) {
                if ( index == startIndex ) {
                    matchesStart = true;
                }
                if ( index == endIndex ) {
                    matchesEnd = true;
                }

                if ( index < askedIndex ) {
                    matchesBefore++;
                }

                if ( index > askedIndex ) {
                    matchesAfter++;
                }
            }
        }

        return (matchesStart || matchesEnd)
                && matchesBefore > 0
                && matchesAfter > 0;
    }

    public boolean hasStartIn(Ints indexes) {
        return indexes.contains(this.startIndex);
    }

    public boolean hasMiddlesIn(ListInt indexes) {
        int index;
        for ( int i = 0; i < indexes.size(); i++ ) {
            index = indexes.get(i);
            if ( index > startIndex && index < endIndex ) {
                return true;
            }
        }

        return false;
    }

    public boolean hasEndIn(ListInt indexes) {
        return indexes.contains(this.endIndex);
    }

    public boolean hasIntersections(Ints indexes) {
        Ints.Elements elements = indexes.elements();
        int index;
        while ( elements.hasNext() ) {
            elements.next();
            index = elements.current();
            if ( index >= startIndex && index <= endIndex ) {
                return true;
            }
        }

        return false;
    }

    public int intersections(Ints indexes) {
        int matches = 0;
        Ints.Elements elements = indexes.elements();
        int index;
        while ( elements.hasNext() ) {
            elements.next();
            index = elements.current();
            if ( index >= startIndex && index <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(Ints indexes, int excludingPosition) {
        int matches = 0;
        Ints.Elements elements = indexes.elements();
        int index;
        while ( elements.hasNext() ) {
            elements.next();
            index = elements.current();
            if ( index == excludingPosition ) {
                continue;
            }
            if ( index >= startIndex && index <= endIndex ) {
                matches++;
            }
        }
        return matches;
    }

    public int intersections(int afterPosition, ArrayInt positions) {
        int positionInVariant;
        int matches = 0;
        for ( int i = 0; i < positions.size(); i++) {
            positionInVariant = positions.i(i);
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

    public int intersections(ArrayInt positions, int startExcludingPosition, int length) {
        int positionInVariant;
        int matches = 0;
        int endExcludingPosition = startExcludingPosition + length - 1;
        for ( int i = 0; i < positions.size(); i++) {
            positionInVariant = positions.i(i);
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
        char c;
        int last = chars.length - 1;
        for ( int i = 0; i < last; i++ ) {
            c = chars[i];
            if ( c == '_') {
                continue;
            }
            sb.append(c).append(' ');
        }
        c = chars[last];
        if ( c != '_') {
            sb.append(c);
        }
        sb.append(']');
        return sb.toString();
    }

    String charsString() {
        return string(chars);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WordInVariant)) return false;
        WordInVariant word = (WordInVariant) o;
        return index == word.index &&
                startIndex == word.startIndex &&
                endIndex == word.endIndex &&
                placing == word.placing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, startIndex, endIndex, placing);
    }
}
