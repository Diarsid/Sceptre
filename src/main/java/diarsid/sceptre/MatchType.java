/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre;

/**
 *
 * @author Diarsid
 */
public enum MatchType {
    
    MATCH_DIRECTLY(4),
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
