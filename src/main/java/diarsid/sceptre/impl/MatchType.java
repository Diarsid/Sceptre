package diarsid.sceptre.impl;

import diarsid.support.objects.CommonEnum;

public enum MatchType implements CommonEnum<MatchType> {
    
    MATCH_DIRECTLY(20),
    MATCH_WORD_END(5),
    MATCH_TYPO_LOOP(3),
    MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT(2, true),
    MATCH_TYPO_NEXT_IN_PATTERN_NEXTx2_IN_VARIANT(3),
    MATCH_TYPO_NEXTx2_IN_PATTERN_NEXT_IN_VARIANT(3),
    MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT(1, true),
    MATCH_TYPO_NEXTx2_IN_PATTERN_NEXTx3_IN_VARIANT(1),
    MATCH_TYPO_NEXTx3_IN_PATTERN_NEXT_IN_VARIANT(1);
    
    private final int strength;
    public boolean isBackward;

    private MatchType(int weakness) {
        this.strength = weakness;
        this.isBackward = false;
    }

    private MatchType(int weakness, boolean isBackward) {
        this.strength = weakness;
        this.isBackward = isBackward;
    }
    
    int strength() {
        return this.strength;
    }
}
