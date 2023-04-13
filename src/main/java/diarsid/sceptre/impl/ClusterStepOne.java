package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.support.exceptions.UnsupportedLogicException;
import diarsid.support.objects.references.Possible;

import static java.lang.Integer.min;

import static diarsid.sceptre.impl.AnalyzeImpl.logAnalyze;
import static diarsid.sceptre.impl.ClusterPreference.PREFER_LEFT;
import static diarsid.sceptre.impl.ClusterPreference.PREFER_RIGHT;
import static diarsid.sceptre.impl.Typos.Placing.AFTER;
import static diarsid.sceptre.impl.Typos.Placing.BEFORE;
import static diarsid.support.misc.MathFunctions.zeroIfNegative;
import static diarsid.support.objects.collections.Lists.lastFrom;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;
import static diarsid.support.strings.StringUtils.isWordsSeparator;
import static diarsid.support.strings.StringUtils.joinAll;

class ClusterStepOne {
    
    private static final int UNINITIALIZED = -5;
    private static final int BEFORE_START = -1;
    private static final int TYPO_RANGE = 3;
    
    private static class StepOneClusterPositionView implements PositionView {
        
        private final ClusterStepOne cluster;

        public StepOneClusterPositionView(ClusterStepOne cluster) {
            this.cluster = cluster;
        }

        @Override
        public int patternPosition() {
            return this.cluster.lastAddedPatternPosition;
        }

        @Override
        public int variantPosition() {
            return this.cluster.lastAddedVariantPosition;
        }
        
    }
    
    private static class StepOneClusterPositionIterableView implements PositionIterableView {
        
        private final ClusterStepOne cluster;
        private int i;

        private StepOneClusterPositionIterableView(ClusterStepOne cluster) {
            this.cluster = cluster;
            this.i = BEFORE_START;
        }
        
        @Override
        public boolean hasNext() {
            return this.i < this.cluster.allVariantPositions.size() - 1;
        }
        
        
        @Override
        public void goToNext() {
            this.i++;
        }

        @Override
        public String match() {
            return "STEP_1_DIRECT_MATCH";
        }

        @Override
        public char character() {
            throw new UnsupportedLogicException();
        }

        @Override
        public int patternPosition() {
            return this.cluster.allPatternPositions.get(this.i);
        }
        
        
        @Override
        public int variantPosition() {
            return this.cluster.allVariantPositions.get(this.i);
        }
        
        @Override
        public boolean isFilled() {
            return false;
        }
        
        @Override
        public boolean isNotFilled() {
            return true;
        }
        
    }
    
    static class PatternCluster {
        
        private final ClusterStepOne cluster;

        PatternCluster(ClusterStepOne cluster) {
            this.cluster = cluster;
        }
        
        boolean isAtPatternStart() {
            return this.firstPosition() == 0;
        }
        
        boolean isAtPatternEnd() {
            return this.cluster.pattern.orThrow().length() - 1 == this.lastPosition();
        }
        
        int firstPosition() {
            return this.cluster.allPatternPositions.get(0);
        }
        
        int lastPosition() {
            return lastFrom(this.cluster.allPatternPositions);
        }
        
        int length() {
            return this.cluster.allPatternPositions.size();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.firstPosition();
            hash = 97 * hash + this.lastPosition();
            hash = 97 * hash + this.length();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final PatternCluster other = ( PatternCluster ) obj;
            
            return 
                    this.firstPosition() == other.firstPosition() && 
                    this.lastPosition() == other.lastPosition();
        }
        
    }
    
    private final Possible<String> variant;
    private final Possible<String> pattern;
    
    private final StepOneClusterPositionIterableView positionIterableView;
    private final StepOneClusterPositionView positionView;
    private final PatternCluster patternCluster;
    
    private final List<Integer> allVariantPositions;
    private final List<Integer> allPatternPositions;
    private final List<Integer> prevsVariantPositions;
    private final List<Integer> prevsPatternPositions;
    private final List<Integer> nextsVariantPositions;
    private final List<Integer> nextsPatternPositions;
    
    private Boolean startsAfterSeparator;
    private Boolean endsBeforeSeparator;
    private Boolean variantPositionsAtStart;
    private Boolean variantPositionsAtEnd;
    
    private final Typos typos;
    
    private int prevVariantPosition;
    private int mainVariantPosition;
    private int nextVariantPosition;
    private int prevPatternPosition;
    private int mainPatternPosition;
    private int nextPatternPosition;
    private boolean hasPrevs;
    private boolean hasNexts;
    
    private int lastAddedVariantPosition;
    private int lastAddedPatternPosition;
    
    private boolean finished;
    
    private int skip;

    public ClusterStepOne() {
        this.variant = simplePossibleButEmpty();
        this.pattern = simplePossibleButEmpty();
        this.positionIterableView = new StepOneClusterPositionIterableView(this);
        this.positionView = new StepOneClusterPositionView(this);
        this.patternCluster = new PatternCluster(this);
        this.allVariantPositions = new ArrayList<>();
        this.allPatternPositions = new ArrayList<>();
        this.prevsVariantPositions = new ArrayList<>();
        this.prevsPatternPositions = new ArrayList<>();
        this.nextsVariantPositions = new ArrayList<>();
        this.nextsPatternPositions = new ArrayList<>();
        this.prevVariantPosition = UNINITIALIZED;
        this.mainVariantPosition = UNINITIALIZED;
        this.nextVariantPosition = UNINITIALIZED;
        this.prevPatternPosition = UNINITIALIZED;
        this.mainPatternPosition = UNINITIALIZED;
        this.nextPatternPosition = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.lastAddedVariantPosition = UNINITIALIZED;
        this.lastAddedPatternPosition = UNINITIALIZED;
        this.finished = true;
        this.skip = 0;
        this.variantPositionsAtStart = null;
        this.startsAfterSeparator = null;
        this.variantPositionsAtEnd = null;
        this.endsBeforeSeparator = null;
        this.typos = new Typos();
    }
    
    void incrementSkip() {
        this.skip++;
    }
    
    int skip() {
        return this.skip;
    }
    
    PositionIterableView positionIterableView() {
        this.finishIfNot();
        this.positionIterableView.i = BEFORE_START;
        return this.positionIterableView;
    }
    
    PositionView lastAdded() {
        return this.positionView;
    }
    
    PatternCluster patternCluster() {
        return this.patternCluster;
    }
    
    int possibleTypoMatches() {
        return this.typos.qtyTotal();
    }
    
    boolean doesHaveMorePossibleTypoMatchesThan(ClusterStepOne other) {
        return this.typos.qtyTotal() > other.typos.qtyTotal();
    }
    
    boolean isAtStart() {
        return this.variantPositionsAtStart;
    }
    
    boolean isAtEnd() {
        return this.variantPositionsAtEnd;
    }
    
    boolean doesStartAfterSeparator() {
        return this.startsAfterSeparator;
    }
    
    boolean doesEndBeforeSeparator() {
        return this.endsBeforeSeparator;
    }

    WordsInVariant.WordsInRange findWords(AnalyzeUnit unit) {
        return unit.wordsInVariant.wordsOfRange(this.allVariantPositions);
    }

    boolean  isPositionsAtStartOf(WordInVariant word) {
        return word.startIndex == this.allVariantPositions.get(0);
    }

    boolean  isPositionsAtEndOf(WordInVariant word) {
        return word.endIndex == this.allVariantPositions.get(this.allVariantPositions.size()-1);
    }

    boolean hasTyposBefore() {
        return this.typos.hasBefore();
    }

    boolean areTyposBeforeIn(WordInVariant word) {
        return this.typos.areBeforeIn(word);
    }

    private int firstVariantPosition() {
        int i = 0;
        int position = -1;

        while ( position < 0 ) {
            position = this.allVariantPositions.get(i);
            i++;
        }

        return position;
    }

    void tryToMergeTyposBeforeIntoPositions(WordInVariant word) {
        logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "          [info] typo merging attempt... ", "");
        Typo typo;
        int pointer = this.firstVariantPosition() - 1; // place pointer on a char in word before added in cluster
        for ( int i = this.typos.qtyBefore()-1 ; i >=0 && pointer >= word.startIndex; i-- ) {
            typo = this.typos.getBefore(i);
            if ( typo.variantIndex() == pointer ) {
                logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             [typo merge] %s (%s in variant) merged to positions", typo.patternIndex(), typo.variantIndex());
                this.mergeInPositions(typo);
                this.typos.removeFromBefore(i);
                if ( pointer == word.startIndex ) {
                    this.variantPositionsAtStart = true;
                }
                pointer--;
            }
        }
    }

    private void mergeInPositions(Typo typo) {
        this.allVariantPositions.add(0, typo.variantIndex());
        this.allPatternPositions.add(0, typo.patternIndex());
    }
    
    private void finishIfNot() {
        if ( this.finished ) {
            this.composeVariantPositions();
            this.composePatternIndexes();
            this.finished = false;
        }        
    }
    
    private void composeVariantPositions() {
        for (int i = this.prevsVariantPositions.size() - 1; i > -1; i--) {
            this.allVariantPositions.add(this.prevsVariantPositions.get(i));
        }
        if ( this.prevVariantPosition > -1 ) {
            this.allVariantPositions.add(this.prevVariantPosition);
        }
        if ( this.mainVariantPosition > -1 ) {
            this.allVariantPositions.add(this.mainVariantPosition);
        }
        if ( this.nextVariantPosition > -1 ) {
            this.allVariantPositions.add(this.nextVariantPosition);
        }
        this.allVariantPositions.addAll(this.nextsVariantPositions);
    }
    
    private void composePatternIndexes() {
        for (int i = this.prevsPatternPositions.size() - 1; i > -1; i--) {
            this.allPatternPositions.add(this.prevsPatternPositions.get(i));
        }
        if ( this.prevPatternPosition > -1 ) {
            this.allPatternPositions.add(this.prevPatternPosition);
        }
        if ( this.mainPatternPosition > -1 ) {
            this.allPatternPositions.add(this.mainPatternPosition);
        }
        if ( this.nextPatternPosition > -1 ) {
            this.allPatternPositions.add(this.nextPatternPosition);
        }
        this.allPatternPositions.addAll(this.nextsPatternPositions);
    }
    
    void finish(String variant, String pattern) {   
        this.variant.resetTo(variant);
        this.pattern.resetTo(pattern);
        this.finishIfNot();
        this.typos.set(variant, pattern);
        
        if ( this.patternCluster.isAtPatternStart() ) {
            
        } else {
            int variantClusterFirstPosition = this.firstVariantPosition();
            if ( variantClusterFirstPosition > 0 ) {
                int patternClusterFirstPosition = this.patternCluster.firstPosition();
                int patternToExcl = patternClusterFirstPosition;
                int patternFromIncl = zeroIfNegative(patternClusterFirstPosition - TYPO_RANGE);
                
                int variantToExcl = variantClusterFirstPosition;
                int variantFromIncl = zeroIfNegative(variantToExcl - TYPO_RANGE);
                
                this.typos.findIn(
                        BEFORE, 
                        variantFromIncl, variantToExcl, 
                        patternFromIncl, patternToExcl);
            }
        }
        
        if ( this.patternCluster.isAtPatternEnd() ) {
            
        } else {
            int variantClusterLastPosition = this.lastVariantPosition();
            if ( variantClusterLastPosition < variant.length() - 1 ) {
                int patternClusterLastPosition = this.patternCluster.lastPosition();
                int patternFromIncl = patternClusterLastPosition + 1;
                int patternToExcl = patternFromIncl + TYPO_RANGE;
                patternToExcl = min(patternToExcl, pattern.length());
                
                int variantFromIncl = variantClusterLastPosition + 1;
                int variantToExcl = variantFromIncl + TYPO_RANGE;
                variantToExcl = min(variantToExcl, variant.length());
                
                this.typos.findIn(
                        AFTER, 
                        variantFromIncl, variantToExcl, 
                        patternFromIncl, patternToExcl);
            }
        }
        
        this.variantPositionsAtStart = this.firstVariantPosition() == 0;
        this.variantPositionsAtEnd = this.lastVariantPosition() == variant.length() - 1;
        
        if ( this.variantPositionsAtStart ) {
            this.startsAfterSeparator = true;
        } else {
            this.startsAfterSeparator = isWordsSeparator(variant.charAt(this.firstVariantPosition() - 1));
        }
        
        if ( this.variantPositionsAtEnd ) {
            this.endsBeforeSeparator = true;
        } else {
            this.endsBeforeSeparator = isWordsSeparator(variant.charAt(this.lastVariantPosition() + 1));
        }
    }

    void copyFrom(ClusterStepOne other) {
        this.variant.resetTo(other.variant);
        this.pattern.resetTo(other.pattern);
        this.positionIterableView.i = other.positionIterableView.i;

        this.allVariantPositions.clear();
        this.prevsVariantPositions.clear();
        this.nextsVariantPositions.clear();
        this.allPatternPositions.clear();
        this.prevsPatternPositions.clear();
        this.nextsPatternPositions.clear();

        this.allVariantPositions.addAll(other.allVariantPositions);
        this.prevsVariantPositions.addAll(other.prevsVariantPositions);
        this.nextsVariantPositions.addAll(other.nextsVariantPositions);
        this.allPatternPositions.addAll(other.allPatternPositions);
        this.prevsPatternPositions.addAll(other.prevsPatternPositions);
        this.nextsPatternPositions.addAll(other.nextsPatternPositions);

        this.prevVariantPosition = other.prevVariantPosition;
        this.mainVariantPosition = other.mainVariantPosition;
        this.nextVariantPosition = other.nextVariantPosition;
        this.prevPatternPosition = other.prevPatternPosition;
        this.mainPatternPosition = other.mainPatternPosition;
        this.nextPatternPosition = other.nextPatternPosition;

        this.hasPrevs = other.hasPrevs;
        this.hasNexts = other.hasNexts;
        this.lastAddedVariantPosition = other.lastAddedVariantPosition;
        this.lastAddedPatternPosition = other.lastAddedPatternPosition;
        this.finished = other.finished;
        this.skip = other.skip;
        this.variantPositionsAtStart = other.variantPositionsAtStart;
        this.startsAfterSeparator = other.startsAfterSeparator;
        this.variantPositionsAtEnd = other.variantPositionsAtEnd;
        this.endsBeforeSeparator = other.endsBeforeSeparator;

        this.typos.clear();
        this.typos.copyFrom(other.typos);
    }
    
    static float calculateSimilarity(
            ClusterStepOne one, ClusterStepOne two) {
        int coincide = min(one.coreLength(), two.coreLength());
        float bonus;
                
        switch (coincide) {
            case 0:
            case 1: 
            case 2: {
                throw new IllegalStateException("Step one clusters length must not be less than 3!");
            }
            case 3: {
                bonus = 2.5f;
                break;
            }
            case 4: {
                bonus = 3.2f;
                break;
            }
            case 5: {
                bonus = 3.8f;
                break;
            }
            default: {
                bonus = coincide * 0.7f;
            }
        }
        
        return bonus;
    }
    
    static ClusterPreference calculateAdditionalPossibleTypoMatches(
            ClusterStepOne one, ClusterStepOne two) {
        String pattern = one.pattern.orThrow();
        String variant = one.variant.orThrow();
        PatternCluster patternCluster = one.patternCluster;
        
        int oneAdditionalMatches = 0;
        int twoAdditionalMatches = 0;
        
        boolean additionalMatchesEqual = true;
        
        int patternBackPointerFromIncl = patternCluster.firstPosition() - TYPO_RANGE - 1;
        if ( patternBackPointerFromIncl >= 0 ) { 
        
            int clusterOnePointerFromIncl = one.firstVariantPosition() - TYPO_RANGE - 1;
            int clusterTwoPointerFromIncl = two.firstVariantPosition() - TYPO_RANGE - 1;
            
            if ( clusterOnePointerFromIncl >= 0 || clusterTwoPointerFromIncl >= 0 ) {
                int patternOnePointer = patternBackPointerFromIncl;
                int patternTwoPointer = patternBackPointerFromIncl;
                int clusterOnePointer = clusterOnePointerFromIncl;
                int clusterTwoPointer = clusterTwoPointerFromIncl;
                char patternOneChar;
                char patternTwoChar;
                char variantOneChar;
                char variantTwoChar;
                boolean oneCanMove = clusterOnePointerFromIncl >= 0;
                boolean twoCanMove = clusterTwoPointerFromIncl >= 0;
                int variantOneLimit;
                int variantTwoLimit;
                if ( one.isBefore(two) ) {
                    variantOneLimit = 0;
                    variantTwoLimit = one.lastVariantPosition() + 1;
                } else {
                    variantOneLimit = two.lastVariantPosition() + 1;
                    variantTwoLimit = 0;
                }

                while ( additionalMatchesEqual && ( oneCanMove || twoCanMove ) ) {
                    
                    if ( oneCanMove ) {
                        patternOneChar = pattern.charAt(patternOnePointer);
                        variantOneChar = variant.charAt(clusterOnePointer);
                        if ( patternOneChar == variantOneChar ) {
                            oneAdditionalMatches++;
                            patternOnePointer--;
                        }
                        clusterOnePointer--;
                    }
                    
                    if ( twoCanMove ) {
                        patternTwoChar = pattern.charAt(patternTwoPointer);
                        variantTwoChar = variant.charAt(clusterTwoPointer);
                        if ( patternTwoChar == variantTwoChar ) {
                            twoAdditionalMatches++;
                            patternTwoPointer--;
                        }
                        clusterTwoPointer--;                        
                    }

                    additionalMatchesEqual = oneAdditionalMatches == twoAdditionalMatches;

                    oneCanMove = clusterOnePointer >= variantOneLimit && patternOnePointer >= 0;
                    twoCanMove = clusterTwoPointer >= variantTwoLimit && patternTwoPointer >= 0;
                }

                if ( ! additionalMatchesEqual ) {
                    if ( oneAdditionalMatches > twoAdditionalMatches ) {
                        return PREFER_LEFT;
                    } else if ( oneAdditionalMatches < twoAdditionalMatches ) {
                        return PREFER_RIGHT;
                    }
                }
            }            
        }
                
        int patternForwPointerFromIncl = patternCluster.lastPosition() + 1 + TYPO_RANGE;
        int patternLength = pattern.length();
        if ( patternForwPointerFromIncl < patternLength ) {
            
            int variantLength = variant.length();
            int clusterOnePointerFromIncl = one.lastVariantPosition() + 1 + TYPO_RANGE;
            int clusterTwoPointerFromIncl = two.lastVariantPosition() + 1 + TYPO_RANGE;
            boolean clusterOneHasSpaceAfter = clusterOnePointerFromIncl < variantLength;
            boolean clusterTwoHasSpaceAfter = clusterTwoPointerFromIncl < variantLength;
            
            if ( clusterOneHasSpaceAfter || clusterTwoHasSpaceAfter ) {
                int patternOnePointer = patternForwPointerFromIncl;
                int patternTwoPointer = patternForwPointerFromIncl;
                int clusterOnePointer = clusterOnePointerFromIncl;
                int clusterTwoPointer = clusterTwoPointerFromIncl;
                char patternOneChar;
                char patternTwoChar;
                char variantOneChar;
                char variantTwoChar;
                boolean oneCanMove = clusterOneHasSpaceAfter;
                boolean twoCanMove = clusterTwoHasSpaceAfter;
                int variantOneLimit;
                int variantTwoLimit;
                if ( one.isBefore(two) ) {
                    variantOneLimit = two.firstVariantPosition();
                    variantTwoLimit = variantLength;
                } else {
                    variantOneLimit = variantLength;
                    variantTwoLimit = one.firstVariantPosition();                    
                }
                
                while ( additionalMatchesEqual && ( oneCanMove || twoCanMove ) ) {
                    
                    if ( oneCanMove ) {
                        patternOneChar = pattern.charAt(patternOnePointer);
                        variantOneChar = variant.charAt(clusterOnePointer);
                        if ( patternOneChar == variantOneChar ) {
                            oneAdditionalMatches++;
                            patternOnePointer++;
                        }
                        clusterOnePointer++;
                    }
                    
                    if ( twoCanMove ) {
                        patternTwoChar = pattern.charAt(patternTwoPointer);
                        variantTwoChar = variant.charAt(clusterTwoPointer);
                        if ( patternTwoChar == variantTwoChar ) {
                            twoAdditionalMatches++;
                            patternTwoPointer++;
                        }
                        clusterTwoPointer++;
                    }
                    
                    additionalMatchesEqual = oneAdditionalMatches == twoAdditionalMatches;

                    oneCanMove = clusterOnePointer < variantOneLimit && patternOnePointer < patternLength;
                    twoCanMove = clusterTwoPointer < variantTwoLimit && patternTwoPointer < patternLength;
                }
            }
        }
        
        if ( oneAdditionalMatches > twoAdditionalMatches ) {
            return PREFER_LEFT;
        } else if ( oneAdditionalMatches < twoAdditionalMatches ) {
            return PREFER_RIGHT;
        } else {
            return null;
        }
    }
    
    boolean isSet() {
        return this.mainVariantPosition > -1;
    }
    
    boolean isNotSet() {
        return this.mainVariantPosition < 0;
    }
    
    void setMain(int patternP, int variantP) {
        this.lastAddedVariantPosition = variantP;
        this.lastAddedPatternPosition = patternP;
        
        this.mainVariantPosition = variantP;
        this.mainPatternPosition = patternP;
    }

    void setPrev(int prev) {
        this.lastAddedVariantPosition = prev;
        this.lastAddedPatternPosition = this.mainPatternPosition - 1;
        
        this.prevVariantPosition = this.lastAddedVariantPosition;
        this.prevPatternPosition = this.lastAddedPatternPosition;
    }

    void setNext(int next) {
        this.lastAddedVariantPosition = next;
        this.lastAddedPatternPosition = this.mainPatternPosition + 1;
        
        this.nextVariantPosition = this.lastAddedVariantPosition;
        this.nextPatternPosition = this.lastAddedPatternPosition;
    }
    
    void addNext(int nextOne) {
        if ( this.nextVariantPosition < 0 ) {
            throw new IllegalStateException();
        }
        this.hasNexts = true;
        
        this.lastAddedVariantPosition = nextOne;
        this.lastAddedPatternPosition = this.nextPatternPosition + this.nextsVariantPositions.size() + 1;
        
        this.nextsVariantPositions.add(this.lastAddedVariantPosition);
        this.nextsPatternPositions.add(this.lastAddedPatternPosition);
    }
    
    void addPrev(int prevOne) {
        if ( this.prevVariantPosition < 0 ) {
            throw new IllegalStateException();
        }
        this.hasPrevs = true;
        
        this.lastAddedVariantPosition = prevOne;
        this.lastAddedPatternPosition = this.prevPatternPosition - this.prevsVariantPositions.size() + 1;
        
        this.prevsVariantPositions.add(this.lastAddedVariantPosition);
        this.prevsPatternPositions.add(this.lastAddedPatternPosition);
    }
    
    int coreLength() {
        return this.prevsVariantPositions.size() + 3 + this.nextsVariantPositions.size();
    }
    
    boolean isBetterThan(ClusterStepOne other, WordsInVariant wordsInVariant) {
        int thisLengthWithNearTypos = this.lengthWithNearTypos();
        int otherLengthWithNearTypos = other.lengthWithNearTypos();
        
        if ( thisLengthWithNearTypos > otherLengthWithNearTypos ) {
            return true;
        }
        else if ( thisLengthWithNearTypos < otherLengthWithNearTypos ) {
            return false;
        }
        else {
            int thisTotalTypos = this.typos.qtyTotal();
            int thisLength = this.coreLength();
            int otherTotalTypos = other.typos.qtyTotal();
            int otherLength = other.coreLength();
            int thisTotalLength = thisLength + thisTotalTypos;
            int otherTotalLength = otherLength + otherTotalTypos;
            
            if ( thisTotalLength > otherTotalLength ) {
                return true;
            }
            else if ( thisTotalLength < otherTotalLength ) {
                return false;
            }
            else {
                WordsInVariant.WordsInRange thisWords = wordsInVariant.wordsOfRange(this.allVariantPositions);
                WordsInVariant.WordsInRange otherWords = wordsInVariant.wordsOfRange(other.allVariantPositions);

                if ( thisWords.hasStartIn(this.allVariantPositions) ) {
                    return true;
                }
                else if ( otherWords.hasStartIn(other.allVariantPositions) ) {
                    return false;
                }
                else {
                    return true;
                }
            }
        }
    }
    
    private int lengthWithNearTypos() {
        int length = this.coreLength();
        
        if ( this.typos.hasBefore() ) {
            int firstVariantPosition = this.firstVariantPosition();
            if ( this.typos.hasInBefore(firstVariantPosition - 1) ) {
                length = length + this.typos.qtyBefore();
            }
        }
        
        if ( this.typos.hasAfter() ) {
            int lastVariantPosition = this.lastVariantPosition();
            if ( this.typos.hasInAfter(lastVariantPosition + 1) ) {
                length = length + this.typos.qtyAfter();
            }
        }
        
        return length;
    }

//    public int firstVariantPosition() {
//        return this.allVariantPositions.get(0);
//    }
    
    public int lastVariantPosition() {
        return lastFrom(this.allVariantPositions);
    }
    
    boolean isBefore(ClusterStepOne other) {
        return this.firstVariantPosition() < other.firstVariantPosition();
    }
    
    void clear() {
        this.variant.nullify();
        this.pattern.nullify();
        this.positionIterableView.i = BEFORE_START;
        this.allVariantPositions.clear();
        this.prevsVariantPositions.clear();
        this.nextsVariantPositions.clear();
        this.allPatternPositions.clear();
        this.prevsPatternPositions.clear();
        this.nextsPatternPositions.clear();
        this.prevVariantPosition = UNINITIALIZED;
        this.mainVariantPosition = UNINITIALIZED;
        this.nextVariantPosition = UNINITIALIZED;
        this.prevPatternPosition = UNINITIALIZED;
        this.mainPatternPosition = UNINITIALIZED;
        this.nextPatternPosition = UNINITIALIZED;
        this.hasPrevs = false;
        this.hasNexts = false;
        this.lastAddedVariantPosition = UNINITIALIZED;
        this.lastAddedPatternPosition = UNINITIALIZED;
        this.finished = true;
        this.skip = 0;
        this.variantPositionsAtStart = null;
        this.startsAfterSeparator = null;
        this.variantPositionsAtEnd = null;
        this.endsBeforeSeparator = null;
        this.typos.clear();
    }
    
    @Override
    public String toString() {
        return 
                "[positions: " +
                joinAll(",", this.prevsVariantPositions, this.prevVariantPosition, this.mainVariantPosition, this.nextVariantPosition, this.nextsVariantPositions) + ", indexes: " + 
                joinAll(",", this.prevsPatternPositions, this.prevPatternPosition, this.mainPatternPosition, this.nextPatternPosition, this.nextsPatternPositions) +
                this.plusTyposString() + "]";
    }
    
    private String plusTyposString() {
        if ( this.typos.qtyTotal() > 0 ) {
            return ", poss.typos: " + this.typos;
        } else {
            return "";
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.variant);
        hash = 61 * hash + Objects.hashCode(this.pattern);
        hash = 61 * hash + Objects.hashCode(this.positionIterableView);
        hash = 61 * hash + Objects.hashCode(this.positionView);
        hash = 61 * hash + Objects.hashCode(this.patternCluster);
        hash = 61 * hash + Objects.hashCode(this.allVariantPositions);
        hash = 61 * hash + Objects.hashCode(this.allPatternPositions);
        hash = 61 * hash + Objects.hashCode(this.prevsVariantPositions);
        hash = 61 * hash + Objects.hashCode(this.prevsPatternPositions);
        hash = 61 * hash + Objects.hashCode(this.nextsVariantPositions);
        hash = 61 * hash + Objects.hashCode(this.nextsPatternPositions);
        hash = 61 * hash + Objects.hashCode(this.startsAfterSeparator);
        hash = 61 * hash + Objects.hashCode(this.endsBeforeSeparator);
        hash = 61 * hash + Objects.hashCode(this.variantPositionsAtStart);
        hash = 61 * hash + Objects.hashCode(this.variantPositionsAtEnd);
        hash = 61 * hash + Objects.hashCode(this.typos);
        hash = 61 * hash + this.prevVariantPosition;
        hash = 61 * hash + this.mainVariantPosition;
        hash = 61 * hash + this.nextVariantPosition;
        hash = 61 * hash + this.prevPatternPosition;
        hash = 61 * hash + this.mainPatternPosition;
        hash = 61 * hash + this.nextPatternPosition;
        hash = 61 * hash + (this.hasPrevs ? 1 : 0);
        hash = 61 * hash + (this.hasNexts ? 1 : 0);
        hash = 61 * hash + this.lastAddedVariantPosition;
        hash = 61 * hash + this.lastAddedPatternPosition;
        hash = 61 * hash + (this.finished ? 1 : 0);
        hash = 61 * hash + this.skip;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final ClusterStepOne other = (ClusterStepOne) obj;
        if ( this.prevVariantPosition != other.prevVariantPosition ) {
            return false;
        }
        if ( this.mainVariantPosition != other.mainVariantPosition ) {
            return false;
        }
        if ( this.nextVariantPosition != other.nextVariantPosition ) {
            return false;
        }
        if ( this.prevPatternPosition != other.prevPatternPosition ) {
            return false;
        }
        if ( this.mainPatternPosition != other.mainPatternPosition ) {
            return false;
        }
        if ( this.nextPatternPosition != other.nextPatternPosition ) {
            return false;
        }
        if ( this.hasPrevs != other.hasPrevs ) {
            return false;
        }
        if ( this.hasNexts != other.hasNexts ) {
            return false;
        }
        if ( this.lastAddedVariantPosition != other.lastAddedVariantPosition ) {
            return false;
        }
        if ( this.lastAddedPatternPosition != other.lastAddedPatternPosition ) {
            return false;
        }
        if ( this.finished != other.finished ) {
            return false;
        }
        if ( this.skip != other.skip ) {
            return false;
        }
        if ( !Objects.equals(this.variant, other.variant) ) {
            return false;
        }
        if ( !Objects.equals(this.pattern, other.pattern) ) {
            return false;
        }
        if ( !Objects.equals(this.positionIterableView, other.positionIterableView) ) {
            return false;
        }
        if ( !Objects.equals(this.positionView, other.positionView) ) {
            return false;
        }
        if ( !Objects.equals(this.patternCluster, other.patternCluster) ) {
            return false;
        }
        if ( !Objects.equals(this.allVariantPositions, other.allVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.allPatternPositions, other.allPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.prevsVariantPositions, other.prevsVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.prevsPatternPositions, other.prevsPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.nextsVariantPositions, other.nextsVariantPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.nextsPatternPositions, other.nextsPatternPositions) ) {
            return false;
        }
        if ( !Objects.equals(this.startsAfterSeparator, other.startsAfterSeparator) ) {
            return false;
        }
        if ( !Objects.equals(this.endsBeforeSeparator, other.endsBeforeSeparator) ) {
            return false;
        }
        if ( !Objects.equals(this.variantPositionsAtStart, other.variantPositionsAtStart) ) {
            return false;
        }
        if ( !Objects.equals(this.variantPositionsAtEnd, other.variantPositionsAtEnd) ) {
            return false;
        }
        if ( !Objects.equals(this.typos, other.typos) ) {
            return false;
        }
        return true;
    }
    
}
