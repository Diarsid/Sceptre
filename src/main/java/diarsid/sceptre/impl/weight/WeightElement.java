package diarsid.sceptre.impl.weight;

import diarsid.support.objects.CommonEnum;

import static java.lang.String.format;

import static diarsid.sceptre.impl.weight.WeightElement.WeightCalculationType.APPLY_PERCENT_TO_SUM;
import static diarsid.sceptre.impl.weight.WeightElement.WeightCalculationType.DEFAULT_CALCULATION_TYPE;
import static diarsid.sceptre.impl.weight.WeightElement.WeightType.CALCULATED;
import static diarsid.sceptre.impl.weight.WeightElement.WeightType.PREDEFINED;

public enum WeightElement implements CommonEnum<WeightElement> {
    
    CHAR_IS_ONE_CHAR_WORD(
            -19.2f, "char is one-char-word"),
    PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START(
            -7.71f, "previous char is word separator, current char is at pattern start!"),
    PREVIOUS_CHAR_IS_SEPARATOR(
            -3.1f, "previous char is word separator"),
    PREVIOUS_CHAR_IS_SEPARATOR_ONLY_SINGLE_CHAR_FOUND_IN_WORD(
            +2.3f, "previous char is word separator, but no other chars are found in this word"),
    CLUSTER_BEFORE_SEPARATOR(
            -10.5f, "there is cluster before separator!"),
    CLUSTER_STARTS_WITH_VARIANT(
            -6.6f, "cluster starts with variant"),
    CLUSTER_STARTS_PREVIOUS_CHAR_IS_WORD_SEPARATOR(
            -6.6f, "cluster start, previous char is word separator"),
    CLUSTER_STARTS_CURRENT_CHAR_IS_WORD_SEPARATOR(
            -6.6f, "cluster start, current char is word separator"),
    NEXT_CHAR_IS_SEPARATOR(
            -3.1f, "next char is word separator"),
    CLUSTER_ENDS_CURRENT_CHAR_IS_WORD_SEPARATOR(
            -6.6f, "cluster ends, current char is word separator"),
    SINGLE_POSITION_AND_FULL_CLUSTER_DENOTE_WORD(
            -8.17f, "single position and full cluster denote word"),
    SINGLE_POSITION_AND_PART_OF_CLUSTER_DENOTE_WORD(
            -3.27f, "single position and part of cluster denote word"),
    CLUSTER_IS_REJECTED_BY_ORDER_DIFFS(
            +24.73f, "cluster is rejected by order diffs"),
    FIRST_CLUSTER_HAS_MISSED_WORD_START(
            +17.61f, "word of first found cluster has not found start"),
    FIRST_CLUSTER_IS_REJECTED(
            +28.07f, "word of first found cluster has not found start"),
    FIRST_PATTERN_CHAR_IS_MISSED_IN_VARIANT_AT_ALL(
            +111.71f, "first pattern char is missed in variant at all"),
    
    VARIANT_EQUAL_PATTERN(
            "variant is equal to pattern"),
    VARIANT_CONTAINS_PATTERN(
            "variant contains pattern"),
    SINGLE_WORD_VARIANT_CONTAINS_PATTERN(
            "single word variant contains pattern"),
    VARIANT_PATH_SEPARATORS(
            "variant path separators"),
    VARIANT_TEXT_SEPARATORS(
            "variant text separators"),
    PATTERN_CONTAINS_CLUSTER(
            "pattern contains cluster"),
    PATTERN_CONTAINS_CLUSTER_LONG_WORD(
            "pattern contains cluster and it is a long word"),
    PATTERN_DOES_NOT_CONTAIN_CLUSTER(
            "pattern DOES NOT contain cluster"),
    CLUSTERS_NEAR_ARE_IN_ONE_PART(
            "clusters near, are in one part"),
    CHAR_AFTER_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD(
            "char after previous separator and cluster enclosing single word"),
    CLUSTER_IS_WORD(
            "cluster is a word"),
    UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START(
            "unnatural positioning - cluster found at end but pattern cluster at start"),
    UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END(
            "unnatural positioning - cluster found at start but pattern cluster at end"),
    PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD(
            "previous cluster and current char belong to one word"),
    CLUSTERS_IMPORTANCE(
            "clusters importance"),
    TOTAL_CLUSTERED_CHARS(
            "total clustered"),
    TOTAL_UNCLUSTERED_CHARS_IMPORTANCE(
            "total unclustered importance"),
    CLUSTERS_ORDER_INCOSISTENT(
            "clusters order incosistency"),
    CLUSTER_CANDIDATES_SIMILARITY(
            "cluster candidates similarity", true /* fading */),
    CLUSTERS_ARE_WEAK_2_LENGTH(
            "all clusters are weak (2 length)"),
    PLACING_PENALTY(
            "placing penalty"), 
    PLACING_BONUS(
            "placing bonus"),
    LENGTH_DELTA(
            "length delta"),
    CLUSTER_IS_CONSISTENT(
            "for consistency"),
    CLUSTER_IS_NOT_CONSISTENT(
            "for inconsistency"),
    CLUSTER_HAS_SHIFTS(
            "for shifts"),
    CLUSTER_ENDS_WITH_VARIANT(
            "cluster ends with variant"),
    CLUSTER_ENDS_NEXT_CHAR_IS_WORD_SEPARATOR(
            "cluster ends, next char is word separator"),
    SINGLE_POSITIONS_DENOTE_WORD(
            "single positions denote word"),
    
    NO_CLUSTERS_SEPARATED_POSITIONS_SORTED(
            "no clusters, all positions are sorted, none missed"),
    NO_CLUSTERS_SEPARATED_POSITIONS_MEANINGFUL(
            "meaningful positions"),
    NO_CLUSTERS_ALL_SEPARATED_POSITIONS_MEANINGFUL(
            "all positions are meaningful"),

    FOUND_POSITIONS_DENOTES_ALL_WORDS(
            "all found positions belong to all variant words"),
    FOUND_POSITIONS_BELONG_TO_ONE_WORD(
            "all found positions belong to one word"),
    
    PERCENT_FOR_MISSED(
            "decrease to percent for missed", 
            APPLY_PERCENT_TO_SUM),

    WORD_QUALITY(
            "word quality");

    public static enum WeightType implements CommonEnum<WeightType> {

        CALCULATED,
        PREDEFINED;
    }

    public static enum WeightCalculationType implements CommonEnum<WeightCalculationType> {

        ADD_VALUE_TO_SUM,
        APPLY_PERCENT_TO_SUM;

        static final WeightCalculationType DEFAULT_CALCULATION_TYPE = ADD_VALUE_TO_SUM;
    }

    public final float predefinedWeight;
    public final boolean isFading;
    public final String description;
    public final WeightType type;
    public final WeightCalculationType calculationType;

    WeightElement(String description) {
        this.predefinedWeight = 0;
        this.isFading = false;
        this.description = description;
        this.type = CALCULATED;
        this.calculationType = DEFAULT_CALCULATION_TYPE;
    }

    WeightElement(String description, boolean isFading) {
        this.predefinedWeight = 0;
        this.isFading = isFading;
        this.description = description;
        this.type = CALCULATED;
        this.calculationType = DEFAULT_CALCULATION_TYPE;
    }

    WeightElement(float predefinedWeight, String description) {
        this.predefinedWeight = predefinedWeight;
        this.isFading = false;
        this.description = description;
        this.type = PREDEFINED;
        this.calculationType = DEFAULT_CALCULATION_TYPE;
    }

    WeightElement(String description, WeightCalculationType calculationType) {
        this.predefinedWeight = 0;
        this.isFading = false;
        this.description = description;
        this.type = CALCULATED;
        this.calculationType = calculationType;
    }

    WeightElement(
            float predefinedWeight, String description, WeightCalculationType calculationType) {
        this.predefinedWeight = predefinedWeight;
        this.isFading = false;
        this.description = description;
        this.type = PREDEFINED;
        this.calculationType = calculationType;
    }
    
    void weightTypeMustBe(WeightType someType) {
        if ( ! this.type.equals(someType) ) {
            throw new IllegalArgumentException(format(
                    "%s type expected, but was %s", this.type, someType));
        }
    }
    
    void weightCalculationTypeMustBe(WeightCalculationType calculationType) {
        if ( ! this.calculationType.equals(calculationType) ) {
            throw new IllegalArgumentException(format(
                    "%s type expected, but was %s", this.calculationType, calculationType));
        }
    }
}
