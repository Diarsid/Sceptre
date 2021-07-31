package diarsid.sceptre;

import diarsid.support.objects.CommonEnum;

public enum PositionsSearchStep implements CommonEnum<PositionsSearchStep> {
    
    STEP_1 (
            /*order*/ 0, 
            /*permission to cluster chars treshold*/ 2, 
            /*min pattern length to apply step*/ 0, 
            /*typo searching allowed*/ false,
            /*positions found during this step can be clustered*/ true), 
    STEP_2 (
            /*order*/ 1, 
            /*permission to cluster chars treshold*/ 1, 
            /*min pattern length to apply step*/ 3, 
            /*typo searching allowed*/ true,
            /*positions found during this step can be clustered*/ true), 
    STEP_3 (
            /*order*/ 2, 
            /*permission to cluster chars treshold*/ 0, 
            /*min pattern length to apply step*/ 5, 
            /*typo searching allowed*/ false,
            /*positions found during this step can be clustered*/ false),
    STEP_4 (
            /*order*/ 4,
            /*permission to cluster chars treshold*/ 0, 
            /*min pattern length to apply step*/ 0, 
            /*typo searching allowed*/ false,
            /*positions found during this step can be clustered*/ false);
    
    private final int order;
    private final int permissionToClusterCharTreshold;
    private final int minPatternLength;
    private final boolean typoSearchingAllowed;
    private final boolean foundPositionsCanBeClustered;
    
    private PositionsSearchStep(
            int order, 
            int permissionTreshold, 
            int minPatternLength, 
            boolean typoSearchingAllowed,
            boolean foundPositionsCanBeClustered) {
        this.order = order;
        this.permissionToClusterCharTreshold = permissionTreshold;
        this.minPatternLength = minPatternLength;
        this.typoSearchingAllowed = typoSearchingAllowed;
        this.foundPositionsCanBeClustered = foundPositionsCanBeClustered;
    }
    
    boolean canProceedWith(int patternLength) {
        return patternLength >= this.minPatternLength;
    }
    
    boolean canAddToPositions(int charsInCluster) {
        return charsInCluster >= this.permissionToClusterCharTreshold;
    }
    
    boolean typoSearchingAllowed() {
        return this.typoSearchingAllowed;
    }
    
    boolean isAfter(PositionsSearchStep other) {
        return this.order > other.order;
    }
    
    boolean isBefore(PositionsSearchStep other) {
        return this.order < other.order;
    }
    
    boolean canAddSingleUnclusteredPosition() {
        return this.permissionToClusterCharTreshold == 0;
    }
    
    boolean foundPositionCanBeClustered() {
        return this.foundPositionsCanBeClustered;
    }
}
