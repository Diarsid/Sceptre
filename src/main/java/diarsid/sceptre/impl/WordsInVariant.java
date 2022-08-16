package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;
import diarsid.support.objects.StatefulClearable;

import static diarsid.sceptre.impl.WordInVariant.Placing.INDEPENDENT;

public class WordsInVariant implements StatefulClearable {

    public static class WordsInRange extends PooledReusable {

        private static final int UNINITIALIZED = -1;

        private int rangeStartIndex;
        private int rangeLength;
        private final List<Integer> rangeIndexes;
        private final List<WordInVariant> found;
        private int length;

        public WordsInRange() {
            this.rangeStartIndex = UNINITIALIZED;
            this.rangeLength = UNINITIALIZED;
            this.rangeIndexes = new ArrayList<>();
            this.found = new ArrayList<>();
            this.length = 0;
        }

        WordInVariant first() {
            return this.found.get(0);
        }

        WordInVariant get(int i) {
            return this.found.get(i);
        }

        void add(WordInVariant word) {
            this.found.add(word);
            this.length = this.length + word.length;
        }

        @Override
        public void clearForReuse() {
            this.rangeStartIndex = UNINITIALIZED;
            this.rangeLength = UNINITIALIZED;
            this.rangeIndexes.clear();
            this.found.clear();
            this.length = 0;
        }

        public int independents() {
            if ( this.areEmpty() ) {
                return 0;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).placing.is(INDEPENDENT) ? 1 : 0;
            }

            int count = 0;
            for ( WordInVariant word : found ) {
                count = count + (word.placing.is(INDEPENDENT) ? 1 : 0);
            }

            return count;
        }

        private void addToRangeIndexes(int[] positions) {
            for ( int i = 0; i < positions.length; i++) {
                this.rangeIndexes.add(i);
            }
        }

        public boolean areEmpty() {
            return this.found.isEmpty();
        }

        public boolean areNotEmpty() {
            return ! this.found.isEmpty();
        }

        public int count() {
            return this.found.size();
        }

        public int intersections(Set<Integer> indexes) {
            if ( this.areEmpty() ) {
                return 0;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).intersections(indexes);
            }

            int intersections = 0;
            for ( WordInVariant word : found ) {
                intersections = intersections + word.intersections(indexes);
            }

            return intersections;
        }

        public int intersections(int[] positions, int startExcludingPosition, int length) {
            if ( this.areEmpty() ) {
                return 0;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).intersections(positions, startExcludingPosition, length);
            }

            int intersections = 0;
            for ( WordInVariant word : found ) {
                intersections = intersections + word.intersections(positions, startExcludingPosition, length);
            }

            return intersections;
        }

        public boolean hasStartIn(Set<Integer> indexes) {
            if ( this.areEmpty() ) {
                return false;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).hasStartIn(indexes);
            }

            for ( WordInVariant word : found ) {
                if ( word.hasStartIn(indexes) ) {
                    return true;
                }
            }
            return false;
        }

        public int length() {
            return this.length;
        }
    }

    private final GuardedPool<WordInVariant> wordsPool;
    private final GuardedPool<WordsInRange> wordsInRangePool;
    private final List<WordsInRange> usedWordsInRanges;
    final List<WordInVariant> all;
    int variantLength;

    public WordsInVariant(GuardedPool<WordInVariant> wordsPool, GuardedPool<WordsInRange> wordsInRangePool) {
        this.wordsPool = wordsPool;
        this.wordsInRangePool = wordsInRangePool;
        this.all = new ArrayList<>();
        this.usedWordsInRanges = new ArrayList<>();
    }

    public WordInVariant next(WordInVariant.Placing placing) {
        WordInVariant wordInVariant = wordsPool.give();
        wordInVariant.placing = placing;
        wordInVariant.index = all.size();
        all.add(wordInVariant);
        return wordInVariant;
    }

    @Override
    public void clear() {
        wordsPool.takeBackAll(all);
        wordsInRangePool.takeBackAll(usedWordsInRanges);
        all.clear();
        usedWordsInRanges.clear();
        this.variantLength = -1;
    }

    void complete() {

    }

    public WordInVariant wordOf(int indexInVariant) {
        for ( WordInVariant word : all ) {
            if ( word.hasIndex(indexInVariant) ) {
                return word;
            }
        }
        throw new IllegalArgumentException();
    }

    public WordsInRange wordsOfRange(int afterPosition, int[] positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        WordInVariant wordExcluded = this.wordOf(afterPosition);

        if ( wordExcluded.index == all.size() - 1 ) {
            words.rangeStartIndex = wordExcluded.endIndex;
            words.rangeLength = 0;

            return words;
        }

        words.rangeStartIndex = wordExcluded.endIndex;
        words.rangeLength = this.variantLength - words.rangeStartIndex;

        WordInVariant word;
        for ( int i = wordExcluded.index + 1; i < all.size(); i++ ) {
            word = all.get(i);
            if ( word.intersections(afterPosition, positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }

    public WordsInRange wordsOfRange(int rangeStartIndex, int rangeLength) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        words.rangeStartIndex = rangeStartIndex;
        words.rangeLength = rangeLength;

        for ( WordInVariant word : all ) {
            if ( word.containsRange(rangeStartIndex, rangeLength) ) {
                words.add(word);
                break;
            }
            else if ( word.containsStartEndOrEnclosed(rangeStartIndex, rangeLength) ) {
                words.add(word);
            }
        }

        return words;
    }

    public WordsInRange wordsOfRange(List<Integer> positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        words.rangeIndexes.addAll(positions);

        for ( WordInVariant word : all ) {
            if ( word.countContains(positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }

    public WordsInRange wordsOfRange(int[] positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        words.addToRangeIndexes(positions);

        for ( WordInVariant word : all ) {
            if ( word.countContains(positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }
}
