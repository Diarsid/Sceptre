package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import diarsid.sceptre.impl.collections.ListChar;
import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListCharImpl;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.support.objects.CommonEnum;
import diarsid.support.objects.references.Possible;
import diarsid.support.objects.references.References;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import static diarsid.sceptre.api.LogType.POSITIONS_SEARCH;
import static diarsid.sceptre.impl.ClusterStepTwo.FoundType.CHOSEN_WORD;
import static diarsid.sceptre.impl.ClusterStepTwo.FoundType.NEW_WORD;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_OTHER_WORD;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_MIDDLE;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_MIDDLE_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_START;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_START_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_START_MIDDLE;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.CHOSEN_WORD_START_MIDDLE_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_OTHER_WORD;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_MIDDLE;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_MIDDLE_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_START;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_START_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_START_MIDDLE;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.NEW_WORD_START_MIDDLE_END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsInWordType.PRIORITY_DIFF;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsPlacing.END;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsPlacing.MIDDLE;
import static diarsid.sceptre.impl.ClusterStepTwo.PositionsPlacing.START;
import static diarsid.sceptre.impl.MatchType.MATCH_DIRECTLY;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_LOOP;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT;
import static diarsid.sceptre.impl.WordInVariant.Placing.DEPENDENT;
import static diarsid.sceptre.impl.WordInVariant.Placing.INDEPENDENT;
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
                        else {
                            boolean thisHasDirectSubcluster = this.cluster.contains(this.patternPosition-1, this.variantPosition-1);
                            boolean otherHasDirectSubcluster = other.cluster.contains(other.patternPosition-1, other.variantPosition-1);

                            if ( thisHasDirectSubcluster && otherHasDirectSubcluster ) {
                                if ( thisVariantDiff < otherVariantDiff ) {
                                    return true;
                                }
                                else {
                                    return false;
                                }
                            }
                            else if ( thisHasDirectSubcluster ) {
                                return true;
                            }
                            else {
                                return false;
                            }
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
                    .fillingsInVariant
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
            this.filled = this.cluster.fillingsInVariant.get(i);
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
        public char character() {
            return this.character;
        }

        @Override
        public String match() {
            return this.matchType.name();
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
    private final ListChar chars;
    private final ListInt patternPositions;
    private final ListInt variantPositions;
    private final List<Boolean> candidates;
    private final List<MatchType> matches;
    private final List<Boolean> fillingsInVariant;
    private final List<Boolean> fillingsInPattern;
    
    private final StepTwoClusterPositionView existingPositionView;
    private final StepTwoClusterPositionView possiblePositionView;
    
    private char assessedChar;
    private int assessedCharPatternPosition;
    private int assessedCharVariantPosition;
    private boolean assessedCharFilledInVariant;
    private int filledInVariantQty;
    private int matchStrength;
    private int mergedDuplicates;
    private final Possible<WordInVariant> word;
    private final Possible<PositionsInWordType> positionsInWordType;
    private int directMatchesCount;

    public ClusterStepTwo(PositionsAnalyze analyze) {
        this.analyze = analyze;
        this.chars = new ListCharImpl();
        this.patternPositions = new ListIntImpl();
        this.variantPositions = new ListIntImpl();
        this.candidates = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.fillingsInVariant = new ArrayList<>();
        this.fillingsInPattern = new ArrayList<>();
        this.filledInVariantQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
        this.word = References.simplePossibleButEmpty();
        this.positionsInWordType = References.simplePossibleButEmpty();
        this.directMatchesCount = 0;
        
        this.existingPositionView = new StepTwoClusterPositionView(this);
        this.possiblePositionView = new StepTwoClusterPositionView(this);
    }
    
    void setAssessed(char c, int patternPosition, int variantPosition) {
        this.word.resetTo(this.analyze.data.wordsInVariant.wordOf(variantPosition));
        this.assessedChar = c;
        this.assessedCharFilledInVariant = this.analyze.filledPositions.contains(variantPosition);
        this.assessedCharPatternPosition = patternPosition;
        this.assessedCharVariantPosition = variantPosition;
        this.analyze.data.log.add(
                POSITIONS_SEARCH,
                "          [info] positions-in-cluster set assessed '%s' pattern:%s, variant:%s",
                c, patternPosition, variantPosition);
    }

    void setAssessed(WordInVariant word, char c, int patternPosition, int variantPosition) {
        this.word.resetTo(word);
        this.assessedChar = c;
        this.assessedCharFilledInVariant = this.analyze.filledPositions.contains(variantPosition);
        this.assessedCharPatternPosition = patternPosition;
        this.assessedCharVariantPosition = variantPosition;
        this.analyze.data.log.add(
                POSITIONS_SEARCH,
                "          [info] positions-in-cluster set assessed '%s' pattern:%s, variant:%s",
                c, patternPosition, variantPosition);
    }

    char assessedChar() {
        return this.assessedChar;
    }

    WordInVariant word() {
        return this.word.orThrow();
    }

    PositionsInWordType positionsInWordType() {
        return this.positionsInWordType.orThrow();
    }
    
    int assessedCharPatternPosition() {
        return this.assessedCharPatternPosition;
    }
    
    int assessedCharVariantPosition() {
        return this.assessedCharVariantPosition;
    }

    String assessedCharBestMatch() {
        MatchType match = null;
        for ( MatchType iMatch : this.matches ) {
            if ( match == null ) {
                match = iMatch;
            }
            else {
                if ( match.strength() < iMatch.strength() ) {
                    match = iMatch;
                }
            }
        }

        if ( match == null ) {
            return "STEP_2_ASSESSED_CHAR_MATCH_UNKONWN";
        }
        else {
            return match.name();
        }
    }

    boolean isAssessedCharFilledInVariant() {
        return this.assessedCharFilledInVariant;
    }
    
    StepTwoClusterPositionView positionView() {
        this.existingPositionView.i = -1;
        return this.existingPositionView;
    }
    
    private StepTwoClusterPositionView positionViewAt(int i) {
        this.existingPositionView.fillFromSubcluster(i);
        return this.existingPositionView;
    }

    private boolean contains(int patternPosition, int variantPosition) {
       if ( this.assessedCharPatternPosition == patternPosition && this.assessedCharVariantPosition == variantPosition ) {
           return true;
       }

       int iPatternPosition;
       int iVariantPosition;
       for ( int i = 0; i < chars.size(); i++ ) {
           iPatternPosition = this.patternPositions.get(i);
           iVariantPosition = this.variantPositions.get(i);

           if ( iPatternPosition == patternPosition && iVariantPosition == variantPosition ) {
               return true;
           }
       }

       return false;
    }
    
    boolean hasChars() {
        if ( this.assessedChar == ' ' ) {
            return false;
        }

        boolean hasChars = this.chars.size() > 0 || this.word.orThrow().length == 1;

        if ( hasChars ) {
            this.positionsInWordType.resetTo(this.definePositionsInWordType());
        }

        return hasChars;
    }

    boolean containsOnlyInClustered(int variantPosition) {
        return this.variantPositions.contains(variantPosition);
    }

    boolean hasCharsNotFoundInVariant() {
        return ( ! this.assessedCharFilledInVariant) || this.variantPositions.size() - this.filledInVariantQty > 0;
    }

    boolean hasChar(char c) {
        if ( this.assessedChar == c ) {
            return true;
        }

        return this.chars.contains(c);
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
        this.analyze.data.log.add(
                POSITIONS_SEARCH,
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

    int rejectCandidatesBelongingByPatternToOtherWords(WordInVariant word) {
        WordInVariant wordOfCandidate;
        int patternPosition;
        int variantPosition;
        int rejectedCount = 0;
        for ( int i = 0; i < this.fillingsInPattern.size(); i++ ) {
            if ( this.fillingsInPattern.get(i) ) {
                patternPosition = this.patternPositions.get(i);
                variantPosition = this.analyze.positions.i(patternPosition);
                wordOfCandidate = this.analyze.data.wordsInVariant.wordOf(variantPosition);
                if ( wordOfCandidate.index != word.index ) {
                    rejectedCount++;
                    this.remove(i);
                    i--;
                }
            }
        }

        return rejectedCount;
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
        boolean isFilledInVariant = this.fillingsInVariant.remove(i);
        this.fillingsInPattern.remove(i);
        this.candidates.remove(i);
        if ( isFilledInVariant ) {
            this.filledInVariantQty--;
        }
        this.matchStrength = this.matchStrength - matchType.strength();
        this.analyze.data.log.add(
                POSITIONS_SEARCH,
                "          [info] reject candidate '%s' pattern:%s, variant:%s, included: %s, %s",
                c, patternPosition, variantPosition, isFilledInVariant, matchType.name());
    }
    
    private void addInternal(
            char c, 
            int patternPosition, 
            int variantPosition, 
            boolean isFilledInVariant,
            boolean isFilledInPattern,
            MatchType matchType,
            Boolean isCandidate) {
        int alreadyExistedInPattern = this.patternPositions.indexOf(patternPosition);

        if ( c == 'n' && patternPosition == 14) {
            boolean debug = true;
        }

        if ( isFilledInPattern ) {
            int cPositionInVariant = this.analyze.positions.i(patternPosition);
            if ( cPositionInVariant != variantPosition ) {
                WordInVariant wordOfFilledChar = this.analyze.data.wordsInVariant.wordOf(cPositionInVariant);
                WordInVariant wordAnother = this.analyze.data.wordsInVariant.wordOf(variantPosition);

                if ( ! wordOfFilledChar.equals(wordAnother) ) {
                    int distance = abs(wordOfFilledChar.index - wordAnother.index);
                    if ( distance > 1 ) {
                        this.analyze.data.log.add(
                                POSITIONS_SEARCH,
                                "          [info] positions-in-cluster false-positive '%s' pattern:%s, variant:%s, included: %s, %s",
                                c, patternPosition, variantPosition, isFilledInVariant, matchType.name());
                        return;
                    }
                }
            }
        }

        if ( alreadyExistedInPattern > -1 ) {
            StepTwoClusterPositionView existingPosition = this.positionViewAt(alreadyExistedInPattern);
            StepTwoClusterPositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, isFilledInVariant, isFilledInPattern, matchType);

            this.analyze.data.log.add(
                    POSITIONS_SEARCH,
                    "          [info] positions-in-cluster duplicate: new '%s' pattern:%s, variant:%s -vs- existed '%s' pattern:%s, variant:%s",
                    possiblePosition.character, possiblePosition.patternPosition, possiblePosition.variantPosition,
                    existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);

            if ( existingPosition.isSameCharAs(possiblePosition) ) {

            }
            else {
                if ( possiblePosition.isBetterThan(existingPosition) ) {
                    possiblePosition.mergeInSubclusterInsteadOf(existingPosition);
                    this.analyze.data.log.add(
                            POSITIONS_SEARCH,
                            "          [info] positions-in-cluster duplicate: new position accepted");
                } else {
                    this.analyze.data.log.add(
                            POSITIONS_SEARCH,
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

                StepTwoClusterPositionView existingPosition = this.positionViewAt(alreadyExistedInVariant);

                if ( this.chars.size() > 1 ) {
                    StepTwoClusterPositionView possiblePosition = this.possiblePositionView.fill(c, patternPosition, variantPosition, isFilledInVariant, isFilledInPattern, matchType);

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
                }
                else {
                    writeGivenAsNew =
                            diffInPatternExisting >= diffInPatternNew;
                }

                if ( writeGivenAsNew ) {
                    this.analyze.data.log.add(
                            POSITIONS_SEARCH,
                            "          [info] positions-in-cluster is garbage: '%s' pattern:%s, variant:%s",
                            existingPosition.character, existingPosition.patternPosition, existingPosition.variantPosition);
                    this.remove(alreadyExistedInVariant);
                    this.analyze.garbagePatternPositions.add(existingPosition.patternPosition);
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
                this.fillingsInVariant.add(isFilledInVariant);
                this.fillingsInPattern.add(isFilledInPattern);
                this.candidates.add(isCandidate);
                if ( isFilledInVariant ) {
                    this.filledInVariantQty++;
                }
                this.directMatchesCount = this.directMatchesCount + this.countDirectMatchesWith(patternPosition, variantPosition);
                this.matchStrength = this.matchStrength + matchType.strength();
                this.analyze.data.log.add(
                        POSITIONS_SEARCH,
                        "          [info] positions-in-cluster '%s' pattern:%s, variant:%s, included(variant:%s pattern:%s), %s, candidate:%s",
                        c, patternPosition, variantPosition, isFilledInVariant, isFilledInPattern, matchType.name(), isCandidate);
            }
        }        
    }

    private int countDirectMatchesWith(
            int patternPosition,
            int variantPosition) {
        int count = 0;

        if ( this.assessedCharPatternPosition == patternPosition + 1 && this.assessedCharVariantPosition == variantPosition + 1 ) {
            count++;
        }

        if ( this.assessedCharPatternPosition == patternPosition - 1 && this.assessedCharVariantPosition == variantPosition - 1 ) {
            count++;
        }

        int iVariant;
        int iPattern;
        for ( int i = 0; i < this.chars.size(); i++ ) {
            iVariant = this.variantPositions.get(i);
            iPattern = this.patternPositions.get(i);

            if ( iPattern == patternPosition + 1 && iVariant == variantPosition + 1 ) {
                count++;
            }

            if ( iPattern == patternPosition - 1 && iVariant == variantPosition - 1 ) {
                count++;
            }
        }

        if ( count > 2 ) {
            count = 2;
        }

        return count;
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

    int firstClusteredPatternPostion() {
        return this.patternPositions.get(0);
    }

    public static enum FoundType implements CommonEnum<FoundType> {
        NEW_WORD, CHOSEN_WORD
    }

    public static enum PositionsPlacing implements CommonEnum<PositionsPlacing> {
        START, MIDDLE, END
    }

    public static enum PositionsInWordType implements CommonEnum<PositionsInWordType> {

        NEW_WORD_START(
                7, NEW_WORD, START),
        NEW_WORD_START_MIDDLE(
                5, NEW_WORD, START, MIDDLE),
        NEW_WORD_START_MIDDLE_END(
                4, NEW_WORD, START, MIDDLE, END),
        NEW_WORD_START_END(
                6, NEW_WORD, START, END),
        NEW_WORD_MIDDLE(
                12, NEW_WORD, MIDDLE),
        NEW_WORD_MIDDLE_END(
                11, NEW_WORD, MIDDLE, END),
        NEW_WORD_END(
                13, NEW_WORD, END),

        CHOSEN_WORD_START(
                3, CHOSEN_WORD, START),
        CHOSEN_WORD_START_MIDDLE(
                1, CHOSEN_WORD, START, MIDDLE),
        CHOSEN_WORD_START_MIDDLE_END(
                0, CHOSEN_WORD, START, MIDDLE, END),
        CHOSEN_WORD_START_END(
                2, CHOSEN_WORD, START, END),
        CHOSEN_WORD_MIDDLE(
                9, CHOSEN_WORD, MIDDLE),
        CHOSEN_WORD_MIDDLE_END(
                8, CHOSEN_WORD, MIDDLE, END),
        CHOSEN_WORD_END(
                10, CHOSEN_WORD, END),

        NEW_OTHER_WORD(15, NEW_WORD),
        CHOSEN_OTHER_WORD(14, CHOSEN_WORD);

        public static final int PRIORITY_DIFF = 5;

        public final FoundType foundType;
        public final List<PositionsPlacing> positionsPlacings;
        public final int priority;

        PositionsInWordType(int priority, FoundType foundType, PositionsPlacing... positionsPlacings) {
            this.foundType = foundType;
            this.positionsPlacings = asList(positionsPlacings);
            this.priority = priority;
        }
    }

    private PositionsInWordType definePositionsInWordType() {
        WordInVariant word = this.word.orThrow();

        boolean chosen = word.hasIntersections(this.analyze.filledPositions);

        boolean hasStart =
                word.startIndex == this.assessedCharVariantPosition ||
                word.hasStartIn(this.variantPositions);

        boolean hasMiddle =
                word.hasMiddlesIn(this.variantPositions);

        if ( ! chosen && hasMiddle && ! hasStart ) {
            if ( this.searchForStartOfNewWord() ) {
                hasStart = word.hasStartIn(this.variantPositions);
            }
        }

        if ( hasStart ) {
            if ( word.startIndex != this.assessedCharVariantPosition ) {
                int i = this.variantPositions.indexOf(word.startIndex);
                MatchType startCharMatchType = this.matches.get(i);
                if ( startCharMatchType.isBackward ) {
                    hasStart = false;
                    if ( ! hasMiddle ) {
                        hasMiddle = true;
                    }
                }
            }

        }

        boolean hasEnd =
                word.endIndex == this.assessedCharVariantPosition ||
                word.hasEndIn(this.variantPositions);

        if ( hasStart && hasEnd && !hasMiddle && word.length == 2 ) {
            hasMiddle = true;
        }

        if ( chosen ) {
            if ( hasStart ) {
                if ( hasMiddle ) {
                    if ( hasEnd ) {
                        return CHOSEN_WORD_START_MIDDLE_END;
                    }
                    else {
                        return CHOSEN_WORD_START_MIDDLE;
                    }
                }
                else {
                    if ( hasEnd ) {
                        return CHOSEN_WORD_START_END;
                    }
                    else {
                        return CHOSEN_WORD_START;
                    }
                }
            }
            else {
                if ( hasMiddle ) {
                    if ( hasEnd ) {
                        return CHOSEN_WORD_MIDDLE_END;
                    }
                    else {
                        return CHOSEN_WORD_MIDDLE;
                    }
                }
                if ( hasEnd ) {
                    return CHOSEN_WORD_END;
                }
                else {
                    return CHOSEN_OTHER_WORD;
                }
            }
        }
        else {
            if ( hasStart ) {
                if ( hasMiddle ) {
                    if ( hasEnd ) {
                        return NEW_WORD_START_MIDDLE_END;
                    }
                    else {
                        return NEW_WORD_START_MIDDLE;
                    }
                }
                else {
                    if ( hasEnd ) {
                        return NEW_WORD_START_END;
                    }
                    else {
                        return NEW_WORD_START;
                    }
                }
            }
            else {
                if ( hasMiddle ) {
                    if ( hasEnd ) {
                        return NEW_WORD_MIDDLE_END;
                    }
                    else {
                        return NEW_WORD_MIDDLE;
                    }
                }
                if ( hasEnd ) {
                    return NEW_WORD_END;
                }
                else {
                    return NEW_OTHER_WORD;
                }
            }
        }
    }

    private boolean searchForStartOfNewWord() {
        WordInVariant word = this.word.orThrow();

        int fromVariantIncl = word.startIndex;
        int toVariantExcl = this.findFirstVariantPositionIn(word);

        if ( toVariantExcl < 0 ) {
            return false;
        }

        int toPatternExcl = this.findFirstPatternPosition();
        int fromPatternIncl = this.analyze.findFirstNotFilledPatternPositionBackwardFrom(toPatternExcl);

        if ( toPatternExcl == fromPatternIncl ) {
            return false;
        }

        this.analyze.data.log.add(
                POSITIONS_SEARCH,
                "             [word start searching] %s",
                this.word.get().charsString());

        char cVariant;
        char cPattern;
        boolean found = false;
        for ( int iVariant = toVariantExcl - 1; iVariant >= fromVariantIncl; iVariant-- ) {
            cVariant = this.analyze.data.variant.charAt(iVariant);
            for ( int iPattern = toPatternExcl - 1; iPattern >= fromPatternIncl; iPattern-- ) {
                cPattern = this.analyze.data.patternChars.i(iPattern);

                if ( cPattern == cVariant ) {
                    this.analyze.data.log.add(
                            POSITIONS_SEARCH,
                            "                [found] '%s'[pattern:%s, variant:%s]",
                            cPattern, iPattern, iVariant);
                    this.addInternal(cPattern, iPattern, iVariant, false, false, MATCH_TYPO_LOOP, false);
                    found = true;
                }
            }
        }

        return found;
    }

    private int findFirstVariantPositionIn(WordInVariant word) {
        int firstVariantPosition = this.assessedCharVariantPosition;

        if ( word.startIndex == firstVariantPosition ) {
            return firstVariantPosition;
        }

        return word.firstIntersectionInVariant(this.variantPositions);
    }

    private int findFirstPatternPosition() {
        int firstIntersectionInPattern = this.patternPositions.min();

        if ( this.assessedCharPatternPosition < firstIntersectionInPattern ) {
            return this.assessedCharPatternPosition;
        }
        else {
            return firstIntersectionInPattern;
        }
    }
    
    boolean isBetterThan(ClusterStepTwo other) {
        WordInVariant thisWord = this.word.orThrow();
        WordInVariant otherWord = other.word.orThrow();

        if ( this.filledInVariantQty == 0 && other.filledInVariantQty == 0 ) {
            /* comparison of found subclusters, both are new */
            /* prefer subcluster that have more matches to fill more chars */

//            int thisMatchDirectly = this.countDirectMatches();
//            int otherMatchDirectly = other.countDirectMatches();

//            if ( thisMatchDirectly > otherMatchDirectly ) {
//                return true;
//            }
//            else if ( thisMatchDirectly < otherMatchDirectly ) {
//                return false;
//            }

            PositionsInWordType thisType = this.positionsInWordType.orThrow();
            PositionsInWordType otherType = other.positionsInWordType.orThrow();

            if ( thisWord.length == 1 && otherWord.length == 1 ) {
                if ( thisType.foundType.is(NEW_WORD) && thisType.foundType.is(NEW_WORD) ) {
                    if ( this.chars.size() > other.chars.size() ) {
                        return true;
                    }
                    else if ( this.chars.size() < other.chars.size() ) {
                        return false;
                    }
                    else {
                        if ( thisWord.hasSameWord(otherWord) ) {
                            if ( thisType.foundType.is(NEW_WORD) && otherType.foundType.is(NEW_WORD) ) {
                                return false;
                            }
                            else if ( thisType.foundType.is(CHOSEN_WORD) && otherType.foundType.is(NEW_WORD) ) {
                                return false;
                            }
                            else if ( thisType.foundType.is(NEW_WORD) && otherType.foundType.is(CHOSEN_WORD) ) {
                                return true;
                            }
                        }

                        WordInVariant prevWordOfThis = this.analyze.data.wordsInVariant.wordBeforeOrNull(thisWord);
                        WordInVariant nextWordOfThis = this.analyze.data.wordsInVariant.wordAfterOrNull(thisWord);

                        WordInVariant prevWordOfOther = this.analyze.data.wordsInVariant.wordBeforeOrNull(otherWord);
                        WordInVariant nextWordOfOther = this.analyze.data.wordsInVariant.wordAfterOrNull(otherWord);

                        int thisIntersections =
                                (isNull(prevWordOfThis) ? 0 : prevWordOfThis.intersections(this.analyze.filledPositions)) +
                                (isNull(nextWordOfThis) ? 0 : nextWordOfThis.intersections(this.analyze.filledPositions));

                        int otherIntersections =
                                (isNull(prevWordOfOther) ? 0 : prevWordOfOther.intersections(this.analyze.filledPositions)) +
                                (isNull(nextWordOfOther) ? 0 : nextWordOfOther.intersections(this.analyze.filledPositions));

                        if ( thisIntersections > otherIntersections ) {
                            return true;
                        }
                        else if ( thisIntersections < otherIntersections ) {
                            return false;
                        }
                        else {

                        }
                    }
                }
                else if ( thisType.foundType.is(CHOSEN_WORD) && otherType.foundType.is(NEW_WORD) ) {
                    return false;
                }
                else if ( thisType.foundType.is(NEW_WORD) && otherType.foundType.is(CHOSEN_WORD) ) {
                    return true;
                }
                else { // both are CHOSEN_WORD
                    if ( this.chars.size() > other.chars.size() ) {
                        return true;
                    }
                    else if ( this.chars.size() < other.chars.size() ) {
                        return false;
                    }
                    else {
                        return false; // prefer first
                    }
                }
            }
            else if ( thisWord.length == 1 ) {
                if ( thisType.foundType.is(CHOSEN_WORD) ) {
                    return false;
                }
                else { // this is NEW_WORD
                    if ( otherType.foundType.is(NEW_WORD) ) {
                        int otherClustered = other.countVariantPositionsWithoutSpaces();
                        if ( otherClustered > 0 ) {
                            return false;
                        }
                        else {
                            return true;
                        }
                    }
                }
            }
            else if ( otherWord.length == 1 ) {
                if ( otherType.foundType.is(CHOSEN_WORD) ) {
                    return true;
                }
                else { // other is NEW_WORD
                    if ( thisType.foundType.is(NEW_WORD) ) {
                        int thisClustered = this.countVariantPositionsWithoutSpaces();
                        if ( thisClustered > 0 ) {
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                }
            }

            if ( thisType.foundType.is(otherType.foundType) ) {
                if ( thisType.priority < otherType.priority ) {
                    return true;
                }
                else if ( thisType.priority > otherType.priority ) {
                    return false;
                }
            }
            else {
                int priorityDiff;

                int thisPriority = thisType.priority;
                int otherPriority = otherType.priority;

                if ( thisWord.hasSameWord(otherWord) ) {
                    int thisWordFoundChars;
                    int otherWordFoundChars;
                    if ( thisType.foundType.is(NEW_WORD) ) {
                        thisWordFoundChars = this.variantPositions.size();
                        otherWordFoundChars = otherWord.intersections(this.analyze.filledPositions)
                                + other.variantPositions.size();
                    }
                    else {
                        thisWordFoundChars = thisWord.intersections(this.analyze.filledPositions)
                                + this.variantPositions.size();
                        otherWordFoundChars = other.variantPositions.size();
                    }

                    if ( thisWordFoundChars > otherWordFoundChars ) {
                        return true;
                    }
                    else if ( thisWordFoundChars < otherWordFoundChars ) {
                        return false;
                    }
                }
                else {
                    int thisPriorityAdj = 0;
                    int otherPriorityAdj = 0;

                    int thisWordLength = thisWord.length;
                    int otherWordLength = otherWord.length;

                    if ( thisType.foundType.is(NEW_WORD) ) {
                        int otherWordHypothNewLength =
                                otherWord.intersections(this.analyze.filledPositions)
                                        + other.variantPositions.size();
                        otherPriorityAdj = otherPriorityAdj - otherWordHypothNewLength;

                        if ( otherWordLength - otherWordHypothNewLength < otherWordLength / 2 ) {
                            otherPriorityAdj = otherPriorityAdj - 1;
                        }

                        thisPriorityAdj = thisPriorityAdj
                                - (this.variantPositions.size() + 1);

                        if ( thisType.positionsPlacings.contains(START) ) {
                            int charsAtStart = this.countCharsAtStartWithoutSpaces();
                            if ( charsAtStart == 1 ) {
                                if ( this.variantPositions.size() + 1 == 2 ) {
                                    thisPriorityAdj = thisPriorityAdj + 1;
                                }
                            }
                            else if ( charsAtStart > 1 ) {
                                thisPriorityAdj = thisPriorityAdj - charsAtStart;
                            }
                        }

                        if ( this.matches.contains(MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT) || this.matches.contains(MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT) ) {
                            thisPriorityAdj = thisPriorityAdj + 1;
                        }
                    }
                    else {
                        int thisWordHypothNewLength =
                                thisWord.intersections(this.analyze.filledPositions)
                                        + this.variantPositions.size();

                        thisPriorityAdj = thisPriorityAdj - thisWordHypothNewLength;

                        if ( thisWordLength - thisWordHypothNewLength < thisWordLength / 2 ) {
                            thisPriorityAdj = thisPriorityAdj - 1;
                        }

                        otherPriorityAdj = otherPriorityAdj
                                - (other.variantPositions.size() + 1);

                        if ( otherType.positionsPlacings.contains(START) ) {
                            int charsAtStart = other.countCharsAtStartWithoutSpaces();
                            if ( charsAtStart == 1 ) {
                                if ( other.variantPositions.size() + 1 == 2 ) {
                                    otherPriorityAdj = otherPriorityAdj + 1;
                                }
                            }
                            else if ( charsAtStart > 1 ) {
                                otherPriorityAdj = otherPriorityAdj - charsAtStart;
                            }
                        }

                        if ( other.matches.contains(MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT) || other.matches.contains(MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT) ) {
                            otherPriorityAdj = otherPriorityAdj + 1;
                        }
                    }

                    thisPriority = thisPriority + thisPriorityAdj;
                    otherPriority = otherPriority + otherPriorityAdj;

                    if ( thisType.priority < otherType.priority ) {
                        priorityDiff = otherType.priority - thisType.priority;
                        if ( priorityDiff <= PRIORITY_DIFF ) {
                            if ( thisPriority < otherPriority ) {
                                return true;
                            }
                            else {
                                return false;
                            }
                        }
                    }
                    else if ( thisType.priority > otherType.priority ) {
                        priorityDiff = thisType.priority - otherType.priority;
                        if ( priorityDiff <= PRIORITY_DIFF ) {
                            if ( thisPriority > otherPriority ) {
                                return false;
                            }
                            else {
                                return true;
                            }
                        }
                    }
                }
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

                if ( /*thisMatchStrength > otherMatchStrength*/ false ) {
                    return true;
                }
                else if ( /*thisMatchStrength < otherMatchStrength*/ false ) {
                    return false;
                }
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
                                if ( thisType.foundType.is(otherType.foundType) ) {
                                    if ( thisWord.index != otherWord.index ) {
                                        WordsInVariant.WordsInRange wordsBefore;
                                        if ( thisWord.placing.is(INDEPENDENT) && otherWord.placing.is(DEPENDENT) ) {
                                            wordsBefore = this.analyze.data.wordsInVariant.independentAndDependentWordsBefore(otherWord);
                                            int intersectionsBefore = wordsBefore.intersections(this.analyze.filledPositions);
                                            if ( intersectionsBefore > 1 ) {
                                                return false;
                                            }
                                        }
                                        else if ( thisWord.placing.is(DEPENDENT) && otherWord.placing.is(INDEPENDENT) ) {
                                            wordsBefore = this.analyze.data.wordsInVariant.independentAndDependentWordsBefore(thisWord);
                                            int intersectionsBefore = wordsBefore.intersections(this.analyze.filledPositions);
                                            if ( intersectionsBefore > 1 ) {
                                                return true;
                                            }
                                        }
                                    }
                                }

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
            if ( this.filledInVariantQty > other.filledInVariantQty) {
                return true;
            } else if ( this.filledInVariantQty < other.filledInVariantQty) {
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

    private int countCharsAtStartWithoutSpaces() {
        WordInVariant word = this.word.orThrow();
        int count = 0;
        if ( word.startIndex == assessedCharVariantPosition ) {
            count++;
        }

        for ( int iWord = word.startIndex + count; iWord <= word.endIndex; iWord++ ) {
            if ( this.variantPositions.contains(iWord) ) {
                count++;
            }
            else {
                return count;
            }
        }

        return count;
    }

    private int countVariantPositionsWithoutSpaces() {
        WordInVariant word = this.word.orThrow();
        int count = 0;

        int iWordPrev = word.startIndex;
        boolean currFound;
        boolean prevFound = this.containsVariantPosition(iWordPrev);
        for ( int iWord = word.startIndex + 1; iWord <= word.endIndex; iWord++ ) {
            currFound = this.containsVariantPosition(iWord);
            if ( prevFound && currFound ) {
                count++;
            }
            prevFound = currFound;
        }

        if ( count > 0 ) {
            count++;
        }

        return count;
    }

    private boolean containsVariantPosition(int p) {
        return this.assessedCharVariantPosition == p || this.variantPositions.contains(p);
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
        this.fillingsInVariant.clear();
        this.fillingsInPattern.clear();
        this.filledInVariantQty = 0;
        this.matchStrength = 0;
        this.mergedDuplicates = 0;
        this.assessedChar = ' ';
        this.assessedCharPatternPosition = UNINITIALIZED;
        this.assessedCharVariantPosition = UNINITIALIZED;
        this.existingPositionView.i = BEFORE_START;
        this.possiblePositionView.i = BEFORE_START;
        this.word.nullify();
        this.positionsInWordType.nullify();
        this.directMatchesCount = 0;
    }
    
    @Override
    public String toString() {
        return format(
                "['%s' variant:%s[included:%s], word:%s, clustered:['%s', pattern:%s, variant:%s, variant-included:%s, pattern-included:%s, matches:%s]]",
                this.assessedChar, this.assessedCharVariantPosition, this.assessedCharFilledInVariant, this.word.isPresent() ? this.word.orThrow().charsString() : null, this.chars, this.patternPositions, this.variantPositions, this.fillingsInVariant, this.fillingsInPattern, this.matches);
    }
}
