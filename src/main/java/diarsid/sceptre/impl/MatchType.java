package diarsid.sceptre.impl;

public enum MatchType {
    
    MATCH_DIRECTLY(10),
    MATCH_TYPO_LOOP(2),
    MATCH_TYPO_1(3),
    MATCH_TYPO_2(2),
    MATCH_TYPO_3_1(1),
    MATCH_TYPO_3_2(1),
    MATCH_TYPO_3_3(1);
    
    private final int strength;

    private MatchType(int weakness) {
        this.strength = weakness;
    }
    
    int strength() {
        return this.strength;
    }
}
