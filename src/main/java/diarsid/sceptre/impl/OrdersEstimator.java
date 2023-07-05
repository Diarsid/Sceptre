package diarsid.sceptre.impl;

import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.sceptre.impl.logs.Logging;
import diarsid.support.objects.StatefulClearable;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import static diarsid.sceptre.api.LogType.POSITIONS_SEARCH;

public class OrdersEstimator implements StatefulClearable {

    private final Logging logging;

    private final ListInt patternPositions = new ListIntImpl();
    private final ListInt variantPositions = new ListIntImpl();

    private int patternPosition;
    private int variantPosition;

    private int patternDiffSum;
    private int patternDiffCount;

    private int variantDiffSum;
    private int variantDiffCount;
    private int variantDiffBackwardSum;

    private int qualityExternalBonus;
    private int quality;
    private int penalty;

    public OrdersEstimator(Logging logging) {
        this.logging = logging;
    }

    void set(int pattern, int variant) {
        this.patternPosition = pattern;
        this.variantPosition = variant;
    }

    void add(int pattern, int variant) {
        this.patternPositions.add(pattern);
        this.variantPositions.add(variant);
    }

    private void accumulate(int i, int pattern, int variant) {
        int prevPattern;
        if ( i > 0 ) {
            prevPattern = this.patternPositions.get(i - 1);
        }
        else {
            prevPattern = this.patternPosition;
        }

        int patternDiff = (pattern - prevPattern - 1);
        if ( patternDiff > 0 ) {
            this.patternDiffSum = this.patternDiffSum + patternDiff;
            this.patternDiffCount++;
        }

        int prevVariant;
        if ( i > 0 ) {
            prevVariant = this.variantPositions.get(i - 1);
        }
        else {
            prevVariant = this.variantPosition;
        }

        int variantDiffRaw = variant - prevVariant;
        boolean isBackward = variantDiffRaw < 0;
        int variantDiff = abs(variantDiffRaw) - 1;
        if ( isBackward && variantDiff == 0 ) {
            variantDiff = 1;
        }
        if ( variantDiff > 0 ) {
            this.variantDiffSum = this.variantDiffSum + variantDiff;
            this.variantDiffCount++;
            if ( isBackward ) {
                this.variantDiffBackwardSum = this.variantDiffBackwardSum + variantDiff;
            }
        }

        if ( patternDiff == 0 && variantDiff == 0 ) {
            if ( i == 0 ) {
                quality++;
            }
            quality++;
        }
        else {
            if ( i == 0 ) {
                if ( patternDiff == 0 && variantDiff < 3 ) {
                    penalty = 1;
                }
                else {
                    if ( patternDiff > 1 && variantDiff > 1 ) {
                        penalty = penalty + 2;
                    }
                    else if ( patternDiff > 1 || variantDiff > 1) {
                        penalty++;
                    }
                }
            }
        }
    }

    void remove(int patternPosition) {
        int i = this.patternPositions.indexOf(patternPosition);
        if ( i < 0 ) {
            return;
        }

        this.patternPositions.remove(i);
        this.variantPositions.remove(i);
    }

    void correlateQuality(int bonus) {
        this.qualityExternalBonus = this.qualityExternalBonus + bonus;
    }

    int qualityTotal() {
        for ( int i = 0; i < this.patternPositions.size(); i++ ) {
            this.accumulate(i, patternPositions.get(i), variantPositions.get(i));
        }

        if ( patternDiffSum == 0 ) {
            quality++;
            quality = quality + ((patternPositions.size()-1) / 2);
        }

        if ( quality > 0 ) {
            int limit = (patternPositions.size() + 1) / 3;
            if ( variantDiffCount == patternDiffCount && variantDiffCount == limit ) {
                if ( max(variantDiffSum, patternDiffSum) < (limit + 2) ) {
                    quality++;
                    penalty--;
                }
            }
        }

        if ( penalty < 0 ) {
            penalty = 0;
        }

        int result = quality - variantDiffSum - variantDiffBackwardSum - patternDiffSum - penalty*2 + qualityExternalBonus;
        logging.add(POSITIONS_SEARCH, "            [orders estimate] result    : %s", result);

        return result;
    }

    int qualityThreshold() {
        int threshold = -1 * this.patternPositions.size();
        logging.add(POSITIONS_SEARCH, "            [orders estimate] threshold : %s", threshold);
        return threshold;
    }

    boolean isOk() {
        return qualityTotal() >= qualityThreshold();
    }

    @Override
    public void clear() {
        this.patternPosition = -1;
        this.variantPosition = -1;

        this.patternPositions.clear();
        this.variantPositions.clear();

        this.patternDiffSum = 0;
        this.patternDiffCount = 0;

        this.variantDiffSum = 0;
        this.variantDiffCount = 0;
        this.variantDiffBackwardSum = 0;

        this.qualityExternalBonus = 0;
        this.quality = 0;
        this.penalty = 0;
    }
}
