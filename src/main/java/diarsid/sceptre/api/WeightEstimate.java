package diarsid.sceptre.api;

import diarsid.sceptre.api.model.Weighted;
import diarsid.support.objects.CommonEnum;

public enum WeightEstimate implements CommonEnum<WeightEstimate> {

    PERFECT(3),
    GOOD(2),
    MODERATE(1),
    BAD(0);

    private final int level;

    private WeightEstimate(int level) {
        this.level = level;
    }

    public static final float TOO_BAD = 9000;
    public static final float BAD_VS_MODERATE_BOUND = -10;
    public static final float MODERATE_VS_GOOD_BOUND = -36;
    public static final float GOOD_VS_PERFECT_BOUND = -75;

    public static WeightEstimate of(Weighted weighted) {
        return of(weighted.weight());
    }

    public static WeightEstimate preliminarilyOf(float weight) {
        return of(weight - 5.0f);
    }

    public static WeightEstimate of(float weight) {
        if (weight > BAD_VS_MODERATE_BOUND) {
            return BAD;
        } else if (BAD_VS_MODERATE_BOUND >= weight && weight > MODERATE_VS_GOOD_BOUND) {
            return MODERATE;
        } else if (MODERATE_VS_GOOD_BOUND >= weight && weight > GOOD_VS_PERFECT_BOUND) {
            return GOOD;
        } else {
            return PERFECT;
        }
    }

    public boolean isEqualOrBetterThan(WeightEstimate other) {
        return this.level >= other.level;
    }

    public boolean isBetterThan(WeightEstimate other) {
        return this.level > other.level;
    }
}
