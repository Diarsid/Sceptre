/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import diarsid.support.objects.references.Possible;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

import static diarsid.sceptre.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.sceptre.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.sceptre.AnalyzeUtil.inconsistencyOf;
import static diarsid.sceptre.AnalyzeUtil.nonClusteredImportanceDependingOn;
import static diarsid.sceptre.AnalyzeUtil.processCluster;
import static diarsid.sceptre.ClusterPreference.PREFER_LEFT;
import static diarsid.sceptre.MatchType.MATCH_DIRECTLY;
import static diarsid.sceptre.MatchType.MATCH_TYPO_1;
import static diarsid.sceptre.MatchType.MATCH_TYPO_2;
import static diarsid.sceptre.MatchType.MATCH_TYPO_3_1;
import static diarsid.sceptre.MatchType.MATCH_TYPO_3_2;
import static diarsid.sceptre.MatchType.MATCH_TYPO_3_3;
import static diarsid.sceptre.MatchType.MATCH_TYPO_LOOP;
import static diarsid.sceptre.PositionsSearchStep.STEP_1;
import static diarsid.sceptre.PositionsSearchStep.STEP_2;
import static diarsid.sceptre.PositionsSearchStep.STEP_3;
import static diarsid.sceptre.PositionsSearchStep.STEP_4;
import static diarsid.sceptre.PositionsSearchStepOneCluster.calculateSimilarity;
import static diarsid.sceptre.PositionsSearchStepOneClusterDuplicateComparison.compare;
import static diarsid.sceptre.WeightAnalyzeReal.logAnalyze;
import static diarsid.sceptre.WeightElement.CHAR_AFTER_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD;
import static diarsid.sceptre.WeightElement.CHAR_IS_ONE_CHAR_WORD;
import static diarsid.sceptre.WeightElement.CLUSTERS_ARE_WEAK_2_LENGTH;
import static diarsid.sceptre.WeightElement.CLUSTERS_NEAR_ARE_IN_ONE_PART;
import static diarsid.sceptre.WeightElement.CLUSTERS_ORDER_INCOSISTENT;
import static diarsid.sceptre.WeightElement.CLUSTER_BEFORE_SEPARATOR;
import static diarsid.sceptre.WeightElement.CLUSTER_CANDIDATES_SIMILARITY;
import static diarsid.sceptre.WeightElement.CLUSTER_ENDS_CURRENT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.WeightElement.CLUSTER_ENDS_NEXT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.WeightElement.CLUSTER_ENDS_WITH_VARIANT;
import static diarsid.sceptre.WeightElement.CLUSTER_HAS_SHIFTS;
import static diarsid.sceptre.WeightElement.CLUSTER_IS_CONSISTENT;
import static diarsid.sceptre.WeightElement.CLUSTER_IS_NOT_CONSISTENT;
import static diarsid.sceptre.WeightElement.CLUSTER_IS_WORD;
import static diarsid.sceptre.WeightElement.CLUSTER_STARTS_CURRENT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.WeightElement.CLUSTER_STARTS_PREVIOUS_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.WeightElement.CLUSTER_STARTS_WITH_VARIANT;
import static diarsid.sceptre.WeightElement.NEXT_CHAR_IS_SEPARATOR;
import static diarsid.sceptre.WeightElement.PATTERN_CONTAINS_CLUSTER;
import static diarsid.sceptre.WeightElement.PATTERN_CONTAINS_CLUSTER_LONG_WORD;
import static diarsid.sceptre.WeightElement.PATTERN_DOES_NOT_CONTAIN_CLUSTER;
import static diarsid.sceptre.WeightElement.PLACING_BONUS;
import static diarsid.sceptre.WeightElement.PLACING_PENALTY;
import static diarsid.sceptre.WeightElement.PREVIOUS_CHAR_IS_SEPARATOR;
import static diarsid.sceptre.WeightElement.PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START;
import static diarsid.sceptre.WeightElement.PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD;
import static diarsid.sceptre.WeightElement.SINGLE_POSITIONS_DENOTE_WORD;
import static diarsid.sceptre.WeightElement.UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START;
import static diarsid.sceptre.WeightElement.UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END;
import static diarsid.sceptre.WeightEstimate.BAD;
import static diarsid.sceptre.WeightEstimate.estimate;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.cube;
import static diarsid.support.misc.MathFunctions.onePointRatio;
import static diarsid.support.misc.MathFunctions.square;
import static diarsid.support.objects.collections.CollectionUtils.first;
import static diarsid.support.objects.collections.CollectionUtils.getNearestToValueFromSetExcluding;
import static diarsid.support.objects.collections.CollectionUtils.isNotEmpty;
import static diarsid.support.objects.collections.CollectionUtils.last;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;
import static diarsid.support.objects.collections.Lists.lastFrom;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;
import static diarsid.support.strings.StringUtils.countWordSeparatorsInBetween;
import static diarsid.support.strings.StringUtils.isWordsSeparator;

/**
 *
 * @author Diarsid
 */
class PositionsAnalyze {
    
    static final int POS_UNINITIALIZED = -9; 
    static final int POS_ERASED = -9; 
    static final int POS_NOT_FOUND = -3;
    static final String NO_REASON = "";
    static boolean logEnabled;
    
    static {
        logEnabled = true;
    }    
    
    /* DEBUG UTIL */ static interface DebugCondition {
    /* DEBUG UTIL */     
    /* DEBUG UTIL */     boolean isMatch();
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */
    /* DEBUG UTIL */ static boolean gotoBreakpointWhen(DebugCondition debugCondition) {
    /* DEBUG UTIL */     return debugCondition.isMatch();
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */ 
    /* DEBUG UTIL */ static void breakpoint() {
    /* DEBUG UTIL */        
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */ 
    /* DEBUG UTIL */ DebugCondition charAndPositionAre(char c, int position) {
    /* DEBUG UTIL */     return () -> {
    /* DEBUG UTIL */         return 
    /* DEBUG UTIL */                 this.currentPatternCharPositionInVariant == position &&
    /* DEBUG UTIL */                 this.currentChar == c;
    /* DEBUG UTIL */     };
    /* DEBUG UTIL */ }
    /* DEBUG UTIL */ 
    /* DEBUG UTIL */ DebugCondition stepCharAndPositionAre(
    /* DEBUG UTIL */         PositionsSearchStep step, char c, int position) {
    /* DEBUG UTIL */     return () -> {
    /* DEBUG UTIL */         return 
    /* DEBUG UTIL */                 this.findPositionsStep.equals(step) &&
    /* DEBUG UTIL */                 this.currentPatternCharPositionInVariant == position &&
    /* DEBUG UTIL */                 this.currentChar == c;
    /* DEBUG UTIL */     };
    /* DEBUG UTIL */ }
    
    final AnalyzeUnit data;
    
    int[] positions;
    
    int clustersQty;
    int clustered;
    int meaningful;
    int nonClustered;
    
    int missed;    
    
    int previousClusterLength;
    int currentClusterLength;
    int currentPosition;
    int currentPositionIndex;
    int nextPosition;
    int alonePositionAfterPreviousSeparator;
    boolean prevCharIsSeparator;
    boolean nextCharIsSeparator;
    
    char currentChar;
    char nextCharInPattern;
    char nextCharInVariant;
    char previousCharInPattern;
    char previousCharInVariant;
    int currentPatternCharPositionInVariant;
    int betterCurrentPatternCharPositionInVariant;
    
    // v.2
    final Set<Integer> unclusteredPatternCharIndexes = new TreeSet<>();
    final Set<Integer> localUnclusteredPatternCharIndexes = new TreeSet<>();
    PositionsSearchStep findPositionsStep;
    boolean previousPositionInVariantFound;
    boolean nextPositionInVariantFound;
    boolean hasPreviousInPattern;
    boolean hasNextInPattern;
    boolean hasPreviousInVariant;
    boolean hasNextInVariant;
    boolean positionAlreadyFilled;
    boolean isCurrentCharPositionAddedToPositions;
    boolean continueSearching;
    boolean stepOneClusterSavedRightNow;
    boolean maySearchAsCharInWord;
    int nextPatternCharsToSkip;
    List<Integer> positionsInCluster = new ArrayList<>();
    int currentCharInVariantQty;
    int currentPatternCharPositionInVariantToSave;
    // --
    
    PositionsSearchStepOneCluster currStepOneCluster = new PositionsSearchStepOneCluster();
    PositionsSearchStepOneCluster prevStepOneCluster = new PositionsSearchStepOneCluster();
    PositionsSearchStepOneCluster lastSavedStepOneCluster = new PositionsSearchStepOneCluster();

    PositionsSearchStepTwoCluster currStepTwoCluster = new PositionsSearchStepTwoCluster();
    PositionsSearchStepTwoCluster prevStepTwoCluster = new PositionsSearchStepTwoCluster();
    
    Possible<String> missedRepeatingsLog = simplePossibleButEmpty();
    List<Integer> extractedMissedRepeatedPositionsIndexes = new ArrayList<>();
    List<Character> missedRepeatedChars = new ArrayList<>();
    List<Integer> missedRepeatedPositions = new ArrayList<>();
    
    // v.3
    Map<Integer, Integer> positionUnsortedOrders = new HashMap<>();
    Map<Integer, Integer> positionPatternIndexes = new HashMap<>();
    Map<Integer, PositionsSearchStep> positionFoundSteps = new HashMap<>();
    Set<Integer> filledPositions = positionFoundSteps.keySet();
    private final PositionCandidate positionCandidate;
    int nearestPositionInVariant;
    List<Integer> currentClusterOrderDiffs = new ArrayList();
    List<Character> notFoundPatternChars = new ArrayList<>();
    Clusters clusters;
    final List<Integer> keyChars;
    private final SinglePositions singlePositions;
    boolean currentClusterOrdersIsConsistent;
    boolean previousClusterOrdersIsConsistent;
    boolean currentClusterOrdersHaveDiffCompensations;
    int unsortedPositions;
    // --
    
    int previousClusterLastPosition = POS_UNINITIALIZED;
    int previousClusterFirstPosition = POS_UNINITIALIZED;
    int currentClusterFirstPosition;
    String badReason;
    
    boolean clusterContinuation;
    boolean clusterStartsWithVariant;
    boolean clusterStartsWithSeparator;
    boolean clusterEndsWithSeparator;
    boolean previousClusterEndsWithSeparator;
    int clustersFacingEdges;
    int clustersFacingStartEdges;
    int clustersFacingEndEdges;
    int separatorsBetweenClusters;
    int allClustersInconsistency;
    
    float clustersImportance;
    int nonClusteredImportance;
    
    Weight weight;
    
    PositionsAnalyze(
            AnalyzeUnit data, 
            Clusters clusters, 
            PositionCandidate positionCandidate) {
        this.data = data;
        this.clusters = clusters;
        this.positionCandidate = positionCandidate;
        this.keyChars = new ArrayList<>();
        this.singlePositions = new SinglePositions();
        this.weight = new Weight();
        this.clearPositionsAnalyze();
    }
    
    static boolean arePositionsEquals(PositionsAnalyze dataOne, PositionsAnalyze dataTwo) {
        return Arrays.equals(dataOne.positions, dataTwo.positions);
    }
    
    int findFirstPosition() {
        int first = first(this.positions);
        if ( first > -1 ) {
            return first;
        }
        
        for (int i = 1; i < this.positions.length; i++) {
            first = this.positions[i];
            if ( first > -1 ) {
                return first;
            }
        }
        
        return POS_NOT_FOUND;
    }
    
    int findLastPosition() {
        int last = last(this.positions);
        if ( last > -1 ) {
            return last;
        }
        
        for (int i = this.positions.length - 2; i > -1; i--) {
            last = this.positions[i];
            if ( last > -1 ) {
                return last;
            } 
        }
        
        return POS_NOT_FOUND;
    }
    
    void fillPositionsFromIndex(int patternInVariantIndex) {
        int length = positions.length;
        int position = patternInVariantIndex;
        logAnalyze(POSITIONS_SEARCH, "  pattern found directly");
        for (int i = 0; i < length; i++) {
            positions[i] = position;
            positionFoundSteps.put(position, STEP_1);
            positionPatternIndexes.put(i, position);
            logAnalyze(POSITIONS_SEARCH, "    [SAVE] %s : %s", data.patternChars[i], position);    
            position++;        
        }
        logAnalyze(POSITIONS_SEARCH, "         %s", displayPositions());
        clearPositionsSearchingState();
    }
    
    private String displayPositions() {
        if ( POSITIONS_SEARCH.isDisabled() ) {
            return "";
        }
        
        String patternPositions = stream(positions)
                .mapToObj(position -> {
                    if ( position == POS_UNINITIALIZED || position == POS_NOT_FOUND ) {
                        return "_";
                    } else {
                        return String.valueOf(data.variant.charAt(position));
                    }
                })
                .collect(joining());
        
        String variantPositions = range(0, data.variant.length())
                .mapToObj(position -> {
                    if ( filledPositions.contains(position) ) {
                        return String.valueOf(data.variant.charAt(position));
                    } else {
                        return "_";                        
                    }
                })
                .collect(joining());
        
        return patternPositions + " : " + variantPositions;
    }
    
    private String displayStepOneClusterLastAddedPosition() {
        if ( POSITIONS_SEARCH.isDisabled() ) {
            return "";
        }
        
        PositionView lastAddedPosition = currStepOneCluster.lastAdded();
        int lastAddedPatternPosition = lastAddedPosition.patternPosition();
        int lastAddedVariantPosition = lastAddedPosition.variantPosition();
        
        char[] patternLine = new char[this.data.pattern.length()];
        char[] variantLine = new char[this.data.variant.length()];
        
        Arrays.fill(patternLine, '_');
        Arrays.fill(variantLine, '_');
        
        patternLine[lastAddedPatternPosition] = this.data.pattern.charAt(lastAddedPatternPosition);
        variantLine[lastAddedVariantPosition] = this.data.variant.charAt(lastAddedVariantPosition);
        
        return new String(patternLine) + " : " + new String(variantLine);
    }
    
    void analyzePositionsClusters() {
        clustersCounting: for (int i = 0; i < this.positions.length; i++) {
            this.setCurrentPosition(i);
            if ( this.isCurrentPositionMissed() ) {
                this.missed++;
                this.nonClustered++;
                this.singlePositions.miss();
                this.tryToProcessSinglePositionsUninterruptedRow();
                continue clustersCounting;
            }
            
            if ( this.hasNextPosition(i) ) {
                this.setNextPosition(i);
                if ( this.isCurrentAndNextPositionInCluster() ) {
                    this.singlePositions.miss();
                    this.tryToProcessSinglePositionsUninterruptedRow();
                    if ( this.clusterContinuation ) {
                        this.clusterIsContinuing();
                    } else {
                        this.newClusterStarts();
                    }
                } else {
                    if ( this.clusterContinuation ) {
                        this.singlePositions.miss();
                        this.tryToProcessSinglePositionsUninterruptedRow();
                        this.clusterEnds();
                    } else {
                        this.singlePositions.add(this.currentPosition);
                        this.prevCharIsSeparator = this.isPreviousCharWordSeparator();
                        this.nextCharIsSeparator = this.isNextCharWordSeparator();
                        
                        if ( this.prevCharIsSeparator && this.nextCharIsSeparator ) {
                            this.doWhenNextAndPreviousCharsAreSeparators();
                            this.clustered++;
                            this.nonClustered--;
                        } else if ( this.prevCharIsSeparator ) {
                            this.doWhenOnlyPreviousCharacterIsSeparator();
                        } else if ( this.nextCharIsSeparator ) {
                            this.doWhenOnlyNextCharacterIsSeparator();
                        }
                        
                        this.nonClustered++;
                    }
                }
            } else {                
                if ( this.clusterContinuation ) {
                    this.singlePositions.miss();
                    this.tryToProcessSinglePositionsUninterruptedRow();
                    this.clusterEnds();                    
                } else {
                    this.singlePositions.add(this.currentPosition);
                    if ( this.isCurrentPositionNotMissed() ) {
                        this.prevCharIsSeparator = this.isPreviousCharWordSeparator();
                        this.nextCharIsSeparator = this.isNextCharWordSeparator();
                        
                        if ( this.prevCharIsSeparator && this.nextCharIsSeparator ) {
                            this.doWhenNextAndPreviousCharsAreSeparators();
                            this.clustered++;
                            this.nonClustered--;
                        } else if ( this.prevCharIsSeparator ) {
                            this.doWhenOnlyPreviousCharacterIsSeparator();
                        } else if ( this.nextCharIsSeparator ) {
                            this.doWhenOnlyNextCharacterIsSeparator();
                        }
                    }
                    
                    this.nonClustered++;
                }
                this.singlePositions.end();
                this.tryToProcessSinglePositionsUninterruptedRow();
            }   
        }
                
        this.clusters.arrange();
        
        if ( this.clusters.nonEmpty() ) {
            this.analyzeAllClustersOrderDiffs();
            int totalTearDown = this.clusters.lookupForTearDowns();
            if ( totalTearDown > 0 ) {
                this.clustered = this.clustered - totalTearDown;
                this.nonClustered = this.nonClustered + totalTearDown;
                logAnalyze(POSITIONS_CLUSTERS, "               [TEARDOWN] total : %s", totalTearDown);
            }            
        }
        
        if ( this.clustersQty > 1 && this.allClustersInconsistency == 0 ) {
            if ( this.separatorsBetweenClusters > 0 ) {
                if ( this.clusters.distanceBetweenClusters() == this.separatorsBetweenClusters ) {
                    logAnalyze(POSITIONS_CLUSTERS, "               [weight] all clusters are one pattern, can be regarded as one cluster!");
                    this.clustersQty = 1;
                }
            } else {
                
            }           
        }
        
        this.analyzeAllClustersPlacing();
    }

    private void tryToProcessSinglePositionsUninterruptedRow() {
        if ( this.singlePositions.doHaveUninterruptedRow() ) {
            List<Integer> uninterruptedPositions = this.singlePositions.uninterruptedRow();
            Integer firstPosition = uninterruptedPositions.get(0);
            Integer lastPosition = lastFrom(uninterruptedPositions);
            Integer firstSeparatorAfterFirstPosition = this.data.variantSeparators.higher(firstPosition);
            Integer lastSeparatorBeforeFirstPosition = this.data.variantSeparators.lower(firstPosition);
            if ( nonNull(firstSeparatorAfterFirstPosition) ) {
                if ( firstSeparatorAfterFirstPosition == lastPosition + 1 ) {
                    if ( nonNull(lastSeparatorBeforeFirstPosition) ) {
                        if ( lastSeparatorBeforeFirstPosition == firstPosition - 1 ) {
                            logAnalyze(
                                    POSITIONS_CLUSTERS, 
                                    "  s.pos. %s_%s (chars '%s_%s') ", 
                                    firstPosition, lastPosition, 
                                    this.data.variant.charAt(firstPosition),
                                    this.data.variant.charAt(lastPosition));
                            this.weight.add(-square(lastPosition - firstPosition + 1), SINGLE_POSITIONS_DENOTE_WORD);
                        }
                    }
                }
            }
//            for (int i = 0; i < uninterruptedPositions.size(); i++) {
//                position = uninterruptedPositions.get(i);
//                
//                logAnalyze(
//                        POSITIONS_CLUSTERS, 
//                        "  s.pos. %s (char '%s') ", 
//                        position, 
//                        this.data.variantText.charAt(position));
//                
//            }
            this.singlePositions.uninterruptedRow().clear();
        }
    }

    private void doWhenNextAndPreviousCharsAreSeparators() {
        this.keyChars.add(this.currentPosition);
        this.weight.add(CHAR_IS_ONE_CHAR_WORD);
        if ( this.alonePositionAfterPreviousSeparator != POS_UNINITIALIZED ) {
            this.alonePositionAfterPreviousSeparator = POS_ERASED;
        }
    }

    private void doWhenOnlyPreviousCharacterIsSeparator() {
        if ( this.currentPositionCharIsPatternStart() ) {
            this.keyChars.add(this.currentPosition);
            this.weight.add(PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START);
            if ( this.isClusterBeforeSeparator() ) {
//                logAnalyze(POSITIONS_CLUSTERS, "               [weight] -15.5 : there is cluster before separator!");
//                this.positionsWeight = this.positionsWeight - 15.5;
            }
        } else if ( this.isClusterBeforeSeparator() ) {
            this.keyChars.add(this.currentPosition);
            this.weight.add(CLUSTER_BEFORE_SEPARATOR);
        } else {
            this.weight.add(PREVIOUS_CHAR_IS_SEPARATOR);
        }
        
        this.alonePositionAfterPreviousSeparator = this.currentPosition;
    }
    
    private boolean isClusterBeforeSeparator() {
        if ( this.previousClusterLastPosition != POS_UNINITIALIZED && this.previousClusterEndsWithSeparator ) {
            return this.previousClusterLastPosition == this.currentPosition - 2;
        } else {
            return false;
        }
    }

    private void doWhenOnlyNextCharacterIsSeparator() {
        this.weight.add(NEXT_CHAR_IS_SEPARATOR);
        
        if ( this.previousClusterLastPosition != POS_UNINITIALIZED && ! this.previousClusterEndsWithSeparator && this.previousClusterOrdersIsConsistent ) {
            if ( ! this.areSeparatorsPresentBetween(this.previousClusterLastPosition, this.currentPosition) ) {
                int bonus = this.previousClusterLength > 2 ? 
                        square(this.previousClusterLength) : this.previousClusterLength;
                this.weight.add(-bonus, PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD);
            } 
        }
    }
    
    private boolean areSeparatorsPresentBetween(final int fromExcl, final int toExcl) {
        logAnalyze(POSITIONS_CLUSTERS, "               [weight] ...searching for separators between %s and %s", fromExcl, toExcl);
        if ( absDiff(toExcl, toExcl) < 2 ) {
            return false;
        }
        String variantText = this.data.variant;
        for (int pointer = fromExcl + 1; pointer < toExcl; pointer++) {
            if ( isWordsSeparator(variantText.charAt(pointer)) ) {
                logAnalyze(POSITIONS_CLUSTERS, "               [weight] separator found - %s", pointer);
                return true;
            }
        }
        return false;
    }
    
    private void analyzeAllClustersOrderDiffs() {
        int orderMeansDifferentFromZero = 0;
        boolean allClustersHaveLength2 = true;
        
        for (Cluster cluster : this.clusters.all()) {
            if ( cluster.ordersDiffMean() != 0  ) {
                orderMeansDifferentFromZero++;
            }       
            if ( cluster.length() > 2 ) {
                allClustersHaveLength2 = false;
            }
        }
        
        if ( orderMeansDifferentFromZero > 0 ) {
            this.allClustersInconsistency = orderMeansDifferentFromZero * 2;
            this.weight.add(allClustersInconsistency, CLUSTERS_ORDER_INCOSISTENT);
        }        
        
        if ( allClustersHaveLength2 ) {
            int penalty = square(this.clusters.quantity());
            this.weight.add(penalty, CLUSTERS_ARE_WEAK_2_LENGTH);
        }
    }
    
    private void analyzeAllClustersPlacing() {
        this.weight.excludeIfAllPresent(
                UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START,
                UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END);
        
        if ( this.clusters.isEmpty() ) {
            return;
        }
        
        if ( estimate(this.weight.sum() + this.data.weight.sum()).equals(BAD) ) {
            float placingPenalty = (float) square( ( 10.0 - this.clustersQty ) / this.clustered );
            logAnalyze(POSITIONS_CLUSTERS, "    [cluster placing] positions weight is too bad for placing assessment");
            this.weight.add(placingPenalty, PLACING_PENALTY);
        } else {
            float placingBonus = this.clusters.calculatePlacingBonus();
            this.weight.add(-placingBonus, PLACING_BONUS);
        }
    }
        
    void findPatternCharsPositions() {
        
        fillNotFoundPositions();
        
        proceedWith(STEP_1);
        logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
        
        for (int currentPatternCharIndex = 0, charsRemained = data.patternChars.length - 1; currentPatternCharIndex < data.patternChars.length; currentPatternCharIndex++, charsRemained--) {                
            processCurrentPatternCharOf(currentPatternCharIndex, charsRemained);
        }
        lastSavedStepOneCluster.clear();
        swapUnclusteredPatternCharIndexes();
        
        proceedWith(STEP_2);
        if ( isAllowedToProceedOnCurrentStep() ) {
            processAccumulatedUnclusteredPatternCharIndexes();           
            swapUnclusteredPatternCharIndexes();

            processAccumulatedUnclusteredPatternCharIndexes();        
            swapUnclusteredPatternCharIndexes();
            
            proceedWith(STEP_3);
            if ( isAllowedToProceedOnCurrentStep() ) {
                processAccumulatedUnclusteredPatternCharIndexes();      
                swapUnclusteredPatternCharIndexes();
            } else {
                proceedWith(STEP_4);
                processAccumulatedUnclusteredPatternCharIndexes();      
                swapUnclusteredPatternCharIndexes();
            }
        } else {
            proceedWith(STEP_4);
            processAccumulatedUnclusteredPatternCharIndexes();      
            swapUnclusteredPatternCharIndexes();
        }
        
        clearPositionsSearchingState();
    }
    
    private void fillNotFoundPositions() {
        for (Character character : data.patternChars) {
            this.notFoundPatternChars.add(character);
        }
    }
    
    private void proceedWith(PositionsSearchStep step) {
        this.findPositionsStep = step;
    }

    private boolean isAllowedToProceedOnCurrentStep() {
        boolean allowed = findPositionsStep.canProceedWith(data.pattern.length());
        if ( ! allowed ) {
            logAnalyze(POSITIONS_SEARCH, "    %s is not allowed for pattern with length %s", findPositionsStep, data.pattern.length());
        }
        return allowed;
    }
    
    private void processAccumulatedUnclusteredPatternCharIndexes() {
        if ( nonEmpty(unclusteredPatternCharIndexes) ) {
            logAnalyze(POSITIONS_SEARCH, "    %s", findPositionsStep);
            int charsRemained = unclusteredPatternCharIndexes.size();
            for (Integer currentPatternCharIndex : unclusteredPatternCharIndexes) {
                charsRemained--;
                processCurrentPatternCharOf(currentPatternCharIndex, charsRemained);
            }            
        } 
    }

    private void swapUnclusteredPatternCharIndexes() {
        unclusteredPatternCharIndexes.clear();
        unclusteredPatternCharIndexes.addAll(localUnclusteredPatternCharIndexes);
        localUnclusteredPatternCharIndexes.clear();
    }

    private void clearPositionsSearchingState() {
        if ( nonNull(this.unclusteredPatternCharIndexes) ) {
            this.unclusteredPatternCharIndexes.clear();
        }
        this.notFoundPatternChars.clear();
        this.unclusteredPatternCharIndexes.clear();
        this.previousPositionInVariantFound = false;
        this.nextPositionInVariantFound = false;
        this.hasPreviousInPattern = false;
        this.hasNextInPattern = false;
        this.hasPreviousInVariant = false;
        this.hasNextInVariant = false;
        this.positionAlreadyFilled = false;
        this.stepOneClusterSavedRightNow = false;
        this.maySearchAsCharInWord = false;
        this.positionsInCluster.clear();
        this.currentChar = ' ';
        this.findPositionsStep = STEP_1;
    }
    
    private boolean isPositionSetAt(int patternIndex) {
        return this.positions[patternIndex] > -1;
    }

    private boolean isPositionNotSetAt(int patternIndex) {
        return this.positions[patternIndex] < 0;
    }
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex, int charsRemained) {
        currentChar = data.patternChars[currentPatternCharIndex];
        logAnalyze(POSITIONS_SEARCH, "      [explore] '%s'(%s in pattern)", this.currentChar, currentPatternCharIndex);
        if ( nextPatternCharsToSkip > 0 ) {
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
            nextPatternCharsToSkip--;
            return;
        }
        else if ( nextPatternCharsToSkip == 0 ) {
            if ( findPositionsStep.equalTo(STEP_1) && stepOneClusterSavedRightNow ) {
                stepOneClusterSavedRightNow = false;
                maySearchAsCharInWord = true;
            }
        }
        
        //if ( prevClusterCandidate.skipIfPossible() ) {
        //    logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
        //    return;
        //}
        
        if ( positions[currentPatternCharIndex] != POS_UNINITIALIZED ) {
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s' in pattern is already found - %s", this.currentChar, positions[currentPatternCharIndex]);
            return;
        }

        hasPreviousInPattern = currentPatternCharIndex > 0;
        hasNextInPattern = currentPatternCharIndex < data.patternChars.length - 1;

        currentPatternCharPositionInVariant = data.variant.indexOf(currentChar);
        
        currentCharInVariantQty = 0;
        if ( currentPatternCharPositionInVariant < 0 ) {
            positions[currentPatternCharIndex] = POS_NOT_FOUND;
            logAnalyze(POSITIONS_SEARCH, "          [info] '%s' not found in variant", this.currentChar);
            return;
        }        
        
        positionsInCluster.clear();
        continueSearching = true;

        characterSearching : while ( currentPatternCharPositionInVariant >= 0 && continueSearching ) {
            logAnalyze(POSITIONS_SEARCH, "        [assess] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
            currStepTwoCluster.setAssessed(currentChar, currentPatternCharIndex, currentPatternCharPositionInVariant);
        
            nearestPositionInVariant = POS_UNINITIALIZED;

            hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
            hasNextInVariant = currentPatternCharPositionInVariant < data.variant.length() - 1;

            currentCharInVariantQty++;
            positionAlreadyFilled = filledPositions.contains(currentPatternCharPositionInVariant);
            positionsInCluster.clear();

            if ( ! positionAlreadyFilled ) {
                
                if ( gotoBreakpointWhen(stepCharAndPositionAre(STEP_2, 'd', 19)) ) {
                    breakpoint();
                }
                
                if ( hasPreviousInPattern && hasPreviousInVariant ) {
                    previousPositionInVariantFound = filledPositions.contains(currentPatternCharPositionInVariant - 1);
                    if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
                        if ( ! previousPositionInVariantFound && ! notFoundPatternChars.contains(this.previousCharInVariant) ) {
                            // omit this match
                        } else {
                            logAnalyze(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'", 
                                    previousCharInVariant, currentPatternCharPositionInVariant - 1, currentChar);
                            positionsInCluster.add(this.currentPatternCharPositionInVariant - 1);
                            int patternIndex = currentPatternCharIndex - 1;
                            currStepTwoCluster.add(
                                    previousCharInVariant,
                                    patternIndex,
                                    currentPatternCharPositionInVariant - 1, 
                                    previousPositionInVariantFound,
                                    this.isPositionSetAt(patternIndex),
                                    MATCH_DIRECTLY);
                            nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                        }                                              
                    }
                }
                if ( hasNextInPattern && hasNextInVariant ) {
                    boolean nextCharInVariantIncluded = filledPositions.contains(currentPatternCharPositionInVariant + 1);
                    /* OLD BREAKING */ // if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) || nextCharInVariantIncluded ) {
                    if ( nextCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
                        logAnalyze(POSITIONS_SEARCH, "          [info] next '%s'(%s in variant) is in cluster with current '%s'", 
                                nextCharInVariant, currentPatternCharPositionInVariant + 1, currentChar);
                        positionsInCluster.add(this.currentPatternCharPositionInVariant + 1);                        
                        currStepTwoCluster.add(
                                nextCharInVariant,
                                currentPatternCharIndex + 1,
                                currentPatternCharPositionInVariant + 1, 
                                nextCharInVariantIncluded,
                                this.isPositionSetAt(currentPatternCharIndex + 1),
                                MATCH_DIRECTLY);
                        nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                    } 
                }
                
                if ( findPositionsStep.typoSearchingAllowed() ) {         
                    boolean distanceOneTypoFound = false;
                    
                    if ( hasPreviousInPattern && hasNextInVariant ) {
                        
                        previousCharInPattern = data.patternChars[currentPatternCharIndex - 1];
                        nextCharInVariant = data.variant.charAt(currentPatternCharPositionInVariant + 1);
                        
                        if ( previousCharInPattern == nextCharInVariant ) {
                            boolean patternOfTypoFilled = isPositionSetAt(currentPatternCharIndex - 1);
                            boolean variantOfTypoFilled = filledPositions.contains(currentPatternCharPositionInVariant + 1);
                            boolean respectMatch = true;
                            if ( patternOfTypoFilled && (! variantOfTypoFilled) ) {
                                respectMatch = notFoundPatternChars.contains(previousCharInPattern);
                            } 
                            if ( respectMatch ) {
                                logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant", 
                                        currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                                positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                currStepTwoCluster.add(
                                        nextCharInVariant,
                                        currentPatternCharIndex - 1,
                                        currentPatternCharPositionInVariant + 1, 
                                        variantOfTypoFilled,
                                        patternOfTypoFilled,
                                        MATCH_TYPO_1);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                            }                            
                        }
                    }
                    if ( hasPreviousInVariant && hasNextInPattern ) {

                        previousCharInVariant = data.variant.charAt(currentPatternCharPositionInVariant - 1);
                        nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];

                        if ( previousCharInVariant == nextCharInPattern ) {
                            if ( filledPositions.contains(currentPatternCharPositionInVariant - 1) || notFoundPatternChars.contains(nextCharInPattern) ) {
                                logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and previous in variant", 
                                        currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                positionsInCluster.add(currentPatternCharPositionInVariant - 1);
                                currStepTwoCluster.add(
                                        previousCharInVariant,
                                        currentPatternCharIndex + 1,
                                        currentPatternCharPositionInVariant - 1, 
                                        filledPositions.contains(currentPatternCharPositionInVariant + 1),
                                        isPositionSetAt(currentPatternCharIndex + 1),
                                        MATCH_TYPO_1);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            }                            
                        }
                    }
                        
                    boolean nextNextFoundAsTypo = false;
                    if ( hasNextInPattern && hasNextInVariant ) {
                        nextCharInPattern = data.patternChars[currentPatternCharIndex + 1];
                        // if there are at least two characters ahead in variant...
                        if ( data.variant.length() - currentPatternCharPositionInVariant > 2 ) {
                            int nextNextPosition = currentPatternCharPositionInVariant + 2;
                            if ( ! data.variantSeparators.contains(nextNextPosition - 1) ) {
                                char nextNextCharInVariant = data.variant.charAt(nextNextPosition);
                                if ( nextCharInPattern == nextNextCharInVariant ) { // TODO here something about _x..ab.. word-cluster logic
                                    boolean nextNextPositionIncluded = filledPositions.contains(nextNextPosition);
                                    if ( nextNextPositionIncluded || notFoundPatternChars.contains(nextCharInPattern) ) {
                                        logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and next*2 in variant", 
                                                currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                        positionsInCluster.add(nextNextPosition);
                                        currStepTwoCluster.add(
                                                nextNextCharInVariant,
                                                currentPatternCharIndex + 1,
                                                nextNextPosition, 
                                                nextNextPositionIncluded,
                                                this.isPositionSetAt(currentPatternCharIndex + 1),
                                                MATCH_TYPO_2);
                                        nearestPositionInVariant = nextNextPosition;
                                        nextNextFoundAsTypo = true;

                                        if ( data.pattern.length() - currentPatternCharIndex > 2 && 
                                             data.variant.length() - currentPatternCharPositionInVariant > 3) {
                                            char next2CharInPattern = data.patternChars[currentPatternCharIndex + 2];
                                            int next3Position = currentPatternCharPositionInVariant + 3;
                                            char next3CharInVariant = data.variant.charAt(next3Position);
                                            if ( next2CharInPattern == next3CharInVariant ) {
                                                positionsInCluster.add(next3Position);
                                                currStepTwoCluster.add(
                                                         next2CharInPattern,
                                                         currentPatternCharIndex + 2,
                                                         next3Position, 
                                                         filledPositions.contains(next3Position),
                                                         isPositionSetAt(currentPatternCharIndex + 2),
                                                         MATCH_TYPO_3_2);

                                                if ( data.pattern.length() - currentPatternCharIndex > 3 ) {
                                                    char next3CharInPattern = data.patternChars[currentPatternCharIndex + 3];
                                                    if ( next3CharInPattern == nextCharInVariant ) {
                                                        boolean needToInclude = notFoundPatternChars.contains(nextCharInVariant);
                                                        if ( needToInclude ) {
                                                            positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                                            currStepTwoCluster.add(
                                                                    next3CharInPattern,
                                                                    currentPatternCharIndex + 3,
                                                                    currentPatternCharPositionInVariant + 1,
                                                                    filledPositions.contains(currentPatternCharPositionInVariant + 1),
                                                                    isPositionSetAt(currentPatternCharIndex + 3),
                                                                    MATCH_TYPO_3_3);
                                                        }                                                    
                                                    }
                                                }
                                            }
                                        }
                                    }                                         
                                }
                                else {
                                    boolean currentCharIsVariantWordStart = this.data.variantSeparators.contains(currentPatternCharPositionInVariant - 1);
                                    if ( currentCharIsVariantWordStart ) {
                                        int iPattern = currentPatternCharIndex + 1;
                                        int iVariant = currentPatternCharPositionInVariant + 1;
                                        int limitPattern = data.pattern.length() -1 -iPattern > 4 ? iPattern + 3 : data.pattern.length()-1;
                                        int limitVariant = data.variant.length() -1 -iVariant > 4 ? iVariant + 3 : data.variant.length()-1; // TODO limit until end of the word
                                        int matches = 0;
                                        char variantCh;
                                        char patternCh;
                                        patternLookup: for (; iPattern <= limitPattern; iPattern++) {
                                            patternCh = data.pattern.charAt(iPattern);
                                            variantLookup: for (int jVariant = iVariant; jVariant <= limitVariant; jVariant++) {
                                                variantCh = data.variant.charAt(jVariant);
                                                if ( patternCh == variantCh ) {
                                                    logAnalyze(POSITIONS_SEARCH, "          [info] loop-typo found '%s' variant:%s pattern:%s)",
                                                            patternCh, jVariant, iPattern);
                                                    matches++;
                                                    if ( matches > 1 ) {
                                                        currStepTwoCluster.add(
                                                                patternCh,
                                                                iPattern,
                                                                jVariant,
                                                                filledPositions.contains(jVariant),
                                                                isPositionSetAt(iPattern),
                                                                MATCH_TYPO_LOOP);
                                                    }
                                                    break variantLookup;
                                                }
                                            }
                                        }
                                    }
                                    int nextNextPatternCharIndex = currentPatternCharIndex + 2;
                                    if ( nextNextPatternCharIndex < data.pattern.length() ) {                                        
                                        char nextNextCharInPattern = data.patternChars[nextNextPatternCharIndex];

                                        if ( nextNextCharInPattern == nextCharInVariant ) {
                                            if ( positionPatternIndexes.containsKey(currentPatternCharPositionInVariant + 1) && 
                                                 positionPatternIndexes.containsValue(nextNextPatternCharIndex) ) {
                                                breakpoint();
                                                
                                                positionsInCluster.add(currentPatternCharPositionInVariant);
                                                currStepTwoCluster.add(
                                                        nextNextCharInPattern, 
                                                        nextNextPatternCharIndex, 
                                                        currentPatternCharPositionInVariant + 1, 
                                                        true,
                                                        true,
                                                        MATCH_TYPO_2);
                                            }
                                        }
                                    }
                                }
                            }

                            if ( data.pattern.length() - currentPatternCharIndex > 2 ) {
                                char next2CharInPattern = data.patternChars[currentPatternCharIndex + 2];
                                if ( nextCharInVariant == next2CharInPattern ) {
                                    if ( notFoundPatternChars.contains(next2CharInPattern) ) {
                                        logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next*2 in pattern and next in variant", 
                                                currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                                        if ( nonEmpty(positionsInCluster) ) {
                                            positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                            currStepTwoCluster.add(
                                                    next2CharInPattern,
                                                    currentPatternCharIndex + 2,
                                                    currentPatternCharPositionInVariant + 1, 
                                                    filledPositions.contains(currentPatternCharPositionInVariant + 1),
                                                    this.isPositionSetAt(currentPatternCharIndex + 2),
                                                    MATCH_TYPO_2);

                                            if ( data.pattern.length() - currentPatternCharIndex > 3 && 
                                                 data.variant.length() - currentPatternCharPositionInVariant > 3 ) {
                                                int next3Position = currentPatternCharPositionInVariant + 3;
                                                char next3CharInPattern = data.patternChars[currentPatternCharIndex + 3];
                                                char next3CharInVariant = data.variant.charAt(next3Position);
                                                if ( next3CharInPattern == next3CharInVariant ) {
                                                    boolean next3PositionIncluded = filledPositions.contains(next3Position);
                                                    if ( next3PositionIncluded || notFoundPatternChars.contains(next3CharInVariant) ) {
                                                        logAnalyze(POSITIONS_SEARCH, "          [info] cluster continuation '%s'(%s in variant) found", 
                                                                next3CharInVariant, currentPatternCharPositionInVariant + 3);
                                                        positionsInCluster.add(currentPatternCharPositionInVariant + 3);
                                                        currStepTwoCluster.add(
                                                                next3CharInVariant,
                                                                currentPatternCharIndex + 3,
                                                                next3Position, 
                                                                next3PositionIncluded,
                                                                this.isPositionSetAt(currentPatternCharIndex + 3),
                                                                MATCH_TYPO_3_1);
                                                    } 
                                                }
                                            }
                                        }                                            
                                    }                                        
                                }
                            }
                        }
                    } 
                    if ( ! nextNextFoundAsTypo && hasPreviousInPattern && hasPreviousInVariant ) {
                        int patternIndex = currentPatternCharIndex - 1;
                        previousCharInPattern = data.patternChars[patternIndex];
                        // if there are at least two characters behind in variant...
                        if ( currentPatternCharPositionInVariant > 1 ) {
                            int prevPrevPosition = currentPatternCharPositionInVariant - 2;
                            if ( ! data.variantSeparators.contains(prevPrevPosition + 1) ) {
                                if ( previousCharInPattern == data.variant.charAt(prevPrevPosition) ) {
                                    boolean prevPrevPositionIncluded = filledPositions.contains(prevPrevPosition);
                                    if ( prevPrevPositionIncluded || notFoundPatternChars.contains(previousCharInPattern) ) {
                                        logAnalyze(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and previous*2 in variant",
                                                currentChar, currentPatternCharPositionInVariant, previousCharInPattern);
                                        positionsInCluster.add(prevPrevPosition);
                                        currStepTwoCluster.add(
                                                previousCharInPattern,
                                                patternIndex,
                                                prevPrevPosition,
                                                prevPrevPositionIncluded,
                                                this.isPositionSetAt(patternIndex),
                                                MATCH_TYPO_3_1);
                                        nearestPositionInVariant = prevPrevPosition;
                                    }
                                }
                            }
                        }
                    }
                }

                if ( findPositionsStep.equals(STEP_1) ) {
                    if ( findPositionsStep.canAddToPositions(positionsInCluster.size()) ) {
                        currStepOneCluster.setMain(currentPatternCharIndex, currentPatternCharPositionInVariant);
                        isCurrentCharPositionAddedToPositions = true;
                        
                        logAnalyze(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
                        logAnalyze(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                        
                        if ( canFillPosition(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1) ) {     
                            currStepOneCluster.setPrev(currentPatternCharPositionInVariant - 1);
                            logAnalyze(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) is previous both in pattern and variant", previousCharInVariant, currentPatternCharPositionInVariant - 1);
                            logAnalyze(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                            logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                            
                            int i = 2;
                            step1BackwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex - i, currentPatternCharPositionInVariant - i) ) {                                
                                char patChar = data.pattern.charAt(currentPatternCharIndex - i);
                                char varChar = data.variant.charAt(currentPatternCharPositionInVariant - i);
                                if ( patChar == varChar ) {
                                    currStepOneCluster.addPrev(currentPatternCharPositionInVariant - i);    
                                    logAnalyze(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) preciding <<<", varChar, currentPatternCharPositionInVariant - i);
                                    logAnalyze(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                                    logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                                } else {
                                    break step1BackwardLoop;    
                                }
                                i++;
                            }
                        }
                        
                        if ( canFillPosition(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1) ) {       
                            currStepOneCluster.setNext(currentPatternCharPositionInVariant + 1);
                            logAnalyze(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) is next both in pattern and variant", nextCharInVariant, currentPatternCharPositionInVariant + 1);
                            logAnalyze(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                            logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                            currStepOneCluster.incrementSkip();
                            
                            int i = 2;
                            step1ForwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex + i, currentPatternCharPositionInVariant + i) ) {  
                                char patChar = data.pattern.charAt(currentPatternCharIndex + i);
                                char varChar = data.variant.charAt(currentPatternCharPositionInVariant + i);
                                if ( patChar == varChar ) {
                                    currStepOneCluster.incrementSkip();
                                    currStepOneCluster.addNext(currentPatternCharPositionInVariant + i);      
                                    logAnalyze(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) following >>>", varChar, currentPatternCharPositionInVariant + i);
                                    logAnalyze(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                                    logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                                } else {
                                        break step1ForwardLoop;    
                                    }
                                    i++;
                                }
                        }
                    }

                    if ( maySearchAsCharInWord ) {
                        Integer separatorIndexBeforeWord = this.data.variantSeparators.lower(lastSavedStepOneCluster.firstVariantPosition());
                        Integer separatorIndexAfterWord = this.data.variantSeparators.higher(lastSavedStepOneCluster.lastVariantPosition());

                        int wordStartIndex = nonNull(separatorIndexBeforeWord) ? separatorIndexBeforeWord + 1 : 0;
                        int wordEndIndex = nonNull(separatorIndexAfterWord) ? separatorIndexAfterWord - 1 : data.variant.length()-1;

                        if ( wordStartIndex <= currentPatternCharPositionInVariant && currentPatternCharPositionInVariant <= wordEndIndex  ) {
                            int a = 5;
                            currStepOneCluster.setMain(currentPatternCharIndex, currentPatternCharPositionInVariant);
                        }
                        maySearchAsCharInWord = false;
                    }
                } else if ( findPositionsStep.typoSearchingAllowed()) {
                    // do nothing                   
                } else {    
                    // on steps, other than STEP_1, do not save positions directly, just record them as appropriate for saving (excluding STEP_4).
                    if ( findPositionsStep.canAddToPositions(positionsInCluster.size()) ) {
                        
                        int orderDiffInPattern;
                        if ( nearestPositionInVariant == POS_UNINITIALIZED && isNotEmpty(positionPatternIndexes) ) {
                            nearestPositionInVariant = getNearestToValueFromSetExcluding(currentPatternCharPositionInVariant, positionPatternIndexes.keySet());
                        }
                        if ( nearestPositionInVariant > POS_NOT_FOUND ) {
                            Integer nearestPatternCharIndex = positionPatternIndexes.get(nearestPositionInVariant);
                            if ( isNull(nearestPatternCharIndex) ) {
                                // nearest char can be null only when current char is clastered with next pattern position
                                nearestPatternCharIndex = currentPatternCharIndex + 1;
                            } 
                            orderDiffInPattern = abs(currentPatternCharIndex - nearestPatternCharIndex);
                        } else {
                            orderDiffInPattern = nearestPositionInVariant;
                        }                        
                        
                        int previousCharInVariantByPattern = POS_UNINITIALIZED;
                        int nextCharInVariantByPattern = POS_UNINITIALIZED;
                        int orderDiffInVariant = POS_UNINITIALIZED;
                        
                        if ( hasPreviousInPattern ) {
                            previousCharInVariantByPattern = positions[currentPatternCharIndex - 1];
                        }
                        if ( previousCharInVariantByPattern > -1 ) {
                            orderDiffInVariant = absDiff(previousCharInVariantByPattern, currentPatternCharPositionInVariant);
                        }
//                        } else {
//                            if ( hasNextInPattern ) {
//                                nextCharInVariantByPattern = positions[currentPatternCharIndex + 1];
//                            }
//                            if ( nextCharInVariantByPattern > -1 ) {
//                                orderDiffInVariant = absDiff(currentPatternCharPositionInVariant, nextCharInVariantByPattern);
//                            }                          
//                        }  
                        if ( orderDiffInVariant == POS_UNINITIALIZED && nonEmpty(filledPositions) ) {
                            int nearestFilledPosition = findNearestFilledPositionTo(currentPatternCharIndex);
                            orderDiffInVariant = absDiff(currentPatternCharPositionInVariant, nearestFilledPosition);
                        }     
                        
                        boolean isNearSeparator = 
                                currentPatternCharPositionInVariant == 0 ||
                                currentPatternCharPositionInVariant == data.variant.length() - 1 ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant + 1) ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant - 1);                        
                        Integer distanceToNearestFilledPosition = null;
                        if ( nonEmpty(filledPositions) ) {
                            Integer nearestFilledPosition = getNearestToValueFromSetExcluding(currentPatternCharPositionInVariant, filledPositions);
                            if ( nonNull(nearestFilledPosition) ) {
                                distanceToNearestFilledPosition = absDiff(nearestFilledPosition, currentPatternCharPositionInVariant);
                            }
                        }                        
                        positionCandidate.tryToMutate(
                                currentPatternCharPositionInVariant, 
                                currentPatternCharIndex,
                                orderDiffInVariant, 
                                orderDiffInPattern, 
                                positionsInCluster.size(),
                                isNearSeparator,
                                distanceToNearestFilledPosition,
                                charsRemained);
                    }
                }                
            } else {
                logAnalyze(POSITIONS_SEARCH, "          [info] already filled, skip");
            } 
            
            if ( findPositionsStep.equals(STEP_1) ) {
                if ( currStepOneCluster.isSet() ) {
                    currStepOneCluster.finish(data.variant, data.pattern);
                    if ( prevStepOneCluster.isSet() ) {
                        if ( currStepOneCluster.isBetterThan(prevStepOneCluster) ) {
                            acceptCurrentCluster();
                        } else if ( prevStepOneCluster.isBetterThan(currStepOneCluster) ) {
                            acceptPreviousCluster();
                        } else {
                            ClusterPreference comparison = compare(prevStepOneCluster, currStepOneCluster);
                            if ( comparison.equals(PREFER_LEFT) ) {
                                acceptPreviousCluster();
                            } else {
                                acceptCurrentCluster();
                            } 
                        }
                                                
                        /* [EXP] */ improveWeightOnFoundDuplicateClusters(prevStepOneCluster, currStepOneCluster);
                    } else {
                        acceptCurrentAndSwapStepOneClusters();
                    }                    
                }                
            }
            
            if ( findPositionsStep.typoSearchingAllowed() ) {
                if ( currStepTwoCluster.isSet() ) {
                    if ( prevStepTwoCluster.isSet() ) {
                        if ( currStepTwoCluster.isBetterThan(prevStepTwoCluster) ) {
                            logAnalyze(POSITIONS_SEARCH, "        [COMPARE POSITION]");
                            logAnalyze(POSITIONS_SEARCH, "             [old]          %s", prevStepTwoCluster);
                            logAnalyze(POSITIONS_SEARCH, "             [new]  better  %s", currStepTwoCluster);
                            acceptCurrentAndSwapStepTwoSubclusters();
                        } else {
                            logAnalyze(POSITIONS_SEARCH, "        [COMPARE POSITION]");
                            logAnalyze(POSITIONS_SEARCH, "             [old]  better  %s", prevStepTwoCluster);
                            logAnalyze(POSITIONS_SEARCH, "             [new]          %s", currStepTwoCluster);
                            currStepTwoCluster.clear();
                        }
                    } else {
                        logAnalyze(POSITIONS_SEARCH, "        [ACCEPT POSITIONS - NOTHING TO COMPARE]");
                        logAnalyze(POSITIONS_SEARCH, "             %s", currStepTwoCluster);
                        acceptCurrentAndSwapStepTwoSubclusters();
                    }                    
                }      
            } else {
                currStepTwoCluster.clear();
                prevStepTwoCluster.clear();
            }

            currentPatternCharPositionInVariantToSave = currentPatternCharPositionInVariant;
            currentPatternCharPositionInVariant = 
                    data.variant
                            .indexOf(
                                    currentChar, 
                                    currentPatternCharPositionInVariant + 1);
        }  
        /* 
         * end of characterFinding loop 
         */
        
        if ( findPositionsStep.equals(STEP_1)) {
            if ( prevStepOneCluster.isSet() ) {
                logAnalyze(POSITIONS_SEARCH, "        [SAVE CLUSTER] %s", prevStepOneCluster);
                fillPositionsFrom(prevStepOneCluster);
            }
        } else if ( findPositionsStep.typoSearchingAllowed() ) { 
            if ( prevStepTwoCluster.isSet() ) {
                logAnalyze(POSITIONS_SEARCH, "        [SAVE POSITIONS] %s", prevStepTwoCluster);
                fillPositionsFrom(prevStepTwoCluster);
            }
        } else if ( positionCandidate.isPresent() ) {
            int position = positionCandidate.position();
                                                
            isCurrentCharPositionAddedToPositions = true;
            fillPosition(currentPatternCharIndex, position);
            logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant), %s", currentChar, position, positionCandidate);
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
            logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
        }
        positionCandidate.clear();

        // if current position has not been added because it does not satisfy requirements...
        if ( ! isCurrentCharPositionAddedToPositions ) {
            // ...but if it is STEP_1 and there are only 1 such char in the whole pattern, there is not sense
            // to do operation for this char in subsequent steps - add this char to filled positions and exclude
            // it from subsequent iterations
            if ( findPositionsStep.canAddSingleUnclusteredPosition() && currentCharInVariantQty == 1 ) {
                if ( positionAlreadyFilled ) {
                    logAnalyze(POSITIONS_SEARCH, "          [info] '%s'(%s in variant) is single char in variant and already saved", currentChar, currentPatternCharPositionInVariantToSave);
                    positions[currentPatternCharIndex] = POS_NOT_FOUND;
                } else {
                    fillPosition(currentPatternCharIndex, currentPatternCharPositionInVariantToSave);
                    logAnalyze(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant) is single char in variant", currentChar, currentPatternCharPositionInVariantToSave);
                    logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
                    logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                }                
            } else {
                logAnalyze(POSITIONS_SEARCH, "        [info] position of '%s' is not defined", currentChar);
                localUnclusteredPatternCharIndexes.add(currentPatternCharIndex);
            }                
        }

        isCurrentCharPositionAddedToPositions = false;
        currentPatternCharPositionInVariantToSave = POS_UNINITIALIZED;     
        nextPatternCharsToSkip = prevStepOneCluster.skip();
        
        prevStepOneCluster.clear();
        currStepOneCluster.clear();
        
        prevStepTwoCluster.clear();
        currStepTwoCluster.clear();
    }

    private void acceptPreviousCluster() {
        logAnalyze(POSITIONS_SEARCH, "        [COMPARE CLUSTERS]");
        logAnalyze(POSITIONS_SEARCH, "             [old]  better  %s", prevStepOneCluster);
        logAnalyze(POSITIONS_SEARCH, "             [new]          %s", currStepOneCluster);
        
        currStepOneCluster.clear();
    }

    private void acceptCurrentCluster() {        
        logAnalyze(POSITIONS_SEARCH, "        [COMPARE CLUSTERS]");
        logAnalyze(POSITIONS_SEARCH, "             [old]          %s", prevStepOneCluster);
        logAnalyze(POSITIONS_SEARCH, "             [new]  better  %s", currStepOneCluster);
        
        acceptCurrentAndSwapStepOneClusters();
    }

    private void acceptCurrentAndSwapStepOneClusters() {
        PositionsSearchStepOneCluster swap = currStepOneCluster;
        prevStepOneCluster.clear();
        currStepOneCluster = prevStepOneCluster;
        prevStepOneCluster = swap;
    }

    private void acceptCurrentAndSwapStepTwoSubclusters() {
        PositionsSearchStepTwoCluster swap = currStepTwoCluster;
        prevStepTwoCluster.clear();
        currStepTwoCluster = prevStepTwoCluster;
        prevStepTwoCluster = swap;
    }
    
    private void improveWeightOnFoundDuplicateClusters(
            PositionsSearchStepOneCluster one, 
            PositionsSearchStepOneCluster two) {
        float bonus = calculateSimilarity(one, two);
        logAnalyze(POSITIONS_SEARCH, "        [clusters are similar] -%s", bonus);
        weight.add(-bonus, CLUSTER_CANDIDATES_SIMILARITY);
    }
    
    private boolean canFillPosition(int positionIndex, int positionValue) {
        if ( positions[positionIndex] == POS_UNINITIALIZED ) {
            if ( ! filledPositions.contains(positionValue) ) {
                return true;
            }
        }
        return false;
    }
    
    private boolean positionsExistAndCanFillPosition(int positionIndex, int positionValue) {
        if ( positionIndex > -1 && 
             positionValue > -1 && 
             positionIndex < positions.length && 
             positionValue < data.variant.length() ) {
            return canFillPosition(positionIndex, positionValue);
        } else {
            return false;
        }
    }
    
    private void fillPositionsFrom(PositionsSearchStepOneCluster subcluster) {
        PositionIterableView position = subcluster.positionIterableView();
        while ( position.hasNext() ) {
            position.goToNext();
            fillPosition(position.patternPosition(), position.variantPosition());
            logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
        }        
        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
        stepOneClusterSavedRightNow = true;
        lastSavedStepOneCluster.copyFrom(subcluster);
    }
    
    private void fillPositionsFrom(PositionsSearchStepTwoCluster subcluster) {
        PositionsSearchStepTwoCluster.StepTwoClusterPositionView position = subcluster.positionView();
        fillPosition(subcluster.charPatternPosition(), subcluster.charVariantPosition());
        logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
        while ( position.hasNext() ) {
            position.goToNext();
            if ( position.canBeWritten() ) {
                fillPosition(position.patternPosition(), position.variantPosition());
                logAnalyze(POSITIONS_SEARCH, "               %s", displayPositions());
            }
        }  
        logAnalyze(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
    }

    private void fillPosition(int patternIndex, int positionValue) {
        positions[patternIndex] = positionValue;
        positionPatternIndexes.put(positionValue, patternIndex);
        positionFoundSteps.put(positionValue, findPositionsStep);
        localUnclusteredPatternCharIndexes.remove(patternIndex);
        Character c = data.patternChars[patternIndex];
        this.notFoundPatternChars.remove(c);
        isCurrentCharPositionAddedToPositions = true;
    }
    
    private int findNearestFilledPositionTo(int patternCharIndex) {
        int nearestFoundPosition;
        
        if ( patternCharIndex == 0 ) {
            nearestFoundPosition = searchForwardNearestFilledPositionTo(patternCharIndex);
        } else if ( patternCharIndex == data.pattern.length() - 1 ) {
            nearestFoundPosition = searchBackwardNearestFilledPositionTo(patternCharIndex);
        } else {
            nearestFoundPosition = searchForwardAndBackwardNearestFilledPositionTo(patternCharIndex);
        }
        
        return nearestFoundPosition;
    }
    
    private int searchForwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        for (int i = patternCharIndex + 1; i < positions.length; i++) {
            position = positions[i];
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        for (int i = patternCharIndex - 1; i > -1; i--) {
            position = positions[i];
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchForwardAndBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        
        if ( patternCharIndex > positions.length / 2 ) {
            position = searchForwardNearestFilledPositionTo(patternCharIndex);
            if ( position == POS_NOT_FOUND ) {
                position = searchBackwardNearestFilledPositionTo(patternCharIndex);
            }
        } else {
            position = searchBackwardNearestFilledPositionTo(patternCharIndex);
            if ( position == POS_NOT_FOUND ) {
                position = searchForwardNearestFilledPositionTo(patternCharIndex);
            }
        }
        
        return position;
    }
    
    boolean isCurrentCharVariantEnd() {
        return this.currentPosition == this.data.variant.length() - 1;
    }
    
    void setCurrentPosition(int i) {
        this.currentPositionIndex = i;
        this.currentPosition = this.positions[i];
    }

    void newClusterStarts() {
        this.currentClusterFirstPosition = this.currentPosition;
        this.clustered++;
        this.clustersQty++;
        this.clusterContinuation = true;
        this.currentClusterLength = 1;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        
        this.processClusterPositionOrderStats();
        
        if ( this.currentPosition == 0 ) {
            this.weight.add(CLUSTER_STARTS_WITH_VARIANT);
            
            this.clusterStartsWithVariant = true;
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        } else if ( this.isPreviousCharWordSeparator() ) {       
            this.weight.add(CLUSTER_STARTS_PREVIOUS_CHAR_IS_WORD_SEPARATOR);
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        } else if ( this.isCurrentCharWordSeparator() ) {
            this.weight.add(CLUSTER_STARTS_CURRENT_CHAR_IS_WORD_SEPARATOR);
            this.clusterStartsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingStartEdges++;
        }
    }
    
    void clusterIsContinuing() {
        this.clustered++;
        this.currentClusterLength++;
        this.processClusterPositionOrderStats();
    }

    void clusterEnds() {
        this.clustered++;
        this.currentClusterLength++;
        this.clusterContinuation = false;  
        
        this.processClusterPositionOrderStats();
        this.accumulateClusterPositionOrdersStats();    
        
        boolean clusterEndsWithVariant = false;

        if ( this.isCurrentCharVariantEnd() ) {
            float bonus = 3.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            this.weight.add(-bonus, CLUSTER_ENDS_WITH_VARIANT);
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
            clusterEndsWithVariant = true;
        } else if ( this.isNextCharWordSeparator() ) {
            float bonus = 3.6f; 
            if ( this.currentClusterLength > 2 ) {
                bonus = bonus + this.currentClusterLength;
            }
            this.weight.add(-bonus, CLUSTER_ENDS_NEXT_CHAR_IS_WORD_SEPARATOR);
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
        } else if ( this.isCurrentCharWordSeparator() ) {
            this.weight.add(CLUSTER_ENDS_CURRENT_CHAR_IS_WORD_SEPARATOR);
            this.clusterEndsWithSeparator = true;
            this.clustersFacingEdges++;
            this.clustersFacingEndEdges++;
        }
        
//        if ( this.currentClusterLength < this.data.pattern.length() ) {
//            if ( data.variantText.length() / 2 > this.data.pattern.length() ) {
//                if ( clusterEndsWithVariant ) {
//                    int patternFirstPosition = this.positionPatternIndexes.get(this.currentPosition) - (this.currentClusterLength - 1);
//                    if ( patternFirstPosition == 0 ) {
//                        int penalty = square(this.currentClusterLength);
//                        this.weight.add(penalty, UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START);
//                    }
//                    if ( patternFirstPosition < 0 ) {
//                        throw new IllegalStateException();
//                    }
//                } else if ( this.clusterStartsWithVariant ) {
//                    int patternLastPosition = this.positionPatternIndexes.get(this.currentPosition);
//                    if ( patternLastPosition == this.data.pattern.length() - 1 ) {
//                        int penalty = square(this.currentClusterLength);
//                        this.weight.add(penalty, UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END);
//                    }
//                }
//            }            
//        }
        
        if ( this.clusterStartsWithVariant && this.currentClusterLength > 2 ) {
            
        }

        boolean isClusterLongWord = false;
        if ( this.clusterEndsWithSeparator ) {
            if ( this.clusterStartsWithSeparator ) {
                float bonus = 10.25f;
                if ( this.currentClusterLength > 2 ) {
                    bonus = bonus + (this.currentClusterLength * 2);
                    isClusterLongWord = true;
                } 
                this.weight.add(-bonus, CLUSTER_IS_WORD);
            } else {
                if ( this.alonePositionAfterPreviousSeparator != POS_UNINITIALIZED &&
                     this.alonePositionAfterPreviousSeparator != POS_ERASED ) {
                    float bonus = 7.25f;
                    if ( this.currentClusterLength > 2 ) {
                        bonus = bonus + (this.currentClusterLength * 2) - (this.clusters.lastAddedCluster().firstPosition() - this.alonePositionAfterPreviousSeparator - 1);
                        isClusterLongWord = true;
                    } 
                    if ( ! containsSeparatorsInVariantInSpan(this.alonePositionAfterPreviousSeparator, this.currentPosition - this.currentClusterLength + 1) ) {
                        this.weight.add(-bonus, CHAR_AFTER_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD);
                        if ( absDiff(this.alonePositionAfterPreviousSeparator, this.clusters.lastAddedCluster().firstPosition()) == 2 ) {
                            char alonePositionBeforePreviousSeparatorChar = data.variant.charAt(this.alonePositionAfterPreviousSeparator);
                            char missedPositionChar = data.variant.charAt(this.alonePositionAfterPreviousSeparator + 1);
                            char firstClusterPositionChar = data.variant.charAt(this.clusters.lastAddedCluster().firstPosition());
                            if ( missedPositionChar == alonePositionBeforePreviousSeparatorChar || 
                                 missedPositionChar == firstClusterPositionChar ) {
                                this.clustered++;
                                this.nonClustered--;
                                String log = format(
                                        "    [cluster fix] missed outer repeat detected %s(%s)%s", 
                                        alonePositionBeforePreviousSeparatorChar, 
                                        missedPositionChar,
                                        firstClusterPositionChar);
                                logAnalyze(POSITIONS_CLUSTERS, 
                                        "         %s", log);
                            }
                        }
                    }                   
                }
            }
        } else {
            if ( this.previousClusterLastPosition > 0 ) {
                if ( ! this.previousClusterEndsWithSeparator && ! this.clusterStartsWithSeparator ) {
                    int distance = this.currentClusterFirstPosition - this.previousClusterLastPosition;

                    if ( distance < this.previousClusterLength + this.currentClusterLength ) {
                        boolean containsSeparators = containsSeparatorsInVariantInSpan(
                                this.previousClusterLastPosition, this.currentClusterFirstPosition);
                        if ( ! containsSeparators ) {
                            int improve = this.previousClusterLength + this.currentClusterLength;
                            this.weight.add(-improve, CLUSTERS_NEAR_ARE_IN_ONE_PART);
                        }
                    }            
                }
            }

            if ( this.alonePositionAfterPreviousSeparator != POS_UNINITIALIZED &&
                 this.alonePositionAfterPreviousSeparator != POS_ERASED ) {
                // TODO add bonus here
            }
        }
        
        if ( this.currentClusterLength > 2 && this.currentClusterOrdersIsConsistent ) {
            if ( this.currentClusterOrdersHaveDiffCompensations ) {
//                Cluster cluster = this.clusters.lastAddedCluster();
//                boolean isAcceptable = 
//                        cluster.ordersDiffSumAbs() == 0 && 
//                        cluster.ordersDiffCount() <= this.currentClusterLength / 2;
//                if ( isAcceptable && this.patternContainsClusterFoundInVariant() ) {
//                    int containingReward = square(this.currentClusterLength);
//                    this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER);
//                } else {
//                    logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] pattern DOES NOT contain cluster!");
//                    this.weight.add(this.currentClusterLength, PATTERN_DOES_NOT_CONTAIN_CLUSTER);
//                }
            } else {
                if ( this.patternContainsClusterFoundInVariant() ) {
                    int containingReward;
                    if ( isClusterLongWord ) {
                        containingReward = cube(this.currentClusterLength) + square(this.currentClusterLength);
                        this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER_LONG_WORD);
                    } else {           
                        containingReward = cube(this.currentClusterLength);
                        this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER);
                    }                  
                } else {
                    logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] pattern DOES NOT contain cluster!");
                    this.weight.add(this.currentClusterLength, PATTERN_DOES_NOT_CONTAIN_CLUSTER);
                }
            }            
        }
        
        this.countSeparatorsBetweenClusters();
        
        if ( this.alonePositionAfterPreviousSeparator != POS_UNINITIALIZED ) {
            this.alonePositionAfterPreviousSeparator = POS_ERASED;
        }
        this.previousClusterLastPosition = this.currentPosition;
        this.previousClusterFirstPosition = this.currentClusterFirstPosition;
        this.previousClusterEndsWithSeparator = this.clusterEndsWithSeparator;
        this.previousClusterLength = this.currentClusterLength;
        this.previousClusterOrdersIsConsistent = this.currentClusterOrdersIsConsistent;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
    }
    
    private void countSeparatorsBetweenClusters() {
        if ( this.clustersQty == 1 ) {
            return;
        }
        
        int distanceBetweenTwoClusters = 
                    this.currentClusterFirstPosition - this.previousClusterLastPosition - 1;
        
        switch ( distanceBetweenTwoClusters ) {
            case 0:
                // impossible block
                break;
            case 1:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                break;
            case 2:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                } 
                if ( this.clusterStartsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                break;
            default:
                if ( this.previousClusterEndsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                if ( this.clusterStartsWithSeparator ) {
                    this.separatorsBetweenClusters++;
                }   
                this.separatorsBetweenClusters = this.separatorsBetweenClusters + 
                        countWordSeparatorsInBetween(this.data.variant,
                                this.previousClusterLastPosition + 2, 
                                this.currentClusterFirstPosition - 2);
                break;
        }
    }
    
    private boolean patternContainsClusterFoundInVariant() {
        if ( this.clusters.lastAddedCluster().haveOrdersDiffCompensations() ) {
            
        }
        char first = this.data.variant.charAt(this.currentClusterFirstPosition);
        char patternChar;
        char clusteredChar;
        
        int clusteredCharPos;
        int patternCharPos;
        
        int variantLength = this.data.variant.length();
        int patternLength = this.data.patternChars.length;
        
        int j;
        
        boolean found = false;
        boolean missedRepeatDetected = false;
        
        patternIterating : for (int i = 0; i < this.data.patternChars.length; i++) {
            patternChar = this.data.patternChars[i];
            if ( first == patternChar ) {                
                found = true;
                j = 1;
                
                clusterIterating : for ( ; j < this.currentClusterLength; j++) {
                    if ( missedRepeatDetected ) {
                        clusteredCharPos = this.currentClusterFirstPosition + j + 1;
                        missedRepeatDetected = false;
                    } else {
                        clusteredCharPos = this.currentClusterFirstPosition + j;
                    }                    
                    patternCharPos = i + j;
                    
                    if ( clusteredCharPos >= variantLength || patternCharPos >= patternLength ) {
                        found = false;
                        break patternIterating;
                    }
                    
                    clusteredChar = this.data.variant.charAt(clusteredCharPos);
                    patternChar = this.data.patternChars[patternCharPos];
                    
                    if ( clusteredChar != patternChar ) {
                        found = false;  
                        if ( nonEmpty(this.missedRepeatedPositions) ) {
                            int missedRepeatIndex = this.missedRepeatedPositions.indexOf(clusteredCharPos);
                            if ( missedRepeatIndex > -1 ) {
                                char missedRepeatChar = this.missedRepeatedChars.get(missedRepeatIndex);
                                if ( missedRepeatChar == clusteredChar ) {
                                    found = true;
                                    this.extractedMissedRepeatedPositionsIndexes.add(missedRepeatIndex);
                                    missedRepeatDetected = true;
                                    j--;
                                }
                            }
                        }
                        
                        if ( ! found ) {
                            this.extractedMissedRepeatedPositionsIndexes.clear();
                            break clusterIterating;
                        }                        
                    }
                }
                
                if ( found ) {
                    if ( nonEmpty(this.extractedMissedRepeatedPositionsIndexes) ) {
                        sort(this.extractedMissedRepeatedPositionsIndexes, reverseOrder());
                        int missedRepeatedPositionsIndex;
                        for (int k = 0; k < this.extractedMissedRepeatedPositionsIndexes.size(); k++) {
                            missedRepeatedPositionsIndex = this.extractedMissedRepeatedPositionsIndexes.get(k);
                            this.missedRepeatedPositions.remove(missedRepeatedPositionsIndex);
                            this.missedRepeatedChars.remove(missedRepeatedPositionsIndex);
                        }
                        this.extractedMissedRepeatedPositionsIndexes.clear();
                    }
                    break patternIterating;
                }                
            }
        }        
                              
        this.missedRepeatedChars.clear();
        this.missedRepeatedPositions.clear();
        this.extractedMissedRepeatedPositionsIndexes.clear();
        
        return found;
    }
    
    private boolean containsSeparatorsInVariantInSpan(int fromExcl, int toExcl) {
        for (int i = fromExcl + 1; i < toExcl; i++) {
            if ( isWordsSeparator(data.variant.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    
    void processClusterPositionOrderStats() {  
        int currentPositionUnsortedOrder = this.positionUnsortedOrders.get(this.currentPosition);
        int orderDiff = currentPositionUnsortedOrder - (this.currentPositionIndex - this.missed);
        this.currentClusterOrderDiffs.add(orderDiff);
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "    [cluster position] %s (char '%s') S-Order: %s U-Order: %s orderDiff: %s", 
                this.currentPosition, 
                this.data.variant.charAt(this.currentPosition),
                (this.currentPositionIndex - this.missed), 
                currentPositionUnsortedOrder, 
                orderDiff);
        
        if ( this.missedRepeatingsLog.isPresent() ) {
            logAnalyze(POSITIONS_CLUSTERS, 
                    "         %s",
                    this.missedRepeatingsLog.extractOrThrow());
        }
    }
    
    private int consistencyRewardDependingOnCurrentClusterLength() {
        int consistencyReward = this.currentClusterLength;
        if ( this.currentClusterLength >= this.data.pattern.length() / 2 ) {
            consistencyReward = consistencyReward * 2;
        }
        return consistencyReward;
    }
    
    void accumulateClusterPositionOrdersStats() {
        if ( this.currentClusterOrderDiffs.isEmpty() ) {
            int consistencyReward = this.consistencyRewardDependingOnCurrentClusterLength();
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] cluster is consistent");
            this.weight.add(-consistencyReward, CLUSTER_IS_CONSISTENT);
            this.currentClusterOrdersIsConsistent = true;
            this.currentClusterOrdersHaveDiffCompensations = false;
            return;
        }
        Cluster cluster = this.clusters.getUnprocessed();
        processCluster(
                this.data.pattern.length(),
                cluster,
                this.currentClusterOrderDiffs,                 
                this.currentClusterFirstPosition,
                this.currentClusterLength);

        this.currentClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        this.currentClusterOrdersHaveDiffCompensations = cluster.haveOrdersDiffCompensations();

        if ( cluster.hasOrdersDiff() ) {
            if ( cluster.hasOnlyOneExccessCharBetween() ) {
                
            } else {
                boolean teardown = this.clusters.testOnTeardown(cluster);
                if ( ! teardown ) {
                    int incosistency = inconsistencyOf(cluster, this.currentClusterLength);
                    this.weight.add(incosistency, CLUSTER_IS_NOT_CONSISTENT);
                } 
            }                       
        } else {
            if ( cluster.hasOrdersDiffShifts() ) {
                float shiftDeviation;
                if ( cluster.ordersDiffShifts() == this.currentClusterLength ) {
                    shiftDeviation = square(cluster.ordersDiffShifts());
                } else {
                    shiftDeviation = cluster.ordersDiffShifts() * (float) onePointRatio(cluster.ordersDiffShifts(), this.currentClusterLength);
                }
                logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] cluster has %s shifts", cluster.ordersDiffShifts());
                this.weight.add(shiftDeviation, CLUSTER_HAS_SHIFTS);
            } else {
                boolean teardown = this.clusters.testOnTeardown(cluster);
                if ( ! teardown ) {
                    if ( this.currentClusterLength == 2 ) {
                        // no reward
                    } else {
                        int consistencyReward = this.consistencyRewardDependingOnCurrentClusterLength();
                        logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] cluster is consistent");
                        this.weight.add(-consistencyReward, CLUSTER_IS_CONSISTENT);
                    }  
                }                
            }
        }
        
        this.previousClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();
        
        this.currentClusterOrderDiffs.clear();  
        this.clusters.acceptProcessed(cluster);
    }

    boolean isCurrentAndNextPositionInCluster() {
        boolean clusteredDirectly =  
                this.currentPosition == this.nextPosition - 1 &&
                this.positionFoundSteps.get(this.currentPosition).foundPositionCanBeClustered();
        
        if ( ! clusteredDirectly ) {
            if ( this.currentPosition == this.nextPosition - 2 ) {
                char currChar = this.data.variant.charAt(this.currentPosition);
                char missChar = this.data.variant.charAt(this.currentPosition + 1);
                char nextChar = this.data.variant.charAt(this.nextPosition);

                if ( currChar == missChar ) {                        
                    this.missedRepeatedChars.add(missChar);
                    this.missedRepeatedPositions.add(this.currentPosition + 1);
                    if ( POSITIONS_CLUSTERS.isEnabled() ) {
                        String log = format(
                                "    [cluster fix] missed internal repeat detected %s(%s)%s", 
                                currChar, 
                                missChar,
                                nextChar);
                        this.missedRepeatingsLog.resetTo(log);
                    }
                    return true;
                }
            }
        }
        
        return clusteredDirectly;
    }
    
    void lookForDuplicatedCharsOfSingleCluster() {
        char variantChar;
        char patternChar;
        
        for (int variantPosition = 0; variantPosition < this.data.variant.length(); variantPosition++) {
            if ( this.filledPositions.contains(variantPosition) ) {
                continue;
            }
            
            variantChar = this.data.variant.charAt(variantPosition);
            for (int patternPosition = 0; patternPosition < this.positions.length; patternPosition++) {
                patternChar = this.data.patternChars[patternPosition];
                if ( patternChar == variantChar ) {
                    logAnalyze(POSITIONS_CLUSTERS, "      [?] duplicate char found '%s' : %s for clustered char '%s' : %s", 
                            variantChar, variantPosition, variantChar, this.positions[patternPosition]);
                    this.meaningful++;
                }                
            }
        }
    }
    
    void lookForSeparatedCharsPlacing() {
        char firstPatternChar = this.data.patternChars[0];
        char firstVariantChar = this.data.variant.charAt(0);
        if ( firstPatternChar == firstVariantChar ) {
            this.meaningful++;
        }
        
        char lastPatternChar = this.data.patternChars[this.data.pattern.length() - 1];
        char lastVariantChar = this.data.variant.charAt(this.data.variant.length() - 1);
        if ( lastPatternChar == lastVariantChar ) {
            this.meaningful++;
        }        
    }

    void setNextPosition(int i) {
        this.nextPosition = this.positions[i + 1];
    }

    boolean hasNextPosition(int i) {
        return i < this.positions.length - 1;
    }

    boolean isCurrentPositionAtVariantStart() {
        return this.currentPosition == 0;
    }
    
    boolean isCurrentPositionAtVariantEnd() {
        return this.currentPosition == this.data.variant.length() - 1;
    }

    boolean isCurrentPositionMissed() {
        return this.currentPosition < 0;
    }
    
    void calculateImportance() {
        this.clustersImportance = clustersImportanceDependingOn(
                this.clustersQty, this.clustered + this.meaningful, this.nonClustered - this.meaningful);
        this.nonClusteredImportance = nonClusteredImportanceDependingOn(
                this.nonClustered, this.missed, this.data.patternChars.length);
        logAnalyze(POSITIONS_CLUSTERS, "    [importance] clusters: %s", this.clustersImportance);
        logAnalyze(POSITIONS_CLUSTERS, "    [importance] non-clustered: %s", this.nonClusteredImportance);
    }
    
    boolean isCurrentPositionNotMissed() {
        return this.currentPosition >= 0;
    }
    
    boolean isCurrentCharWordSeparator() {
        return isWordsSeparator(this.data.variant.charAt(this.currentPosition));
    }

    boolean isPreviousCharWordSeparator() {
        return 
                this.currentPosition == 0 || 
                isWordsSeparator(this.data.variant.charAt(this.currentPosition - 1));
    } 
        
    boolean isNextCharWordSeparator() {
        return 
                this.currentPosition == this.data.variant.length() - 1 ||
                isWordsSeparator(this.data.variant.charAt(this.currentPosition + 1));
    }
    
    private boolean positionAfterWordSeparatorIsContinuingPreviousCluster() {
        if ( this.currentPosition < 1 || this.currentPositionIndex == 0) {
            return false;
        } 
        return (this.currentPosition - this.previousClusterLastPosition) == 2;
    }
    
    private boolean currentPositionCharIsPatternStart() {
        return this.data.patternChars[0] == this.data.variant.charAt(this.currentPosition);
    }
    
    private boolean currentPositionCharIsDifferentFromFirstFoundPositionChar() {
        return 
                this.data.variant.charAt(this.currentPosition) != 
                this.data.variant.charAt(this.positions[0 + this.missed]);
    }
    
    final void clearPositionsAnalyze() {
        this.positions = null;
        this.missed = 0;
        this.clustersQty = 0;
        this.clustered = 0;
        this.meaningful = 0;
        this.nonClustered = 0;
        this.currentClusterLength = 0;
        this.previousClusterLength = 0;
        this.clusterContinuation = false;
        this.clusterStartsWithVariant = false;
        this.clusterStartsWithSeparator = false;
        this.clusterEndsWithSeparator = false;
        this.previousClusterEndsWithSeparator = false;
        this.clustersFacingEdges = 0;
        this.clustersFacingStartEdges = 0;
        this.clustersFacingEndEdges = 0;
        this.separatorsBetweenClusters = 0;
        this.currentPosition = POS_UNINITIALIZED;
        this.currentPositionIndex = POS_UNINITIALIZED;
        this.nextPosition = POS_UNINITIALIZED;
        this.alonePositionAfterPreviousSeparator = POS_UNINITIALIZED;
        this.prevCharIsSeparator = false;
        this.nextCharIsSeparator = false;
        this.currentPatternCharPositionInVariant = POS_UNINITIALIZED;
        this.clustersImportance = 0;
        this.nonClusteredImportance = 0;
        this.weight.clear();
        this.previousClusterLastPosition = POS_UNINITIALIZED;
        this.previousClusterFirstPosition = POS_UNINITIALIZED;
        this.currentClusterFirstPosition = POS_UNINITIALIZED;
        this.badReason = NO_REASON;
        this.currentChar = ' ';
        this.nextPatternCharsToSkip = 0;
        this.positionUnsortedOrders.clear();
        this.positionPatternIndexes.clear();
        this.positionFoundSteps.clear();
        this.positionCandidate.clear();
        this.nearestPositionInVariant = POS_UNINITIALIZED;
        this.currentClusterOrderDiffs.clear();
        this.allClustersInconsistency = 0;
        this.clusters.clear();
        this.keyChars.clear();
        this.singlePositions.clear();
        this.currentClusterOrdersIsConsistent = false;
        this.previousClusterOrdersIsConsistent = false;
        this.currentClusterOrdersHaveDiffCompensations = false;
        this.unsortedPositions = 0;
        this.missedRepeatedChars.clear();
        this.missedRepeatedPositions.clear();
        this.extractedMissedRepeatedPositionsIndexes.clear();
    }
    
    boolean previousCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {
        if ( this.currentPatternCharPositionInVariant <= 0 ) {
            return false;
        }
        this.previousCharInPattern = this.data.patternChars[currentPatternCharIndex - 1];
        this.previousCharInVariant = this.data.variant.charAt(this.currentPatternCharPositionInVariant - 1);
        
        return ( this.previousCharInPattern == this.previousCharInVariant );
    }
    
    boolean nextCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {        
        if ( this.currentPatternCharPositionInVariant < 0 || 
             this.currentPatternCharPositionInVariant >= this.data.variant.length() - 1 ) {
            return false;
        }
        this.nextCharInPattern = this.data.patternChars[currentPatternCharIndex + 1];
        this.nextCharInVariant = this.data.variant.charAt(this.currentPatternCharPositionInVariant + 1);
        
        return ( this.nextCharInPattern == this.nextCharInVariant );
    }
        
    void sortPositions() {
        if ( ! this.positionUnsortedOrders.isEmpty() ) {
            this.positionUnsortedOrders.clear();
        }
        int position;
        int previousPosition = POS_UNINITIALIZED;
        int notFoundOrderOffset = 0;
        for (int i = 0; i < this.positions.length; i++) {
            position = this.positions[i];
            if ( position > -1 ) {
                this.positionUnsortedOrders.put(position, i - notFoundOrderOffset);
                if ( previousPosition != POS_UNINITIALIZED ) {
                    if ( previousPosition > position ) {
                        this.unsortedPositions++;
                    }
                }
                previousPosition = position;
            } else {
                notFoundOrderOffset++;
            }
        }
        Arrays.sort(this.positions);
    }
    
}
