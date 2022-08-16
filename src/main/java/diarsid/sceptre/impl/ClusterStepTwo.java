package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.sceptre.impl.logs.AnalyzeLogType;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.abs;
import static java.lang.String.format;

import static diarsid.sceptre.impl.MatchType.MATCH_DIRECTLY;
import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.misc.MathFunctions.absDiff;

class ClusterStepTwo {
    
    private static final int UNINITIALIZED = -9;
    private static final int BEFORE_START = -1;
    
    static class StepTwoClusterPositionView implements PositionIterableView {

        public StepTwoClusterPositionView(ClusterStepTwo cluster) {
            this.cluster = cluster;
        }
        
        private final ClusterStepTwo cluster;
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

        private boolean is(int otherPatternPosition, int otherVariantPosition) {
            return
                    this.patternPosition == otherPatternPosition &&
                    this.variantPosition == otherVariantPosition;
        }

        private static int multiplyIfNegative(int i, int muptiplicator) {
            if ( i < 0 ) {
                return i * -1 * muptiplicator;
            }
            else {
                return i;
            }
        }

        private int countDiffWith(int otherPatternPosition, int otherVariantPosition) {
            int patternDiff;
            int variantDiff;

            if ( this.patternPosition > otherPatternPosition ) {
                patternDiff = this.patternPosition - otherPatternPosition - 1;
                variantDiff = this.variantPosition - otherVariantPosition - 1;
            }
            else {
                patternDiff = otherPatternPosition - this.patternPosition - 1;
                variantDiff = otherVariantPosition - this.variantPosition - 1;
            }

            return multiplyIfNegative(patternDiff, 2) + multiplyIfNegative(variantDiff, 2);
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
                    int thisPatternDiff = absDiff(this.patternPosition, this.cluster.assessedCharPatternPosition);
                    int otherPatternDiff = absDiff(other.patternPosition, other.cluster.assessedCharPatternPosition);
                    int thisVariantDiff = absDiff(this.variantPosition, this.cluster.assessedCharVariantPosition);
                    int otherVariantDiff = absDiff(other.variantPosition, other.cluster.assessedCharVariantPosition);

                    if ( thisPatternDiff == otherPatternDiff ) {
                        if ( thisVariantDiff == otherVariantDiff ) {
                            throw new IllegalArgumentException("Unexpected matching!");
                        }
                        else if ( thisVariantDiff < otherVariantDiff ) {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    else if ( thisPatternDiff > otherPatternDiff ) {
                        return false;
                    }
                    else {
                        return true;
                    }
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

        public boolean isSameCharAs(StepTwoClusterPositionView other) {
            return character == other.character &&
                    patternPosition == other.patternPosition &&
                    variantPosition == other.variantPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StepTwoClusterPositionView)) return false;
            StepTwoClusterPositionView that = (StepTwoClusterPositionView) o;
            return i == that.i &&
                    character == that.character &&
                    patternPosition == that.patternPosition &&
                    variantPosition == that.variantPosition &&
                    filled == that.filled &&
                    filledInPattern == that.filledInPattern &&
                    filledFromSubcluster == that.filledFromSubcluster &&
                    cluster.equals(that.cluster) &&
                    matchType == that.matchType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cluster, i, character, patternPosition, variantPosition, filled, filledInPattern, matchType, filledFromSubcluster);
        }
    }

    private final PositionsAnalyze analyze;
    private final List<Character> chars;
    private final List<Integer> patternPositions;
    private final List<Integer> variantPositions;
    private final List<Boolean> candidates;
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

    public ClusterStepTwo(PositionsAnalyze analyze) {
        this.analyze = analyze;
        this.chars = new ArrayList<>();
        this.patternPositions = new ArrayList<>();
        this.variantPositions = new ArrayList<>();
        this.candidates = new ArrayList<>();
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
        this.existingPositionView.i = -1;
        return this.existingPositionView;
    }
    
    private StepTwoClusterPositionView positionViewAt(int i) {
        this.existingPositionView.fillFromSubcluster(i);
        return this.existingPositionView;
    }
    
    boolean hasChars() {
        return this.chars.size() > 0;
    }

    void add(
            char c,
            int patternPosition,
            int variantPosition,
            boolean isFilled,
            boolean isFilledInPattern,
            MatchType matchType) {
        this.addInternal(c, patternPosition, variantPosition, isFilled, isFilledInPattern, matchType, FALSE);
    }

    void addAsCandidate(
            char c,
            int patternPosition,
            int variantPosition,
            boolean isFilled,
            boolean isFilledInPattern,
            MatchType matchType) {
        this.addInternal(c, patternPosition, variantPosition, isFilled, isFilledInPattern, matchType, TRUE);
    }

    void approveCandidates() {
        logAnalyze(
                AnalyzeLogType.POSITIONS_SEARCH,
                "          [info] approve candidates ");
        for ( int i = 0; i < this.candidates.size(); i++ ) {
            if ( this.candidates.get(i) ) {
                this.candidates.set(i, FALSE);
            }
        }
    }

    void rejectCandidates() {
        for ( int i = 0; i < this.candidates.size(); i++ ) {
            if ( this.candidates.get(i) ) {
                this.remove(i);
                i--;
            }
        }
    }

    boolean hasBackwardTypos() {
        int position;
        for ( int i = 0; i < this.variantPositions.size(); i++ ) {
            position = this.variantPositions.get(i);
            if ( position < this.assessedCharVariantPosition ) {
                return true;
            }
        }
        return false;
    }

    private void remove(int i) {
        char c = this.chars.remove(i);
        int patternPosition = this.patternPositions.remove(i);
        int variantPosition = this.variantPositions.remove(i);
        MatchType matchType = this.matches.remove(i);
        boolean isFilled = this.fillings.remove(i);
        this.fillingsInPattern.remove(i);
        this.candidates.remove(i);
        if ( isFilled ) {
            this.filledQty--;
        }
        this.matchStrength = this.matchStrength - matchType.strength();
        logAnalyze(
                AnalyzeLogType.POSITIONS_SEARCH,
                "          [info] reject candidate '%s' pattern:%s, variant:%s, included: %s, %s",
                c, patternPosition, variantPosition, isFilled, matchType.name());
    }
    
    private void addInternal(
            char c, 
            int patternPosition, 
            int variantPosition, 
            boolean isFilled,
            boolean isFilledInPattern,
            MatchType matchType,
            Boolean isCandidate) {
        int alreadyExistedInPattern = this.patternPositions.indexOf(patternPosition);

        if ( isFilledInPattern ) {
            int cPositionInVariant = this.analyze.positions[patternPosition];
            if ( cPositionInVariant != variantPosition ) {
                WordInVariant wordOfFilledChar = this.analyze.data.wordsInVariant.wordOf(cPositionInVariant);
                WordInVariant wordAnother = this.analyze.data.wordsInVariant.wordOf(variantPosition);

                if ( ! wordOfFilledChar.equals(wordAnother) ) {
                    int distance = abs(wordOfFilledChar.index - wordAnother.index);
                    if ( distance > 1 ) {
                        logAnalyze(
                                AnalyzeLogType.POSITIONS_SEARCH,
                                "          [info] positions-in-cluster false-positive '%s' pattern:%s, variant:%s, included: %s, %s",
                                c, patternPosition, variantPosition, isFilled, matchType.name());
                        return;
                    }
                }
            }
        }

        if ( alreadyExistedInPattern > -1 ) {
            StepTwoClusterPositionView existingPosition = this.positionViewAt(alreadyExistedInPattern);
            StepTwoClusterPositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, isFilled, isFilledInPattern, matchType);

            logAnalyze(
                    AnalyzeLogType.POSITIONS_SEARCH,
                    "          [info] positions-in-cluster duplicate: new '%s' pattern:%s, variant:%s -vs- existed '%s' pattern:%s, variant:%s",
                    possiblePosition.character, possiblePosition.patternPosition, possiblePosition.variantPosition,
                    existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);

            if ( existingPosition.isSameCharAs(possiblePosition) ) {

            }
            else {
                if ( possiblePosition.isBetterThan(existingPosition) ) {
                    possiblePosition.mergeInSubclusterInsteadOf(existingPosition);
                    logAnalyze(
                            AnalyzeLogType.POSITIONS_SEARCH,
                            "          [info] positions-in-cluster duplicate: new position accepted");
                } else {
                    logAnalyze(
                            AnalyzeLogType.POSITIONS_SEARCH,
                            "          [info] positions-in-cluster duplicate: new position rejected");
                }

            }
            this.mergedDuplicates++;
        }
        else {
            int alreadyExistedInVariant = this.variantPositions.indexOf(variantPosition);
            boolean writeGivenAsNew;

            if ( alreadyExistedInVariant > -1 ) {
                int patternPositionOfExistedInVariant = this.patternPositions.get(alreadyExistedInVariant);

                int diffInPatternExisting = abs(assessedCharPatternPosition - patternPositionOfExistedInVariant);
                int diffInPatternNew = abs(assessedCharPatternPosition - patternPosition);

                if ( this.chars.size() > 1 ) {
                    StepTwoClusterPositionView existingPosition = this.positionViewAt(alreadyExistedInVariant);
                    StepTwoClusterPositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, isFilled, isFilledInPattern, matchType);

                    int testPatternPosition;
                    int testVariantPosition;
                    int diffWithExisting;
                    int diffWithPossible;
                    int diffWithExistingSum = 0;
                    int diffWithPossibleSum = 0;
                    for ( int i = 0; i < chars.size(); i++) {
                        testPatternPosition = patternPositions.get(i);
                        testVariantPosition = variantPositions.get(i);
                        if ( existingPosition.is(testPatternPosition, testVariantPosition) ) {
                            continue;
                        }

                        diffWithExisting = existingPosition.countDiffWith(testPatternPosition, testVariantPosition);
                        diffWithPossible = possiblePosition.countDiffWith(testPatternPosition, testVariantPosition);

                        diffWithExistingSum = diffWithExistingSum + diffWithExisting;
                        diffWithPossibleSum = diffWithPossibleSum + diffWithPossible;
                    }
                    writeGivenAsNew =
                            diffInPatternExisting + diffWithExistingSum >= diffInPatternNew + diffWithPossibleSum;

                    if ( writeGivenAsNew ) {
                        logAnalyze(
                                AnalyzeLogType.POSITIONS_SEARCH,
                                "          [info] positions-in-cluster is garbage: '%s' pattern:%s, variant:%s",
                                existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);
                        this.remove(alreadyExistedInVariant);
                        this.analyze.garbagePatternPositions.add(existingPosition.patternPosition);
                    }
                }
                else {
                    writeGivenAsNew =
                            diffInPatternExisting >= diffInPatternNew;
                }
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
                this.candidates.add(isCandidate);
                if ( isFilled ) {
                    this.filledQty++;
                }
                this.matchStrength = this.matchStrength + matchType.strength();
                logAnalyze(
                        AnalyzeLogType.POSITIONS_SEARCH,
                        "          [info] positions-in-cluster '%s' pattern:%s, variant:%s, included: %s, %s, candidate:%s",
                        c, patternPosition, variantPosition, isFilled, matchType.name(), isCandidate);
            }
        }        
    }

    private int countDirectMatches() {
        if ( this.matches.isEmpty() ) {
            return 0;
        }

        if ( this.matches.size() == 1 ) {
            return this.matches.get(0).is(MATCH_DIRECTLY) ? 1 : 0;
        }

        int count = 0;
        for (int i = 0; i < this.matches.size(); i++) {
            if ( this.matches.get(i).is(MATCH_DIRECTLY) ) {
                count++;
            }
        }

        return count;
    }
    
    boolean isBetterThan(ClusterStepTwo other) {
        if ( this.filledQty == 0 && other.filledQty == 0 ) {
            /* comparison of found subclusters, both are new */
            /* prefer subcluster that have more matches to fill more chars */
            int thisMatchDirectly = this.countDirectMatches();
            int otherMatchDirectly = other.countDirectMatches();

            if ( thisMatchDirectly > otherMatchDirectly ) {
                return true;
            }
            else if ( thisMatchDirectly < otherMatchDirectly ) {
                return false;
            }

            if ( this.matched() > other.matched() ) {
                return true;
            } else if ( this.matched() < other.matched() ) {
                return false;
            } else {
                WordInVariant thisAssessedCharWord = this.analyze.data.wordsInVariant.wordOf(this.assessedCharVariantPosition);
                WordInVariant otherAssessedCharWord = other.analyze.data.wordsInVariant.wordOf(other.assessedCharVariantPosition);

                int thisAssessedCharWordIntersections = thisAssessedCharWord.intersections(this.analyze.filledPositions);
                int otherAssessedCharWordIntersections = otherAssessedCharWord.intersections(other.analyze.filledPositions);

                int thisMatchStrength = this.matchStrength + thisAssessedCharWordIntersections;
                int otherMatchStrength = other.matchStrength + otherAssessedCharWordIntersections;

                if ( thisMatchStrength > otherMatchStrength ) {
                    return true;
                }
                else if ( thisMatchStrength < otherMatchStrength ) {
                    return false;
                }
//                if ( this.matchStrength > other.matchStrength ) {
//                    return true;
//                }
//                else if ( this.matchStrength < other.matchStrength ) {
//                    return false;
//                }
                else {
                    int thisDiff = this.calculatedDiff();
                    int otherDiff = other.calculatedDiff();

                    if ( thisDiff < otherDiff ) {
                        return true;
                    }
                    else if ( thisDiff > otherDiff ) {
                        return false;
                    }
                    else {
                        WordsInVariant.WordsInRange thisPositionsWords = this.analyze.data.wordsInVariant.wordsOfRange(this.variantPositions);
                        WordsInVariant.WordsInRange otherPositionsWords = other.analyze.data.wordsInVariant.wordsOfRange(other.variantPositions);

                        if ( thisPositionsWords.areNotEmpty() && otherPositionsWords.areEmpty() ) {
                            return true;
                        }
                        else if ( thisPositionsWords.areEmpty() && otherPositionsWords.areNotEmpty() ) {
                            return false;
                        }
                        else {
                            int thisIntersections = thisPositionsWords.intersections(this.analyze.filledPositions);
                            int otherIntersections = otherPositionsWords.intersections(this.analyze.filledPositions);

                            if ( thisIntersections > otherIntersections ) {
                                return true;
                            }
                            else if ( thisIntersections < otherIntersections ) {
                                return false;
                            }
                            else {
                                if ( thisPositionsWords.independents() > otherPositionsWords.independents() ) {
                                    return true;
                                }
                                else if ( thisPositionsWords.independents() < otherPositionsWords.independents() ) {
                                    return false;
                                }
                                else {
                                    if ( this.mergedDuplicates > other.mergedDuplicates ) {
                                        return true;
                                    }
                                    else if ( this.mergedDuplicates < other.mergedDuplicates ) {
                                        return false;
                                    }
                                    else {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
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
                    if ( this.matchStrength > other.matchStrength ) {
                        return true;
                    }
                    else if ( this.matchStrength < other.matchStrength ) {
                        return false;
                    }
                    else {
                        if ( this.mergedDuplicates > other.mergedDuplicates ) {
                            return true;
                        }
                        else if ( this.mergedDuplicates < other.mergedDuplicates ) {
                            return false;
                        }
                        else {
                            return false;
                        }
                    }
                }
            }
        }        
    }

    private int calculatedDiff() {
        if ( chars.size() == 1 ) {
            int patternDiff = absDiff(this.assessedCharPatternPosition, patternPositions.get(0));
            int variantDiff = absDiff(this.assessedCharVariantPosition, variantPositions.get(0));
            return patternDiff + variantDiff;
        }
        else {
            int patternDiff = 0;
            int variantDiff = 0;
            int patternDiffSum = 0;
            int variantDiffSum = 0;
            for (int i = 0; i < chars.size()-1; i++ ) {
                patternDiff = patternPositions.get(i + 1) - patternPositions.get(i);
                if ( patternDiff < 0 ) {
                    patternDiff = patternDiff * -2;
                }
                patternDiffSum = patternDiffSum + patternDiff;

                variantDiff = variantPositions.get(i + 1) - variantPositions.get(i);
                if ( variantDiff < 0 ) {
                    variantDiff = variantDiff * -2;
                }
                variantDiffSum = variantDiffSum + variantDiff;
            }
            return patternDiffSum + variantDiffSum;
        }
    }
    
    int matched() {
        return this.chars.size() ;
//        + this.mergedDuplicates;
    }
    
    void clear() {
        this.chars.clear();
        this.patternPositions.clear();
        this.variantPositions.clear();
        this.candidates.clear();
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
