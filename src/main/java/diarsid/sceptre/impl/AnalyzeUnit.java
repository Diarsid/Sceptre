package diarsid.sceptre.impl;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.function.IntFunction;

import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.sceptre.impl.weight.Weight;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;

import static java.lang.String.format;
import static java.util.Arrays.fill;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import static diarsid.sceptre.api.model.WeightEstimate.estimatePreliminarily;
import static diarsid.sceptre.impl.AnalyzeUtil.lengthImportanceRatio;
import static diarsid.sceptre.impl.AnalyzeUtil.missedTooMuch;
import static diarsid.sceptre.impl.PositionsAnalyze.POS_NOT_FOUND;
import static diarsid.sceptre.impl.PositionsAnalyze.POS_UNINITIALIZED;
import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.sceptre.impl.weight.WeightElement.CLUSTERS_IMPORTANCE;
import static diarsid.sceptre.impl.weight.WeightElement.LENGTH_DELTA;
import static diarsid.sceptre.impl.weight.WeightElement.NO_CLUSTERS_ALL_SEPARATED_POSITIONS_MEANINGFUL;
import static diarsid.sceptre.impl.weight.WeightElement.NO_CLUSTERS_SEPARATED_POSITIONS_MEANINGFUL;
import static diarsid.sceptre.impl.weight.WeightElement.NO_CLUSTERS_SEPARATED_POSITIONS_SORTED;
import static diarsid.sceptre.impl.weight.WeightElement.PERCENT_FOR_MISSED;
import static diarsid.sceptre.impl.weight.WeightElement.SINGLE_WORD_VARIANT_CONTAINS_PATTERN;
import static diarsid.sceptre.impl.weight.WeightElement.TOTAL_CLUSTERED_CHARS;
import static diarsid.sceptre.impl.weight.WeightElement.TOTAL_UNCLUSTERED_CHARS_IMPORTANCE;
import static diarsid.sceptre.impl.weight.WeightElement.VARIANT_CONTAINS_PATTERN;
import static diarsid.sceptre.impl.weight.WeightElement.VARIANT_EQUAL_PATTERN;
import static diarsid.sceptre.impl.weight.WeightElement.VARIANT_PATH_SEPARATORS;
import static diarsid.sceptre.impl.weight.WeightElement.VARIANT_TEXT_SEPARATORS;
import static diarsid.sceptre.api.model.WeightEstimate.BAD;
import static diarsid.sceptre.api.model.WeightEstimate.estimate;
import static diarsid.support.misc.MathFunctions.percentAsFloat;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.ratio;
import static diarsid.support.objects.collections.CollectionUtils.isNotEmpty;
import static diarsid.support.strings.StringIgnoreCaseUtil.indexOfIgnoreCase;
import static diarsid.support.strings.StringUtils.isPathSeparator;
import static diarsid.support.strings.StringUtils.isTextSeparator;
import static diarsid.support.strings.StringUtils.lower;
import static diarsid.support.strings.StringUtils.nonEmpty;

class AnalyzeUnit extends PooledReusable {
    
    private static final IntFunction<String> POSITION_INT_TO_STRING;
    
    static {
        POSITION_INT_TO_STRING = (position) -> {
            if ( position == POS_NOT_FOUND ) {
                return "x";
            } else {
                return String.valueOf(position);
            }                    
        };
    }
    
    final PositionsAnalyze positionsAnalyze;
    
    TreeSet<Integer> variantSeparators;
    TreeSet<Integer> variantPathSeparators;
    TreeSet<Integer> variantTextSeparators;
    String variant;
    boolean variantEqualsToPattern;
    boolean variantContainsPattern;
    int patternInVariantIndex;
    
    Weight weight;
    float lengthDelta;
    boolean allPositionsPresentSortedAndNotPathSeparatorsBetween;
    boolean calculatedAsUsualClusters;
    boolean canClustersBeBad = true;
    
    char[] patternChars;
    String pattern;
    
    int missedPercent;
        
    AnalyzeUnit(GuardedPool<Cluster> clusterPool) {
        super();
        this.positionsAnalyze = new PositionsAnalyze(
                this, 
                new Clusters(this, clusterPool), 
                new PositionCandidate(this));
        this.variantSeparators = new TreeSet<>();
        this.variantPathSeparators = new TreeSet<>();
        this.variantTextSeparators = new TreeSet<>();
        this.weight = new Weight();
    }
    
    void set(String pattern, String variant) {
        this.variant = lower(variant);
        this.pattern = pattern;
        this.checkIfVariantEqualsToPatternAndAssignWeight();
    }

    private void checkIfVariantEqualsToPatternAndAssignWeight() {
        this.variantEqualsToPattern = this.pattern.equalsIgnoreCase(this.variant);
        if ( this.variantEqualsToPattern ) {
            this.weight.add(- this.variant.length() * 1024, VARIANT_EQUAL_PATTERN);       
        }
    }
    
    @Override
    public void clearForReuse() {
        this.pattern = null;
        this.positionsAnalyze.clearPositionsAnalyze();
        this.variantSeparators.clear();
        this.variantPathSeparators.clear();
        this.variantTextSeparators.clear();
        this.variant = "";
        this.variantEqualsToPattern = false;
        this.variantContainsPattern = false;
        this.patternInVariantIndex = -1;
        this.weight.clear();
        this.lengthDelta = 0;
        this.calculatedAsUsualClusters = true;
        this.canClustersBeBad = true;
        this.allPositionsPresentSortedAndNotPathSeparatorsBetween = false;
        this.missedPercent = 0;
    }

    void calculateWeight() {        
        this.weight.add(this.positionsAnalyze.weight);
        logAnalyze(AnalyzeLogType.BASE, "  weight on step 1: %s (positions: %s) ", this.weight.sum(), this.positionsAnalyze.weight.sum());
                
        if ( this.weight.sum() > 0 ) {
            this.positionsAnalyze.badReason = "preliminary position calculation is too bad";
            return;
        }
        if ( this.positionsAnalyze.clustersQty > 0 ) {
            switch ( this.pattern.length() ) {
                case 0 : 
                case 1 : {
                    throw new IllegalStateException(
                            "This analyze is not intended to process 0 or 1 length patterns!");
                }
                case 2 : {
                    this.calculateAsUsualClusters();
                    this.calculatedAsUsualClusters = true;
                    break;
                }
                case 3 :
                case 4 : {
                    if ( this.positionsAnalyze.missed == 0 && this.positionsAnalyze.nonClustered > 2 ) {
                        if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions: " + this.positionsAnalyze.nonClustered;
                            return; 
                        }
                    } else if ( this.positionsAnalyze.missed == 1 && this.positionsAnalyze.nonClustered > 1 ) {
                        this.positionsAnalyze.badReason = "Too much unclustered and missed positions: " + (this.positionsAnalyze.missed + this.positionsAnalyze.nonClustered);
                        return;
                    } else if ( this.positionsAnalyze.missed == 0 && this.positionsAnalyze.nonClustered > 1 ) {
                        if ( this.positionsAnalyze.clustersFacingStartEdges > 0 ) {
                            this.calculateAsUsualClusters();
                            this.calculatedAsUsualClusters = true;
                        } else if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions";
                            return;
                        }                        
                    } else {
                        this.calculateAsUsualClusters();
                        this.calculatedAsUsualClusters = true;
                    }
                    break;
                }
                default: {
                    float tresholdRatio;
                    
                    if ( this.positionsAnalyze.clustersQty == 1 ) {
                        tresholdRatio = 0.5f;
                    } else {
                        tresholdRatio = 0.4f;
                    }
                    
                    if ( ratio(this.positionsAnalyze.nonClustered, this.patternChars.length) > tresholdRatio ) {
                        if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
                            this.calculateAsSeparatedCharsWithoutClusters();
                            this.calculatedAsUsualClusters = false;
                        } else {
                            this.positionsAnalyze.badReason = "Too much unclustered positions";
                            return;
                        }                        
                    } else {
                        this.calculateAsUsualClusters();
                        this.calculatedAsUsualClusters = true;
                    }
                }
            }            
        } else {
            if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
                this.calculateAsSeparatedCharsWithoutClusters();
                this.calculatedAsUsualClusters = false;
            } else {
                this.positionsAnalyze.badReason = "There are no clusters, positions are unsorted";
                return;
            }
        }
        
        logAnalyze(AnalyzeLogType.BASE, "  weight on step 2: %s", this.weight);
    }

    void areAllPositionsPresentSortedAndNotPathSeparatorsBetween() {
        if ( this.positionsAnalyze.unsortedPositions == 0 && this.positionsAnalyze.missed == 0 ) {
            if ( this.variantPathSeparators.isEmpty() ) {
                this.allPositionsPresentSortedAndNotPathSeparatorsBetween = true;
            } else {
                int first = this.positionsAnalyze.findFirstPosition();
                int last = this.positionsAnalyze.findLastPosition();
                
                if ( first < 0 || last < 0 ) {
                    this.allPositionsPresentSortedAndNotPathSeparatorsBetween = false;
                } else {
                    Integer possibleSeparator = this.variantPathSeparators.higher(first);
                    if ( nonNull(possibleSeparator) ) {
                        this.allPositionsPresentSortedAndNotPathSeparatorsBetween = last < possibleSeparator;
                    } else {
                        this.allPositionsPresentSortedAndNotPathSeparatorsBetween = true;
                    }
                }
            }
        } else {
            this.allPositionsPresentSortedAndNotPathSeparatorsBetween = false;
        }
    }
    
    private void calculateAsUsualClusters() {
        if ( this.positionsAnalyze.nonClustered == 0 && 
             this.positionsAnalyze.missed == 0 &&
             this.variant.length() == this.positionsAnalyze.clustered + 
                                          this.variantPathSeparators.size() + 
                                          this.variantTextSeparators.size() ) {
            this.weight.add(-this.positionsAnalyze.clustersImportance, CLUSTERS_IMPORTANCE);
            this.weight.add(-this.positionsAnalyze.clustered, TOTAL_CLUSTERED_CHARS);
        } else {
            float lengthImportance = lengthImportanceRatio(this.variant.length());
            this.lengthDelta = ( this.variant.length() - this.positionsAnalyze.clustered - this.positionsAnalyze.meaningful ) * 0.3f * lengthImportance;
            
            this.weight.add(this.positionsAnalyze.nonClusteredImportance, TOTAL_UNCLUSTERED_CHARS_IMPORTANCE);
            this.weight.add(-this.positionsAnalyze.clustersImportance, CLUSTERS_IMPORTANCE);
            this.weight.add(this.lengthDelta, LENGTH_DELTA);
            this.weight.add(this.variantPathSeparators.size(), VARIANT_PATH_SEPARATORS);
            this.weight.add(this.variantTextSeparators.size(), VARIANT_TEXT_SEPARATORS);
            
            if ( this.positionsAnalyze.missed > 0 ) {
                this.missedPercent = 100 - percentAsInt(this.positionsAnalyze.missed, this.pattern.length());
                this.weight.applyPercent(missedPercent, PERCENT_FOR_MISSED);
            }
        }        
    }
    
    private void calculateAsSeparatedCharsWithoutClusters() {        
        float lengthImportance = lengthImportanceRatio(this.variant.length());
        this.lengthDelta = ( this.variant.length() - this.positionsAnalyze.meaningful ) * 0.1f * lengthImportance;
        this.weight.add(this.lengthDelta, LENGTH_DELTA);
            
        float bonus = this.positionsAnalyze.positions.length * 5.1f;
        this.weight.add(-bonus, NO_CLUSTERS_SEPARATED_POSITIONS_SORTED);
        
        int meanigfulPositions = this.positionsAnalyze.meaningful;
        if ( meanigfulPositions > 0 ) {
            bonus = bonus * ( meanigfulPositions + 1 ) * 0.8f;
            this.weight.add(-bonus, NO_CLUSTERS_SEPARATED_POSITIONS_MEANINGFUL);
            if ( meanigfulPositions == this.pattern.length() ) {
                bonus = meanigfulPositions * 2.8f;
                this.weight.add(-bonus, NO_CLUSTERS_ALL_SEPARATED_POSITIONS_MEANINGFUL);
            }
        }
    }

    void calculateClustersImportance() {
        this.positionsAnalyze.calculateImportance();
    }
    
    PositionsAnalyze positions() {
        return this.positionsAnalyze;
    }
    
    boolean ifClustersPresentButWeightTooBad() {
        return 
                this.canClustersBeBad &&
                this.positionsAnalyze.clustersQty > 0 && 
                estimatePreliminarily(this.positionsAnalyze.weight.sum()).equals(BAD);
    }

    boolean isVariantTooBad() {
        return nonEmpty(this.positionsAnalyze.badReason) || estimate(this.weight.sum()).equals(BAD);
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        // TODO if there are no path separators if variant is quite short and if clusters are 0-2
//        if ( pattern.charAt(0) == this.variantText.charAt(0) ) {  
//            logAnalyze(BASE, "               [weight] -3.4 : first char match in variant and pattern ");
//            this.variantWeight = this.variantWeight - 3.4;            
//        }
    }

    void logState() {  
        logAnalyze(AnalyzeLogType.BASE, "  variant       : %s", this.variant);
        
        String patternCharsString = Arrays.stream(this.positionsAnalyze.positions)
                .mapToObj(position -> {
                    if ( position < 0 ) {
                        return "*";
                    } else {
                        return String.valueOf(this.variant.charAt(position));
                    }                    
                })
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        String positionsString =  Arrays.stream(this.positionsAnalyze.positions)
                .mapToObj(POSITION_INT_TO_STRING)
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        logAnalyze(AnalyzeLogType.BASE, "  pattern chars : %s", patternCharsString);
        logAnalyze(AnalyzeLogType.BASE, "  positions     : %s", positionsString);
                
        if ( nonEmpty(this.positionsAnalyze.badReason) ) {
            logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "bad reason", this.positionsAnalyze.badReason);
            return;
        }
        
        if ( this.calculatedAsUsualClusters ) {
            this.logClustersState();
        } else {
            logAnalyze(AnalyzeLogType.BASE, "  calculated as separated characters");
        }
        if (AnalyzeLogType.POSITIONS_CLUSTERS.isEnabled()) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "  weight elements:");
            this.weight.observeAll((i, weightValue, element) -> {
                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, format("      %1$s) %2$-+7.2f : %3$s", i, weightValue, element.description()));
            });
        }
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "total weight", this.weight);
    }
    
    private void logClustersState() {
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "clusters", positionsAnalyze.clustersQty);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "clustered", positionsAnalyze.clustered);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "length delta", this.lengthDelta);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "distance between clusters", positionsAnalyze.clusters.distanceBetweenClusters());
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "separators between clusters", positionsAnalyze.separatorsBetweenClusters);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "variant text separators ", this.variantTextSeparators.size());
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "variant path separators ", this.variantPathSeparators.size());
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "nonClustered", positionsAnalyze.nonClustered);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "nonClusteredImportance", positionsAnalyze.nonClusteredImportance);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "clustersImportance", positionsAnalyze.clustersImportance);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s", "missed", positionsAnalyze.missed);
        logAnalyze(AnalyzeLogType.BASE, "    %1$-25s %2$s%%", "missedPercent", this.missedPercent);
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.positionsAnalyze.missed, this.patternChars.length);
        if ( tooMuchMissed ) {
            logAnalyze(AnalyzeLogType.BASE, "    %s, missed: %s to much, skip variant!", this.variant, this.positionsAnalyze.missed);
        }
        return tooMuchMissed;
    }

    void sortPositions() {
        this.positionsAnalyze.sortPositions();
    }
    
    void findPositionsClusters() {
        this.positionsAnalyze.analyzePositionsClusters();
    }
    
    void ifSingleWordAbbreviation() {
        boolean patternIsSingleWord = this.variantSeparators.isEmpty();
        if ( patternIsSingleWord ) {
            this.canClustersBeBad = false;
            logAnalyze(AnalyzeLogType.BASE, "  variant is a single word");
            switch ( this.positionsAnalyze.clusters.quantity() ) {
                case 0 : doWhenNoClusters(); break;
                case 1 : doWhenSingleCluster(); break;
                default : doWhenManyClusters(); break;
            }
        }
    } 
    
    private void doWhenNoClusters() {
        logAnalyze(AnalyzeLogType.BASE, "    [abbreviation] ");
        if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
            this.positionsAnalyze.lookForSeparatedCharsPlacing();
        }
    }
    
    private void doWhenSingleCluster() {
        if ( this.positionsAnalyze.clusters.firstCluster().length() < 4 ) {
            logAnalyze(AnalyzeLogType.BASE, "    [mark sequence] ");
            if ( this.weight.contains(VARIANT_CONTAINS_PATTERN) ) {
                float bonus = percentAsFloat(pattern.length(), variant.length()) / 10;
                this.weight.add(-bonus, SINGLE_WORD_VARIANT_CONTAINS_PATTERN);
            }
            this.positionsAnalyze.lookForDuplicatedCharsOfSingleCluster();
        } else {
            
        }        
    }
    
    private void doWhenManyClusters() {
        // no logic here yet
    }
    
    boolean isVariantEqualsPattern() {
        return this.variantEqualsToPattern;
    }
    
    boolean isVariantNotEqualsPattern() {
        return ! this.variantEqualsToPattern;
    }

    void checkIfVariantTextContainsPatternDirectly() {
        this.patternInVariantIndex = indexOfIgnoreCase(this.variant, this.pattern);
        if ( this.patternInVariantIndex >= 0 ) {
            float lengthRatio = patternLengthRatio(this.pattern);
            logAnalyze(AnalyzeLogType.BASE, "  variant contains pattern: weight -%s", lengthRatio);
            this.weight.add(-lengthRatio, VARIANT_CONTAINS_PATTERN);
            this.variantContainsPattern = true;
        }
    }
    
    void findPathAndTextSeparators() {
        for (int i = 0; i < this.variant.length(); i++) {
            if ( isPathSeparator(this.variant.charAt(i)) ) {
                this.variantPathSeparators.add(i);
            }
            if ( isTextSeparator(this.variant.charAt(i)) ) {
                this.variantTextSeparators.add(i);
            }
        }
        if ( isNotEmpty(this.variantPathSeparators) ) {
            this.variantSeparators.addAll(this.variantPathSeparators);
        }
        if ( isNotEmpty(this.variantTextSeparators) ) {
            this.variantSeparators.addAll(this.variantTextSeparators);
        }
    }

    void setPatternCharsAndPositions() {
        this.patternChars = this.pattern.toCharArray();
        this.positionsAnalyze.positions = new int[this.patternChars.length];
        fill(this.positionsAnalyze.positions, POS_UNINITIALIZED);
    }
    
    private static float patternLengthRatio(String pattern) {
        return pattern.length() * 5.5f;
    }

    void findPatternCharsPositions() {
        if ( this.variantContainsPattern ) {
            this.positionsAnalyze.fillPositionsFromIndex(this.patternInVariantIndex);
        } else {
            this.positionsAnalyze.findPatternCharsPositions();
        }
    }

    void logUnsortedPositions() {
        this.logUnsortedPositionsOf(this.positionsAnalyze);
    }

    private void logUnsortedPositionsOf(PositionsAnalyze data) {
        String positionsS = Arrays.stream(data.positions)
                .mapToObj(POSITION_INT_TO_STRING)
                .collect(joining(" "));
        logAnalyze(AnalyzeLogType.BASE, "  positions before sorting: %s", positionsS);
    }
}