package diarsid.sceptre.api.model;

public enum WeightEstimate {
    
    PERFECT (3),
    GOOD (2),
    MODERATE (1),
    BAD (0);
    
    private final int level;

    private WeightEstimate(int level) {
        this.level = level;
    }
    
    private static final float BAD_BOUND = -10;
    private static final float MODERATE_BOUND = -36;
    private static final float GOOD_BOUND = -75;
    
    public static WeightEstimate estimateWeightOf(Variant variant) {
        return estimate(variant.weight());
    }
    
    public static WeightEstimate estimatePreliminarily(float weight) {
        return estimate(weight - 5.0f);
    }
    
    public static WeightEstimate estimate(float weight) {
        if ( weight > BAD_BOUND ) {
            return BAD;
        } else if ( BAD_BOUND >= weight && weight > MODERATE_BOUND ) {
            return MODERATE;
        } else if ( MODERATE_BOUND >= weight && weight > GOOD_BOUND ) {
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
