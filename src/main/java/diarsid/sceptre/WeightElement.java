package diarsid.sceptre;

import static java.lang.String.format;

import static diarsid.sceptre.WeightElement.WeightCalculationType.APPLY_PERCENT_TO_SUM;
import static diarsid.sceptre.WeightElement.WeightCalculationType.DEFAULT_CALCULATION_TYPE;
import static diarsid.sceptre.WeightElement.WeightType.CALCULATED;
import static diarsid.sceptre.WeightElement.WeightType.PREDEFINED;

public enum WeightElement {
    
    CHAR_IS_ONE_CHAR_WORD(
            -19.2f, "char is one-char-word"),
    PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START(
            -7.71f, "previous char is word separator, current char is at pattern start!"),
    PREVIOUS_CHAR_IS_SEPARATOR(
            -3.1f, "previous char is word separator"),
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
    CHAR_BEFORE_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD(
            "char before previous separator and cluster enclosing single word"),
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
            "cluster candidates similarity"),
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
    
    PERCENT_FOR_MISSED(
            "decrease to percent for missed", 
            APPLY_PERCENT_TO_SUM);

    static enum WeightType {
        CALCULATED,
        PREDEFINED;
    }

    static enum WeightCalculationType {
        ADD_VALUE_TO_SUM,
        APPLY_PERCENT_TO_SUM;

        static final WeightCalculationType DEFAULT_CALCULATION_TYPE = ADD_VALUE_TO_SUM;
    }



    private final float predefinedWeight;
    private final String description;
    private final WeightType type;
    private final WeightCalculationType calculationType;

    WeightElement(String description) {
        this.predefinedWeight = 0;
        this.description = description;
        this.type = CALCULATED;
        this.calculationType = DEFAULT_CALCULATION_TYPE;
    }

    WeightElement(float predefinedWeight, String description) {
        this.predefinedWeight = predefinedWeight;
        this.description = description;
        this.type = PREDEFINED;
        this.calculationType = DEFAULT_CALCULATION_TYPE;
    }

    WeightElement(String description, WeightCalculationType calculationType) {
        this.predefinedWeight = 0;
        this.description = description;
        this.type = CALCULATED;
        this.calculationType = calculationType;
    }

    WeightElement(
            float predefinedWeight, String description, WeightCalculationType calculationType) {
        this.predefinedWeight = predefinedWeight;
        this.description = description;
        this.type = PREDEFINED;
        this.calculationType = calculationType;
    }
    
    float predefinedWeight() {
        return this.predefinedWeight;
    }
    
    String description() {
        return this.description;
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
