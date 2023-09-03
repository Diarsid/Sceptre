package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;

import diarsid.sceptre.impl.collections.ArrayInt;
import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.MapIntInt;
import diarsid.sceptre.impl.collections.impl.MapIntIntImpl;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;
import diarsid.support.objects.StatefulClearable;

import static diarsid.sceptre.impl.WordInInput.Placing.DEPENDENT;
import static diarsid.sceptre.impl.WordInInput.Placing.INDEPENDENT;

public class WordsInInput implements StatefulClearable {

    public static class WordsInRange extends PooledReusable {

        private static final int UNINITIALIZED = -1;

        private int rangeStartIndex;
        private final List<WordInInput> found;
        private int length;

        public WordsInRange() {
            this.rangeStartIndex = UNINITIALIZED;
            this.found = new ArrayList<>();
            this.length = 0;
        }

        WordInInput first() {
            return this.found.get(0);
        }

        WordInInput get(int i) {
            return this.found.get(i);
        }

        void add(WordInInput word) {
            this.found.add(word);
            this.length = this.length + word.length;
        }

        @Override
        public void clearForReuse() {
            this.rangeStartIndex = UNINITIALIZED;
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
            for ( WordInInput word : found ) {
                count = count + (word.placing.is(INDEPENDENT) ? 1 : 0);
            }

            return count;
        }

        public List<WordInInput> all() {
            return this.found;
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

        public int intersections(Ints indexes) {
            if ( this.areEmpty() ) {
                return 0;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).intersections(indexes);
            }

            int intersections = 0;
            for ( WordInInput word : found ) {
                intersections = intersections + word.intersections(indexes);
            }

            return intersections;
        }

        public int intersections(ArrayInt positions, int startExcludingPosition, int length) {
            if ( this.areEmpty() ) {
                return 0;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).intersections(positions, startExcludingPosition, length);
            }

            int intersections = 0;
            for ( WordInInput word : found ) {
                intersections = intersections + word.intersections(positions, startExcludingPosition, length);
            }

            return intersections;
        }

        public boolean hasStartIn(Ints indexes) {
            if ( this.areEmpty() ) {
                return false;
            }

            if ( this.found.size() == 1 ) {
                return this.found.get(0).hasStartIn(indexes);
            }

            for ( WordInInput word : found ) {
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

    public static class Qualities implements StatefulClearable {

        final WordsInInput wordsInInput;
        final MapIntInt qualitiesByWordIndex;

        public Qualities(WordsInInput wordsInInput) {
            this.wordsInInput = wordsInInput;
            this.qualitiesByWordIndex = new MapIntIntImpl();
        }

        @Override
        public void clear() {
            this.qualitiesByWordIndex.clear();
        }

        public int qualityOf(WordInInput word) {
            return this.qualitiesByWordIndex.get(word.index);
        }

        public void add(WordInInput word, int quality) {
            this.qualitiesByWordIndex.put(word.index, quality);
        }
    }

    private final GuardedPool<WordInInput> wordsPool;
    private final GuardedPool<WordsInRange> wordsInRangePool;
    private final List<WordsInRange> usedWordsInRanges;
    private final List<WordInInput> wordsByCharInVariantIndex;
    final List<WordInInput> all;
    final Qualities qualities;
    int variantLength;

    public WordsInInput(GuardedPool<WordInInput> wordsPool, GuardedPool<WordsInRange> wordsInRangePool) {
        this.wordsPool = wordsPool;
        this.wordsInRangePool = wordsInRangePool;
        this.all = new ArrayList<>();
        this.usedWordsInRanges = new ArrayList<>();
        this.wordsByCharInVariantIndex = new ArrayList<>(128);
        this.qualities = new Qualities(this);
    }

    public WordInInput next(WordInInput.Placing placing) {
        WordInInput wordInInput = this.wordsPool.give();
        wordInInput.placing = placing;
        wordInInput.index = this.all.size();
        this.all.add(wordInInput);
        return wordInInput;
    }

    @Override
    public void clear() {
        this.qualities.clear();
        this.wordsByCharInVariantIndex.clear();
        this.wordsPool.takeBackAll(this.all);
        this.wordsInRangePool.takeBackAll(usedWordsInRanges);
        this.all.clear();
        this.usedWordsInRanges.clear();
        this.variantLength = -1;
    }

    void complete() {
        int iVariant = 0;
        int iWord = 0;

        WordInInput word = null;
        for ( ; iVariant < this.variantLength; iVariant++ ) {
            if ( word == null && iWord < this.all.size() ) {
                word = this.all.get(iWord);
            }

            if ( word.hasIndex(iVariant) ) {
                this.wordsByCharInVariantIndex.add(word);
                if ( iVariant == word.endIndex ) {
                    word = null;
                    iWord++;
                }
            }
            else if ( iVariant < word.startIndex ) {
                this.wordsByCharInVariantIndex.add(null);
            }
            else if ( iVariant > word.endIndex ) {
                this.wordsByCharInVariantIndex.add(null);
                word = null;
            }
        }
    }

    public WordInInput firstIndependentBefore(WordInInput dependentWord) {
        dependentWord.placing.mustBe(DEPENDENT);

        WordInInput word;
        for ( int i = dependentWord.index-1; i > -1; i-- ) {
            word = this.all.get(i);
            if ( word.placing.is(INDEPENDENT) ) {
                return word;
            }
        }

        throw new IllegalStateException();
    }

    public WordsInRange independentAndDependentWordsBefore(WordInInput dependentWord) {
        WordInInput firstIndependent = this.firstIndependentBefore(dependentWord);

        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        words.add(firstIndependent);

        for ( int i = firstIndependent.index + 1; i < dependentWord.index; i++ ) {
            words.add(this.all.get(i));
        }

        return words;
    }

    public WordInInput wordOrNullOf(int startIndex, int endIndex) {
        WordInInput word = this.wordsByCharInVariantIndex.get(startIndex);

        if ( word != null && word.startIndex == startIndex && word.endIndex == endIndex ) {
            return word;
        }

        return null;
    }

    public WordInInput wordOf(int indexInVariant) {
        WordInInput word = this.wordsByCharInVariantIndex.get(indexInVariant);
        if ( word == null ) {
            throw new IllegalArgumentException();
        }
        return word;
    }

    public WordInInput wordBeforeOrNull(WordInInput word) {
        if ( word.index == 0 ) {
            return null;
        }

        return this.all.get(word.index - 1);
    }

    public WordInInput wordAfterOrNull(WordInInput word) {
        if ( word.index == all.size() - 1 ) {
            return null;
        }

        return this.all.get(word.index + 1);
    }

    public WordsInRange wordsOfOrNull(Cluster cluster) {
        var words = wordsOfRange(cluster.firstPosition(), cluster.length());

        if ( words.areEmpty() ) {
            this.usedWordsInRanges.remove(this.usedWordsInRanges.size() - 1);
            this.wordsInRangePool.takeBack(words);

            return null;
        }
        else {
            return words;
        }
    }

    public WordsInRange allDependentAfterOrNull(WordInInput independentWord) {
        if ( independentWord.placing.is(DEPENDENT) ) {
            throw new IllegalArgumentException();
        }

        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        WordInInput word;
        for ( int i = independentWord.index + 1; i < this.all.size(); i++ ) {
            word = this.all.get(i);
            if ( word.placing.is(DEPENDENT) ) {
                words.add(word);
            }
        }

        if ( words.areEmpty() ) {
            this.usedWordsInRanges.remove(this.usedWordsInRanges.size() - 1);
            this.wordsInRangePool.takeBack(words);

            return null;
        }
        else {
            words.rangeStartIndex = words.all().get(0).startIndex;

            return words;
        }
    }

    public WordsInRange wordsOfRange(int afterPosition, ArrayInt positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        WordInInput wordExcluded = this.wordOf(afterPosition);

        if ( wordExcluded.index == this.all.size() - 1 ) {
            words.rangeStartIndex = wordExcluded.endIndex;

            return words;
        }

        words.rangeStartIndex = wordExcluded.endIndex;

        WordInInput word;
        for ( int i = wordExcluded.index + 1; i < this.all.size(); i++ ) {
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

        for ( WordInInput word : all ) {
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

    public WordsInRange wordsOfRange(ListInt positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        for ( WordInInput word : all ) {
            if ( word.intersections(positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }

    public WordsInRange wordsOfRange(ArrayInt positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        for ( WordInInput word : all ) {
            if ( word.intersections(positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }

    public WordsInRange wordsOfRange(Ints positions) {
        WordsInRange words = this.wordsInRangePool.give();
        this.usedWordsInRanges.add(words);

        for ( WordInInput word : all ) {
            if ( word.intersections(positions) > 0 ) {
                words.add(word);
            }
        }

        return words;
    }
}
