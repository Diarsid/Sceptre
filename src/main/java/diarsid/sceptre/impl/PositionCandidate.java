/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre.impl;

import diarsid.sceptre.impl.logs.AnalyzeLogType;

import static java.lang.Integer.max;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.percentAsInt;

/**
 *
 * @author Diarsid
 */
class PositionCandidate {
    
    private static final int UNINITIALIZED = -9;
    private static final boolean CURRENT_IS_BETTER = false;
    private static final boolean CURRENT_IS_WORSE = true;
    
    private final AnalyzeUnit data;
    private int position;
    private int orderDiffInPattern;
    private int orderDiffInVariant;
    private int placementDiff;
    private boolean isNearSeparator;
    private Integer distanceToNearestFilledPosition;
    private int clusteredAround;
    private int mutationsCommitted;
    private int mutationsAttempts;

    public PositionCandidate(AnalyzeUnit data) {
        this.data = data;
        this.position = UNINITIALIZED;
        this.orderDiffInPattern = UNINITIALIZED;
        this.orderDiffInVariant = UNINITIALIZED;
        this.placementDiff = UNINITIALIZED;
        this.isNearSeparator = false;
        this.distanceToNearestFilledPosition = null;
        this.clusteredAround = UNINITIALIZED;
        this.mutationsCommitted = 0;
        this.mutationsAttempts = 0;
    }
    
    void tryToMutate(
            int position, 
            int positionInPatternIndex, 
            int orderDiffInVariant, 
            int orderDiffInPattern, 
            int clusteredAround, 
            boolean isNearSeparator,
            Integer distanceToNearestFilledPosition,
            int charsRemained) {
        this.mutationsAttempts++;
        
        int variantPlacement = percentAsInt(position, this.data.variant.length()) / 10;
        int patternPlacement = percentAsInt(positionInPatternIndex, this.data.pattern.length()) / 10;
        int otherPlacementDiff = absDiff(variantPlacement, patternPlacement);
        
        if (AnalyzeLogType.POSITIONS_SEARCH.isEnabled()) {
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "          [info] candidate %s in variant has:", position);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             pattern order diff     %s", orderDiffInPattern == UNINITIALIZED ? "_" : orderDiffInPattern);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             variant order diff     %s", orderDiffInVariant == UNINITIALIZED ? "_" : orderDiffInVariant);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             approx. placement diff %s%%", otherPlacementDiff);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             is near separator      %s", isNearSeparator);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             to nearest position    %s", isNull(distanceToNearestFilledPosition) ? "_" : distanceToNearestFilledPosition);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             clustered positions    %s", clusteredAround);
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             chars remained         %s", charsRemained);
        }
        
        /* DEBUG */ if ( position == 12 ) {
        /* DEBUG */     int a = 5;
        /* DEBUG */ }
        if ( this.isCurrentStateWorseThan(
                orderDiffInVariant, 
                orderDiffInPattern, 
                otherPlacementDiff, 
                isNearSeparator, 
                distanceToNearestFilledPosition, 
                clusteredAround) ) {
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "          [info] accept %s", position);
            this.position = position;
            this.orderDiffInPattern = orderDiffInPattern;
            this.orderDiffInVariant = orderDiffInVariant;
            this.placementDiff = otherPlacementDiff;
            this.isNearSeparator = isNearSeparator;
            this.distanceToNearestFilledPosition = distanceToNearestFilledPosition;
            this.clusteredAround = clusteredAround;
            this.mutationsCommitted++;
        } else {
            logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "          [info] worse than %s, reject %s", this.position, position);
        }       
    }
    
    private boolean isCurrentStateWorseThan(
            int otherOrderDiffInVariant, 
            int otherOrderDiffInPattern, 
            int otherPlacementDiff,
            boolean otherIsNearSeparator,
            Integer otherDistanceToNearestFilledPosition,
            int otherClusteredAround) {
        if ( this.position == UNINITIALIZED ) {
            return CURRENT_IS_WORSE;
        }
        
        if ( this.orderDiffInPattern == 1 && this.orderDiffInVariant == 1 ) {
            if ( otherOrderDiffInPattern == 1 && otherOrderDiffInVariant == 1 ) {
                if ( otherClusteredAround > this.clusteredAround ) {
                    return CURRENT_IS_WORSE;
                } else {
                    return CURRENT_IS_BETTER;
                }
            }
        }
        
        if ( isNull(this.distanceToNearestFilledPosition) || this.distanceToNearestFilledPosition < 0 ) {
            if ( this.isNearSeparator != otherIsNearSeparator ) {
                return compareDependingOnNearSeparator(
                        otherOrderDiffInPattern, 
                        otherClusteredAround, 
                        otherOrderDiffInVariant, 
                        otherPlacementDiff);
            } else {
                return compareAsUsual(
                        otherOrderDiffInPattern, 
                        otherOrderDiffInVariant, 
                        otherPlacementDiff, 
                        otherClusteredAround);
            }
        } else {
            if ( this.distanceToNearestFilledPosition.equals(otherDistanceToNearestFilledPosition) ) {
                if ( this.isNearSeparator != otherIsNearSeparator ) {
                    return compareDependingOnNearSeparator(
                            otherOrderDiffInPattern, 
                            otherClusteredAround, 
                            otherOrderDiffInVariant, 
                            otherPlacementDiff);
                } else {
                    return compareAsUsual(
                            otherOrderDiffInPattern, 
                            otherOrderDiffInVariant, 
                            otherPlacementDiff, 
                            otherClusteredAround);
                }
            } else {
                if ( this.isNearSeparator != otherIsNearSeparator ) {
                    return compareDependingOnNearSeparatorAndDistanceToFilledPosition(
                            otherOrderDiffInPattern, 
                            otherClusteredAround, 
                            otherOrderDiffInVariant, 
                            otherPlacementDiff,
                            otherDistanceToNearestFilledPosition);
                } else {
                    return compareDependingOnDistanceToFilledPosition(
                            otherOrderDiffInPattern, 
                            otherClusteredAround, 
                            otherOrderDiffInVariant, 
                            otherPlacementDiff,
                            otherDistanceToNearestFilledPosition);
                }
            }
        }      
    }
    
    private boolean compareDependingOnDistanceToFilledPosition(
            int otherOrderDiffInPattern, 
            int otherClusteredAround, 
            int otherOrderDiffInVariant, 
            int otherOrderDiffAbsolute,
            int otherDistanceToNearestFilledPosition) {
        int thisSum =
                zeroIfNotInit(this.orderDiffInPattern) +
                zeroIfNotInit(this.orderDiffInVariant) +
                max(this.placementDiff, this.distanceToNearestFilledPosition) -
                this.clusteredAround;
        int otherSum =
                zeroIfNotInit(otherOrderDiffInPattern) +
                zeroIfNotInit(otherOrderDiffInVariant) +
                max(otherOrderDiffAbsolute, otherDistanceToNearestFilledPosition) - 
                otherClusteredAround;
        
        if ( thisSum > otherSum ) {
            return CURRENT_IS_WORSE;
        } else {
            return CURRENT_IS_BETTER;
        }
    }
    
    private boolean compareDependingOnNearSeparatorAndDistanceToFilledPosition(
            int otherOrderDiffInPattern, 
            int otherClusteredAround, 
            int otherOrderDiffInVariant, 
            int otherPlacementDiff,
            int otherDistanceToNearestFilledPosition) {
        int thisSum = zeroIfNotInit(this.orderDiffInPattern) + 
                      this.distanceToNearestFilledPosition - 
                      this.clusteredAround;
        int otherSum = zeroIfNotInit(otherOrderDiffInPattern) + 
                       otherDistanceToNearestFilledPosition - 
                       otherClusteredAround;
        
        if ( this.isNearSeparator ) {
            otherSum = otherSum + zeroIfNotInit(otherOrderDiffInVariant) + otherPlacementDiff;
        } else {
            thisSum = thisSum + zeroIfNotInit(this.orderDiffInVariant) + this.placementDiff;
        }
        
        if ( thisSum > otherSum ) {
            return CURRENT_IS_WORSE;
        } else {
            return CURRENT_IS_BETTER;
        }
    }

    private boolean compareDependingOnNearSeparator(
            int otherOrderDiffInPattern, 
            int otherClusteredAround, 
            int otherOrderDiffInVariant, 
            int otherPlacementDiff) {
        int thisSum = zeroIfNotInit(this.orderDiffInPattern) - this.clusteredAround;
        int otherSum = zeroIfNotInit(otherOrderDiffInPattern) - otherClusteredAround;
        
        if ( this.isNearSeparator ) {
            otherSum = otherSum + zeroIfNotInit(otherOrderDiffInVariant) + otherPlacementDiff;
        } else {
            thisSum = thisSum + zeroIfNotInit(this.orderDiffInVariant) + this.placementDiff;
        }
        
        if ( thisSum > otherSum ) {
            return CURRENT_IS_WORSE;
        } else {
            return CURRENT_IS_BETTER;
        }
    }
 
    private boolean compareAsUsual(
            int otherOrderDiffInPattern, 
            int otherOrderDiffInVariant, 
            int otherOrderDiffAbsolute, 
            int otherClusteredAround) {
        int thisSum =
                zeroIfNotInit(this.orderDiffInPattern) +
                zeroIfNotInit(this.orderDiffInVariant) +
                this.placementDiff -
                this.clusteredAround;
        int otherSum =
                zeroIfNotInit(otherOrderDiffInPattern) +
                zeroIfNotInit(otherOrderDiffInVariant) +
                otherOrderDiffAbsolute - 
                otherClusteredAround;
        
        if ( thisSum > otherSum ) {
            return CURRENT_IS_WORSE;
        } else {
            return CURRENT_IS_BETTER;
        }        
    }
    
    boolean isPresent() {
        return this.position != UNINITIALIZED;
    }
    
    int position() {
        return this.position;
    }
    
    int committedMutations() {
        return this.mutationsCommitted;
    }
    
    int mutationAttempts() {
        return this.mutationsAttempts;
    }
    
    boolean hasAtLeastOneAcceptedCandidate() {
        return this.mutationsCommitted > 0;
    }
    
    boolean hasRejectedMutations() {
        return this.mutationsAttempts > this.mutationsCommitted;
    }
    
    void clear() {
        this.position = UNINITIALIZED;
        this.orderDiffInPattern = UNINITIALIZED;
        this.orderDiffInVariant = UNINITIALIZED;
        this.placementDiff = UNINITIALIZED;
        this.isNearSeparator = false;
        this.distanceToNearestFilledPosition = null;
        this.clusteredAround = UNINITIALIZED;
        this.mutationsAttempts = 0;
        this.mutationsCommitted = 0;
    }

    @Override
    public String toString() {
        if ( this.position == UNINITIALIZED ) {
            return "PositionCandidate[UNINITIALIZED]";
        }
        
        return format("PositionCandidate[pos:%s clusteredAround:%s mutations:[attemtps:%s committed:%s] ]", 
                      this.position, this.clusteredAround, this.mutationsAttempts, this.mutationsCommitted);
    }
    
    private static int zeroIfNotInit(int value) {
        if ( value == UNINITIALIZED ) {
            return 0;
        } 
        return value;
    }
    
}
