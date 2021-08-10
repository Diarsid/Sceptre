package diarsid.sceptre;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.String.format;

import static diarsid.sceptre.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.sceptre.WeightAnalyzeReal.logAnalyze;

class PositionsSearchStepTwoCluster {
    
    private static final int UNINITIALIZED = -9;
    private static final int BEFORE_START = -1;
    
    static class StepTwoClusterPositionView implements PositionIterableView {

        public StepTwoClusterPositionView(PositionsSearchStepTwoCluster cluster) {
            this.cluster = cluster;
        }
        
        private final PositionsSearchStepTwoCluster cluster;
        private int i;
        private char character;
        private int patternPosition;
        private int variantPosition;
        private boolean filled;
        private boolean filledInPattern;
        private MatchType matchType;
        private boolean filledFromSubcluster;
        
        private StepTwoClusterPositionView fill(
                char c, 
                int patternPosition, 
                int variantPosition, 
                boolean included,
                boolean includedInPattern,
                MatchType matchType) {
            this.character = c;
            this.filled = included;
            this.filledInPattern = includedInPattern;
            this.patternPosition = patternPosition;
            this.variantPosition = variantPosition;
            this.matchType = matchType;
            
            this.i = BEFORE_START;
            this.filledFromSubcluster = false;
            
            return this;
        }
        
        private boolean isBetterThan(StepTwoClusterPositionView other) {
            // TODO possible loss here - does not consider filledInPattern
            if ( this.filled && (! other.filled) ) {
                return true;
            } else if ( (! this.filled) && other.filled ) {
                return false;
            } else {
                if ( this.matchType.strength() > other.matchType.strength() ) {
                    return true;
                } else if ( this.matchType.strength() < other.matchType.strength() ) {
                    return false;
                } else {
                    throw new IllegalArgumentException("Unexpected matching!");
                }
            }
        }
        
        private void mergeInSubclusterInsteadOf(StepTwoClusterPositionView other) {
            this.i = other.i;
            
            this.cluster
                    .chars
                    .set(this.i, this.character);
            this.cluster
                    .fillings
                    .set(this.i, this.filled);
            this.cluster
                    .fillingsInPattern
                    .set(this.i, this.filledInPattern);
            this.cluster
                    .patternPositions
                    .set(this.i, this.patternPosition);
            this.cluster
                    .variantPositions
                    .set(this.i, this.variantPosition);
            this.cluster
                    .matches
                    .set(this.i, this.matchType);
            
            this.cluster.matchStrength = this.cluster.matchStrength + this.matchType.strength();            
        }

        private StepTwoClusterPositionView fillFromSubcluster(int i) {
            this.character = this.cluster.chars.get(i);
            this.filled = this.cluster.fillings.get(i);
            this.filledInPattern = this.cluster.fillingsInPattern.get(i);
            this.patternPosition = this.cluster.patternPositions.get(i);
            this.variantPosition = this.cluster.variantPositions.get(i);
            this.matchType = this.cluster.matches.get(i);
            
            this.i = i;
            this.filledFromSubcluster = true;
            
            return this;
        }
        
        @Override
        public void goToNext() {
            if ( this.hasNext() ) {
                this.i++;
                this.fillFromSubcluster(this.i);
            }   
        }
        
        @Override
        public boolean hasNext() {
            return this.i < this.cluster.variantPositions.size() - 1;
        }
        
        @Override
        public int patternPosition() {
            return this.patternPosition;
        }
        
        @Override
        public int variantPosition() {
            return this.variantPosition;
        }
        
        @Override
        public boolean isFilled() {
            return this.filled;
        }
        
        @Override
        public boolean isNotFilled() {
            return ! this.filled;
        }

        public boolean isFilledInPattern() {
            return this.filledInPattern;
        }

        public boolean isNotFilledInPattern() {
            return ! this.filledInPattern;
        }

        public boolean canBeWritten() {
            return ! this.filled && ! this.filledInPattern;
        }

        public boolean canNotBeWritten() {
            return this.filled || this.filledInPattern;
        }
    }
    
    private final List<Character> chars;
    private final List<Integer> patternPositions;
    private final List<Integer> variantPositions;
    private final List<MatchType> matches;
    private final List<Boolean> fillings;
    private final List<Boolean> fillingsInPattern;
    
    private final StepTwoClusterPositionView existingPositionView;
    private final StepTwoClusterPositionView possiblePositionView;
    
    private char assessedChar;
    private int assessedCharPatternPosition;
    private int assessedCharVariantPosition;    
    private int filledQty;
    private int matchStrength;
    private int mergedDuplicates;

    public PositionsSearchStepTwoCluster() {
        this.chars = new ArrayList<>();
        this.patternPositions = new ArrayList<>();
        this.variantPositions = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.fillings = new ArrayList<>();
        this.fillingsInPattern = new ArrayList<>();
        this.filledQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
        
        this.existingPositionView = new StepTwoClusterPositionView(this);
        this.possiblePositionView = new StepTwoClusterPositionView(this);
    }
    
    void setAssessed(char c, int patternPosition, int variantPosition) {
        this.assessedChar = c;
        this.assessedCharPatternPosition = patternPosition;
        this.assessedCharVariantPosition = variantPosition;
    }
    
    int charPatternPosition() {
        return this.assessedCharPatternPosition;
    }
    
    int charVariantPosition() {
        return this.assessedCharVariantPosition;
    }
    
    StepTwoClusterPositionView positionView() {
        return this.existingPositionView;
    }
    
    private StepTwoClusterPositionView positionViewAt(int i) {
        this.existingPositionView.fillFromSubcluster(i);
        return this.existingPositionView;
    }
    
    boolean isSet() {
        return this.chars.size() > 0;
    }
    
    void add(
            char c, 
            int patternPosition, 
            int variantPosition, 
            boolean isFilled,
            boolean isFilledInPattern,
            MatchType matchType) {
        int alreadyExistedInPattern = this.patternPositions.indexOf(patternPosition);
        if ( alreadyExistedInPattern > -1 ) {
            StepTwoClusterPositionView existingPosition = this.positionViewAt(alreadyExistedInPattern);
            StepTwoClusterPositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, isFilled, isFilledInPattern, matchType);
            logAnalyze(
                    POSITIONS_SEARCH, 
                    "          [info] positions-in-cluster duplicate: new '%s' pattern:%s, variant:%s -vs- existed '%s' pattern:%s, variant:%s",
                    possiblePosition.character, possiblePosition.patternPosition, possiblePosition.variantPosition,
                    existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);
            
            if ( possiblePosition.isBetterThan(existingPosition) ) {
                possiblePosition.mergeInSubclusterInsteadOf(existingPosition);
                logAnalyze(
                        POSITIONS_SEARCH, 
                        "          [info] positions-in-cluster duplicate: new position accepted");
            } else {
                logAnalyze(
                        POSITIONS_SEARCH, 
                        "          [info] positions-in-cluster duplicate: new position rejected");
            }
            
            this.mergedDuplicates++;
        } else {
            int alreadyExistedInVariant = this.variantPositions.indexOf(variantPosition);
            boolean writeGivenAsNew;

            if ( alreadyExistedInVariant > -1 ) {
                int patternPositionOfExistedInVariant = this.patternPositions.get(alreadyExistedInVariant);
                int diffExisting = abs(assessedCharPatternPosition - patternPositionOfExistedInVariant);
                int diffNew = abs(assessedCharPatternPosition - patternPosition);
                writeGivenAsNew = diffExisting >= diffNew;
            }
            else {
                writeGivenAsNew = true;
            }

            if ( writeGivenAsNew ) {
                this.chars.add(c);
                this.patternPositions.add(patternPosition);
                this.variantPositions.add(variantPosition);
                this.matches.add(matchType);
                this.fillings.add(isFilled);
                this.fillingsInPattern.add(isFilledInPattern);
                if ( isFilled ) {
                    this.filledQty++;
                }
                this.matchStrength = this.matchStrength + matchType.strength();
                logAnalyze(
                        POSITIONS_SEARCH,
                        "          [info] positions-in-cluster '%s' pattern:%s, variant:%s, included: %s, %s",
                        c, patternPosition, variantPosition, isFilled, matchType.name());
            }

        }        
    }
    
    boolean isBetterThan(PositionsSearchStepTwoCluster other) {
        if ( this.filledQty == 0 && other.filledQty == 0 ) {
            /* comparison of found subclusters, both are new */
            /* prefer subcluster that have more matches to fill more chars */
            if ( this.matched() > other.matched() ) {
                return true;
            } else if ( this.matched() < other.matched() ) {
                return false;
            } else {
                return this.matchStrength >= other.matchStrength;
            }
        } else {
            /* comparison of found subclusters, some subclusters have ties with already found chars */
            /* prefer subcluster that have more ties with found chars to increase consistency */
            if ( this.filledQty > other.filledQty ) {
                return true;
            } else if ( this.filledQty < other.filledQty ) {
                return false;
            } else {
                if ( this.matched() > other.matched() ) {
                    return true;
                } else if ( this.matched() < other.matched() ) {
                    return false;
                } else {
                    return this.matchStrength >= other.matchStrength;
                }
            }
        }        
    }
    
    int matched() {
        return this.chars.size() + this.mergedDuplicates;
    }
    
    void clear() {
        this.chars.clear();
        this.patternPositions.clear();
        this.variantPositions.clear();
        this.matches.clear();
        this.fillings.clear();
        this.fillingsInPattern.clear();
        this.filledQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
        this.existingPositionView.i = BEFORE_START;
        this.possiblePositionView.i = BEFORE_START;
    }
    
    @Override
    public String toString() {
        return format(
                "['%s' variant:%s, clustered:['%s', pattern:%s, variant:%s, variant-included:%s, pattern-included:%s, matches:%s]]",
                this.assessedChar, this.assessedCharVariantPosition, this.chars, this.patternPositions, this.variantPositions, this.fillings, this.fillingsInPattern, this.matches);
    }
}
