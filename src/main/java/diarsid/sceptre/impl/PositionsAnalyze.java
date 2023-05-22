package diarsid.sceptre.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import diarsid.sceptre.impl.collections.ArrayChar;
import diarsid.sceptre.impl.collections.ArrayInt;
import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.ListChar;
import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.MapInt;
import diarsid.sceptre.impl.collections.MapIntInt;
import diarsid.sceptre.impl.collections.SetInt;
import diarsid.sceptre.impl.collections.impl.ArrayIntImpl;
import diarsid.sceptre.impl.collections.impl.ListCharImpl;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.sceptre.impl.collections.impl.MapIntImpl;
import diarsid.sceptre.impl.collections.impl.MapIntIntImpl;
import diarsid.sceptre.impl.collections.impl.SetIntImpl;
import diarsid.sceptre.impl.weight.Weight;
import diarsid.support.misc.MathFunctions;
import diarsid.support.objects.references.Possible;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

import static diarsid.sceptre.api.WeightEstimate.BAD;
import static diarsid.sceptre.api.WeightEstimate.of;
import static diarsid.sceptre.impl.AnalyzeUtil.clustersImportanceDependingOn;
import static diarsid.sceptre.impl.AnalyzeUtil.cubeUpTo5AddSquareIfOver;
import static diarsid.sceptre.impl.AnalyzeUtil.inconsistencyOf;
import static diarsid.sceptre.impl.AnalyzeUtil.nonClusteredImportanceDependingOn;
import static diarsid.sceptre.impl.ClusterPreference.PREFER_LEFT;
import static diarsid.sceptre.impl.ClusterStepOne.calculateSimilarity;
import static diarsid.sceptre.impl.ClusterStepOneDuplicateComparison.compare;
import static diarsid.sceptre.impl.MatchType.MATCH_DIRECTLY;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_LOOP;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXT_IN_PATTERN_NEXTx2_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXTx2_IN_PATTERN_NEXT_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXTx2_IN_PATTERN_NEXTx3_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_NEXTx3_IN_PATTERN_NEXT_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT;
import static diarsid.sceptre.impl.MatchType.MATCH_WORD_END;
import static diarsid.sceptre.impl.Step.STEP_1;
import static diarsid.sceptre.impl.Step.STEP_2;
import static diarsid.sceptre.impl.Step.STEP_3;
import static diarsid.sceptre.impl.Step.STEP_4;
import static diarsid.sceptre.impl.WordInVariant.Placing.DEPENDENT;
import static diarsid.sceptre.impl.WordInVariant.Placing.INDEPENDENT;
import static diarsid.sceptre.impl.collections.Ints.doesExist;
import static diarsid.sceptre.impl.collections.Ints.getNearestToValueFromSetExcluding;
import static diarsid.sceptre.impl.collections.Ints.meanSmartIgnoringZeros;
import static diarsid.sceptre.impl.collections.impl.Sort.REVERSE;
import static diarsid.sceptre.impl.collections.impl.Sort.STRAIGHT;
import static diarsid.sceptre.api.LogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.api.LogType.POSITIONS_SEARCH;
import static diarsid.sceptre.impl.weight.WeightElement.CHAR_AFTER_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.CHAR_IS_ONE_CHAR_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTERS_ARE_WEAK_2_LENGTH;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTERS_NEAR_ARE_IN_ONE_PART;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTERS_ORDER_INCOSISTENT;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_BEFORE_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_CANDIDATES_SIMILARITY;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_ENDS_CURRENT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_ENDS_NEXT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_ENDS_WITH_VARIANT;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_HAS_SHIFTS;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_IS_CONSISTENT;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_IS_NOT_CONSISTENT;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_IS_REJECTED_BY_ORDER_DIFFS;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_IS_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_STARTS_CURRENT_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_STARTS_PREVIOUS_CHAR_IS_WORD_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTER_STARTS_WITH_VARIANT;
import static diarsid.sceptre.impl.weight.WeightElement.FIRST_CLUSTER_HAS_MISSED_WORD_START;
import static diarsid.sceptre.impl.weight.WeightElement.FIRST_CLUSTER_IS_REJECTED;
import static diarsid.sceptre.impl.weight.WeightElement.FIRST_PATTERN_CHAR_IS_MISSED_IN_VARIANT_AT_ALL;
import static diarsid.sceptre.impl.weight.WeightElement.FOUND_POSITIONS_BELONG_TO_ONE_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.FOUND_POSITIONS_DENOTES_ALL_WORDS;
import static diarsid.sceptre.impl.weight.WeightElement.NEXT_CHAR_IS_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.PATTERN_CONTAINS_CLUSTER;
import static diarsid.sceptre.impl.weight.WeightElement.PATTERN_CONTAINS_CLUSTER_LONG_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.PATTERN_DOES_NOT_CONTAIN_CLUSTER;
import static diarsid.sceptre.impl.weight.WeightElement.PLACING_BONUS;
import static diarsid.sceptre.impl.weight.WeightElement.PLACING_PENALTY;
import static diarsid.sceptre.impl.weight.WeightElement.PREVIOUS_CHAR_IS_SEPARATOR;
import static diarsid.sceptre.impl.weight.WeightElement.PREVIOUS_CHAR_IS_SEPARATOR_CURRENT_CHAR_AT_PATTERN_START;
import static diarsid.sceptre.impl.weight.WeightElement.PREVIOUS_CHAR_IS_SEPARATOR_ONLY_SINGLE_CHAR_FOUND_IN_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.SINGLE_POSITIONS_DENOTE_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.SINGLE_POSITION_AND_FULL_CLUSTER_DENOTE_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.SINGLE_POSITION_AND_PART_OF_CLUSTER_DENOTE_WORD;
import static diarsid.sceptre.impl.weight.WeightElement.UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START;
import static diarsid.sceptre.impl.weight.WeightElement.UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END;
import static diarsid.sceptre.impl.weight.WeightElement.WORD_QUALITY;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.cube;
import static diarsid.support.misc.MathFunctions.onePointRatio;
import static diarsid.support.misc.MathFunctions.square;
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
    /* DEBUG UTIL */         Step step, char c, int position) {
    /* DEBUG UTIL */     return () -> {
    /* DEBUG UTIL */         return 
    /* DEBUG UTIL */                 this.findPositionsStep.equals(step) &&
    /* DEBUG UTIL */                 this.currentPatternCharPositionInVariant == position &&
    /* DEBUG UTIL */                 this.currentChar == c;
    /* DEBUG UTIL */     };
    /* DEBUG UTIL */ }
    
    final AnalyzeUnit data;
    
    final ArrayInt positions;
    
    int clustersQty;
    int clustered;
    int meaningful;
    int nonClustered;

    private ListInt positionsPossibleToAddToClustered = new ListIntImpl();
    
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
    boolean currentCharIsUniqueInPattern;
    char nextCharInPattern;
    char nextCharInVariant;
    char previousCharInPattern;
    char previousCharInVariant;
    int currentPatternCharPositionInVariant;
    
    // v.2
    final SetInt unclusteredPatternCharIndexes = new SetIntImpl();
    final SetInt localUnclusteredPatternCharIndexes = new SetIntImpl();
    Step findPositionsStep;
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
    int nextPatternCharsToSkip;
    final ListInt positionsInCluster = new ListIntImpl();
    int currentCharInVariantQty;
    int currentPatternCharPositionInVariantToSave;
    // --

    final MapInt<String> matchTypesByVariantPosition = new MapIntImpl<>();

    ClusterStepOne currStepOneCluster;
    ClusterStepOne prevStepOneCluster;
    ClusterStepOne lastSavedStepOneCluster;

    ClusterStepTwo currStepTwoCluster = new ClusterStepTwo(this);
    ClusterStepTwo prevStepTwoCluster = new ClusterStepTwo(this);
    ClusterStepTwo swapStepTwoCluster = new ClusterStepTwo(this);

    final ListInt garbagePatternPositions = new ListIntImpl();

    final Possible<String> missedRepeatingsLog = simplePossibleButEmpty();
    final ListInt extractedMissedRepeatedPositionsIndexes = new ListIntImpl();
    final ListChar missedRepeatedChars = new ListCharImpl();
    final ListInt missedRepeatedPositions = new ListIntImpl();
    
    // v.3
    final MapIntInt positionUnsortedOrders = new MapIntIntImpl();
    final MapIntInt patternIndexesByVariantPosition = new MapIntIntImpl();
    final MapInt<Step> positionFoundSteps = new MapIntImpl<>();
    final MapInt.Keys filledPositions = positionFoundSteps.keys();
    private final PositionCandidate positionCandidate;
    int nearestPositionInVariant;
    final ListInt currentClusterOrderDiffs = new ListIntImpl();
    final ListChar notFoundPatternChars = new ListCharImpl();
    final Clusters clusters;
    final ListInt keyChars;
    private final SinglePositions singlePositions;
    boolean currentClusterOrdersIsConsistent;
    boolean previousClusterOrdersIsConsistent;
    boolean currentClusterOrdersHaveDiffCompensations;
    int unsortedPositions;
    // --

    boolean currentClusterIsRejected;
    boolean currentClusterWordStartFound;
    
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
    
    final Weight weight;
    
    PositionsAnalyze(
            AnalyzeUnit data, 
            Clusters clusters, 
            PositionCandidate positionCandidate) {
        this.positions = new ArrayIntImpl();
        this.data = data;
        this.clusters = clusters;
        this.positionCandidate = positionCandidate;
        this.keyChars = new ListIntImpl();
        this.singlePositions = new SinglePositions();
        this.weight = new Weight(this.data.log);

        this.currStepOneCluster = new ClusterStepOne(this.data.log);
        this.prevStepOneCluster = new ClusterStepOne(this.data.log);
        this.lastSavedStepOneCluster = new ClusterStepOne(this.data.log);

        this.clearPositionsAnalyze();
    }
    
    static boolean arePositionsEquals(PositionsAnalyze dataOne, PositionsAnalyze dataTwo) {
        return dataOne.positions.equals(dataTwo.positions);
    }
    
    int findFirstPosition() {
        int first = this.positions.i(0);
        if ( first > -1 ) {
            return first;
        }
        
        for (int i = 1; i < this.positions.size(); i++) {
            first = this.positions.i(i);
            if ( first > -1 ) {
                return first;
            }
        }
        
        return POS_NOT_FOUND;
    }
    
    int findLastPosition() {
        int last = this.positions.last();
        if ( last > -1 ) {
            return last;
        }
        
        for (int i = this.positions.size() - 2; i > -1; i--) {
            last = this.positions.i(i);
            if ( last > -1 ) {
                return last;
            } 
        }
        
        return POS_NOT_FOUND;
    }

    int findFirstNotFilledPatternPositionBackwardFrom(int patternIndexExcl) {
        for ( int i = patternIndexExcl - 1; i > -1; i-- ) {
            if ( positions.i(i) != POS_UNINITIALIZED ) {
                return i + 1;
            }
        }
        return 0;
    }
    
    void fillPositionsFromIndex(int patternInVariantIndex) {
        int length = positions.size();
        int position = patternInVariantIndex;
        data.log.add(POSITIONS_SEARCH, "  pattern found directly");
        for (int i = 0; i < length; i++) {
            positions.i(i, position);
            matchTypesByVariantPosition.put(i, "STEP_1_PATTERN-IN-VARIANT");
            positionFoundSteps.put(position, STEP_1);
            patternIndexesByVariantPosition.put(i, position);
            data.log.add(POSITIONS_SEARCH, "    [SAVE] %s : %s", data.patternChars.i(i), position);
            position++;        
        }
        data.log.add(POSITIONS_SEARCH, "         %s", displayPositions());
        clearPositionsSearchingState();
    }
    
    private String displayPositions() {
        if ( data.log.isNotEnabled(POSITIONS_SEARCH) ) {
            return "";
        }
        
        String patternPositions = positions
                .stream()
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

    private String displayPositionsUpperCase(int iPattern, int iVariant) {
        if ( data.log.isNotEnabled(POSITIONS_SEARCH) ) {
            return "";
        }

        AtomicInteger i = new AtomicInteger(0);
        String patternPositions = positions
                .stream()
                .mapToObj(position -> {
                    if ( position == POS_UNINITIALIZED || position == POS_NOT_FOUND ) {
                        i.incrementAndGet();
                        return "_";
                    }
                    else {
                        var positionString = String.valueOf(data.variant.charAt(position));
                        if ( iPattern == i.get() ) {
                            positionString = positionString.toUpperCase();
                        }
                        i.incrementAndGet();
                        return positionString;
                    }
                })
                .collect(joining());

        String variantPositions = range(0, data.variant.length())
                .mapToObj(position -> {
                    if ( filledPositions.contains(position) ) {
                        var positionString = String.valueOf(data.variant.charAt(position));
                        if ( iVariant == position ) {
                            positionString = positionString.toUpperCase();
                        }
                        return positionString;
                    }
                    else {
                        return "_";
                    }
                })
                .collect(joining());

        return patternPositions + " : " + variantPositions;
    }
    
    private String displayStepOneClusterLastAddedPosition() {
        if ( data.log.isNotEnabled(POSITIONS_SEARCH) ) {
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
        clustersCounting: for (int i = 0; i < this.positions.size(); i++) {
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

                        } else if ( this.prevCharIsSeparator ) {
                            this.doWhenOnlyPreviousCharacterIsSeparator();
                        } else if ( this.nextCharIsSeparator ) {
                            this.doWhenOnlyNextCharacterIsSeparator();
                        }

                        if ( this.data.wordsInVariant.all.size() > 1 ) {
                            WordInVariant word = this.data.wordsInVariant.wordOf(this.currentPosition);

                            boolean isEnclosedByFoundInWord = word.isEnclosedByFound(this.filledPositions, this.currentPosition);
                            if ( isEnclosedByFoundInWord ) {
                                this.clustered++;
                            }
                            else {
                                boolean addToClustered = false;
                                if ( word.placing.is(DEPENDENT) ) {
                                    if ( this.currentPosition == word.startIndex || word.hasStartIn(this.filledPositions) ) {
                                        WordInVariant firstIndependentWordBefore = this.data.wordsInVariant.firstIndependentBefore(word);
                                        if ( firstIndependentWordBefore.hasStartIn(this.filledPositions) ) {
                                            addToClustered = true;
                                        }
                                    }
                                }
                                else if ( word.placing.is(INDEPENDENT) ) {
                                    if ( this.currentPosition == word.startIndex || word.hasStartIn(this.filledPositions) ) {
                                        if ( word.intersections(this.filledPositions) > 2 ) {
                                            addToClustered = true;
                                        }
                                        else {
                                            WordsInVariant.WordsInRange dependentWords = this.data.wordsInVariant.allDependentAfterOrNull(word);
                                            if ( nonNull(dependentWords) ) {
                                                if ( dependentWords.intersections(this.filledPositions) > 2 ) {
                                                    addToClustered = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                if ( addToClustered ) {
                                    this.positionsPossibleToAddToClustered.add(this.currentPosition);
                                }

                                this.nonClustered++;
                            }
                        }
                        else {
                            this.nonClustered++;
                        }
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

        if ( this.positionsPossibleToAddToClustered.isNotEmpty() ) {
            int position;
            WordInVariant word;
            Cluster cluster;
            Ints.Elements elements = this.positionsPossibleToAddToClustered.elements();
            boolean addToClustered;
            positionsLoop: while ( elements.hasNext() ) {
                addToClustered = false;
                elements.next();
                position = elements.current();
                word = this.data.wordsInVariant.wordOf(position);
                this.clusters.chooseAllOf(word);

                if ( this.clusters.chosenInWord().isEmpty() ) {
                    if ( position == word.startIndex && word.length < 5 ) {
                        if ( this.isUniqueInVariant(word.startIndex) || this.keyChars.contains(word.startIndex) ) {
                            addToClustered = true;
                        }
                    }

                    if ( ! addToClustered ) {
                        if ( word.placing.is(INDEPENDENT) ) {
                            if ( this.nextDependentWordsHasFoundPositions(word) ) {
                                addToClustered = true;
                            }
                        }
                    }
                }
                else {
                    clustersInWordLoop: for ( int i = 0; i < this.clusters.chosenInWord().size(); i++ ) {
                        cluster = this.clusters.chosenInWord().get(i);
                        if ( cluster.length() > 2 ) {
                            addToClustered = true;
                        }
                        if ( cluster.hasTeardown() ) {
                            addToClustered = false;
                            break clustersInWordLoop;
                        }
                    }
                }

                if ( addToClustered ) {
                    this.nonClustered--;
                    this.clustered++;
                }
            }
        }

        if ( this.nonClustered < 0 ) {
            this.nonClustered = 0;
        }
                
        this.clusters.arrange();
        
        if ( this.clusters.nonEmpty() ) {
            this.analyzeAllClustersOrderDiffs();
            int totalTearDown = this.clusters.lookupForTearDowns();
            if ( totalTearDown > 0 ) {
                this.clustered = this.clustered - totalTearDown;
                this.nonClustered = this.nonClustered + totalTearDown;
                data.log.add(POSITIONS_CLUSTERS, "               [TEARDOWN] total : %s", totalTearDown);
            }            
        }

        this.checkWordsCoverageByAllClusters();
        
        if ( this.clustersQty > 1 && this.allClustersInconsistency == 0 ) {
            if ( this.separatorsBetweenClusters > 0 ) {
                if ( this.clusters.distanceBetweenClusters() == this.separatorsBetweenClusters ) {
                    data.log.add(POSITIONS_CLUSTERS, "               [weight] all clusters are one pattern, can be regarded as one cluster!");
                    this.clustersQty = 1;
                }
            } else {
                
            }           
        }
        
        this.analyzeAllClustersPlacing();
        this.tryToRecoverMissedByDuplicatedCharInWords();
    }

    private void tryToProcessSinglePositionsUninterruptedRow() {
        if ( this.singlePositions.doHaveUninterruptedRow() ) {
            ListInt uninterruptedPositions = this.singlePositions.uninterruptedRow();
            int firstPosition = uninterruptedPositions.get(0);
            int lastPosition = uninterruptedPositions.last();
            int firstSeparatorAfterFirstPosition = this.data.variantSeparators.greaterThan(firstPosition);
            int lastSeparatorBeforeFirstPosition = this.data.variantSeparators.lesserThan(firstPosition);
            if ( Ints.doesExist(firstSeparatorAfterFirstPosition) ) {
                if ( firstSeparatorAfterFirstPosition == lastPosition + 1 ) {
                    if ( Ints.doesExist(lastSeparatorBeforeFirstPosition) ) {
                        if ( lastSeparatorBeforeFirstPosition == firstPosition - 1 ) {
                            data.log.add(
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
//                data.log.add(
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

            if ( data.wordsInVariant.all.size() > 1 ) {
                WordInVariant word = data.wordsInVariant.wordOf(this.currentPosition);
                int intersections = word.intersections(this.positions, this.currentPosition);
                if ( intersections > 0 ) {
                    this.clustered++;
                    this.nonClustered--;
                    data.log.add(POSITIONS_CLUSTERS, "    [single position] %s enrich next cluster", this.currentPosition);
                }

                if ( intersections == data.pattern.length() ) {

                }
            }
        } else if ( this.isClusterBeforeSeparator() ) {
            this.keyChars.add(this.currentPosition);
            this.weight.add(CLUSTER_BEFORE_SEPARATOR);
        } else {
            WordInVariant word = this.data.wordsInVariant.wordOf(this.currentPosition);
            int intersections = word.intersections(this.positions, this.currentPosition);
            if ( intersections > 0 ) {
                this.weight.add(PREVIOUS_CHAR_IS_SEPARATOR);
            }
            else {
                boolean penalty = true;
                if ( word.placing.is(INDEPENDENT) ) {
                    if ( this.nextDependentWordsHasFoundPositions(word) ) {
                        penalty = false;
                    }
                }

                if ( penalty ) {
                    this.weight.add(PREVIOUS_CHAR_IS_SEPARATOR_ONLY_SINGLE_CHAR_FOUND_IN_WORD);
                }
                else {
                    this.weight.add(PREVIOUS_CHAR_IS_SEPARATOR);
                }
            }
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

    private int notFoundPatternCharsCount() {
        return this.data.pattern.length() - this.filledPositions.size();
    }

    private void doWhenOnlyNextCharacterIsSeparator() {
        if ( this.data.wordsInVariant.all.size() == 1 ) {
            int notFoundCharsCount = this.notFoundPatternCharsCount();
            if ( notFoundCharsCount == 0 ) {
                this.weight.add(NEXT_CHAR_IS_SEPARATOR);
                this.weight.add(-cube(this.previousClusterLength), PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD);
                this.clustered++;
                this.nonClustered--;
                data.log.add(POSITIONS_CLUSTERS, "    [single position] %s enrich previous cluster", this.currentPosition);
            }
            else {
                int allowedNotFoundPatternChars;

                if ( data.pattern.length() <= 4 ) {
                    allowedNotFoundPatternChars = 0;
                }
                else if ( data.pattern.length() == 5 ) {
                    allowedNotFoundPatternChars = 1;
                }
                else if ( data.pattern.length() <= 8 ) {
                    allowedNotFoundPatternChars = 2;
                }
                else {
                    allowedNotFoundPatternChars = 3;
                }

                if ( notFoundCharsCount <= allowedNotFoundPatternChars ) {
                    this.weight.add(NEXT_CHAR_IS_SEPARATOR);
                    this.weight.add(-this.previousClusterLength*2, PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD);
                    this.clustered++;
                    this.nonClustered--;
                    data.log.add(POSITIONS_CLUSTERS, "    [single position] %s enrich previous cluster", this.currentPosition);
                }
                else {

                }
            }
        }
        else {
            this.weight.add(NEXT_CHAR_IS_SEPARATOR);

            if ( this.previousClusterLastPosition != POS_UNINITIALIZED && ! this.previousClusterEndsWithSeparator && this.previousClusterOrdersIsConsistent ) {
                if ( ! this.areSeparatorsPresentBetween(this.previousClusterLastPosition, this.currentPosition) ) {
                    int bonus = this.previousClusterLength > 2 ?
                            square(this.previousClusterLength) : this.previousClusterLength;
                    this.weight.add(-bonus, PREVIOUS_CLUSTER_AND_CURRENT_CHAR_BELONG_TO_ONE_WORD);
                    this.clustered++;
                    this.nonClustered--;
                    data.log.add(POSITIONS_CLUSTERS, "    [single position] %s enrich previous cluster", this.currentPosition);
                }
            }
        }
    }
    
    private boolean areSeparatorsPresentBetween(final int fromExcl, final int toExcl) {
        data.log.add(POSITIONS_CLUSTERS, "               [weight] ...searching for separators between %s and %s", fromExcl, toExcl);
        if ( absDiff(fromExcl, toExcl) < 2 ) {
            return false;
        }
        String variantText = this.data.variant;
        for (int pointer = fromExcl + 1; pointer < toExcl; pointer++) {
            if ( isWordsSeparator(variantText.charAt(pointer)) ) {
                data.log.add(POSITIONS_CLUSTERS, "               [weight] separator found - %s", pointer);
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
            if ( this.data.pattern.length() > 2  ) {
                int penalty = square(this.clusters.quantity());
                this.weight.add(penalty, CLUSTERS_ARE_WEAK_2_LENGTH);
            }
        }
    }

    private void applyPenaltyIfPatternFirstCharIsNotFound() {
        if ( this.positions.i(0) == POS_NOT_FOUND ) {
            this.weight.add(FIRST_PATTERN_CHAR_IS_MISSED_IN_VARIANT_AT_ALL);
        }
    }
    
    private void analyzeAllClustersPlacing() {
        this.weight.excludeIfAllPresent(
                UNNATURAL_POSITIONING_CLUSTER_AT_END_PATTERN_AT_START,
                UNNATURAL_POSITIONING_CLUSTER_AT_START_PATTERN_AT_END);
        
        if ( this.clusters.isEmpty() ) {
            return;
        }
        
        if ( of(this.weight.sum() + this.data.weight.sum()).equals(BAD) ) {
            float placingPenalty = (float) square( ( 10.0 - this.clustersQty ) / this.clustered );
            data.log.add(POSITIONS_CLUSTERS, "    [cluster placing] positions weight is too bad for placing assessment");
            this.weight.add(placingPenalty, PLACING_PENALTY);
        } else {
            float placingBonus = this.clusters.calculatePlacingBonus();
            this.weight.add(-placingBonus, PLACING_BONUS);
        }
    }

    private void tryToRecoverMissedByDuplicatedCharInWords() {

    }
        
    void findPatternCharsPositions() {
        traverseThroughPatternAndFillNotFoundPositions();
        
        proceedWith(STEP_1);
        data.log.add(POSITIONS_SEARCH, "    %s", findPositionsStep);
        
        for (int currentPatternCharIndex = 0, charsRemained = data.patternChars.size() - 1; currentPatternCharIndex < data.patternChars.size(); currentPatternCharIndex++, charsRemained--) {
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
        checkWordsInRangeOfFoundPositions();
        applyPenaltyIfPatternFirstCharIsNotFound();;
    }
    
    private void traverseThroughPatternAndFillNotFoundPositions() {
        ArrayChar.Elements elements = data.patternChars.elements();
        char character;
        while ( elements.hasNext() ) {
            elements.next();
            character = elements.current();
            this.data.patternCharsCount.increment(character);
            this.notFoundPatternChars.add(character);
        }
    }
    
    private void proceedWith(Step step) {
        this.findPositionsStep = step;
    }

    private boolean isAllowedToProceedOnCurrentStep() {
        boolean allowed = findPositionsStep.canProceedWith(data.pattern.length());
        if ( ! allowed ) {
            data.log.add(POSITIONS_SEARCH, "    %s is not allowed for pattern with length %s", findPositionsStep, data.pattern.length());
        }
        return allowed;
    }
    
    private void processAccumulatedUnclusteredPatternCharIndexes() {
        if ( unclusteredPatternCharIndexes.isNotEmpty() ) {
            data.log.add(POSITIONS_SEARCH, "    %s", findPositionsStep);
            int charsRemained = unclusteredPatternCharIndexes.size();

            Ints.Elements elements = unclusteredPatternCharIndexes.elements();
            int currentPatternCharIndex;
            while ( elements.hasNext() ) {
                elements.next();
                currentPatternCharIndex = elements.current();
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
        this.positionsInCluster.clear();
        this.currentChar = ' ';
        this.findPositionsStep = STEP_1;
    }
    
    private boolean isPositionSetAt(int patternIndex) {
        return this.positions.i(patternIndex) > -1;
    }

    private boolean isPositionNotSetAt(int patternIndex) {
        return this.positions.i(patternIndex) < 0;
    }
    
    private void processCurrentPatternCharOf(int currentPatternCharIndex, int charsRemained) {
        currentChar = data.patternChars.i(currentPatternCharIndex);
        currentCharIsUniqueInPattern = data.patternCharsCount.countOf(currentChar) == 1;
        data.log.add(POSITIONS_SEARCH, "      [explore] '%s'(%s in pattern)", this.currentChar, currentPatternCharIndex);

        if ( isWordsSeparator(currentChar) ) {
            data.log.add(POSITIONS_SEARCH, "          [WARN] '%s'(%s in pattern) is separator, skip!", this.currentChar, currentPatternCharIndex);
            return;
        }

        if ( nextPatternCharsToSkip > 0 ) {
            data.log.add(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is skipped!", this.currentChar, currentPatternCharIndex);
            nextPatternCharsToSkip--;
            return;
        }
        else if ( nextPatternCharsToSkip == 0 ) {
            if ( findPositionsStep.equalTo(STEP_1) && stepOneClusterSavedRightNow ) {
                stepOneClusterSavedRightNow = false;
            }
        }

        if ( garbagePatternPositions.contains(currentPatternCharIndex) ) {
            data.log.add(POSITIONS_SEARCH, "          [info] '%s'(%s in pattern) is marked as garbage!", this.currentChar, currentPatternCharIndex);
            return;
        }

        int savedPosition = positions.i(currentPatternCharIndex);
        if ( savedPosition != POS_UNINITIALIZED && savedPosition != POS_NOT_FOUND ) {
            data.log.add(POSITIONS_SEARCH, "          [info] '%s' in pattern is already found - %s", this.currentChar, savedPosition);
            if ( false ) {
                // Experimental thing, don't know if it worth further development, gives too much false positives
                if ( findPositionsStep.isAfter(STEP_1) ) {
                    Step foundStep = positionFoundSteps.get(savedPosition);
                    if ( foundStep.isAfter(STEP_1) ) {
                        WordInVariant savedPositionWord = data.wordsInVariant.wordOf(savedPosition);
                        WordInVariant word;
                        WordsInVariant.WordsInRange foundWords = data.wordsInVariant.wordsOfRange(filledPositions);
                        int charInWordPosition;
                        for ( int i = 0 ; i < foundWords.all().size(); i++ ) {
                            word = foundWords.all().get(i);
                            if ( word.index == savedPositionWord.index ) {
                                continue;
                            }
                            charInWordPosition = word.charPositionOf(currentChar);
                            if ( doesExist(charInWordPosition) ) {
                                if ( ! filledPositions.contains(charInWordPosition) ) {
                                    data.log.add(POSITIONS_SEARCH, "             [word possible change] %s -> %s", savedPositionWord.charsString(), word.charsString());
                                }
                            }
                        }
                    }
                }
            }

            return;
        }

        hasPreviousInPattern = currentPatternCharIndex > 0;
        hasNextInPattern = currentPatternCharIndex < data.patternChars.size() - 1;

        currentPatternCharPositionInVariant = data.variant.indexOf(currentChar);
        
        currentCharInVariantQty = 0;
        if ( currentPatternCharPositionInVariant < 0 ) {
            fillPosition(
                    currentPatternCharIndex,
                    POS_NOT_FOUND,
                    null);
            data.log.add(POSITIONS_SEARCH, "          [info] '%s' not found in variant", this.currentChar);
            return;
        }        
        
        positionsInCluster.clear();
        continueSearching = true;

        boolean duplicateChar = false;

        characterSearching : while ( currentPatternCharPositionInVariant >= 0 && continueSearching ) {
            data.log.add(POSITIONS_SEARCH, "        [assess] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);

            if ( findPositionsStep.is(STEP_2) ) {
                currStepTwoCluster.setAssessed(currentChar, currentPatternCharIndex, currentPatternCharPositionInVariant);
            }
        
            nearestPositionInVariant = POS_UNINITIALIZED;

            hasPreviousInVariant = currentPatternCharPositionInVariant > 0;
            hasNextInVariant = currentPatternCharPositionInVariant < data.variant.length() - 1;

            currentCharInVariantQty++;
            positionAlreadyFilled = filledPositions.contains(currentPatternCharPositionInVariant);
            positionsInCluster.clear();

            if ( hasPreviousInPattern ) {
                previousCharInPattern = data.patternChars.i(currentPatternCharIndex - 1);
                duplicateChar = currentChar == previousCharInPattern;

                if ( duplicateChar ) {
                    data.log.add(POSITIONS_SEARCH, "          [info] '%s' is duplicated!", this.currentChar);
                }
            }

            if ( ! positionAlreadyFilled || ( duplicateChar && findPositionsStep.isBefore(STEP_3) ) ) {
                
                if ( gotoBreakpointWhen(stepCharAndPositionAre(STEP_2, 's', 38)) ) {
                    breakpoint();
                }
                
                if ( hasPreviousInPattern && hasPreviousInVariant ) {
                    previousPositionInVariantFound = filledPositions.contains(currentPatternCharPositionInVariant - 1);
                    if ( previousCharInVariantInClusterWithCurrentChar(currentPatternCharIndex) ) {
                        if ( ! previousPositionInVariantFound && ! notFoundPatternChars.contains(this.previousCharInVariant) ) {
                            // omit this match
                        } else {
                            data.log.add(POSITIONS_SEARCH, "          [info] previous '%s'(%s in variant) is in cluster with current '%s'",
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
                        data.log.add(POSITIONS_SEARCH, "          [info] next '%s'(%s in variant) is in cluster with current '%s'",
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
                        
                        previousCharInPattern = data.patternChars.i(currentPatternCharIndex - 1);
                        nextCharInVariant = data.variant.charAt(currentPatternCharPositionInVariant + 1);
                        
                        if ( previousCharInPattern == nextCharInVariant ) {
                            boolean patternOfTypoFilled = isPositionSetAt(currentPatternCharIndex - 1);
                            boolean variantOfTypoFilled = filledPositions.contains(currentPatternCharPositionInVariant + 1);
                            boolean respectMatch = true;
                            if ( patternOfTypoFilled && (! variantOfTypoFilled) ) {
                                respectMatch = notFoundPatternChars.contains(previousCharInPattern);
                            } 
                            if ( respectMatch ) {
                                data.log.add(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and next in variant",
                                        currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
                                positionsInCluster.add(currentPatternCharPositionInVariant + 1);
                                currStepTwoCluster.add(
                                        nextCharInVariant,
                                        currentPatternCharIndex - 1,
                                        currentPatternCharPositionInVariant + 1, 
                                        variantOfTypoFilled,
                                        patternOfTypoFilled,
                                        MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant + 1;
                            }                            
                        }
                    }

                    if ( hasPreviousInVariant && hasNextInPattern ) {

                        previousCharInVariant = data.variant.charAt(currentPatternCharPositionInVariant - 1);
                        nextCharInPattern = data.patternChars.i(currentPatternCharIndex + 1);

                        if ( previousCharInVariant == nextCharInPattern ) {
                            if ( filledPositions.contains(currentPatternCharPositionInVariant - 1) || notFoundPatternChars.contains(nextCharInPattern) ) {
                                data.log.add(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and previous in variant",
                                        currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                positionsInCluster.add(currentPatternCharPositionInVariant - 1);
                                currStepTwoCluster.add(
                                        previousCharInVariant,
                                        currentPatternCharIndex + 1,
                                        currentPatternCharPositionInVariant - 1, 
                                        filledPositions.contains(currentPatternCharPositionInVariant - 1),
                                        isPositionSetAt(currentPatternCharIndex + 1),
                                        MATCH_TYPO_NEXT_IN_PATTERN_PREVIOUS_IN_VARIANT);
                                distanceOneTypoFound = true;
                                nearestPositionInVariant = currentPatternCharPositionInVariant - 1;
                            }                            
                        }
                    }
                        
                    boolean nextNextFoundAsTypo = false;
                    if ( hasNextInPattern && hasNextInVariant ) {
                        nextCharInPattern = data.patternChars.i(currentPatternCharIndex + 1);
                        // if there are at least two characters ahead in variant...
                        if ( data.variant.length() - currentPatternCharPositionInVariant > 2 ) {
                            int nextNextPosition = currentPatternCharPositionInVariant + 2;
                            if ( ! data.variantSeparators.contains(nextNextPosition - 1) ) {
                                char nextNextCharInVariant = data.variant.charAt(nextNextPosition);
                                if ( nextCharInPattern == nextNextCharInVariant ) { // TODO here something about _x..ab.. word-cluster logic
                                    boolean nextNextPositionIncluded = filledPositions.contains(nextNextPosition);
                                    if ( nextNextPositionIncluded || notFoundPatternChars.contains(nextCharInPattern) ) {
                                        data.log.add(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next in pattern and next*2 in variant",
                                                currentChar, currentPatternCharPositionInVariant, nextCharInPattern);
                                        positionsInCluster.add(nextNextPosition);
                                        currStepTwoCluster.add(
                                                nextNextCharInVariant,
                                                currentPatternCharIndex + 1,
                                                nextNextPosition, 
                                                nextNextPositionIncluded,
                                                this.isPositionSetAt(currentPatternCharIndex + 1),
                                                MATCH_TYPO_NEXT_IN_PATTERN_NEXTx2_IN_VARIANT);
                                        nearestPositionInVariant = nextNextPosition;
                                        nextNextFoundAsTypo = true;

                                        if ( data.pattern.length() - currentPatternCharIndex > 2 && 
                                             data.variant.length() - currentPatternCharPositionInVariant > 3) {
                                            char next2CharInPattern = data.patternChars.i(currentPatternCharIndex + 2);
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
                                                        MATCH_TYPO_NEXTx2_IN_PATTERN_NEXTx3_IN_VARIANT);

                                                if ( data.pattern.length() - currentPatternCharIndex > 3 ) {
                                                    char next3CharInPattern = data.patternChars.i(currentPatternCharIndex + 3);
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
                                                                    MATCH_TYPO_NEXTx3_IN_PATTERN_NEXT_IN_VARIANT);
                                                        }                                                    
                                                    }
                                                }
                                            }
                                        }
                                    }                                         
                                }
                                else {
                                    int nextNextPatternCharIndex = currentPatternCharIndex + 2;
                                    if ( nextNextPatternCharIndex < data.pattern.length() ) {                                        
                                        char nextNextCharInPattern = data.patternChars.i(nextNextPatternCharIndex);

                                        if ( nextNextCharInPattern == nextCharInVariant ) {
                                            if ( patternIndexesByVariantPosition.containsKey(currentPatternCharPositionInVariant + 1) &&
                                                 patternIndexesByVariantPosition.containsValue(nextNextPatternCharIndex) ) {
                                                breakpoint();
                                                
                                                positionsInCluster.add(currentPatternCharPositionInVariant);
                                                currStepTwoCluster.add(
                                                        nextNextCharInPattern, 
                                                        nextNextPatternCharIndex, 
                                                        currentPatternCharPositionInVariant + 1, 
                                                        true,
                                                        true,
                                                        MATCH_TYPO_NEXTx2_IN_PATTERN_NEXT_IN_VARIANT);
                                            }
                                        }
                                    }
                                }
                            }

                            WordInVariant wordOfCurrentChar = this.data.wordsInVariant.wordOf(currentPatternCharPositionInVariant);

                            boolean canDoLoopSearch = wordOfCurrentChar.startIndex == currentPatternCharPositionInVariant ||
                                    currStepTwoCluster.containsOnlyInClustered(wordOfCurrentChar.startIndex) ||
                                    currentPatternCharPositionInVariant == 0;

                            if ( wordOfCurrentChar.placing.is(DEPENDENT) ) {
                                if ( canDoLoopSearch ) {
                                    WordInVariant firstIndependentWordBefore = this.data.wordsInVariant.firstIndependentBefore(wordOfCurrentChar);
                                    canDoLoopSearch =
                                            firstIndependentWordBefore.intersections(this.filledPositions) > 0;
                                }
                            }

                            if ( canDoLoopSearch ) {
                                int iPattern = currentPatternCharIndex + 1;
                                int iVariant = currentPatternCharPositionInVariant + 1;
                                int limitPattern = data.pattern.length() -1 -iPattern > 4 ? iPattern + 3 : data.pattern.length()-1;
                                int limitVariant = data.variant.length() -1 -iVariant > 4 ? iVariant + 4 : data.variant.length()-1;

                                WordInVariant currentWord = this.data.wordsInVariant.wordOf(currStepTwoCluster.assessedCharVariantPosition());
                                int endOfAssessedWord = currentWord.endIndex;
                                if ( limitVariant > endOfAssessedWord ) {
                                    limitVariant = endOfAssessedWord;
                                    data.log.add(POSITIONS_SEARCH, "          [info] loop limited by end of current word '%s' - %s",
                                            currentWord.charsString(), limitVariant);
                                }

                                int matches = 0;
                                char variantCh;
                                char patternCh;
                                patternLookup: for (; iPattern <= limitPattern; iPattern++) {
                                    patternCh = data.pattern.charAt(iPattern);
                                    variantLookup: for (int jVariant = iVariant; jVariant <= limitVariant; jVariant++) {
                                        variantCh = data.variant.charAt(jVariant);
                                        if ( patternCh == variantCh ) {
                                            data.log.add(POSITIONS_SEARCH, "          [info] loop-typo found '%s' (variant:%s pattern:%s)",
                                                    patternCh, jVariant, iPattern);
                                            matches++;
                                            currStepTwoCluster.addAsCandidate(
                                                    patternCh,
                                                    iPattern,
                                                    jVariant,
                                                    filledPositions.contains(jVariant),
                                                    isPositionSetAt(iPattern),
                                                    MATCH_TYPO_LOOP);
                                        }
                                    }
                                }

                                if ( matches > 1 ) {
                                    currStepTwoCluster.approveCandidates();
                                }
                                else {
                                    currStepTwoCluster.rejectCandidates();
                                }
                            }
                            else {
                                boolean currentCharIsVariantWordEnd =
                                        this.data.variantSeparators.contains(currentPatternCharPositionInVariant + 1) ||
                                        currentPatternCharPositionInVariant == this.data.variant.length() - 1;
                                if ( currentCharIsVariantWordEnd ) {
                                    data.log.add(POSITIONS_SEARCH, "          [info] '%s' is word end (variant:%s pattern:%s)",
                                            currentChar, currentPatternCharPositionInVariant, currentPatternCharIndex);
                                    WordInVariant word = data.wordsInVariant.wordOf(currentPatternCharPositionInVariant);
                                    if ( word.hasStartIn(filledPositions) ) {
                                        currStepTwoCluster.add(
                                                currentChar,
                                                currentPatternCharIndex,
                                                currentPatternCharPositionInVariant,
                                                filledPositions.contains(currentPatternCharPositionInVariant),
                                                isPositionSetAt(currentPatternCharIndex),
                                                MATCH_WORD_END);
                                    }
                                }
                            }

//                            if ( data.pattern.length() - currentPatternCharIndex > 2 ) {
//                                char next2CharInPattern = data.patternChars[currentPatternCharIndex + 2];
//                                if ( nextCharInVariant == next2CharInPattern ) {
//                                    if ( notFoundPatternChars.contains(next2CharInPattern) ) {
//                                        data.log.add(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is next*2 in pattern and next in variant",
//                                                currentChar, currentPatternCharPositionInVariant, nextCharInVariant);
//                                        if ( nonEmpty(positionsInCluster) ) {
//                                            positionsInCluster.add(currentPatternCharPositionInVariant + 1);
//                                            currStepTwoCluster.add(
//                                                    next2CharInPattern,
//                                                    currentPatternCharIndex + 2,
//                                                    currentPatternCharPositionInVariant + 1,
//                                                    filledPositions.contains(currentPatternCharPositionInVariant + 1),
//                                                    this.isPositionSetAt(currentPatternCharIndex + 2),
//                                                    MATCH_TYPO_2);
//
//                                            if ( data.pattern.length() - currentPatternCharIndex > 3 &&
//                                                 data.variant.length() - currentPatternCharPositionInVariant > 3 ) {
//                                                int next3Position = currentPatternCharPositionInVariant + 3;
//                                                char next3CharInPattern = data.patternChars[currentPatternCharIndex + 3];
//                                                char next3CharInVariant = data.variant.charAt(next3Position);
//                                                if ( next3CharInPattern == next3CharInVariant ) {
//                                                    boolean next3PositionIncluded = filledPositions.contains(next3Position);
//                                                    if ( next3PositionIncluded || notFoundPatternChars.contains(next3CharInVariant) ) {
//                                                        data.log.add(POSITIONS_SEARCH, "          [info] cluster continuation '%s'(%s in variant) found",
//                                                                next3CharInVariant, currentPatternCharPositionInVariant + 3);
//                                                        positionsInCluster.add(currentPatternCharPositionInVariant + 3);
//                                                        currStepTwoCluster.add(
//                                                                next3CharInVariant,
//                                                                currentPatternCharIndex + 3,
//                                                                next3Position,
//                                                                next3PositionIncluded,
//                                                                this.isPositionSetAt(currentPatternCharIndex + 3),
//                                                                MATCH_TYPO_3_1);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }
                    }
                    else if ( ! hasNextInVariant ) {
                        boolean currentCharIsVariantWordEnd = currentPatternCharPositionInVariant == this.data.variant.length() - 1;
                        if ( currentCharIsVariantWordEnd ) {
                            data.log.add(POSITIONS_SEARCH, "          [info] '%s' is word end (variant:%s pattern:%s)",
                                    currentChar, currentPatternCharPositionInVariant, currentPatternCharIndex);
                            WordInVariant word = data.wordsInVariant.wordOf(currentPatternCharPositionInVariant);
                            if ( word.hasStartIn(filledPositions) ) {
                                currStepTwoCluster.add(
                                        currentChar,
                                        currentPatternCharIndex,
                                        currentPatternCharPositionInVariant,
                                        filledPositions.contains(currentPatternCharPositionInVariant),
                                        isPositionSetAt(currentPatternCharIndex),
                                        MATCH_WORD_END);
                            }
                        }
                    }

                    if ( ! nextNextFoundAsTypo && hasPreviousInPattern && hasPreviousInVariant ) {
                        int patternIndex = currentPatternCharIndex - 1;
                        previousCharInPattern = data.patternChars.i(patternIndex);
                        // if there are at least two characters behind in variant...
                        if ( currentPatternCharPositionInVariant > 1 ) {
                            int prevPrevPosition = currentPatternCharPositionInVariant - 2;
                            if ( ! data.variantSeparators.contains(prevPrevPosition + 1) ) {
                                if ( previousCharInPattern == data.variant.charAt(prevPrevPosition) ) {
                                    boolean prevPrevPositionIncluded = filledPositions.contains(prevPrevPosition);
                                    if ( prevPrevPositionIncluded || notFoundPatternChars.contains(previousCharInPattern) ) {
                                        data.log.add(POSITIONS_SEARCH, "          [info] typo found '%s'(%s in variant) - '%s' is previous in pattern and previous*2 in variant",
                                                currentChar, currentPatternCharPositionInVariant, previousCharInPattern);
                                        positionsInCluster.add(prevPrevPosition);
                                        currStepTwoCluster.add(
                                                previousCharInPattern,
                                                patternIndex,
                                                prevPrevPosition,
                                                prevPrevPositionIncluded,
                                                this.isPositionSetAt(patternIndex),
                                                MATCH_TYPO_PREVIOUS_IN_PATTERN_PREVIOUSx2_IN_VARIANT);
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
                        
                        data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariant);
                        data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                        data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                        
                        if ( canFillPosition(currentPatternCharIndex - 1, currentPatternCharPositionInVariant - 1) ) {     
                            currStepOneCluster.setPrev(currentPatternCharPositionInVariant - 1);
                            data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) is previous both in pattern and variant", previousCharInVariant, currentPatternCharPositionInVariant - 1);
                            data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                            data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                            
                            int i = 2;
                            step1BackwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex - i, currentPatternCharPositionInVariant - i) ) {                                
                                char patChar = data.pattern.charAt(currentPatternCharIndex - i);
                                char varChar = data.variant.charAt(currentPatternCharPositionInVariant - i);
                                if ( patChar == varChar ) {
                                    currStepOneCluster.addPrev(currentPatternCharPositionInVariant - i);    
                                    data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) preciding <<<", varChar, currentPatternCharPositionInVariant - i);
                                    data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                                    data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                                } else {
                                    break step1BackwardLoop;    
                                }
                                i++;
                            }
                        }
                        
                        if ( canFillPosition(currentPatternCharIndex + 1, currentPatternCharPositionInVariant + 1) ) {       
                            currStepOneCluster.setNext(currentPatternCharPositionInVariant + 1);
                            data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) is next both in pattern and variant", nextCharInVariant, currentPatternCharPositionInVariant + 1);
                            data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                            data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                            currStepOneCluster.incrementSkip();
                            
                            int i = 2;
                            int iPattern;
                            int iVariant;
                            char patternChar;
                            char variantChar;
                            boolean hasAdded = false;

                            char lastAddedChar = '_';
                            int lastAddedCharVariantPosition = -1;
                            int lastAddedCharPatternPosition = -1;

                            step1ForwardLoop: while ( positionsExistAndCanFillPosition(currentPatternCharIndex + i, currentPatternCharPositionInVariant + i) ) {  
                                iPattern = currentPatternCharIndex + i;
                                iVariant = currentPatternCharPositionInVariant + i;
                                patternChar = data.pattern.charAt(iPattern);
                                variantChar = data.variant.charAt(iVariant);
                                if ( patternChar == variantChar ) {
                                    hasAdded = true;
                                    lastAddedChar = variantChar;
                                    lastAddedCharVariantPosition = iVariant;
                                    lastAddedCharPatternPosition = iPattern;
                                    currStepOneCluster.incrementSkip();
                                    currStepOneCluster.addNext(currentPatternCharPositionInVariant + i);
                                    data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) following >>>", variantChar, currentPatternCharPositionInVariant + i);
                                    data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                                    data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                                }
                                else {
                                    break step1ForwardLoop;
                                }
                                i++;
                            }

                            if ( hasAdded ) {
                                WordInVariant word;
                                char wordFirstChar;
                                wordsConflictsLoop: for ( int iWord = 0; iWord < data.wordsInVariant.all.size(); iWord++ ) {
                                    word = data.wordsInVariant.all.get(iWord);
                                    if ( word.length > 2 ) {
                                        wordFirstChar = data.variant.charAt(word.startIndex);
                                        if ( wordFirstChar == lastAddedChar ) {
                                            if ( lastAddedCharPatternPosition < data.pattern.length() - 1 ) {
                                                char nextWordChar = data.variant.charAt(word.startIndex + 1);
                                                char nextPatternChar = data.pattern.charAt(lastAddedCharPatternPosition + 1);
                                                if ( nextWordChar == nextPatternChar ) {
                                                    currStepOneCluster.removeLastNext();
                                                    data.log.add(POSITIONS_SEARCH, "          [candidate] '%s'(%s in variant) reject - conflicts with word %s", lastAddedChar, lastAddedCharVariantPosition, word.charsString());
                                                    data.log.add(POSITIONS_SEARCH, "               %s", displayStepOneClusterLastAddedPosition());
                                                    data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                                                    break wordsConflictsLoop;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if ( findPositionsStep.typoSearchingAllowed()) {
                    // do nothing                   
                } else {    
                    // on steps, other than STEP_1, do not save positions directly, just record them as appropriate for saving (excluding STEP_4).
                    if ( findPositionsStep.canAddToPositions(positionsInCluster.size()) ) {
                        
                        int orderDiffInPattern;
                        if ( nearestPositionInVariant == POS_UNINITIALIZED && patternIndexesByVariantPosition.isNotEmpty() ) {
                            nearestPositionInVariant = getNearestToValueFromSetExcluding(currentPatternCharPositionInVariant, patternIndexesByVariantPosition.keys());
                        }
                        if ( nearestPositionInVariant > POS_NOT_FOUND ) {
                            int nearestPatternCharIndex = patternIndexesByVariantPosition.get(nearestPositionInVariant);
                            if ( nearestPatternCharIndex == MIN_VALUE ) {
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
                            previousCharInVariantByPattern = positions.i(currentPatternCharIndex - 1);
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
                        if ( orderDiffInVariant == POS_UNINITIALIZED && filledPositions.isNotEmpty() ) {
                            int nearestFilledPosition = findNearestFilledPositionTo(currentPatternCharIndex);
                            orderDiffInVariant = absDiff(currentPatternCharPositionInVariant, nearestFilledPosition);
                        }     
                        
                        boolean isNearSeparator = 
                                currentPatternCharPositionInVariant == 0 ||
                                currentPatternCharPositionInVariant == data.variant.length() - 1 ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant + 1) ||
                                data.variantSeparators.contains(currentPatternCharPositionInVariant - 1);                        
                        int distanceToNearestFilledPosition = MIN_VALUE;
                        if ( filledPositions.isNotEmpty() ) {
                            int nearestFilledPosition = getNearestToValueFromSetExcluding(currentPatternCharPositionInVariant, filledPositions);
                            if ( doesExist(nearestFilledPosition) ) {
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
                data.log.add(POSITIONS_SEARCH, "          [info] already filled, skip");
            } 
            
            if ( findPositionsStep.equals(STEP_1) ) {
                if ( currStepOneCluster.isSet() ) {

                    boolean ignoreStepOneCluster = false;
                    WordInVariant word;
                    WordInVariant clusterWord = data.wordsInVariant.wordOf(currentPatternCharPositionInVariant);
                    if ( clusterWord.startIndex == currStepOneCluster.firstOfVariant() ) {

                    }
                    else {
                        conflictSearch : for ( int i = 0; i < data.wordsInVariant.all.size(); i++ ) {
                            if ( i == clusterWord.index ) {
                                continue conflictSearch;
                            }
                            word = data.wordsInVariant.all.get(i);
                            if ( word.length > 1 && hasPossibleConflict(word, currStepOneCluster) ) {
                                ignoreStepOneCluster = true;
                                data.log.add(POSITIONS_SEARCH, "          [info] reject cluster - conflicts with word %s", word.charsString());
                                break conflictSearch;
                            }
                        }
                    }

                    if ( ignoreStepOneCluster ) {
                        currStepOneCluster.clear();
                    }
                    else {
                        currStepOneCluster.finish(data.variant, data.pattern);
                        if ( prevStepOneCluster.isSet() ) {
                            if ( currStepOneCluster.isBetterThan(prevStepOneCluster, data.wordsInVariant) ) {
                                acceptCurrentCluster();
                            }
                            else if ( prevStepOneCluster.isBetterThan(currStepOneCluster, data.wordsInVariant) ) {
                                acceptPreviousCluster();
                            }
                            else {
                                ClusterPreference comparison = compare(prevStepOneCluster, currStepOneCluster);
                                if ( comparison.equals(PREFER_LEFT) ) {
                                    acceptPreviousCluster();
                                }
                                else {
                                    acceptCurrentCluster();
                                }
                            }

                            improveWeightOnFoundDuplicateClusters(prevStepOneCluster, currStepOneCluster);
                        } else {
                            acceptCurrentAndSwapStepOneClusters();
                        }
                    }
                }                
            }
            
            if ( findPositionsStep.typoSearchingAllowed() ) {
                if ( currStepTwoCluster.hasChars() ) {
                    if ( prevStepTwoCluster.hasChars() ) {
                        boolean currHasVariantNotFilledChars = currStepTwoCluster.hasCharsNotFoundInVariant();
                        boolean prevHasVariantNorFilledChars = prevStepTwoCluster.hasCharsNotFoundInVariant();

                        boolean bothHas = currHasVariantNotFilledChars && prevHasVariantNorFilledChars;
                        boolean onlyOneHas = currHasVariantNotFilledChars ^ prevHasVariantNorFilledChars;

                        if ( bothHas || onlyOneHas ) {
                            boolean compareClusters = true;
                            if ( currStepTwoCluster.word().length > 1 ) {
                                if ( currentCharInVariantQty > 1 ) {
                                    int firstPatternPosition = currStepTwoCluster.firstClusteredPatternPostion();
                                    if ( this.currentCharIsPresentInPatternBefore(currentPatternCharIndex, firstPatternPosition) ) {
                                        WordInVariant word = data.wordsInVariant.wordOf(currStepTwoCluster.assessedCharVariantPosition());
                                        if ( word.length > 1 ) { // exclude single-char-words
                                            data.log.add(POSITIONS_SEARCH, "          [info] '%s' has closer position to new cluster, ignore new cluster", currentChar);
                                            compareClusters = false;
                                        }
                                    }
                                }
                            }


                            if ( compareClusters ) {
                                data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITION]");
                                if ( currStepTwoCluster.isBetterThan(prevStepTwoCluster) ) {
                                    log(prevStepTwoCluster, "old");
                                    log(currStepTwoCluster, "new");
                                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  new");
                                    clearPreviousAndSwapStepTwoSubclusters();
                                } else {
                                    log(prevStepTwoCluster, "old");
                                    log(currStepTwoCluster, "new");
                                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  old");
                                    currStepTwoCluster.clear();
                                }
                            }
                            else {
                                data.log.add(POSITIONS_SEARCH, "        [POSITIONS - NEW IS IGNORED]");
                                log(prevStepTwoCluster, "old");
                                log(currStepTwoCluster, "new");
                                data.log.add(POSITIONS_SEARCH, "          [choice]  ->  old");
                                currStepTwoCluster.clear();
                            }
                        }
                        else {
                            data.log.add(POSITIONS_SEARCH, "        [POSITIONS - BOT CHLUSTERS HAS NO UNFILLED POSITIONS, IGNORE NEW]");
                            log(prevStepTwoCluster, "old");
                            log(currStepTwoCluster, "new");
                            data.log.add(POSITIONS_SEARCH, "          [choice]  ->  old");
                            currStepTwoCluster.clear();
                        }
                    }
                    else {
                        data.log.add(POSITIONS_SEARCH, "        [POSITIONS - NOTHING TO COMPARE]");
                        log(currStepTwoCluster, "new");
                        data.log.add(POSITIONS_SEARCH, "          [choice]  ->  new");
                        clearPreviousAndSwapStepTwoSubclusters();
                        boolean attemptHasResults = tryToSearchBetterPositionsInWords();
                        if ( attemptHasResults ) {
                            if ( currStepTwoCluster.hasChars() ) {
                                if ( currStepTwoCluster.isBetterThan(prevStepTwoCluster) ) {
                                    data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITION IN WORDS]");
                                    log(prevStepTwoCluster, "old");
                                    log(currStepTwoCluster, "new-in-word");
                                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  new-in-word");
                                    clearPreviousAndSwapStepTwoSubclusters();
                                }
                                else {
                                    data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITION IN WORDS]");
                                    log(prevStepTwoCluster, "old");
                                    log(currStepTwoCluster, "new-in-word");
                                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  old");
                                    currStepTwoCluster.clear();
                                }
                            }
                        }
                    }                    
                }      
            }
            else {
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
         * end of characterFinding [asses] loop
         */
        
        if ( findPositionsStep.equals(STEP_1)) {
            if ( prevStepOneCluster.isSet() ) {
                data.log.add(POSITIONS_SEARCH, "        [SAVE CLUSTER] %s", prevStepOneCluster);
                fillPositionsFrom(prevStepOneCluster);
            }
            else if ( currentCharIsUniqueInPattern && currentCharInVariantQty == 1 ) {
                data.log.add(POSITIONS_SEARCH, "        [SAVE UNIQUE POSITION] '%s'(%s in variant)", currentChar, currentPatternCharPositionInVariantToSave);
                isCurrentCharPositionAddedToPositions = true;
                fillPosition(
                        currentPatternCharIndex,
                        currentPatternCharPositionInVariantToSave,
                        "UNIQUE");
                data.log.add(POSITIONS_SEARCH, "               %s", displayPositions());
                data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
            }
        }
        else if ( findPositionsStep.typoSearchingAllowed() ) {
            if ( prevStepTwoCluster.hasChars() ) {
                if ( prevStepTwoCluster.hasCharsNotFoundInVariant() ) {
                    data.log.add(POSITIONS_SEARCH, "        [SAVE POSITIONS] %s", prevStepTwoCluster);
                    fillPositionsFrom(prevStepTwoCluster);
                }
                else {
                    data.log.add(POSITIONS_SEARCH, "        [NOTHING TO SAVE - ALL POSITIONS ARE FOUND ALREADY] %s", prevStepTwoCluster);
                }
            }
        }
        else if ( positionCandidate.isPresent() ) {
            int position = positionCandidate.position();
                                                
            isCurrentCharPositionAddedToPositions = true;
            fillPosition(
                    currentPatternCharIndex,
                    position,
                    "CANDIDATE");
            data.log.add(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant), %s", currentChar, position, positionCandidate);
            data.log.add(POSITIONS_SEARCH, "               %s", displayPositions());
            data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
        }
        positionCandidate.clear();

        // if current position has not been added because it does not satisfy requirements...
        if ( ! isCurrentCharPositionAddedToPositions ) {
            // ...but if it is STEP_1 and there are only 1 such char in the whole pattern, there is not sense
            // to do operation for this char in subsequent steps - add this char to filled positions and exclude
            // it from subsequent iterations
            if ( findPositionsStep.canAddSingleUnclusteredPosition() && currentCharInVariantQty == 1 ) {
                if ( positionAlreadyFilled ) {
                    data.log.add(POSITIONS_SEARCH, "          [info] '%s'(%s in variant) is single char in variant and already saved", currentChar, currentPatternCharPositionInVariantToSave);
//                    positions[currentPatternCharIndex] = POS_NOT_FOUND;
                    fillPosition(
                            currentPatternCharIndex,
                            POS_NOT_FOUND,
                            null);
                } else {
                    fillPosition(
                            currentPatternCharIndex,
                            currentPatternCharPositionInVariantToSave,
                            "SINGLE_CHAR");
                    data.log.add(POSITIONS_SEARCH, "        [SAVE] '%s'(%s in variant) is single char in variant", currentChar, currentPatternCharPositionInVariantToSave);
                    data.log.add(POSITIONS_SEARCH, "               %s", displayPositions());
                    data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
                }                
            } else {
                data.log.add(POSITIONS_SEARCH, "        [info] position of '%s' is not defined", currentChar);
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
        swapStepTwoCluster.clear();
    }

    private boolean hasPossibleConflict(WordInVariant word, ClusterStepOne cluster) {
        int clusterPosition1 = cluster.mainOfVariant();
        int clusterPosition2 = cluster.nextOfVariant();

        int wordPosition0 = word.startIndex;
        int wordPosition1 = wordPosition0 + 1;

        char wordChar0 = data.variant.charAt(wordPosition0);
        char wordChar1 = data.variant.charAt(wordPosition1);

        char clusterChar1 = data.variant.charAt(clusterPosition1);
        char clusterChar2 = data.variant.charAt(clusterPosition2);

        return ( wordChar0 == clusterChar1 && wordChar1 == clusterChar2 );
    }

    private void acceptPreviousCluster() {
        data.log.add(POSITIONS_SEARCH, "        [COMPARE CLUSTERS]");
        data.log.add(POSITIONS_SEARCH, "             [old]  better  %s", prevStepOneCluster);
        data.log.add(POSITIONS_SEARCH, "             [new]          %s", currStepOneCluster);
        
        currStepOneCluster.clear();
    }

    private void acceptCurrentCluster() {        
        data.log.add(POSITIONS_SEARCH, "        [COMPARE CLUSTERS]");
        data.log.add(POSITIONS_SEARCH, "             [old]          %s", prevStepOneCluster);
        data.log.add(POSITIONS_SEARCH, "             [new]  better  %s", currStepOneCluster);
        
        acceptCurrentAndSwapStepOneClusters();
    }

    private void acceptCurrentAndSwapStepOneClusters() {
        ClusterStepOne swap = currStepOneCluster;
        prevStepOneCluster.clear();
        currStepOneCluster = prevStepOneCluster;
        prevStepOneCluster = swap;
    }

    private void clearPreviousAndSwapStepTwoSubclusters() {
        ClusterStepTwo swap = currStepTwoCluster;
        prevStepTwoCluster.clear();
        currStepTwoCluster = prevStepTwoCluster;
        prevStepTwoCluster = swap;
    }

    private boolean currentCharIsPresentInPatternBefore(int currentCharPatternIndex, int patternIndexExcl) {
        for ( int patternI = currentCharPatternIndex + 1; patternI < patternIndexExcl; patternI++ ) {
            if ( data.patternChars.i(patternI) == currentChar ) {
                return true;
            }
        }

        return false;
    }

    private boolean tryToSearchBetterPositionsInWords() {
        if ( prevStepTwoCluster.hasBackwardTypos() ) {
            int currentAssessedCharPosition = prevStepTwoCluster.assessedCharVariantPosition();

            WordInVariant wordOfCurrentAssessedChar = data.wordsInVariant.wordOf(currentAssessedCharPosition);
            int foundInWord = wordOfCurrentAssessedChar.intersections(positions);
            if ( foundInWord > 0 ) {
                return false;
            }

            WordsInVariant.WordsInRange matchingWordsAfterCurrentAssessedChar = data.wordsInVariant.wordsOfRange(currentAssessedCharPosition, this.positions);

            if ( matchingWordsAfterCurrentAssessedChar.areEmpty() ) {
                return false;
            }

            WordInVariant word;
            for ( int i = 0; i < matchingWordsAfterCurrentAssessedChar.count(); i++ ) {
                word = matchingWordsAfterCurrentAssessedChar.get(i);
                assessPositionsIn(word);
            }

            if ( swapStepTwoCluster.hasChars() ) {
                currStepTwoCluster.clear();
                var swap = currStepTwoCluster;
                currStepTwoCluster = swapStepTwoCluster;
                swapStepTwoCluster = swap;

                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private void assessPositionsIn(WordInVariant word) {
        char variantCh;
        char patternCh;

        char lastFoundVariantCh = '_';
        int lastFoundVariantPosition = UNINITIALIZED;

        char firstFoundVariantCh;
        int firstFoundVariantPosition = UNINITIALIZED;

        int lastFoundCharPositionInWord = UNINITIALIZED;

        data.log.add(POSITIONS_SEARCH, "             [word-review] '%s'", word);
        int iWord = 0;
        variantLookup: for (int iVariant = word.startIndex; iVariant <= word.endIndex; iVariant++) {
            variantCh = data.variant.charAt(iVariant);
            if ( filledPositions.contains(iVariant) ) {
                if ( firstFoundVariantPosition == UNINITIALIZED ) {
                    firstFoundVariantCh = variantCh;
                    firstFoundVariantPosition = iVariant;
                }
                lastFoundVariantCh = variantCh;
                lastFoundVariantPosition = iVariant;
                lastFoundCharPositionInWord = iWord;
            }
            else {
                data.log.add(POSITIONS_SEARCH, "                [word-char] '%s'[variant:%s word:%s] of word '%s'", variantCh, iVariant, iWord, word.charsString());
                patternLookup: for (int iPattern = prevStepTwoCluster.assessedCharPatternPosition(); iPattern < data.patternChars.size(); iPattern++) {
                    patternCh = data.patternChars.i(iPattern);
                    if ( this.isPositionSetAt(iPattern) ) {
                        continue patternLookup;
                    }
                    data.log.add(POSITIONS_SEARCH, "                  [pattern-char] :'%s'[%s]", patternCh, iPattern);

                    if ( patternCh == variantCh ) {
                        if ( prevStepTwoCluster.hasChar(patternCh) ) {
                            if ( firstFoundVariantPosition == UNINITIALIZED ) {

                            }
                            else {
                                MatchType matchType;
                                if ( iVariant == word.endIndex ) {
                                    matchType = MATCH_WORD_END;
                                }
                                else {
                                    matchType = MATCH_TYPO_LOOP;
                                }

                                if ( ! currStepTwoCluster.hasChars() ) {
                                    data.log.add(POSITIONS_SEARCH, "             [word-review] '%s'[variant:%s word:%s] is last filled in word", lastFoundVariantCh, lastFoundVariantPosition, lastFoundCharPositionInWord);
                                    currStepTwoCluster.setAssessed(
                                            word,
                                            lastFoundVariantCh,
                                            patternIndexesByVariantPosition.get(lastFoundVariantPosition),
                                            lastFoundVariantPosition);
                                }

                                currStepTwoCluster.add(
                                        variantCh,
                                        iPattern,
                                        iVariant,
                                        false,
                                        false,
                                        matchType);
                            }
                        }
                    }
                }
            }
            iWord++;
        }

        if ( currStepTwoCluster.hasChars() ) {
            if ( swapStepTwoCluster.hasChars() ) {
                if ( currStepTwoCluster.isBetterThan(swapStepTwoCluster) ) {
                    data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITIONS IN WORD]");
                    log(swapStepTwoCluster, "old");
                    log(currStepTwoCluster, "new");
                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  new");
                    swapStepTwoCluster.clear();
                    var swap = swapStepTwoCluster;
                    swapStepTwoCluster = currStepTwoCluster;
                    currStepTwoCluster = swap;
                }
                else {
                    data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITIONS IN WORD]");
                    log(swapStepTwoCluster, "old");
                    log(currStepTwoCluster, "new");
                    data.log.add(POSITIONS_SEARCH, "          [choice]  ->  old");
                    currStepTwoCluster.clear();
                }
            }
            else {
                data.log.add(POSITIONS_SEARCH, "        [COMPARE POSITIONS IN WORD - NOTHING TO COMPARE]");
                log(currStepTwoCluster, "new");
                data.log.add(POSITIONS_SEARCH, "          [choice]  ->  new");
                swapStepTwoCluster.clear();
                var swap = swapStepTwoCluster;
                swapStepTwoCluster = currStepTwoCluster;
                currStepTwoCluster = swap;
            }
        }
        else {
            currStepTwoCluster.clear();
        }
    }
    
    private void improveWeightOnFoundDuplicateClusters(
            ClusterStepOne one,
            ClusterStepOne two) {
        float bonus = calculateSimilarity(one, two);
        data.log.add(POSITIONS_SEARCH, "        [clusters are similar] -%s", bonus);
        weight.add(-bonus, CLUSTER_CANDIDATES_SIMILARITY);
    }
    
    private boolean canFillPosition(int positionIndex, int positionValue) {
        if ( positions.i(positionIndex) == POS_UNINITIALIZED ) {
            if ( ! filledPositions.contains(positionValue) ) {
                return true;
            }
        }
        return false;
    }
    
    private boolean positionsExistAndCanFillPosition(int positionIndex, int positionValue) {
        if ( positionIndex > -1 && 
             positionValue > -1 && 
             positionIndex < positions.size() &&
             positionValue < data.variant.length() ) {
            return canFillPosition(positionIndex, positionValue);
        } else {
            return false;
        }
    }
    
    private void fillPositionsFrom(ClusterStepOne subcluster) {
        PositionIterableView position = subcluster.positionIterableView();
        WordsInVariant.WordsInRange words = subcluster.findWords(this.data);
        if ( words.count() == 1 ) {
            WordInVariant word = words.get(0);

            if ( subcluster.isPositionsAtStartOf(word) ) {

            }
            else if ( subcluster.isPositionsAtEndOf(word) ) {

            }
            else {
                if ( subcluster.hasTyposBefore() ) {
                    if ( subcluster.areTyposBeforeIn(word) ) {
                        subcluster.tryToMergeTyposBeforeIntoPositions(word);
                    }
                }
            }
        }

        while ( position.hasNext() ) {
            position.goToNext();
            fillPosition(
                    position.patternPosition(),
                    position.variantPosition(),
                    position.match());
            data.log.add(POSITIONS_SEARCH, "               %s", displayPositions());
        }        
        data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
        stepOneClusterSavedRightNow = true;
        lastSavedStepOneCluster.copyFrom(subcluster);
    }
    
    private void fillPositionsFrom(ClusterStepTwo subcluster) {
        ClusterStepTwo.StepTwoClusterPositionView position = subcluster.positionView();
        if ( ! subcluster.isAssessedCharFilledInVariant() ) {
            fillPosition(
                    subcluster.assessedCharPatternPosition(),
                    subcluster.assessedCharVariantPosition(),
                    subcluster.assessedCharBestMatch());
            data.log.add(POSITIONS_SEARCH, "               %s", displayPositionsUpperCase(subcluster.assessedCharPatternPosition(), subcluster.assessedCharVariantPosition()));
        }
        else {
            data.log.add(POSITIONS_SEARCH, "               '%s'[variant:%s] is already filled", subcluster.assessedChar(), subcluster.assessedCharVariantPosition());
        }

        while ( position.hasNext() ) {
            position.goToNext();
            if ( position.canBeWritten() ) {
                fillPosition(
                        position.patternPosition(),
                        position.variantPosition(),
                        position.match());
                data.log.add(POSITIONS_SEARCH, "               %s", displayPositionsUpperCase(position.patternPosition(), position.variantPosition()));
            }
            else {
                data.log.add(POSITIONS_SEARCH, "               '%s'[variant:%s] cannot be written - filled in %s",
                        position.character(),
                        position.variantPosition(),
                        position.isFilledInPattern() ? "pattern" : "variant");
            }
        }  
        data.log.add(POSITIONS_SEARCH, "               %s : %s", data.pattern, data.variant);
    }

    private void fillPosition(int patternIndex, int positionInVariant, String match) {
        if ( positionInVariant == POS_NOT_FOUND ) {
            positions.i(patternIndex, POS_NOT_FOUND);
            matchTypesByVariantPosition.remove(positionInVariant);
            patternIndexesByVariantPosition.remove(positionInVariant);
            positionFoundSteps.remove(positionInVariant);
            localUnclusteredPatternCharIndexes.add(patternIndex);
            char c = data.patternChars.i(patternIndex);
            this.notFoundPatternChars.add(c);
            isCurrentCharPositionAddedToPositions = false;

            return;
        }

        positions.i(patternIndex, positionInVariant);
        matchTypesByVariantPosition.put(positionInVariant, match);
        patternIndexesByVariantPosition.put(positionInVariant, patternIndex);
        positionFoundSteps.put(positionInVariant, findPositionsStep);
        localUnclusteredPatternCharIndexes.remove(patternIndex);
        char c = data.patternChars.i(patternIndex);
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
        for (int i = patternCharIndex + 1; i < positions.size(); i++) {
            position = positions.i(i);
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        for (int i = patternCharIndex - 1; i > -1; i--) {
            position = positions.i(i);
            if ( position != POS_UNINITIALIZED ) {
                return position;
            }
        }
        return POS_NOT_FOUND;
    }
    
    private int searchForwardAndBackwardNearestFilledPositionTo(int patternCharIndex) {
        int position;
        
        if ( patternCharIndex > positions.size() / 2 ) {
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
        this.currentPosition = this.positions.i(i);
    }

    void newClusterStarts() {
        this.currentClusterIsRejected = false;
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
        this.currentClusterWordStartFound = false;
        
        this.processClusterPositionOrderStats();

        if ( ! this.data.variantContainsPattern ) {
            WordsInVariant.WordsInRange wordsInRange = this.data.wordsInVariant.wordsOfRange(this.currentClusterFirstPosition, this.currentClusterLength);

            if ( wordsInRange.areEmpty() ) {
                this.currentClusterIsRejected = true;
                data.log.add(POSITIONS_CLUSTERS, "    [cluster] there are no words for cluster!");
            }
            else {
                if ( wordsInRange.hasStartIn(this.filledPositions) ) {
                    this.currentClusterWordStartFound = true;
                }
                else {
                    if ( wordsInRange.count() == 1 ) {
                        WordInVariant word = wordsInRange.first();
                        WordInVariant prevWord = this.data.wordsInVariant.wordBeforeOrNull(word);
                        if ( nonNull(prevWord) ) {
                            char lastCharOfPrevWord = this.data.variant.charAt(prevWord.endIndex);
                            char firstCharOfClusterWord = this.data.variant.charAt(word.startIndex);
                            if ( lastCharOfPrevWord == firstCharOfClusterWord ) {
                                if ( this.filledPositions.contains(prevWord.endIndex) ) {
                                    data.log.add(POSITIONS_CLUSTERS, "    [cluster] word of cluster has its start found in last char of previous word!");
                                    this.currentClusterWordStartFound = true;
                                }
                            }
                        }
                    }

                    if ( ! this.currentClusterWordStartFound ) {
                        this.currentClusterIsRejected = true;
                        data.log.add(POSITIONS_CLUSTERS, "    [cluster] word of cluster has not its start found, cluster is bad!");
                    }
                }
            }
        }

        this.accumulateClusterPositionOrdersStats();

        if ( this.currentClusterIsRejected ) {
            this.clustersQty--;
            this.clustered = this.clustered - this.currentClusterLength;
            this.nonClustered = this.nonClustered + this.currentClusterLength;
            data.log.add(POSITIONS_CLUSTERS, "    [cluster] cluster is rejected by order diff analysis");

            if ( this.clustersQty == 0 ) {
                data.log.add(POSITIONS_CLUSTERS, "    [cluster] first cluster in variant is rejected");
                this.weight.add(FIRST_CLUSTER_IS_REJECTED);
            }

            boolean ignoreClusterPenalty = false;
            if ( this.currentClusterLength == 2 ) {
                if ( this.currentClusterWordStartFound ) {
                    ignoreClusterPenalty = true;
                    // partially compensate clustering importance
                    if ( data.wordsInVariant.all.size() > 2 && this.filledPositions.size() > 7 ) {
                        this.clustered++;
                        this.nonClustered--;
                    }
                }
            }

            if ( ! ignoreClusterPenalty ) {
                this.weight.add(CLUSTER_IS_REJECTED_BY_ORDER_DIFFS);
            }

            return;
        }
        
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
            }
            else {
                if ( this.alonePositionAfterPreviousSeparator != POS_UNINITIALIZED &&
                     this.alonePositionAfterPreviousSeparator != POS_ERASED ) {
                    float bonus = 7.25f;
                    boolean hasSeparatorsBetween = containsSeparatorsInVariantInSpan(this.alonePositionAfterPreviousSeparator, this.currentPosition - this.currentClusterLength + 1);

                    if ( this.currentClusterLength > 2 ) {
                        if ( ! hasSeparatorsBetween ) {
                            bonus = bonus + (this.currentClusterLength * 2) - (this.clusters.lastAddedCluster().firstPosition() - this.alonePositionAfterPreviousSeparator - 1);
                            isClusterLongWord = true;
                        }
                    } 
                    if ( ! hasSeparatorsBetween ) {
                        this.weight.add(-bonus, CHAR_AFTER_PREVIOUS_SEPARATOR_AND_CLUSTER_ENCLOSING_WORD);
                        if ( absDiff(this.alonePositionAfterPreviousSeparator, this.clusters.lastAddedCluster().firstPosition()) == 2 ) {
                            char alonePositionBeforePreviousSeparatorChar = data.variant.charAt(this.alonePositionAfterPreviousSeparator);
                            char missedPositionChar = data.variant.charAt(this.alonePositionAfterPreviousSeparator + 1);
                            char firstClusterPositionChar = data.variant.charAt(this.clusters.lastAddedCluster().firstPosition());
                            if ( missedPositionChar == alonePositionBeforePreviousSeparatorChar || 
                                 missedPositionChar == firstClusterPositionChar ) {
                                String log = format(
                                        "    [cluster fix] missed outer repeat detected %s(%s)%s", 
                                        alonePositionBeforePreviousSeparatorChar, 
                                        missedPositionChar,
                                        firstClusterPositionChar);
                                data.log.add(POSITIONS_CLUSTERS,
                                        "         %s", log);
                                this.clustered++;
                                this.nonClustered--;
                            }
                        }
                    }
                    else {
                        this.checkWordsInRangeOfCurrentCluster();
                    }
                }
                else {
                    this.checkWordsInRangeOfCurrentCluster();
                }
            }
        }
        else {
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
                if ( this.existMismatches() ) {

                }
                else {
                    WordInVariant word0 = data.wordsInVariant.wordOf(this.alonePositionAfterPreviousSeparator);
                    WordInVariant word1 = data.wordsInVariant.wordOf(this.currentClusterFirstPosition);

                    if ( word0.index == word1.index ) {
                        int word0intersection = word0.intersectsWithRange(this.currentClusterFirstPosition, this.currentClusterLength);

                        if ( word0intersection == this.currentClusterLength ) {
                            this.weight.add(SINGLE_POSITION_AND_FULL_CLUSTER_DENOTE_WORD);
                        }
                        else if ( word0intersection < this.currentClusterLength && word0intersection > 0 ) {
                            this.weight.add(SINGLE_POSITION_AND_PART_OF_CLUSTER_DENOTE_WORD);
                        }
                    }
                    else if ( word1.placing.is(DEPENDENT) && word0.index == word1.index - 1 ) {
                        int word1intersection = word1.intersectsWithRange(this.currentClusterFirstPosition, this.currentClusterLength);

                        if ( word1intersection >= this.currentClusterLength ) {
                            this.weight.add(SINGLE_POSITION_AND_FULL_CLUSTER_DENOTE_WORD);
                        }
                        else if ( word1intersection > 0 ) {
                            this.weight.add(SINGLE_POSITION_AND_PART_OF_CLUSTER_DENOTE_WORD);
                        }
                    }
                }
            }

            this.checkWordsInRangeOfCurrentCluster();
        }
        
        if ( this.currentClusterLength > 2 && this.currentClusterOrdersIsConsistent && ! this.currentClusterIsRejected ) {
            if ( this.currentClusterOrdersHaveDiffCompensations ) {
//                Cluster cluster = this.clusters.lastAddedCluster();
//                boolean isAcceptable = 
//                        cluster.ordersDiffSumAbs() == 0 && 
//                        cluster.ordersDiffCount() <= this.currentClusterLength / 2;
//                if ( isAcceptable && this.patternContainsClusterFoundInVariant() ) {
//                    int containingReward = square(this.currentClusterLength);
//                    this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER);
//                } else {
//                    data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] pattern DOES NOT contain cluster!");
//                    this.weight.add(this.currentClusterLength, PATTERN_DOES_NOT_CONTAIN_CLUSTER);
//                }
            } else {
                if ( this.patternContainsClusterFoundInVariant() ) {
                    int containingReward;
                    if ( isClusterLongWord ) {
                        containingReward = cubeUpTo5AddSquareIfOver(this.currentClusterLength) + square(this.currentClusterLength);
                        this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER_LONG_WORD);
                    } else {           
                        containingReward = cubeUpTo5AddSquareIfOver(this.currentClusterLength);
                        this.weight.add(-containingReward, PATTERN_CONTAINS_CLUSTER);
                    }                  
                } else {
                    data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] pattern DOES NOT contain cluster!");
                    this.weight.add(this.currentClusterLength, PATTERN_DOES_NOT_CONTAIN_CLUSTER);
                }
            }            
        }

        if ( ! this.data.variantContainsPattern ) {
            if ( this.data.pattern.length() < 6 ) {
                if ( this.currentClusterLength == 2 ) {
                    Cluster cluster = this.clusters.lastAddedCluster();
                    int pattern0 = this.patternIndexesByVariantPosition.get(cluster.position(0));
                    int pattern1 = this.patternIndexesByVariantPosition.get(cluster.position(1));
                    int patternDiff = absDiff(pattern0, pattern1);
                    if ( patternDiff > 1 ) {
                        data.log.add(POSITIONS_CLUSTERS,
                                "             [cluster] pattern diff detected, revert cluster counting");
                        this.clustered = this.clustered - this.currentClusterLength;
                        this.nonClustered = this.nonClustered + this.currentClusterLength;
                    }
                }
                else if ( this.currentClusterLength == 3 ) {

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

    private void checkWordsInRangeOfCurrentCluster() {
        if ( this.data.variantContainsPattern ) {
            return;
        }

        WordsInVariant.WordsInRange wordsInRange = data.wordsInVariant
                .wordsOfRange(this.currentClusterFirstPosition, this.currentClusterLength);

        if (wordsInRange.areEmpty()) {
            data.log.add(POSITIONS_CLUSTERS,
                    "             [cluster] cluster is senseless");
            this.clustered = this.clustered - this.currentClusterLength;
            this.nonClustered = this.nonClustered + this.currentClusterLength;
        }
        else {
            boolean clusterIsLongEnough =
                    this.currentClusterLength > 3 ||
                    (this.currentClusterLength == 3 && this.currentClusterLength > wordsInRange.length() / 2);

            if ( ! clusterIsLongEnough ) {

                if ( ! this.currentClusterWordStartFound ) {
                    data.log.add(POSITIONS_CLUSTERS,
                            "             [cluster] cluster word has not found start");
                    this.clustered = this.clustered - this.currentClusterLength;
                    this.nonClustered = this.nonClustered + this.currentClusterLength;
                }
            }
        }
    }

    private void checkWordsInRangeOfFoundPositions() {
        if ( this.data.wordsInVariant.all.size() > 1 ) {
            if ( ! this.data.variantContainsPattern ) {
                int firstFoundPosition = this.findFirstPosition();
                WordInVariant word = this.data.wordsInVariant.wordOf(firstFoundPosition);

                if ( ! this.filledPositions.contains(word.startIndex) ) {
                    this.weight.add(FIRST_CLUSTER_HAS_MISSED_WORD_START);
                }
            }
        }
    }

    void applySingleWordQuality() {
        checkWordsCoverageByAllClusters();
    }

    private void checkWordsCoverageByAllClusters() {
        data.log.add(POSITIONS_CLUSTERS, "    [Words]");

        WordsInVariant.WordsInRange foundWords = this.data.wordsInVariant.wordsOfRange(this.positions);

        if ( foundWords.count() == data.wordsInVariant.all.size() ) {
            float wordBonus = -13.37f;
            this.weight.add(foundWords.count() * wordBonus, FOUND_POSITIONS_DENOTES_ALL_WORDS);
        }

        if ( foundWords.count() == 1 ) {
            WordInVariant word = foundWords.get(0);

            if ( this.notFoundPatternCharsCount() == 0 ) {
                float bonus;
                if ( this.data.pattern.length() > (word.length/2)+1 ) {
                    bonus = -3.11f * this.data.pattern.length();
                }
                else {
                    bonus = -1.73f * this.data.pattern.length();
                }
                this.weight.add(bonus, FOUND_POSITIONS_BELONG_TO_ONE_WORD);
            }
        }

        boolean isSpecialCase;
        int wordQuality;

        WordInVariant word;
        List<WordInVariant> all = foundWords.all();
        iterateFoundWords: for ( int iWord = 0; iWord < all.size(); iWord++ ) {
            word = all.get(iWord);
            wordQuality = 0;
            isSpecialCase = false;

            data.log.add(POSITIONS_CLUSTERS, "       [Word quality] %s", word.charsString());

            if ( word.length == 1 ) {
                wordQuality = 1;
                data.log.add(POSITIONS_CLUSTERS, "          1 single-char-word");
            }
            else {
                this.clusters.chooseAllOf(word);
                List<Cluster> clustersInWord = this.clusters.chosenInWord();
                int spanInWord = -1;

                Cluster cluster;
                if ( clustersInWord.isEmpty() ) {
                    int singlePositionsInWordCount = word.intersections(this.singlePositions.filled());

                    if ( singlePositionsInWordCount == 0 ) {
                        if ( iWord == 0 ) {
                            wordQuality = -12;
                            data.log.add(POSITIONS_CLUSTERS, "          -12 first word, no cluster, no positions");
                        }
                        else {
                            wordQuality = -10;
                            data.log.add(POSITIONS_CLUSTERS, "          -10 no cluster, no positions");
                        }
                    }
                    else {
                        if ( singlePositionsInWordCount == 1 ) {
                            boolean importantWord = false;
                            if ( this.singlePositions.contains(word.startIndex) ) {

                                if ( this.isUniqueInVariant(word.startIndex) ) {
                                    importantWord = true;
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 no cluster, word start is unique position");
                                }

                                if ( this.keyChars.contains(word.startIndex) ) {
                                    importantWord = true;
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 no cluster, word start is key char");
                                }

                                if ( word.length < 4 ) {
                                    if ( importantWord ) {
                                        wordQuality = wordQuality + 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          +1 no cluster, single position 1 at start, important word length < 4");
                                    }

                                    if ( this.singlePositions.filled().size() == 1 ) {
                                        importantWord = true;
                                        wordQuality = wordQuality + 2;
                                        data.log.add(POSITIONS_CLUSTERS, "          +2 no cluster, single positions count == 1");
                                    }
                                    else {
                                        if ( this.keyChars.contains(word.startIndex) ) {
                                            importantWord = true;
                                            wordQuality = wordQuality + 1;
                                            data.log.add(POSITIONS_CLUSTERS, "          +1 no cluster, key chars contains word start");
                                        }
                                    }

                                    if ( ! importantWord ) {
                                        if ( word.placing.is(INDEPENDENT) ) {
                                            if ( this.nextDependentWordsHasFoundPositions(word) ) {
                                                importantWord = true;
                                                wordQuality = wordQuality + 2;
                                                data.log.add(POSITIONS_CLUSTERS, "          +2 no cluster, single positions count == 1, independent word havin positions in further dependent");
                                            }
                                        }
                                    }
                                }
                                else {
                                    if ( ! importantWord ) {
                                        wordQuality = wordQuality - 4;
                                        data.log.add(POSITIONS_CLUSTERS, "          -4 no cluster, single position 1 at start, word length > 3");
                                    }
                                }
                            }
                            else {
//                                if ( this.data.wordsInVariant.all.size() > 3 ) {
//                                    wordQuality = wordQuality - 2;
//                                    data.log.add(POSITIONS_CLUSTERS, "          -2 no cluster, word start not in single positions, words qty > 3");
//                                }
//                                else {
//                                    wordQuality = wordQuality - 4;
//                                    data.log.add(POSITIONS_CLUSTERS, "          -4 no cluster, word start not in single positions, words qty <= 3");
//                                }
                            }

                            if ( wordQuality > 0 ) {
                                if ( foundWords.count() == this.data.wordsInVariant.all.size() ) {
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 all words are found");
                                }
                                else if ( foundWords.count() > this.data.wordsInVariant.all.size() / 2 ) {

                                }
                                else {
                                    if ( ! importantWord ) {
                                        wordQuality = wordQuality - 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          -1 majority of words are not found");
                                    }
                                }
                            }
                            else {
//                                if ( ! importantWord ) {
//                                    wordQuality = wordQuality - 1;
//                                    data.log.add(POSITIONS_CLUSTERS, "          -1 senseless word match");
//                                }
                            }
                        }
                        else {
                            if ( this.singlePositions.contains(word.startIndex) ) {
                                if ( this.singlePositions.contains(word.endIndex) ) {
                                    if ( word.length == 3 ) {
                                        wordQuality = wordQuality + 3;
                                        data.log.add(POSITIONS_CLUSTERS, "          +3 single positions contains word start and end, word length == 3");
                                    }
                                    else if ( word.length == 4 ) {
                                        wordQuality = wordQuality + 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          +1 single positions contains word start and end, word length == 4");
                                    }
                                    else if ( singlePositionsInWordCount > 2 ) {
                                        int bonus = singlePositionsInWordCount-2;
                                        wordQuality = wordQuality + bonus;
                                        data.log.add(POSITIONS_CLUSTERS, "          +%s single positions contains word start and end, word length > 4, extra positions inside word", bonus);
                                    }
                                    else {
                                        wordQuality--;
                                        data.log.add(POSITIONS_CLUSTERS, "          -1 single positions contains word start and end, no extra conditions");
                                    }
                                }
                                else {
                                    if ( word.length == 2 ) {

                                    }
                                    else if ( word.length == 3 ) {

                                    }
                                    else {
                                        if ( singlePositionsInWordCount == 2 ) {
                                            if ( this.clusters.quantity() == 0 ) {
                                                wordQuality = wordQuality + 1;
                                                data.log.add(POSITIONS_CLUSTERS, "          +1 single positions contains word start and end, no clusters at all found");
                                            }
                                            else {
                                                boolean bad = true;

                                                if ( this.isUniqueInVariant(word.startIndex) ) {
                                                    bad = false;
                                                    wordQuality = wordQuality + 1;
                                                    data.log.add(POSITIONS_CLUSTERS, "          +1 single positions contains only start and middle, word start is unique position");
                                                }

                                                if ( this.keyChars.contains(word.startIndex) ) {
                                                    bad = false;
                                                    wordQuality = wordQuality + 1;
                                                    data.log.add(POSITIONS_CLUSTERS, "          +1 single positions contains only start and middle, word start is key char");
                                                }

                                                if ( bad ) {
                                                    if ( word.length > 5 ) {
                                                        int penalty = word.length - 5;
                                                        wordQuality = wordQuality - penalty;
                                                        data.log.add(POSITIONS_CLUSTERS, "          -%s single positions contains only start and middle", penalty);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if ( clustersInWord.size() == 1 ) {
                    cluster = clustersInWord.get(0);

                    if ( cluster.isRejected() || cluster.hasTeardown() ) {
                        wordQuality =  wordQuality - cluster.teardown();
                        data.log.add(POSITIONS_CLUSTERS, "          -%s teardown", cluster.teardown());
                    }

                    if ( this.data.patternInVariantIndex > -1 ) {
                        isSpecialCase = true;
                        wordQuality = 6;
                        data.log.add(POSITIONS_CLUSTERS, "          6 special case");
                    }

                    if ( ! isSpecialCase ) {

                        int singlePositionsInWordCount = word.intersections(this.singlePositions.filled());

                        if ( word.startIndex == cluster.firstPosition() || cluster.contains(word.startIndex) ) {
                            if ( word.endIndex == cluster.lastPosition() || cluster.contains(word.endIndex) ) {
                                // word: abdcdxyz
                                wordQuality = wordQuality + 3;
                                data.log.add(POSITIONS_CLUSTERS, "          +3 word: abdcdxyz");

                                if ( word.length > 2 ) {
                                    int bonus = word.length-2;
                                    wordQuality = wordQuality + bonus;
                                    data.log.add(POSITIONS_CLUSTERS, "          +%s word length > 2", bonus);
                                }
                                spanInWord = word.length;
                            }
                            else if ( this.singlePositions.contains(word.endIndex) ) {
                                if ( singlePositionsInWordCount > 1 ) {
                                    // word: abd_c_z
                                    wordQuality = wordQuality + 3;
                                    data.log.add(POSITIONS_CLUSTERS, "          +3 word: abd_c_z");
                                }
                                else {
                                    wordQuality = wordQuality + 2;
                                    data.log.add(POSITIONS_CLUSTERS, "          +2 word: abd___z");
                                    // word: abd___z
                                }
                                spanInWord = word.length;
                            }
                            else {
                                if ( singlePositionsInWordCount > 0 ) {
                                    // word: abd_c__
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 word: abd_c__");
                                    spanInWord =
                                            this.singlePositions.lastBetween(cluster.lastPosition(), word.endIndex)
                                                    - word.startIndex
                                                    + 1;
                                }
                                else {
                                    // word: abd____
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 word: abd____");
                                    spanInWord = cluster.length();
                                }
                            }
                        }
                        else if ( word.endIndex == cluster.lastPosition() || cluster.contains(word.endIndex) ) {
                            if ( this.singlePositions.contains(word.startIndex) ) {
                                if ( singlePositionsInWordCount > 1 ) {
                                    // word: a_c_xyz
                                    wordQuality = wordQuality + 3;
                                    data.log.add(POSITIONS_CLUSTERS, "          +3 word: a_c_xyz");
                                }
                                else {
                                    // word: a___xyz
                                    wordQuality = wordQuality + 2;
                                    data.log.add(POSITIONS_CLUSTERS, "          +2 word: a___xyz");
                                }
                                spanInWord = word.length;
                            }
                            else {
                                if ( singlePositionsInWordCount > 0 ) {
                                    // word: _c_xyz
                                }
                                else {
                                    // word: ___xyz
                                    data.log.add(POSITIONS_CLUSTERS, "          -1 word: ___xyz");
                                    wordQuality--;
                                }
                            }
                        }
                        else if ( this.singlePositions.contains(word.startIndex) ) {
                            if ( this.singlePositions.contains(word.endIndex) ) {
                                if ( singlePositionsInWordCount > 2 ) {
                                    // word: a_bcd_e_f
                                    wordQuality = wordQuality + 4;
                                    data.log.add(POSITIONS_CLUSTERS, "          +4 word: a_bcd_e_f");
                                }
                                else {
                                    // word: a_bcd___f
                                    data.log.add(POSITIONS_CLUSTERS, "          +3 word: a_bcd___f");
                                    wordQuality = wordQuality + 3;
                                }
                                spanInWord = word.length;
                            }
                            else {
                                if ( singlePositionsInWordCount > 1 ) {
                                    wordQuality = wordQuality + 2;
                                    data.log.add(POSITIONS_CLUSTERS, "          +2 word: a_bcd_e__ or a_e_bcd__");

                                    int lastSinglePosition = this.singlePositions.lastBetween(cluster.lastPosition(), word.endIndex);

                                    if ( lastSinglePosition > -1 ) {
                                        // word: a_bcd_e__
                                        spanInWord =
                                                lastSinglePosition
                                                        - word.startIndex
                                                        + 1;
                                    }
                                    else {
                                        // word: a_e_bcd__
                                        spanInWord = cluster.lastPosition() - word.startIndex + 1;
                                    }
                                }
                                else {
                                    // word: a_bcd____
                                    wordQuality = wordQuality + 1;
                                    spanInWord = cluster.lastPosition() - word.startIndex + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 word: a_bcd____");
                                }
                            }
                        }
                        else if ( this.singlePositions.contains(word.endIndex) ) {
                            if ( singlePositionsInWordCount > 1 ) {
                                // word: __bcd_e_f
                                data.log.add(POSITIONS_CLUSTERS, "          -2 word: __bcd_e_f");
                                wordQuality = wordQuality - 2;
                            }
                            else {
                                // word: __bcd___f
                                data.log.add(POSITIONS_CLUSTERS, "          -3 word: __bcd___f");
                                wordQuality = wordQuality - 3;
                            }
                        }
                        else {
                            // word: __bcd____
                            data.log.add(POSITIONS_CLUSTERS, "          -7 word: __bcd____");
                            wordQuality = wordQuality - 7;
                        }

                        if ( cluster.firstPosition() == word.startIndex ) {

                            if ( wordQuality > 0 ) {
                                int clusterLengthOver2 = cluster.length() - 2;
                                if ( clusterLengthOver2 > 0 ) {
                                    wordQuality = wordQuality + clusterLengthOver2;
                                    data.log.add(POSITIONS_CLUSTERS, "          +%s cluster length over 2", clusterLengthOver2);
                                }

                                if ( cluster.length() > 4 ) {
                                    int clusterLengthOver4 = cluster.length() - 4;
                                    wordQuality = wordQuality + clusterLengthOver4;
                                    data.log.add(POSITIONS_CLUSTERS, "          +%s cluster length over 4", clusterLengthOver4);
                                }

                                if ( cluster.length() + singlePositionsInWordCount > 3 ) {
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 cluster length and single positions in word > 3");
                                }
                            }

                            if ( wordQuality >= 0 && spanInWord > 0 && cluster.length() > 2 ) {
                                int filled = cluster.length() + singlePositionsInWordCount;
                                int notFilled = word.length - filled;

                                int spanPercent = MathFunctions.percentAsInt(spanInWord, word.length);
                                if ( filled > notFilled ) {
                                    wordQuality = wordQuality + 1;
                                    data.log.add(POSITIONS_CLUSTERS, "          +1 filled > notFilled");
                                    if ( spanPercent > 70 ) {
                                        wordQuality = wordQuality + 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          +1 span in word > 70%");
                                        if ( notFilled == 0 ) {
                                            wordQuality = wordQuality + 1;
                                            data.log.add(POSITIONS_CLUSTERS, "          +1 cluster == word");
                                        }
                                    }
                                }
                                else if ( filled > notFilled/2 ){
                                    if ( spanPercent > 60 ) {
                                        wordQuality = wordQuality + 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          +1 filled > notFilled/2 and span in word > 60%");
                                        if ( spanPercent > 80 ) {
                                            wordQuality = wordQuality + 1;
                                            data.log.add(POSITIONS_CLUSTERS, "          +1 filled > notFilled/2 and span in word > 80%");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    int singlePositionsInWordCount = word.intersections(this.singlePositions.filled());

                    boolean clusterAtWordStart = false;
                    boolean clusterAtWordEnd = false;
                    boolean wordStartFound = false;
                    boolean wordEndFound = false;

                    int clustersInMiddleCount = 0;

                    int clustersCount = clustersInWord.size();
                    int lastCluster = clustersCount - 1;

                    int clusteredInWordSum = 0;

                    iterateClustersInWord: for ( int iCluster = 0; iCluster < clustersCount; iCluster++ ) {
                        cluster = clustersInWord.get(iCluster);
                        clusteredInWordSum = clusteredInWordSum + cluster.length() - cluster.teardown();

                        if ( cluster.isRejected() || cluster.hasTeardown() ) {
                            wordQuality = wordQuality - cluster.teardown();
                            data.log.add(POSITIONS_CLUSTERS, "          -%s teardown", cluster.teardown());

                            if ( iCluster == 0 ) {
                                wordQuality = wordQuality - 2;
                                data.log.add(POSITIONS_CLUSTERS, "          -2 first cluster is rejected");
                            }

                            continue iterateClustersInWord;
                        }

                        if ( cluster.length() > 2 ) {
                            wordQuality++;
                            data.log.add(POSITIONS_CLUSTERS, "          +1 cluster length > 2");
                            if ( cluster.length() > 3 ) {
                                wordQuality++;
                                data.log.add(POSITIONS_CLUSTERS, "          +1 cluster length > 3");
                            }
                        }

                        if ( iCluster == 0 ) {
                            if ( word.startIndex == cluster.firstPosition() || cluster.contains(word.startIndex) ) {
                                clusterAtWordStart = true;
                                wordStartFound = true;
                                wordQuality++;
                                data.log.add(POSITIONS_CLUSTERS, "          +1 first cluster, word starts and ends in single cluster");
                            }
                            else if ( this.singlePositions.contains(word.startIndex) ) {
                                wordStartFound = true;
                            }
                            else {
                                wordQuality = wordQuality - 3;
                                data.log.add(POSITIONS_CLUSTERS, "          -3 first cluster, word start and end not in cluster");
                            }
                        }
                        else if ( iCluster == lastCluster ) {
                            if ( word.endIndex == cluster.lastPosition() || cluster.contains(word.endIndex) ) {
                                clusterAtWordEnd = true;
                                wordEndFound = true;
                                wordQuality++;
                                data.log.add(POSITIONS_CLUSTERS, "          +1 last cluster, word starts and ends in single cluster");
                            }
                            else if ( this.singlePositions.contains(word.endIndex) ) {
                                wordEndFound = true;
                            }
                        }
                        else {
                            clustersInMiddleCount++;
                        }
                    }

                    if ( clusterAtWordStart ) {
                        if ( clusterAtWordEnd ) {
                            if ( clustersInMiddleCount > 0 ) {
                                if ( singlePositionsInWordCount > 0 ) {
                                    wordQuality = wordQuality + 7;
                                    data.log.add(POSITIONS_CLUSTERS, "          +7 clusterAtWordStart, clusterAtWordEnd, clustersInMiddle, singlePositionsInWord");
                                }
                                else {
                                    wordQuality = wordQuality + 6;
                                    data.log.add(POSITIONS_CLUSTERS, "          +6 clusterAtWordStart, clusterAtWordEnd, clustersInMiddle");
                                }
                            }
                            else {
                                if ( singlePositionsInWordCount > 0 ) {
                                    wordQuality = wordQuality + 5;
                                    data.log.add(POSITIONS_CLUSTERS, "          +5 clusterAtWordStart, clusterAtWordEnd, singlePositionsInWord");
                                }
                                else {
                                    wordQuality = wordQuality + 4;
                                    data.log.add(POSITIONS_CLUSTERS, "          +4 clusterAtWordStart, clusterAtWordEnd");
                                }
                            }
                        }
                        else if ( wordEndFound ) {
                            if ( clustersInMiddleCount > 0 ) {
                                if ( singlePositionsInWordCount > 1 ) {
                                    wordQuality = wordQuality + 6;
                                    data.log.add(POSITIONS_CLUSTERS, "          +6 clusterAtWordStart, wordEndFound, clustersInMiddle, singlePositionsInWord");
                                }
                                else {
                                    wordQuality = wordQuality + 5;
                                    data.log.add(POSITIONS_CLUSTERS, "          +5 clusterAtWordStart, wordEndFound, clustersInMiddle");
                                }
                            }
                            else {
                                if ( singlePositionsInWordCount > 1 ) {
                                    wordQuality = wordQuality + 4;
                                    data.log.add(POSITIONS_CLUSTERS, "          +4 clusterAtWordStart, wordEndFound, singlePositionsInWord");
                                }
                                else {
                                    // no +
                                }
                            }
                        }
                        else {
                            if ( clustersInMiddleCount > 0 ) {
                                if ( singlePositionsInWordCount > 0 ) {
                                    wordQuality = wordQuality + 4;
                                    data.log.add(POSITIONS_CLUSTERS, "          +4 clusterAtWordStart, clustersInMiddle, singlePositionsInWord");
                                }
                                else {
                                    wordQuality = wordQuality + 3;
                                    data.log.add(POSITIONS_CLUSTERS, "          +3 clusterAtWordStart, clustersInMiddle");
                                }
                            }
                            else {
                                if ( singlePositionsInWordCount > 0 ) {
                                    if ( singlePositionsInWordCount > 1 ) {
                                        wordQuality = wordQuality + 2;
                                        data.log.add(POSITIONS_CLUSTERS, "          +2 clusterAtWordStart, singlePositionsInWord > 1");
                                    }
                                    else {
                                        wordQuality = wordQuality + 1;
                                        data.log.add(POSITIONS_CLUSTERS, "          +1 clusterAtWordStart, singlePositionsInWord > 0");
                                    }
                                }
                                else {
                                    // no +
                                }
                            }
                        }
                    }
                    else if ( wordStartFound ) {
                        if ( clusterAtWordEnd ) {
                            if ( clustersInMiddleCount > 0 ) {
                                if ( singlePositionsInWordCount > 1 ) {

                                }
                                else {

                                }
                            }
                            else {
                                if ( singlePositionsInWordCount > 1 ) {

                                }
                                else {

                                }
                            }
                        }
                        else if ( wordEndFound ) {
                            if ( clustersInMiddleCount > 0 ) {
                                if ( singlePositionsInWordCount > 2 ) {

                                }
                                else {

                                }
                            }
                            else {
                                throw new IllegalStateException();
                            }
                        }
                        else {

                        }
                    }
                    else {

                    }
                }
            }

            data.log.add(POSITIONS_CLUSTERS, "          sum : %s", wordQuality);
            int weightValue;
            if ( wordQuality == 1 ) {
                weightValue = -2;
            }
            else if ( wordQuality == -1 ) {
                weightValue = 2;
            }
            else if ( wordQuality > 0 ) {
                weightValue = -square(wordQuality);
            }
            else if ( wordQuality < 0 ) {
                weightValue = square(wordQuality);
            }
            else {
                weightValue = 0;
            }

            if ( weightValue != 0 ) {
                this.weight.add(weightValue, WORD_QUALITY);
                if ( weightValue > 0 && weightValue < 50 ) {
                    this.weight.add(50, WORD_QUALITY);
                }
            }

        }
    }

    private boolean existMismatches() {
        int position;
        int unsortedOrder;
        int unsortedOrderOfSinglePosition = this.positionUnsortedOrders.get(this.alonePositionAfterPreviousSeparator);
        int unsortedOrderOfClusterFirstPosition = this.positionUnsortedOrders.get(this.currentClusterFirstPosition);

        if ( unsortedOrderOfClusterFirstPosition < unsortedOrderOfSinglePosition ) {
            return true;
        }

        for ( int i = 0; i < this.positions.size(); i++ ) {
            position = positions.i(i);
            if ( position > this.alonePositionAfterPreviousSeparator && position < this.currentClusterFirstPosition ) {
                unsortedOrder = this.positionUnsortedOrders.get(position);
                if ( unsortedOrderOfSinglePosition < unsortedOrder && unsortedOrderOfClusterFirstPosition > unsortedOrder ) {

                }
                else {
                    data.log.add(POSITIONS_CLUSTERS, "    [single position] %s - position %s is out of natural order!", this.alonePositionAfterPreviousSeparator, position);
                    return true;
                }
            }
        }

        return false;
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
        int patternLength = this.data.patternChars.size();
        
        int j;
        
        boolean found = false;
        boolean missedRepeatDetected = false;
        
        patternIterating : for (int i = 0; i < this.data.patternChars.size(); i++) {
            patternChar = this.data.patternChars.i(i);
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
                    patternChar = this.data.patternChars.i(patternCharPos);
                    
                    if ( clusteredChar != patternChar ) {
                        found = false;  
                        if ( this.missedRepeatedPositions.isNotEmpty() ) {
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
                    if ( this.extractedMissedRepeatedPositionsIndexes.isNotEmpty() ) {
                        this.extractedMissedRepeatedPositionsIndexes.sort(REVERSE);
                        int missedRepeatedPositionsIndex;
                        for (int k = 0; k < this.extractedMissedRepeatedPositionsIndexes.size(); k++) {
                            missedRepeatedPositionsIndex = this.extractedMissedRepeatedPositionsIndexes.get(k);
                            this.missedRepeatedPositions.removeElement(missedRepeatedPositionsIndex);
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
        data.log.add(
                POSITIONS_CLUSTERS,
                "    [cluster position] %s (char '%s') S-Order: %s U-Order: %s orderDiff: %s", 
                this.currentPosition, 
                this.data.variant.charAt(this.currentPosition),
                (this.currentPositionIndex - this.missed), 
                currentPositionUnsortedOrder, 
                orderDiff);
        
        if ( this.missedRepeatingsLog.isPresent() ) {
            data.log.add(POSITIONS_CLUSTERS,
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
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] cluster is consistent");
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

        if ( cluster.isRejected() || this.currentClusterIsRejected ) {
            this.currentClusterOrderDiffs.clear();
            this.clusters.acceptProcessed(cluster);
            this.currentClusterIsRejected = true;
        }
        else {
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
                    data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] cluster has %s shifts", cluster.ordersDiffShifts());
                    this.weight.add(shiftDeviation, CLUSTER_HAS_SHIFTS);
                } else {
                    boolean teardown = this.clusters.testOnTeardown(cluster);
                    if ( ! teardown ) {
                        if ( this.currentClusterLength == 2 ) {
                            // no reward
                        } else {
                            int consistencyReward = this.consistencyRewardDependingOnCurrentClusterLength();
                            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] cluster is consistent");
                            this.weight.add(-consistencyReward, CLUSTER_IS_CONSISTENT);
                        }
                    }
                }
            }

            this.previousClusterOrdersIsConsistent = ! cluster.hasOrdersDiff();

            this.currentClusterOrderDiffs.clear();
            this.clusters.acceptProcessed(cluster);
        }
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
                    if ( data.log.isEnabled(POSITIONS_CLUSTERS) ) {
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
            for (int patternPosition = 0; patternPosition < this.positions.size(); patternPosition++) {
                patternChar = this.data.patternChars.i(patternPosition);
                if ( patternChar == variantChar ) {
                    data.log.add(POSITIONS_CLUSTERS, "      [?] duplicate char found '%s' : %s for clustered char '%s' : %s",
                            variantChar, variantPosition, variantChar, this.positions.i(patternPosition));
                    this.meaningful++;
                }                
            }
        }
    }
    
    void lookForSeparatedCharsPlacing() {
        char firstPatternChar = this.data.patternChars.i(0);
        char firstVariantChar = this.data.variant.charAt(0);
        if ( firstPatternChar == firstVariantChar ) {
            this.meaningful++;
        }
        
        char lastPatternChar = this.data.patternChars.i(this.data.pattern.length() - 1);
        char lastVariantChar = this.data.variant.charAt(this.data.variant.length() - 1);
        if ( lastPatternChar == lastVariantChar ) {
            this.meaningful++;
        }        
    }

    void setNextPosition(int i) {
        this.nextPosition = this.positions.i(i + 1);
    }

    boolean hasNextPosition(int i) {
        return i < this.positions.size() - 1;
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

    boolean isUniqueInVariant(int position) {
        String matchType = this.matchTypesByVariantPosition.get(position);
        return nonNull(matchType) && matchType.equals("UNIQUE");
    }

    private boolean nextDependentWordsHasFoundPositions(WordInVariant word) {
        WordsInVariant.WordsInRange dependentWords = this.data.wordsInVariant.allDependentAfterOrNull(word);
        if ( nonNull(dependentWords) ) {
            WordInVariant nextWord = dependentWords.get(0);
            if ( nextWord.intersections(this.filledPositions) > 2 ) {
                return true;
            }
            else if ( nextWord.length < 5 ) {
                if ( dependentWords.count() > 1 ) {
                    WordInVariant nextNextWord = dependentWords.get(1);
                    if ( nextNextWord.intersections(this.filledPositions) > 2 ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    void calculateImportance() {
        this.clustersImportance = clustersImportanceDependingOn(
                this.clustersQty, this.clustered + this.meaningful, this.nonClustered - this.meaningful);
        this.nonClusteredImportance = nonClusteredImportanceDependingOn(
                this.nonClustered, this.missed, this.data.patternChars.size());
        data.log.add(POSITIONS_CLUSTERS, "    [importance] clusters: %s", this.clustersImportance);
        data.log.add(POSITIONS_CLUSTERS, "    [importance] non-clustered: %s", this.nonClusteredImportance);
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
        return this.data.patternChars.i(0) == this.data.variant.charAt(this.currentPosition);
    }
    
    private boolean currentPositionCharIsDifferentFromFirstFoundPositionChar() {
        return 
                this.data.variant.charAt(this.currentPosition) != 
                this.data.variant.charAt(this.positions.i(0 + this.missed));
    }
    
    final void clearPositionsAnalyze() {
        this.positions.clear();
        this.missed = 0;
        this.clustersQty = 0;
        this.clustered = 0;
        this.meaningful = 0;
        this.nonClustered = 0;
        this.positionsPossibleToAddToClustered.clear();
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
        this.patternIndexesByVariantPosition.clear();
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
        this.garbagePatternPositions.clear();
        this.currentClusterIsRejected = false;
        this.currentClusterWordStartFound = false;
    }
    
    boolean previousCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {
        if ( this.currentPatternCharPositionInVariant <= 0 ) {
            return false;
        }
        this.previousCharInPattern = this.data.patternChars.i(currentPatternCharIndex - 1);
        this.previousCharInVariant = this.data.variant.charAt(this.currentPatternCharPositionInVariant - 1);
        
        return ( this.previousCharInPattern == this.previousCharInVariant );
    }
    
    boolean nextCharInVariantInClusterWithCurrentChar(int currentPatternCharIndex) {        
        if ( this.currentPatternCharPositionInVariant < 0 || 
             this.currentPatternCharPositionInVariant >= this.data.variant.length() - 1 ) {
            return false;
        }
        this.nextCharInPattern = this.data.patternChars.i(currentPatternCharIndex + 1);
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
        for (int i = 0; i < this.positions.size(); i++) {
            position = this.positions.i(i);
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
        this.positions.sort(STRAIGHT);
    }

    private static final int UNINITIALIZED = MIN_VALUE;

    private void processCluster(
            int patternLength,
            Cluster cluster,
            ListInt orders,
            int clusterFirstPosition,
            int clusterLength) {
        int mean = meanSmartIgnoringZeros(orders);
        if ( data.log.isEnabled(POSITIONS_CLUSTERS) ) {
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diffs         %s",
                    orders.join(" "));
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diffs mean    %s", mean);
        }

        int limit = orders.size() - 1;

        int previous = UNINITIALIZED;
        int current;
        int next;

        int lastBeforeRepeat = UNINITIALIZED;
        int repeat = 0;
        int repeatQty = 0;
        int shifts = 0;
        int compensationSum = 0;
        boolean haveCompensation = false;
        boolean haveCompensationInCurrentStep = false;
        boolean previousIsRepeat = false;
        boolean currentEqualsNext = false;
        int repeatAbsDiffSum;

        boolean isLastPair;

        boolean isBad = false;

        // initial analize of first element
        int firstOrder = orders.get(0);
        int diffSumAbs = absDiff(firstOrder, mean);
        int diffSumReal = firstOrder;
        int diffCount = 0;

        for (int i = 0; i < limit; i++) {
            isLastPair = ( i + 1 == limit );

            current = orders.get(i);
            next = orders.get(i + 1);

            currentEqualsNext = current == next;

            if ( currentEqualsNext ) {

            }

            diffSumAbs = diffSumAbs + absDiff(next, mean);
            diffSumReal = diffSumReal + next;
            if ( next != mean ) {
                diffCount++;
            }

            if ( currentEqualsNext && ! isLastPair ) {
                previousIsRepeat = true;

                repeat = current;
                if ( repeatQty == 0 ) {
                    repeatQty = 2;
                } else {
                    repeatQty++;
                }

            } else {

                if ( currentEqualsNext && isLastPair ) {
                    previousIsRepeat = true;

                    repeat = current;
                    if ( repeatQty == 0 ) {
                        repeatQty = 2;
                    } else {
                        repeatQty++;
                    }
                }

                if ( absDiff(current, next) == 2 ) {
                    if ( absDiff(current, mean) == 1 ) {
                        data.log.add(POSITIONS_CLUSTERS, "              [order-diff] mutual +1-1 compensation for %s_vs_%s", current, next);
                        haveCompensation = true;
                        compensationSum = compensationSum + 2;
                        haveCompensationInCurrentStep = true;
                        diffSumAbs = diffSumAbs - 2;
                        lastBeforeRepeat = UNINITIALIZED;
                        if ( clusterLength == 2 || clusterLength == 3 ) {
                            shifts = 2;
                            if ( clusterLength > 2 && firstOrder != mean ) {
                                WordsInVariant.WordsInRange wordsInRange = this.data.wordsInVariant.wordsOfRange(this.currentClusterFirstPosition, this.currentClusterLength);

                                if ( wordsInRange.areEmpty() ) {
                                    haveCompensation = false;
                                    haveCompensationInCurrentStep = false;
                                    isBad = true;
                                    data.log.add(POSITIONS_CLUSTERS, "              [order-diff] starts with shift, reject compensation!");
                                }
                                else {
                                    int intersections = wordsInRange.intersections(this.positions, this.currentClusterFirstPosition, this.currentClusterLength);
                                    if ( wordsInRange.hasStartIn(this.filledPositions) ) {
                                        if ( this.data.wordsInVariant.all.size() == 1 ) {
                                            if ( intersections < this.currentClusterLength ) {
                                                haveCompensation = false;
                                                haveCompensationInCurrentStep = false;
                                                isBad = true;
                                                data.log.add(POSITIONS_CLUSTERS, "              [order-diff] starts with shift, reject compensation!");
                                            }
                                        }
                                        else {

                                        }
                                    }
                                    else {
                                        if ( intersections <= this.currentClusterLength ) {
                                            haveCompensation = false;
                                            haveCompensationInCurrentStep = false;
                                            isBad = true;
                                            data.log.add(POSITIONS_CLUSTERS, "              [order-diff] starts with shift, reject compensation!");
                                        }
                                    }
                                }
                            }
                        }
                    } else if ( absDiff(previous, next) == 4 &&
                            absDiff(previous, current) == 2 &&
                            absDiff(current, mean) == 0 ) {
                        data.log.add(POSITIONS_CLUSTERS, "              [order-diff] mutual +2 0 -2 compensation for %s_vs_%s", previous, next);
                        if ( firstOrder != mean ) {
                            haveCompensation = false;
                            haveCompensationInCurrentStep = false;
                            isBad = true;
                            data.log.add(POSITIONS_CLUSTERS, "              [order-diff] starts with shift, reject compensation!");
                        }
                        else {
                            haveCompensation = true;
                            haveCompensationInCurrentStep = true;
                            diffSumAbs = diffSumAbs - 4;
                            if ( diffCount == 2 ) {
                                compensationSum = compensationSum + 2;
                            }
                            lastBeforeRepeat = UNINITIALIZED;
                            if ( clusterLength == 3 ) {
                                shifts = 3;
                            }
                        }
                    }
                }

                if ( previousIsRepeat ) {
                    if ( ! haveCompensationInCurrentStep ) {

                        repeatAbsDiffSum = absDiff(repeat * repeatQty, mean * repeatQty);

                        if ( repeatAbsDiffSum > 0 ) {
                            if ( lastBeforeRepeat != UNINITIALIZED && absDiff(lastBeforeRepeat, mean) == repeatAbsDiffSum ) {
                                data.log.add(POSITIONS_CLUSTERS, "              [order-diff] compensation for %s_vs_(%s * %s)", lastBeforeRepeat, repeat, repeatQty);
                                diffSumAbs = diffSumAbs - (repeatAbsDiffSum * 2);
                                shifts = shifts + repeatQty;
                                haveCompensation = true;
                                lastBeforeRepeat = UNINITIALIZED;
                            } else if ( absDiff(next, mean) == repeatAbsDiffSum ) {
                                data.log.add(POSITIONS_CLUSTERS, "              [order-diff] compensation for (%s * %s)_vs_%s", repeat, repeatQty, next);
                                diffSumAbs = diffSumAbs - (repeatAbsDiffSum * 2);
                                shifts = shifts + repeatQty;
                                haveCompensation = true;
                                lastBeforeRepeat = UNINITIALIZED;
                            }
                        }
                    }
                } else {
                    lastBeforeRepeat = current;
                }

                haveCompensationInCurrentStep = false;
                previousIsRepeat = false;
                if ( repeatQty != 0 ) {
                    cluster.repeats().add(repeat);
                    cluster.repeatQties().add(repeatQty);
                }
                repeat = 0;
                repeatQty = 0;
            }
            previous = current;
        }

//        if ( ! this.data.variantContainsPattern ) {
//            if ( ! isBad ) {
//                WordsInVariant.WordsInRange wordsInRange = this.data.wordsInVariant.wordsOfRange(this.currentClusterFirstPosition, this.currentClusterLength);
//
//                if ( wordsInRange.areEmpty() ) {
//                    haveCompensation = false;
//                    haveCompensationInCurrentStep = false;
//                    isBad = true;
//                    data.log.add(POSITIONS_CLUSTERS, "              [order-diff] there are no words for cluster!");
//                }
//                else {
//                    if ( wordsInRange.hasStartIn(this.filledPositions) ) {
//
//                    }
//                    else {
//                        haveCompensation = false;
//                        haveCompensationInCurrentStep = false;
//                        isBad = true;
//                        data.log.add(POSITIONS_CLUSTERS, "              [order-diff] word of cluster has not its start found, cluster is bad!");
//                    }
//                }
//            }
//        }

        if ( firstOrder != mean ) {
            diffCount++;

            boolean ignore = false;

            int repeatsCount = cluster.repeats().size();
            if ( repeatsCount == 2 ) {
                int repeatsDiff = absDiff(
                        cluster.repeats().get(0),
                        cluster.repeats().get(1));

                if ( repeatsDiff == 1 ) {
                    int repeat0Count = cluster.repeatQties().get(0);
                    int repeat1Count = cluster.repeatQties().get(1);
                    ignore =
                            repeat0Count > 1 &&
                            repeat1Count > 1;
                }
            }

            if ( ! ignore ) {
                WordsInVariant.WordsInRange words = this.data.wordsInVariant.wordsOfRange(clusterFirstPosition, clusterLength);
                if ( words.first().startIndex == clusterFirstPosition ) {
                    isBad = true;
                }
            }
        }

        if ( clusterLength == 2 ) {
            if ( diffCount == 2 && diffSumAbs > 2 ) {
                isBad = true;
            }
        }

        if ( data.log.isEnabled(POSITIONS_CLUSTERS) ) {
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order repeats       %s", cluster
                    .repeats()
                    .stream()
                    .mapToObj(repeating -> String.valueOf(repeating))
                    .collect(joining(",", "<", ">")));
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order repeats qties %s", cluster
                    .repeatQties()
                    .stream()
                    .mapToObj(repeating -> String.valueOf(repeating))
                    .collect(joining(",", "<", ">")));
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum real %s", diffSumReal);
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum abs  %s", diffSumAbs);
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diff count    %s", diffCount);
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diff compensation  %s", compensationSum);
        }
        if ( diffSumAbs == 0 && haveCompensation && clusterLength == 2 ) {
            diffSumAbs = 1;
            data.log.add(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum fix  %s", diffSumAbs);
        }

        if ( ! isBad && this.currentClusterIsRejected ) {
            isBad = true;
        }
        cluster.set(
                clusterFirstPosition,
                patternLength,
                clusterLength,
                mean,
                diffSumReal,
                diffSumAbs,
                diffCount,
                shifts,
                haveCompensation,
                compensationSum,
                isBad);

        cluster.finish();
    }

    private void log(ClusterStepTwo cluster, String header) {
        if ( data.log.isEnabled(POSITIONS_SEARCH) ) {
            data.log.add(POSITIONS_SEARCH, "          [%s]", header);
            WordInVariant word = cluster.word();
            data.log.add(POSITIONS_SEARCH, "             [main] '%s' variant:%s, pattern:%s, included:%s", cluster.assessedChar(), cluster.assessedCharVariantPosition(), cluster.assessedCharPatternPosition(), cluster.isAssessedCharFilledInVariant());
            var view = cluster.positionView();
            while ( view.hasNext() ) {
                view.goToNext();
                data.log.add(POSITIONS_SEARCH, "             [clustered] '%s' variant:%s, pattern:%s, included(variant:%s, pattern:%s), match:%s",
                        view.character(),
                        view.variantPosition(),
                        view.patternPosition(),
                        view.isFilled(),
                        view.isFilledInPattern(),
                        view.match());
            }
            data.log.add(POSITIONS_SEARCH, "             [word] %s [%s...%s]", word.charsString(), word.startIndex, word.endIndex);
            data.log.add(POSITIONS_SEARCH, "             [type] %s", cluster.positionsInWordType());
        }
    }
    
}
