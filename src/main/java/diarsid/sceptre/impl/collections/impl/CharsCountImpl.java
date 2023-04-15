package diarsid.sceptre.impl.collections.impl;

import java.util.ArrayList;
import java.util.List;

import diarsid.sceptre.impl.collections.CharsCount;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;

public class CharsCountImpl implements CharsCount {

    private static class Count extends PooledReusable {

        static final char EMPTY = '#';

        char c;
        int value;

        @Override
        protected void clearForReuse() {
            this.value = -1;
            this.c = EMPTY;
        }
    }

    private static final GuardedPool<Count> COUNTS_POOL = new GuardedPool<>(Count::new);

    private final List<Count> counts;

    public CharsCountImpl() {
        this.counts = new ArrayList<>();
    }

    @Override
    public int countOf(char c) {
        Count count;
        for ( int i = 0; i < this.counts.size(); i++ ) {
            count = this.counts.get(i);
            if ( count.c == c ) {
                return count.value;
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public void increment(char c) {
        Count count;

        boolean done = false;
        for ( int i = 0; i < this.counts.size(); i++ ) {
            count = this.counts.get(i);
            if ( count.c == c ) {
                count.value++;
                done = true;
            }
        }

        if ( ! done ) {
            count = COUNTS_POOL.give();

            count.c = c;
            count.value = 1;

            this.counts.add(count);
        }
    }

    @Override
    public void clear() {
        COUNTS_POOL.takeBackAll(this.counts);
        this.counts.clear();
    }
}
