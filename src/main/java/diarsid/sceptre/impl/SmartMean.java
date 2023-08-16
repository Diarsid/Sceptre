package diarsid.sceptre.impl;

import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.MapIntInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.sceptre.impl.collections.impl.MapIntIntImpl;
import diarsid.support.objects.StatefulClearable;

import static java.lang.Math.round;

import static diarsid.sceptre.impl.collections.Ints.meanSmartConsideringZeros;
import static diarsid.support.misc.MathFunctions.absDiff;

public class SmartMean implements StatefulClearable {

    private final MapIntInt elementsCounts;
    private final ListInt maxElements;

    public SmartMean() {
        this.elementsCounts = new MapIntIntImpl();
        this.maxElements = new ListIntImpl();
    }

    public int calculate(ListInt ints) {
        try {
            return this.calculateInternally(ints);
        }
        finally {
            this.elementsCounts.clear();
            this.maxElements.clear();
        }
    }

    private int calculateInternally(ListInt ints) {
        int size = ints.size();
        int e;
        int sum = 0;
        int qtyNoZero = 0;
        for ( int i = 0; i < size; i++ ) {
            e = ints.get(i);
            if ( e != 0 ) {
                sum = sum + e;
                qtyNoZero++;
            }
            this.elementsCounts.putOrIncrementValueIfKeyExists(e, 1);
        }

        Ints.Elements counts = this.elementsCounts.values().elements();

        int maxCount = Integer.MIN_VALUE;
        int count;
        while ( counts.hasNext() ) {
            counts.next();
            count = counts.current();
            if ( count > maxCount ) {
                maxCount = count;
            }
        }

        if ( maxCount == 1 ) {
            return meanSmartConsideringZeros(sum, size, size-qtyNoZero);
        }

        MapIntInt.Entries entries = this.elementsCounts.entries();

        int maxElementsSum = 0;
        while ( entries.hasNext() ) {
            entries.next();
            if ( entries.currentValue() == maxCount ) {
                this.maxElements.add(entries.currentKey());
                maxElementsSum = maxElementsSum + entries.currentKey();
            }
        }

        maxElementsSum = maxElementsSum * maxCount;

        if ( this.maxElements.hasSize(1) ) {
            return this.maxElements.get(0);
        }
        else {
            int sumWithoutMaximums = sum - maxElementsSum;
            int qtyNoZeroNoMaximums = qtyNoZero - (this.maxElements.size() * maxCount);

            if ( qtyNoZeroNoMaximums == 0 ) {
                return round( (float) sum / size);
            }

            double mean = ((double) sumWithoutMaximums) / qtyNoZeroNoMaximums;
            double absDiff;
            double minAbsDiff = Double.MAX_VALUE;
            int bestMaxElement = Integer.MIN_VALUE;
            int maxElement;
            for ( int i = 0; i < this.maxElements.size(); i++ ) {
                maxElement = this.maxElements.get(i);
                absDiff = absDiff(mean, maxElement);
                if ( absDiff < minAbsDiff ) {
                    minAbsDiff = absDiff;
                    bestMaxElement = maxElement;
                }
            }

            if ( bestMaxElement == Integer.MIN_VALUE ) {
                throw new IllegalStateException();
            }

            return bestMaxElement;
        }
    }

    @Override
    public void clear() {
        this.elementsCounts.clear();
        this.maxElements.clear();
    }
}
