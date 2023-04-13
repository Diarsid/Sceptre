package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.StatefulClearable;
import diarsid.support.objects.references.Possible;

import static java.lang.Math.negateExact;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import static diarsid.sceptre.impl.AnalyzeImpl.logAnalyze;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.mean;
import static diarsid.support.misc.MathFunctions.percentAsFloat;
import static diarsid.support.misc.MathFunctions.percentAsFloatOf;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.percentAsIntOf;
import static diarsid.support.objects.collections.CollectionUtils.containsAnyCommonElement;
import static diarsid.support.objects.collections.CollectionUtils.isNotEmpty;
import static diarsid.support.objects.collections.Lists.lastFrom;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

class Clusters implements StatefulClearable {
    
    private static final int UNKNOWN_VALUE;
    private static final int MAX_PLACING_BONUS;
    
    static {
        UNKNOWN_VALUE = -9;
        MAX_PLACING_BONUS = 20;
    }
    
    private final GuardedPool<Cluster> clusterPool;
    private int rejectedTeardownSum;
    private final List<Cluster> clustersMarkedTeardown;
    private final List<Cluster> clustersMarkedTeardownRejected;
    
    /* Clusters variables block */
    private final List<Cluster> clusters;
    private final List<Cluster> clustersTakenFromPool;
    private final Possible<Cluster> lastAdded;
    private final AnalyzeUnit data;
    private boolean arranged;
    private int clustersTotalLength;
    private int distanceBetweenClusters;
    private boolean allClustersSeparatedByOneChar;

    /* Placing variables block */
    private final Possible<String> placingCase;
    private final Possible<String> placingBonusNotApplicableReason;
    private int clustersPercentInVariant;
    private int meanPosition = UNKNOWN_VALUE;
    private int adjustedVariantLength = UNKNOWN_VALUE;
    private float placingPercent = UNKNOWN_VALUE;
    private float clustersPlacingImportance = UNKNOWN_VALUE;
    private float distanceBetweenClustersImportance = UNKNOWN_VALUE;
    private float placingBonus;
    private float placingBonusLimit;

    private final List<Cluster> chosenByWord;

    Clusters(
            AnalyzeUnit analyzeData, 
            GuardedPool<Cluster> clusterPool) {
        this.clusterPool = clusterPool;
        
        this.clustersMarkedTeardown = new ArrayList<>();
        this.clustersMarkedTeardownRejected = new ArrayList<>();
        
        this.data = analyzeData;
        this.clusters = new ArrayList<>();
        this.clustersTakenFromPool = new ArrayList<>();
        this.lastAdded = simplePossibleButEmpty();
        this.arranged = false;
        this.clustersTotalLength = 0;
        this.distanceBetweenClusters = 0;
        this.allClustersSeparatedByOneChar = false;        
        
        this.clustersPercentInVariant = UNKNOWN_VALUE;        
        this.meanPosition = UNKNOWN_VALUE;
        this.adjustedVariantLength = UNKNOWN_VALUE;
        this.placingPercent = UNKNOWN_VALUE;
        this.clustersPlacingImportance = UNKNOWN_VALUE;
        this.distanceBetweenClustersImportance = UNKNOWN_VALUE;
        this.placingBonus = UNKNOWN_VALUE;
        this.placingBonusLimit = UNKNOWN_VALUE;
        this.placingCase = simplePossibleButEmpty();
        this.placingBonusNotApplicableReason = simplePossibleButEmpty();

        this.chosenByWord = new ArrayList<>();
    }
    
    Cluster getUnprocessed() {
        Cluster cluster = this.clusterPool.give();
        this.clustersTakenFromPool.add(cluster);
        return cluster;
    }
    
    void acceptProcessed(Cluster cluster) {
        if ( this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to add next cluster after arrengement!");
        }

        if ( cluster.isRejected() ) {
            this.clustersTakenFromPool.remove(cluster);
            this.clusterPool.takeBack(cluster);
        }
        else {
            this.lastAdded.resetTo(cluster);
            this.clusters.add(cluster);
        }
    }
    
    List<Cluster> all() {
        return this.clusters;
    }
    
    int distanceBetweenClusters() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get distance between clusters before arrangement!");
        }
        
        if ( this.isEmpty() ) {
            return 0;
        } 
        
        return this.distanceBetweenClusters;
    }

    void chooseAllOf(WordInVariant word) {
        this.chosenByWord.clear();

        for ( Cluster cluster : this.clusters ) {
            if ( cluster.intersectsWith(word) ) {
                this.chosenByWord.add(cluster);
            }
        }
    }

    List<Cluster> chosenInWord() {
        return this.chosenByWord;
    }
    
    int totalLength() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get total length before arrangement!");
        }    
        
        return this.clustersTotalLength;
    }
    
    private int findMeanPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().positionsMean();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.positionsMean())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    private int findMeanFirstPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().firstPosition();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.firstPosition())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    private int findMeanLastPosition() {
        if ( this.isEmpty() ) {
            return 0;
        }
        
        if ( this.quantity() == 1 ) {
            return this.lastAdded.orThrow().lastPosition();
        }
        
        int middlePositionsSum = this.clusters
                .stream()
                .mapToInt(cluster -> cluster.lastPosition())
                .sum();
        
        return middlePositionsSum / this.clusters.size();
    }
    
    Cluster lastAddedCluster() {
        return this.lastAdded.orThrow();
    }
    
    Cluster lastCluster() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to get last cluster before arrangement!");
        }
        
        return lastFrom(this.clusters);
    }
    
    Cluster firstCluster() {
        return this.clusters.get(0);
    }
    
    Cluster firstClusterAfter(int position) {
        if ( this.lastCluster().firstPosition() < position ) {
            return null;
        }
        
        Cluster previousCluster = lastFrom(this.clusters);
        Cluster visitedCluster;
        for (int i = this.clusters.size() - 1; i > -1; i--) {
            visitedCluster = this.clusters.get(i);
            if ( visitedCluster.firstPosition() == position ) {
                return visitedCluster;
            } else if ( visitedCluster.firstPosition() > position ) {
                previousCluster = visitedCluster;
            } else {
                break;
            }
        }
        return previousCluster;
    }
    
    boolean isEmpty() {
        return this.clusters.isEmpty();
    }
    
    boolean nonEmpty() {
        return ! this.isEmpty();
    }
    
    int quantity() {
        return this.clusters.size();
    }
    
    void arrange() {
        if ( this.arranged ) {
            return;
        }
        
        Collections.sort(this.clusters);
        this.arranged = true;
        
        this.loopThroughClustersAndCollectData();
    }
    
    boolean testOnTeardown(Cluster cluster) {
        boolean teardown = cluster.testOnTeardown();
        if ( teardown ) {
            this.clustersMarkedTeardown.add(cluster);
        }
        return teardown;
    }
    
    int lookupForTearDowns() {
        for (Cluster cluster : this.clustersMarkedTeardown) {
            Cluster exchangeCluster = this.clustersMarkedTeardown
                    .stream()
                    .filter(otherCluster -> {
                        return 
                                otherCluster != cluster &&
                                otherCluster.ordersDiffSumReal() == negateExact(cluster.ordersDiffSumReal()) &&
                                containsAnyCommonElement(
                                        otherCluster.repeatQties(),
                                        cluster.repeatQties());
                    })
                    .findFirst()
                    .orElse(null);
            
            if ( nonNull(exchangeCluster) ) {
                this.clustersMarkedTeardownRejected.add(cluster);
                this.clustersMarkedTeardownRejected.add(exchangeCluster);
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "               [TEARDOWN REJECTION] clusters %s <---> %s has mutualy compensated subclusters", cluster, exchangeCluster);
            }                    
        }
        
        if ( isNotEmpty(this.clustersMarkedTeardownRejected) ) {
            this.clustersMarkedTeardown.removeAll(this.clustersMarkedTeardownRejected);
        }
        
        if ( isNotEmpty(this.clustersMarkedTeardown) ) {
            return this.clustersMarkedTeardown
                    .stream()
                    .mapToInt(cluster -> cluster.teardown())
                    .sum();
        } else {
            return 0;
        }
    }
    
    private void loopThroughClustersAndCollectData() {
        if ( this.quantity() < 2 ) {
            this.distanceBetweenClusters = 0;
            this.allClustersSeparatedByOneChar = false;
            if ( this.nonEmpty() ) {
                this.clustersTotalLength = this.lastAdded.orThrow().length();
            }           
            return;
        }
        
        this.allClustersSeparatedByOneChar = true;
        
        Cluster currentCluster = this.clusters.get(0);
        int prevClusterEnd = currentCluster.lastPosition();
        int totalLength = currentCluster.length();
        int nextClusterStart;
        int distanceBetweenTwoClusters;        
        
        for (int i = 1; i < this.clusters.size(); i++) {
            currentCluster = this.clusters.get(i);    
            totalLength += currentCluster.length();
            nextClusterStart = currentCluster.firstPosition();
            distanceBetweenTwoClusters = absDiff(prevClusterEnd, nextClusterStart) - 1;
            
            if ( distanceBetweenTwoClusters != 1 ) {
                this.allClustersSeparatedByOneChar = false;
            }
            this.distanceBetweenClusters += distanceBetweenTwoClusters;
            
            prevClusterEnd = currentCluster.lastPosition();
        }
        
        this.clustersTotalLength = totalLength;
    }
    
    boolean areAllClustersSeparatedByOneChar() {
        if ( ! this.arranged ) {
            throw new IllegalStateException(
                    "It is forbidden to query if all clusters " +
                    "separated by one char before arrangement!");
        }
        
        return this.allClustersSeparatedByOneChar;
    }
    
    float calculatePlacingBonus() {        
        this.clustersPercentInVariant = percentAsInt(this.totalLength(), this.data.variant.length()); 
        
        if ( this.data.variantPathSeparators.isEmpty() ) {
            if ( this.quantity() == 1 ) {
                if ( this.isFirstClusterAtVariantStart() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.bestPlacing();
                    } else {
                        this.placingBonusOf(97, "single cluster at variant start, no separators");
                    }
                } else if ( this.isLastClusterAtVariantEnd() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.placingBonusOf(70, "single cluster at variant end, no separators, majority");
                    } else {
                        this.placingBonusOf(60, "single cluster at variant end, no separators");
                    }
                } else {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToStartIsBetter();
                    } else {
                        this.singleClosierToStartIsBetter();
                    }                    
                }
            } else {
                if ( this.areAllClustersSeparatedByOneChar() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.placingBonusOf(90, "all clusters-as-one at variant start, forming majority, no separators");
                        } else {
                            this.manyAsOneMajorClosierToStartAreBetter();
                        }
                    } else {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.placingBonusOf(85, "all clusters-as-one at variant start, no separators");
                        } else {
                            this.manyAsOneClosierToStartAreBetter();
                        }
                    }                    
                } else {
                    if ( this.isClusteredPartFormingMajority() ) {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.manyMajorClosierToStartAreBetterFirstAtStart();
                        } else {
                            this.manyMajorClosierToStartAreBetter();
                        }
                    } else {
                        if ( this.isFirstClusterAtVariantStart() ) {
                            this.manyClosierToStartAreBetterFirstAtStart();
                        } else {
                            this.manyClosierToStartAreBetter();
                        }
                    }                    
                }                
            }
        } else {
            /* variant contains path separators */
            if ( this.quantity() == 1 ) {
                if ( this.isLastClusterStartsWithSeparator() ) {
                    this.placingBonusOf(95, "single cluster begins at last path element");
                } else if ( this.isLastClusterAtVariantEnd() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.placingBonusOf(90, "single cluster at variant end, forming majority, with separators");
                    } else {
                        this.placingBonusOf(85, "single cluster at variant end, with separators");
                    }
                } else if ( this.isSingleClusterAfterLastPathSeparator() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        /* BAD LOGIC */ // this.singleMajorClosierToLastSeparatorIsBetterAfterLastSeparator();
                        this.placingBonusOf(90, "\"single cluster, forming majority, with separators, after last separator\"");
                    } else {
                        /* BAD LOGIC */ // this.singleClosierToLastSeparatorIsBetterAfterLastSeparator();
                        this.placingBonusOf(85, "single cluster, with separators, after last separator");
                    }
                } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToStartIsBetterBeforeFirstPathSeparator();
                    } else {
                        this.singleClosierToStartIsBetterBeforeFirstPathSeparator();
                    }
                    if ( ! this.isFirstClusterStartsWithSeparator() ) {
                        this.applyCasePenaltyToBonus(60); 
                    }      
                } else {
                    this.placingBonusNoMoreThanPercent(60);
                    if ( this.isClusteredPartFormingMajority() ) {
                        this.singleMajorClosierToEndIsBetter();
                    } else {
                        this.singleClosierToEndIsBetter();
                    }
                }
            } else {
                /* many clusters */
                if ( this.areAllClustersSeparatedByOneChar() ) {
                    if ( this.isLastClusterAtVariantEnd() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.placingBonusOf(75, "all clusters-as-one at variant end, forming majority, with separators");
                            this.applyPathComplexityPenaltyToBonus();
                        } else {
                            this.placingBonusOf(70, "all clusters-as-one at variant end, with separators");
                            this.applyPathComplexityPenaltyToBonus();
                        }
                    } else if ( this.areAllClustersAfterLastPathSeparator() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            if ( this.isFirstClusterStartsWithSeparator() ) {
                                this.placingBonusOf(95, "all clusters-as-one forming majority, starts with last path element");
                                this.applyPathComplexityPenaltyToBonus();
                            } else {
                                this.manyAsOneMajorClosierToEndAreBetterAfterLastPathSeparator();
                            }
                        } else {
                            if ( this.isFirstClusterStartsWithSeparator() ) {                                
                                this.placingBonusOf(90, "all clusters-as-one, starts with last path element");
                                this.applyPathComplexityPenaltyToBonus();
                            } else {
                                this.manyAsOneClosierToEndAreBetterAfterLastPathSeparator();
                            }
                        }
                    } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(60);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyAsOneMajorClosierToStartAreBetterBeforeFirstPathSeparator();
                        } else {
                            this.manyAsOneClosierToStartAreBetterBeforeFirstPathSeparator();
                        }
                        if ( ! this.isFirstClusterStartsWithSeparator() ) {
                            this.applyCasePenaltyToBonus(60); 
                        }                         
                    } else {
                        this.placingBonusNoMoreThanPercent(75);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyAsOneMajorClosierToEndAreBetter();
                        } else {
                            this.manyAsOneClosierToEndAreBetter();
                        }
                    }
                } else {
                    /* no many-as-one clusters */
                    if ( this.isEveryClusterMeansEveryPathElement() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                           this.placingBonusOf(60, "every cluster means every path element, majority");
                        } else {
                           this.placingBonusOf(50, "every cluster means every path element");
                        }
                    } else if ( this.areClustersAtFirstAndLastPathElement() ) {
                        if ( this.isFirstClusterStartsWithSeparator() ) {
                            if ( this.isLastClusterStartsWithSeparator() ) {
                                this.placingBonusOf(80, "two clusters at start and end");
                            } else {
                                this.placingBonusNoMoreThanPercent(80);
                                this.clusterCloserToLastSeparatorIsBetter();
                            }
                        } else if ( this.isLastClusterStartsWithSeparator() ) {
                            this.placingBonusNoMoreThanPercent(80);
                            this.firstClusterCloserToStartIsBetter();
                        } else {
                            this.placingBonusNoMoreThanPercent(75);
                            this.firstAndLastClustersCloserToStartAreBetter();
                            this.applyCasePenaltyToBonus(90);    
                        }
                    } else if ( this.areAllClustersAfterLastPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(80);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToEndAreBetterAfterLastPathSeparator();
                        } else {
                            this.placingBonusNoMoreThanPercent(75);
                            this.manyClosierToEndAreBetterAfterLastPathSeparator();
                        }
                    } else if ( this.isLastClusterAtVariantEnd() ) {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToEndAreBetterLastAtEnd();
                        } else {
                            this.manyClosierToEndAreBetterLastAtEnd();
                        }
                        this.applyCasePenaltyToBonus(85);  
                    } else if ( this.areAllClustersBeforeFirstPathSeparator() ) {
                        this.placingBonusNoMoreThanPercent(50);
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.manyMajorClosierToStartAreBetterBeforeFirstPathSeparator();
                        } else {
                            this.manyClosierToStartAreBetterBeforeFirstPathSeparator();
                        }
                        if ( ! this.isFirstClusterStartsWithSeparator() ) {
                            this.applyCasePenaltyToBonus(60); 
                        } 
                    } else {
                        if ( this.isClusteredPartFormingMajority() ) {
                            this.placingBonusNoMoreThanPercent(65);
                            this.manyMajorClosierToEndAreBetter();
                        } else {
                            this.placingBonusNoMoreThanPercent(50);
                            this.manyClosierToEndAreBetter();
                        }
                        this.applyCasePenaltyToBonus(55);  
                    }
                }
            } 
        } 
        
        this.placingBonusMustBePresent();
        this.applyBonusHigherLimitationIfSpecified();
        this.subtractMissedPositionsFromBonus();
        this.subtractSeparatorsBetweenClusters();
        this.logState();        
        
        return this.placingBonus;
    }

    private void placingBonusMustBePresent() throws IllegalStateException {
        if ( this.placingBonus == UNKNOWN_VALUE ) {
            throw new IllegalStateException("Placing bonus have not been calculated!");
        }
    }
    
    private void applyBonusHigherLimitationIfSpecified() {
        if ( this.placingBonusLimit != UNKNOWN_VALUE ) {
            if ( this.placingBonus > this.placingBonusLimit ) {
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] WARN!!! placing bonus limit applied : placing bonus is %s but limit is %s", this.placingBonus, this.placingBonusLimit);
                this.placingBonus = this.placingBonusLimit;
            }
        }
    }
    
    private void subtractMissedPositionsFromBonus() {
        PositionsAnalyze positionsData = this.data.positions();
        if ( positionsData.missed == 0 ) {
            return;
        }
        
        float presentPercent = percentAsFloat(this.data.patternChars.length - positionsData.missed, this.data.patternChars.length);
        
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] missed position placing penalty : *%s%%", presentPercent);
        this.placingBonus = percentAsFloatOf(this.placingBonus, presentPercent);
        
        if ( this.placingBonus < 0 ) {
            this.placingBonus = 0;
        }
    }
    
    private void subtractSeparatorsBetweenClusters() {
        if ( this.isEmpty() ) {
            return;
        }
        
        if ( this.quantity() == 1 ) {
            return;
        }
        
        int separatorsBetweenClustersPenalty = this.data.positions().separatorsBetweenClusters - (this.quantity() - 1);
        
        if ( separatorsBetweenClustersPenalty <= 0 ) {
            return;
        }
        
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing bonus separators penalty : -%s", separatorsBetweenClustersPenalty);
        this.placingBonus = this.placingBonus - separatorsBetweenClustersPenalty;
        
        if ( this.placingBonus < 0 ) {
            this.placingBonus = 0;
        }
    }
    
    private void placingBonusNoMoreThanPercent(int percent) {
        this.placingBonusLimit = (MAX_PLACING_BONUS * (float) percent) / 100;
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing bonus limit : %s (%s%%)", this.placingBonusLimit, percent);
    }

    private void placingPercentNoHigherThan(int limit) {
        if ( this.placingPercent > limit ) {
            this.placingPercent = limit;
        }
    }
    
    private void clustersPlacingImportancePercentNoHigherThan(int limit) {
        if ( this.clustersPlacingImportance > limit ) {
            this.clustersPlacingImportance = limit;
        }
    }
    
    private void clustersPlacingImportancePercentNoLowerThan(int limit) {
        if ( this.clustersPlacingImportance < limit ) {
            this.clustersPlacingImportance = limit;
        }
    }
    
    private boolean isClusteredPartFormingMajority() {
        return this.clustersPercentInVariant >= 66;
    }
    
    private boolean isFirstClusterAtVariantStart() {
        return this.firstCluster().firstPosition() == 0;
    }
    
    private boolean isSingleClusterAfterLastPathSeparator() {
        return this.quantity() == 1 && 
               this.firstCluster().firstPosition() > this.data.variantPathSeparators.last();
    }
    
    private boolean areAllClustersAfterLastPathSeparator() {
        return this.firstCluster().firstPosition() > this.data.variantPathSeparators.last();
    }
    
    private boolean areAllClustersBeforeFirstPathSeparator() {
        return this.lastCluster().lastPosition() < this.data.variantPathSeparators.first();
    }
    
    private boolean areClustersAtFirstAndLastPathElement() {
        return 
                this.firstCluster().lastPosition() < this.data.variantPathSeparators.first() &&
                this.lastCluster().firstPosition() > this.data.variantPathSeparators.last();
    }
    
    private boolean isFirstClusterStartsWithSeparator() {
        int firstClusterFirstPosition = this.firstCluster().firstPosition();
        return 
                firstClusterFirstPosition == 0 ||
                this.data.variantPathSeparators.contains(firstClusterFirstPosition - 1);
    }
    
    private boolean isLastClusterStartsWithSeparator() {
        return this.lastCluster().firstPosition() == this.data.variantPathSeparators.last() + 1;
    }
    
    private boolean isEveryClusterMeansEveryPathElement() {
        if ( this.data.variantPathSeparators.isEmpty() ) {
            return false;
        }
        if ( this.quantity() < 2 ) {
            return false;
        }
        if ( this.quantity() != this.data.variantPathSeparators.size() + 1 ) {
            return false;
        }
        
        TreeSet<Integer> pathSeparators = this.data.variantPathSeparators;
        Integer nextSeparatorPosition = pathSeparators.first();
        
        Cluster clusterPrev;
        Cluster clusterNext;
        boolean clustersSeparated;
        
        for (int i = 0; i < this.clusters.size() - 1; i++) {
            clusterPrev = this.clusters.get(i);
            clusterNext = this.clusters.get(i + 1);
            
            clustersSeparated = 
                    clusterPrev.lastPosition() < nextSeparatorPosition &&
                    clusterNext.firstPosition() > nextSeparatorPosition;
            
            if ( clustersSeparated ) {
                nextSeparatorPosition = pathSeparators.higher(nextSeparatorPosition);
                if ( isNull(nextSeparatorPosition) ) {
                    return this.lastCluster().equals(clusterNext);
                } 
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isPenultimate(Cluster cluster) {
        switch ( this.clusters.size() ) {
            case 0:
                return false;
            case 1:
                return false;
            case 2: 
                return this.clusters.indexOf(cluster) == 0;
            case 3: 
                return this.clusters.indexOf(cluster) == 1;
            default:
                int index = this.clusters.indexOf(cluster);
                return index == this.clusters.size() - 2;
        }        
    }
    
    private void bestPlacing() {
        this.placingCase.resetTo("single cluster at variant start, forming majority, no separators");
        this.placingBonus = MAX_PLACING_BONUS;
    }
    
    private void placingBonusOf(int percent, String description) {
        this.placingCase.resetTo(description);
        this.placingBonus = ( MAX_PLACING_BONUS * (float) percent ) / 100f;
    }
    
    private void clusterCloserToLastSeparatorIsBetter() {
        this.placingCase.resetTo("first cluster starts with variant, last cluster in last path element");
        int lastPathSeparator = this.data.variantPathSeparators.last();
        this.meanPosition = this.firstClusterAfter(lastPathSeparator).firstPosition();
        this.adjustMeanPositionAfterLastPathSeparator(lastPathSeparator);
        this.placingPercent = 100f - percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void firstClusterCloserToStartIsBetter() {
        this.placingCase.resetTo("first cluster in first path element, last cluster starts with last path element");
        this.meanPosition = this.firstCluster().firstPosition();
        this.adjustVariantLengthBeforeFirstPathSeparator();
        this.placingPercent = 100f - percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void firstAndLastClustersCloserToStartAreBetter() {
        this.placingCase.resetTo("first cluster in first path element, last cluster in last path element");
        
        int lastPathSeparator = this.data.variantPathSeparators.last();
        int lastClusterAdjustedMeanPosition = this.lastCluster().firstPosition() - lastPathSeparator;
        int lastPathElementLength = this.data.variant.length() - 1 - lastPathSeparator;
        float placingPercentInLastPathElement = 100f - percentAsFloat(lastClusterAdjustedMeanPosition, lastPathElementLength);
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        int firstClusterMeanPosition = this.firstCluster().firstPosition();
        int firstPathElementLength = firstPathSeparator;
        float placingPercentInFirstPathElement = 100f - percentAsFloat(firstClusterMeanPosition, firstPathElementLength);
        
        this.placingPercent = mean(placingPercentInLastPathElement, placingPercentInFirstPathElement);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleClosierToStartIsBetter() {
        this.placingCase.resetTo("single cluster, no separators");
        this.meanPosition = this.findMeanPosition();
        this.placingPercent = 100f - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(90);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleMajorClosierToStartIsBetter() {
        this.placingCase.resetTo("single cluster, forming majority, no separators");
        this.meanPosition = this.firstCluster().firstPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleClosierToEndIsBetter() {
        this.placingCase.resetTo("single cluster, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        
        this.clustersPlacingImportance = 100f - this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(80);
        this.clustersPlacingImportancePercentNoHigherThan(75);
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.clustersPlacingImportance )
                / 100f;
    }
    
    private void singleMajorClosierToEndIsBetter() {
        this.placingCase.resetTo("single cluster, forming majority, with separators");
        this.meanPosition = this.lastCluster().lastPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);        
        this.applyKeyCharsBonusToPlacingPercent();
        
        this.clustersPlacingImportance = this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(85);
        this.clustersPlacingImportancePercentNoHigherThan(80);
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.clustersPlacingImportance )
                / 100f;
    }
    
    private void singleClosierToLastSeparatorIsBetterAfterLastSeparator() {
        this.placingCase.resetTo("single cluster, with separators, after last separator");
        this.meanPosition = this.lastCluster().positionsMean();
        
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void singleMajorClosierToLastSeparatorIsBetterAfterLastSeparator() {
        this.placingCase.resetTo("single cluster, forming majority, with separators, after last separator");
        this.meanPosition = this.lastCluster().firstPosition();
        
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100f;
    }
    
    private void manyClosierToStartAreBetterFirstAtStart() {
        this.placingCase.resetTo("many clusters, no separators, first at start");
        this.meanPosition = this.findMeanPosition() / 2;
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(87);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyMajorClosierToStartAreBetterFirstAtStart() {
        this.placingCase.resetTo("many clusters, forming majority, no separators");
        this.meanPosition = this.findMeanFirstPosition() / 2;
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(95);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyClosierToStartAreBetter() {
        this.placingCase.resetTo("many clusters, no separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        this.placingPercentNoHigherThan(75);
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent, 
                        this.clustersPlacingImportance, 
                        this.distanceBetweenClustersImportance )
                / 100;        
    }
    
    private void manyMajorClosierToStartAreBetter() {
        this.placingCase.resetTo("many clusters, forming majority, no separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        this.placingPercentNoHigherThan(80);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(this.placingPercent, this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void manyAsOneClosierToStartAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, no separators");
        this.meanPosition = this.findMeanPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(85);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToStartAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, forming majority, no separators");
        this.meanPosition = this.findMeanFirstPosition();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneClosierToEndAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, with separators");
        this.meanPosition = this.findMeanLastPosition();
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(85);
        this.applyPathComplexityPenaltyToPlacingPercent();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToEndAreBetter() {
        this.placingCase.resetTo("many-as-one clusters, forming majority, with separators");
        this.meanPosition = this.lastCluster().lastPosition();
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(93);
        this.applyPathComplexityPenaltyToPlacingPercent();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many-as-one clusters, with separators, after last separator");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyAsOneMajorClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many-as-one major clusters, with separators, after last separator");
        this.meanPosition = this.lastCluster().lastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void applyAfterLastPathSeparatorPlacingPercentBonus() {
        int separatorsBonus = 10 + ( this.data.variantPathSeparators.size() * 10 );
        this.placingPercent = this.placingPercent + separatorsBonus;
    }
    
    private void applyPathComplexityPenaltyToBonus() {
        int pathComplexity = calculatePathComplexity();
        
        if ( pathComplexity <= 0 ) {
            return;
        }
        
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] path complexity bonus penalty : -%s%%", pathComplexity * 10);
        this.placingBonus = percentAsFloatOf(this.placingBonus, 100 - (pathComplexity * 10));
        this.placingBonus = placingBonus - pathComplexity;
        
        if ( this.placingBonus <= 0 ) {
            this.placingBonus = 0;
        }
    }
    
    private void applyPathComplexityPenaltyToPlacingPercent() {
        int pathComplexity = calculatePathComplexity();
        
        if ( pathComplexity <= 0 ) {
            return;
        }
        
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] path complexity placing penalty : -%s%%", pathComplexity * 10);
        
        this.placingPercent = this.placingPercent - (pathComplexity * 10);
        if ( this.placingPercent <= 0 ) {
            this.placingPercent = 0;
        }
    }

    private int calculatePathComplexity() {
        int pathComplexity = this.data.variantPathSeparators.size()
                             - this.data.positions().keyChars.size();
        return pathComplexity;
    }
    
    private void applyCasePenaltyToBonus(int penaltyPercent) {
        this.placingBonus = percentAsFloatOf(this.placingBonus, penaltyPercent);
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] case-dependent bonus penalty : *%s%%", penaltyPercent);
    }
    
    private void adjustMeanPositionAfterLastPathSeparator() {
        int lastPathSeparator = this.data.variantPathSeparators.last();
        this.meanPosition = this.meanPosition - lastPathSeparator;
        this.adjustedVariantLength = this.data.variant.length() - 1 - lastPathSeparator;
    }
    
    private void adjustMeanPositionAfterLastPathSeparator(int lastPathSeparator) {        
        this.meanPosition = this.meanPosition - lastPathSeparator;
        this.adjustedVariantLength = this.data.variant.length() - 1 - lastPathSeparator;
    }
    
    private void adjustVariantLengthBeforeFirstPathSeparator() {
        this.adjustedVariantLength = this.data.variant.length() - 1 - this.data.variantPathSeparators.first();
    }
    
    private void manyClosierToEndAreBetterLastAtEnd() {
        this.placingCase.resetTo("many clusters, with separators, last at end");
        this.meanPosition = this.findMeanPosition() / 2;
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(87);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyMajorClosierToEndAreBetterLastAtEnd() {
        this.placingCase.resetTo("many clusters, forming majority, with separators, last at end");
        this.meanPosition = this.findMeanLastPosition() / 2;
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        
        this.placingPercentNoHigherThan(95);
        
        this.placingBonus = ( MAX_PLACING_BONUS
                * this.placingPercent )
                / 100;
    }
    
    private void manyClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many clusters, with separators, after last separator");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.clustersPlacingImportance,
                        this.distanceBetweenClustersImportance)
                / 100;   
    }
    
    private void manyMajorClosierToEndAreBetterAfterLastPathSeparator() {
        this.placingCase.resetTo("many clusters, forming majority, with separators");
        this.meanPosition = this.findMeanLastPosition();
        this.adjustMeanPositionAfterLastPathSeparator();
        this.placingPercent = percentAsFloat(this.meanPosition, this.adjustedVariantLength);
        this.applyAfterLastPathSeparatorPlacingPercentBonus();
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void manyClosierToEndAreBetter() {
        this.placingCase.resetTo("many clusters, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.placingPercentNoHigherThan(70);
        this.applyPathComplexityPenaltyToPlacingPercent();
        
        this.clustersPlacingImportance = 100 - this.clustersPercentInVariant;
        this.applyKeyCharsBonusToPlacingImportance();
        this.clustersPlacingImportancePercentNoLowerThan(30);
        
        this.calculateDistanceBetweenClustersImportance();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.clustersPlacingImportance)
                / 100;   
        
        this.placingBonus = percentAsFloatOf(this.placingBonus, this.distanceBetweenClustersImportance);
    }

    private void calculateDistanceBetweenClustersImportance() {
        float distanceToVariantLengthPercent = 100 - percentAsFloat(this.distanceBetweenClusters, this.data.variant.length() - this.totalLength());
        int distanceToTotalLengthPercent = percentAsInt(this.distanceBetweenClusters, this.totalLength());
        this.distanceBetweenClustersImportance = distanceToVariantLengthPercent;
    }
    
    private void manyMajorClosierToEndAreBetter() {
        this.placingCase.resetTo("many clusters, forming majority, with separators");
        this.meanPosition = this.findMeanPosition();
        
        this.placingPercent = percentAsFloat(this.meanPosition, this.data.variant.length() - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.placingPercentNoHigherThan(75);
        
        this.calculateDistanceBetweenClustersImportance();
        this.applyPathComplexityPenaltyToPlacingPercent();
        
        this.placingBonus = MAX_PLACING_BONUS
                * mean(
                        this.placingPercent,
                        this.distanceBetweenClustersImportance)
                / 100;
    }
    
    private void applyKeyCharsBonusToPlacingPercent() {
        if ( this.data.positions().keyChars.size() > 0 ) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] key chars bonus to placing : +20% ");
            this.placingPercent = this.placingPercent + 20;
            
            if ( this.placingPercent > 100 ) {
                this.placingPercent = 100;
            }
        }
    }
    
    private void applyKeyCharsBonusToPlacingImportance() {
        if ( this.clustersPlacingImportance == UNKNOWN_VALUE ) {
            return;
        }
        
        if ( this.data.positions().keyChars.size() > 0 ) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] key chars bonus to placing importance : +10% ");
            this.clustersPlacingImportance = this.clustersPlacingImportance + 10;
            
            if ( this.clustersPlacingImportance > 100 ) {
                this.clustersPlacingImportance = 100;
            }
        }
    }
    
    private void manyAsOneMajorClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.singleMajorClosierToStartIsBetterBeforeFirstPathSeparator();
        this.placingCase.resetTo("many-as-one majority clusters, with separators, before first separator");
    }
    
    private void manyAsOneClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.manyMajorClosierToStartAreBetterBeforeFirstPathSeparator();
        this.placingCase.resetTo("many-as-one clusters, with separators, before first separator");
    }
    
    private void manyMajorClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("many major clusters, with separators, before first separator");
        this.meanPosition = this.findMeanFirstPosition();
        this.adjustVariantLengthBeforeFirstPathSeparator();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, adjustedVariantLength);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenaltyToPlacingPercent();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variant.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsIntOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }
    
    private void manyClosierToStartAreBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("many clusters, with separators, before first separator");
        this.meanPosition = this.findMeanPosition();
        this.adjustVariantLengthBeforeFirstPathSeparator();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, adjustedVariantLength);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenaltyToPlacingPercent();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variant.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsIntOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }    
    
    private void singleMajorClosierToStartIsBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("single cluster, with separators, before first separator");
        this.meanPosition = this.firstCluster().firstPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenaltyToPlacingPercent();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variant.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsFloatOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(65);
        this.clustersPlacingImportancePercentNoHigherThan(65);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }
    
    private void singleClosierToStartIsBetterBeforeFirstPathSeparator() {
        this.placingCase.resetTo("single cluster, with separators, before first separator");
        this.meanPosition = this.firstCluster().firstPosition();
        
        int firstPathSeparator = this.data.variantPathSeparators.first();
        this.placingPercent = 100 - percentAsFloat(this.meanPosition, firstPathSeparator - 1);
        this.applyKeyCharsBonusToPlacingPercent();
        this.applyPathComplexityPenaltyToPlacingPercent();
        float firstPathPartPercent = percentAsFloat(firstPathSeparator, this.data.variant.length());
        
        this.placingPercent = percentAsFloatOf(this.placingPercent, firstPathPartPercent);
        
        this.clustersPlacingImportance = percentAsFloatOf(100 - this.clustersPercentInVariant, 50);
        this.applyKeyCharsBonusToPlacingImportance();
        
        this.placingPercentNoHigherThan(50);
        this.clustersPlacingImportancePercentNoHigherThan(50);
        
        this.placingBonus = MAX_PLACING_BONUS
                            * mean(this.placingPercent, this.clustersPlacingImportance )
                            / 100;
    }

    private boolean isLastClusterAtVariantEnd() {
        return this.lastCluster().lastPosition() == this.data.variant.length() - 1;
    }

    void logState() {
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] %s", this.toString());
        if ( placingBonusNotApplicableReason.isNotPresent() ) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing case       : %s ", placingCase.orThrow());
            if ( meanPosition != UNKNOWN_VALUE ) {
                if ( this.adjustedVariantLength != UNKNOWN_VALUE ) {
                    logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] mean position      : %s/%s (%s) ", meanPosition, this.adjustedVariantLength, this.data.variant.length() - 1);
                } else {
                    logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] mean position      : %s/%s ", meanPosition, this.data.variant.length() - 1);
                } 
            }
            if ( placingPercent != UNKNOWN_VALUE ) {
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing percent    : %s%% ", placingPercent);
            }
            if ( clustersPlacingImportance != UNKNOWN_VALUE ) {
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing importance : %s%% ", clustersPlacingImportance);
            }
            if ( distanceBetweenClustersImportance != UNKNOWN_VALUE ) {
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] cluster distance importance : %s%% ", distanceBetweenClustersImportance);
            }
        } else {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing not applicable : %s ", placingBonusNotApplicableReason.orThrow());
        }

        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "    [cluster placing] placing bonus      : %s ", placingBonus);
    }
    
    @Override
    public void clear() {   
        this.lastAdded.nullify();
        this.clusterPool.takeBackAll(this.clustersTakenFromPool);
        this.clustersTakenFromPool.clear();
        this.clusters.clear();
        this.rejectedTeardownSum = 0;
        this.clustersMarkedTeardown.clear();
        this.clustersMarkedTeardownRejected.clear();
        this.arranged = false;
        this.clustersTotalLength = 0;
        this.distanceBetweenClusters = 0;
        this.allClustersSeparatedByOneChar = false;
        
        this.clustersPercentInVariant = UNKNOWN_VALUE;        
        this.meanPosition = UNKNOWN_VALUE;
        this.adjustedVariantLength = UNKNOWN_VALUE;
        this.placingPercent = UNKNOWN_VALUE;
        this.clustersPlacingImportance = UNKNOWN_VALUE;
        this.distanceBetweenClustersImportance = UNKNOWN_VALUE;
        this.placingBonus = UNKNOWN_VALUE;
        this.placingBonusLimit = UNKNOWN_VALUE;
        this.placingCase.nullify();
        this.placingBonusNotApplicableReason.nullify();

        this.chosenByWord.clear();
    }
    
    @Override
    public String toString() {
        if ( this.isEmpty() ) {
            return "Clusters[ empty ]";
        }
        
        return this.clusters
                .stream()
                .map(cluster -> cluster.toString())
                .collect(joining(" ", "Clusters[ ", " ]"));
    }
    
}
