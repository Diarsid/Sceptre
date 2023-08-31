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
    private int patternDiffBackwardSum;

    private int variantDiffSum;
    private int variantDiffCount;
    private int variantDiffBackwardSum;

    private int qualityExternalBonus;
    private int quality;
    private int penalty;

    private boolean prevIsBackward;

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

    void change(int oldPattern, int oldVariant, int newPattern, int newVariant) {
        int iPattern = this.patternPositions.indexOf(oldPattern);
        int iVariant = this.variantPositions.indexOf(oldVariant);

        if ( iPattern != iVariant ) {
            throw new IllegalStateException();
        }

        this.patternPositions.set(iPattern, newPattern);
        this.variantPositions.set(iVariant, newVariant);
    }

    private void accumulate(int i, int pattern, int variant) {
        if ( i == 0 ) {
            this.prevIsBackward = false;
        }

        int prevPattern;
        if ( i > 0 ) {
            prevPattern = this.patternPositions.get(i - 1);
        }
        else {
            prevPattern = this.patternPosition;
        }

        int patternDiffRaw = pattern - prevPattern;
        boolean isBackward = patternDiffRaw < 0;
        int patternDiff = abs(patternDiffRaw) - 1;
        if ( isBackward && patternDiff == 0 ) {
            patternDiff = 1;
        }
        if ( patternDiff > 0 ) {
            this.patternDiffSum = this.patternDiffSum + patternDiff;
            this.patternDiffCount++;
            if ( isBackward ) {
                this.patternDiffBackwardSum = this.patternDiffBackwardSum + patternDiff;
            }
        }

        int prevVariant;
        if ( i > 0 ) {
            prevVariant = this.variantPositions.get(i - 1);
        }
        else {
            prevVariant = this.variantPosition;
        }

        int variantDiffRaw = variant - prevVariant;

        if ( ! isBackward ) {
            if ( variantDiffRaw < 0 ) {
                isBackward = true;
            }
        }

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
            quality++;
        }
        else if ( (patternDiff == 0 && variantDiff == 1) ) {
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

            if ( patternDiff == variantDiff && patternDiff < 3 ) {
                quality++;
            }
        }

        if ( isBackward && this.prevIsBackward ) {
            quality--;
            penalty++;
        }

        this.prevIsBackward = isBackward;
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

        if ( patternPositions.size() == 1 ) {
            if ( variantDiffSum == 0 || patternDiffSum == 0 ) {
                if ( variantDiffBackwardSum == 0 ) {
                    return 1;
                }
            }
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

        int result = quality - variantDiffSum - variantDiffBackwardSum - patternDiffBackwardSum - patternDiffSum - penalty*2 + qualityExternalBonus;

        if ( variantDiffBackwardSum == 0 && patternDiffBackwardSum == 0 ) {
            result++;
        }

        return result;
    }

    int qualityThreshold() {
        int threshold = -1 * this.patternPositions.size();
        return threshold;
    }

    boolean isOk() {
        int quality = qualityTotal();
        int threshold = qualityThreshold();
        logging.add(POSITIONS_SEARCH, "            [orders estimate] pattern   : %s %s", patternPosition, patternPositions);
        logging.add(POSITIONS_SEARCH, "            [orders estimate] variant   : %s %s", variantPosition, variantPositions);
        logging.add(POSITIONS_SEARCH, "            [orders estimate] result    : %s", quality);
        logging.add(POSITIONS_SEARCH, "            [orders estimate] threshold : %s", threshold);
        return quality >= threshold;
    }

    @Override
    public void clear() {
        this.patternPosition = -1;
        this.variantPosition = -1;

        this.patternPositions.clear();
        this.variantPositions.clear();

        this.patternDiffSum = 0;
        this.patternDiffCount = 0;
        this.patternDiffBackwardSum = 0;

        this.variantDiffSum = 0;
        this.variantDiffCount = 0;
        this.variantDiffBackwardSum = 0;

        this.qualityExternalBonus = 0;
        this.quality = 0;
        this.penalty = 0;
    }
}
