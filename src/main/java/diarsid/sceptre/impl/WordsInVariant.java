package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;

import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.StatefulClearable;

public class WordsInVariant implements StatefulClearable {

    final GuardedPool<WordInVariant> pool;
    final List<WordInVariant> all;

    public WordsInVariant(GuardedPool<WordInVariant> pool) {
        this.pool = pool;
        this.all = new ArrayList<>();
    }

    public WordInVariant next() {
        WordInVariant wordInVariant = pool.give();
        all.add(wordInVariant);
        return wordInVariant;
    }

    @Override
    public void clear() {
        pool.takeBackAll(all);
        all.clear();
    }

    public WordInVariant wordOf(int indexInVariant) {
        for ( WordInVariant word : all ) {
            if ( word.hasIndex(indexInVariant) ) {
                return word;
            }
        }
        throw new IllegalArgumentException();
    }

    public WordInVariant wordOfRange(int rangeStartIndex, int rangeLength) {
        for ( WordInVariant word : all ) {
            if ( word.containsRange(rangeStartIndex, rangeLength) ) {
                return word;
            }
        }
        return null;
    }

    public WordInVariant wordOfRange(List<Integer> positions) {
        for ( WordInVariant word : all ) {
            if ( word.contains(positions) ) {
                return word;
            }
        }
        return null;
    }
}
