package diarsid.sceptre;

import diarsid.sceptre.PositionsSearchStepOneCluster.PatternCluster;

import static java.util.Objects.nonNull;

import static diarsid.sceptre.ClusterPreference.PREFER_LEFT;
import static diarsid.sceptre.ClusterPreference.PREFER_RIGHT;
import static diarsid.sceptre.PositionsSearchStepOneCluster.calculateAdditionalPossibleTypoMatches;

public enum PositionsSearchStepOneClusterDuplicateComparison {
    
    A;
    
    static ClusterPreference compare(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.patternCluster().equals(two.patternCluster()) ) {
            PatternCluster commonPatternCluster = one.patternCluster();
            
            if ( commonPatternCluster.isAtPatternStart() ) {
                if ( one.isAtStart() ) {
                    return PREFER_LEFT;
                } else if ( two.isAtStart() ) {
                    return PREFER_RIGHT;
                } else {
                    return compareByPossibleTypoMatches(one, two);
                }
            } else if ( commonPatternCluster.isAtPatternEnd() ) {
                if ( one.isAtEnd() ) {
                    return PREFER_LEFT;
                } else if ( two.isAtEnd() ) {
                    return PREFER_RIGHT;
                } else {
                    return compareByPossibleTypoMatches(one, two);
                }
            } else {
                return compareByPossibleTypoMatches(one, two);
            }
        } else {            
            return compareByPossibleTypoMatches(one, two);
        }
    }

    private static ClusterPreference compareByPossibleTypoMatches(
            PositionsSearchStepOneCluster one, PositionsSearchStepOneCluster two) {
        if ( one.doesHaveMorePossibleTypoMatchesThan(two) ) {
            return PREFER_LEFT;
        } else if ( two.doesHaveMorePossibleTypoMatchesThan(one) ) {
            return PREFER_RIGHT;
        } else {
            ClusterPreference preference = calculateAdditionalPossibleTypoMatches(one, two);
            if ( nonNull(preference) ) {
                return preference;
            } else {
                if ( one.doesStartAfterSeparator() ) {
                    if ( two.doesStartAfterSeparator() ) {
                        if ( one.doesEndBeforeSeparator() ) {
                            return PREFER_LEFT;
                        } else if ( two.doesEndBeforeSeparator() ) {
                            return PREFER_RIGHT;
                        } else {
                            return PREFER_RIGHT;
                        }
                    } else {
                        return PREFER_LEFT;
                    }
                } else if ( two.doesStartAfterSeparator() ) {
                    return PREFER_RIGHT;
                } else {
                    if ( one.doesEndBeforeSeparator() ) {
                        return PREFER_LEFT;
                    } else if ( two.doesEndBeforeSeparator() ) {
                        return PREFER_RIGHT;
                    } else {
                        return PREFER_RIGHT;
                    }
                }
            }
        }
    }
    
}
