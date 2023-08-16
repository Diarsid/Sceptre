package diarsid.sceptre.impl;

import java.util.List;
import java.util.function.IntFunction;

import diarsid.sceptre.api.LogType;
import diarsid.sceptre.impl.collections.ArrayChar;
import diarsid.sceptre.impl.collections.CharsCount;
import diarsid.sceptre.impl.collections.Ints;
import diarsid.sceptre.impl.collections.SetInt;
import diarsid.sceptre.impl.collections.impl.ArrayCharImpl;
import diarsid.sceptre.impl.collections.impl.CharsCountImpl;
import diarsid.sceptre.impl.collections.impl.SetIntImpl;
import diarsid.sceptre.impl.logs.Logging;
import diarsid.sceptre.impl.weight.Weight;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.PooledReusable;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

import static diarsid.sceptre.api.LogType.BASE;
import static diarsid.sceptre.api.LogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.api.WeightEstimate.BAD;
import static diarsid.sceptre.api.WeightEstimate.of;
import static diarsid.sceptre.api.WeightEstimate.preliminarilyOf;
import static diarsid.sceptre.impl.AnalyzeUtil.lengthImportanceRatio;
import static diarsid.sceptre.impl.AnalyzeUtil.missedTooMuch;
import static diarsid.sceptre.impl.PositionsAnalyze.POS_NOT_FOUND;
import static diarsid.sceptre.impl.PositionsAnalyze.POS_UNINITIALIZED;
import static diarsid.sceptre.impl.WordInVariant.Placing.DEPENDENT;
import static diarsid.sceptre.impl.WordInVariant.Placing.INDEPENDENT;
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
import static diarsid.sceptre.impl.weight.WeightElement.WeightCalculationType.APPLY_PERCENT_TO_SUM;
import static diarsid.support.misc.MathFunctions.percentAsFloat;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.ratio;
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

    final Logging log;

    final PositionsAnalyze positionsAnalyze;
    final WordsInVariant wordsInVariant;

    final SetInt variantSeparators;
    final SetInt variantPathSeparators;
    final SetInt variantTextSeparators;

    String variant;
    String variantOriginal;
    boolean variantEqualsToPattern;
    boolean variantContainsPattern;
    int patternInVariantIndex;
    
    final Weight weight;
    float lengthDelta;
    boolean allPositionsPresentSortedAndNotPathSeparatorsBetween;
    boolean calculatedAsUsualClusters;
    boolean canClustersBeBad = true;
    
    final ArrayChar patternChars;
    String pattern;
    final CharsCount patternCharsCount;
    
    int notMissedPercent;
        
    AnalyzeUnit(
            Logging log,
            GuardedPool<Cluster> clusterPool,
            GuardedPool<WordInVariant> wordPool,
            GuardedPool<WordsInVariant.WordsInRange> wordsInRangePool) {
        super();
        this.log = log;
        this.positionsAnalyze = new PositionsAnalyze(
                this, 
                new Clusters(this, clusterPool), 
                new PositionCandidate(this));
        this.patternChars = new ArrayCharImpl();
        this.variantSeparators = new SetIntImpl();
        this.variantPathSeparators = new SetIntImpl();
        this.variantTextSeparators = new SetIntImpl();
        this.wordsInVariant = new WordsInVariant(wordPool, wordsInRangePool);
        this.weight = new Weight(this.log);
        this.patternCharsCount = new CharsCountImpl();
    }
    
    void set(String pattern, String variant) {
        this.variant = lower(variant);
        this.variantOriginal = variant;
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
        this.variantOriginal = "";
        this.variantEqualsToPattern = false;
        this.variantContainsPattern = false;
        this.patternInVariantIndex = -1;
        this.weight.clear();
        this.lengthDelta = 0;
        this.calculatedAsUsualClusters = true;
        this.canClustersBeBad = true;
        this.allPositionsPresentSortedAndNotPathSeparatorsBetween = false;
        this.notMissedPercent = 0;
        this.wordsInVariant.clear();
        this.patternCharsCount.clear();
    }

    void calculateWeight() {        
        this.weight.add(this.positionsAnalyze.weight);
        log.add(BASE, "  weight on step 1: %s (positions: %s) ", this.weight.sum(), this.positionsAnalyze.weight.sum());
                
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
                    
                    if ( ratio(this.positionsAnalyze.nonClustered, this.patternChars.size()) > tresholdRatio ) {
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
        
        log.add(BASE, "  weight on step 2: %s", this.weight);
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
                    int possibleSeparator = this.variantPathSeparators.greaterThan(first);
                    if ( Ints.doesExist(possibleSeparator) ) {
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
//            this.lengthDelta = ( this.variant.length() - this.positionsAnalyze.clustered - this.positionsAnalyze.meaningful ) * 0.3f * lengthImportance;

            calculateLengthDelta();

            this.weight.add(this.positionsAnalyze.nonClusteredImportance, TOTAL_UNCLUSTERED_CHARS_IMPORTANCE);
            this.weight.add(-this.positionsAnalyze.clustersImportance, CLUSTERS_IMPORTANCE);
            this.weight.add(this.lengthDelta, LENGTH_DELTA);
            this.weight.add(this.variantPathSeparators.size(), VARIANT_PATH_SEPARATORS);
            addWeightForVariantTextSeparators();

            if ( this.positionsAnalyze.missed > 0 ) {
                this.notMissedPercent = 100 - percentAsInt(this.positionsAnalyze.missed, this.pattern.length());
                this.weight.applyPercent(this.notMissedPercent, PERCENT_FOR_MISSED);
            }
        }        
    }

    private void addWeightForVariantTextSeparators() {
        int textSeparatorsCount = this.variantTextSeparators.size();

        float weight;
        if ( textSeparatorsCount < 10 ) {
            weight = textSeparatorsCount;
        }
        else if ( textSeparatorsCount < 30 ) {
            weight = 10 + (float) Math.pow(textSeparatorsCount - 10, 0.8);
        }
        else if ( textSeparatorsCount < 100 ) {
            weight = 10
                    + (float) Math.pow(29, 0.8)
                    + (float) Math.pow(textSeparatorsCount - 29, 0.33);
        }
        else {
            weight = 10
                    + (float) Math.pow(29, 0.8)
                    + (float) Math.pow(70 /* 99 - 29 */ , 0.33)
                    + (float) Math.pow(textSeparatorsCount - 99, 0.33);
        }

        this.weight.add(weight, VARIANT_TEXT_SEPARATORS);
    }

    private void calculateLengthDelta() {
        int otherLength = this.variant.length() - this.positionsAnalyze.filledPositions.size();
        this.lengthDelta = (float) Math.pow(otherLength, 0.75);

        if ( this.lengthDelta > 25 ) {
            this.lengthDelta = 25 + (float) Math.pow(otherLength, 0.25);
        }
    }

    private void calculateAsSeparatedCharsWithoutClusters() {        
        float lengthImportance = lengthImportanceRatio(this.variant.length());
//        this.lengthDelta = ( this.variant.length() - this.positionsAnalyze.meaningful ) * 0.1f * lengthImportance;

        calculateLengthDelta();

        this.weight.add(this.lengthDelta, LENGTH_DELTA);
            
        float bonus = this.positionsAnalyze.positions.size() * 5.1f;
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
                preliminarilyOf(this.positionsAnalyze.weight.sum()).equals(BAD);
    }

    boolean isVariantTooBad() {
        return nonEmpty(this.positionsAnalyze.badReason) || of(this.weight.sum()).equals(BAD);
    }

    void isFirstCharMatchInVariantAndPattern(String pattern) {
        // TODO if there are no path separators if variant is quite short and if clusters are 0-2
//        if ( pattern.charAt(0) == this.variantText.charAt(0) ) {  
//            logAnalyze(BASE, "               [weight] -3.4 : first char match in variant and pattern ");
//            this.variantWeight = this.variantWeight - 3.4;            
//        }
    }

    void logState() {  
        log.add(BASE, "  variant       : %s", this.variant);
        
        String patternCharsString = this.positionsAnalyze.positions
                .stream()
                .mapToObj(position -> {
                    if ( position < 0 ) {
                        return "*";
                    } else {
                        return String.valueOf(this.variant.charAt(position));
                    }                    
                })
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        String positionsString =  this.positionsAnalyze.positions
                .stream()
                .mapToObj(POSITION_INT_TO_STRING)
                .map(s -> s.length() == 1 ? " " + s : s)
                .collect(joining(" "));
        log.add(BASE, "  pattern chars : %s", patternCharsString);
        log.add(BASE, "  positions     : %s", positionsString);
                
        if ( nonEmpty(this.positionsAnalyze.badReason) ) {
            log.add(BASE, "    %1$-25s %2$s", "bad reason", this.positionsAnalyze.badReason);
            return;
        }
        
        if ( this.calculatedAsUsualClusters ) {
            this.logClustersState();
        } else {
            log.add(BASE, "  calculated as separated characters");
        }
        if ( log.isEnabled(POSITIONS_CLUSTERS) ) {
            log.add(POSITIONS_CLUSTERS, "  weight elements:");
            this.weight.observeAll((i, weightValue, element) -> {
                if ( element.calculationType.is(APPLY_PERCENT_TO_SUM) ) {
                    log.add(POSITIONS_CLUSTERS, format("      %1$s) x%2$7.2f%% : %3$s", i, weightValue, element.description));
                }
                else {
                    log.add(POSITIONS_CLUSTERS, format("      %1$s) %2$-+7.2f : %3$s", i, weightValue, element.description));
                }
            });
        }
        log.add(BASE, "    %1$-25s %2$s", "total weight", this.weight);
    }
    
    private void logClustersState() {
        log.add(BASE, "    %1$-25s %2$s", "clusters", positionsAnalyze.clustersQty);
        log.add(BASE, "    %1$-25s %2$s", "clustered", positionsAnalyze.clustered);
        log.add(BASE, "    %1$-25s %2$s", "length delta", this.lengthDelta);
        log.add(BASE, "    %1$-25s %2$s", "distance between clusters", positionsAnalyze.clusters.distanceBetweenClusters());
        log.add(BASE, "    %1$-25s %2$s", "separators between clusters", positionsAnalyze.separatorsBetweenClusters);
        log.add(BASE, "    %1$-25s %2$s", "variant text separators ", this.variantTextSeparators.size());
        log.add(BASE, "    %1$-25s %2$s", "variant path separators ", this.variantPathSeparators.size());
        log.add(BASE, "    %1$-25s %2$s", "nonClustered", positionsAnalyze.nonClustered);
        log.add(BASE, "    %1$-25s %2$s", "nonClusteredImportance", positionsAnalyze.nonClusteredImportance);
        log.add(BASE, "    %1$-25s %2$s", "clustersImportance", positionsAnalyze.clustersImportance);
        log.add(BASE, "    %1$-25s %2$s", "missed", positionsAnalyze.missed);
        log.add(BASE, "    %1$-25s %2$s%%", "notMissedPercent", this.notMissedPercent);
    }

    boolean areTooMuchPositionsMissed() {
        boolean tooMuchMissed = missedTooMuch(this.positionsAnalyze.missed, this.patternChars.size());
        if ( tooMuchMissed ) {
            log.add(BASE, "    %s, missed: %s to much, skip variant!", this.variant, this.positionsAnalyze.missed);
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
            log.add(BASE, "  variant is a single word");
            switch ( this.positionsAnalyze.clusters.quantity() ) {
                case 0 : doWhenNoClusters(); break;
                case 1 : doWhenSingleCluster(); break;
                default : doWhenManyClusters(); break;
            }
        }
    } 
    
    private void doWhenNoClusters() {
        log.add(BASE, "    [abbreviation] ");
        if ( this.allPositionsPresentSortedAndNotPathSeparatorsBetween ) {
            this.positionsAnalyze.lookForSeparatedCharsPlacing();
        }
    }
    
    private void doWhenSingleCluster() {
//        this.positionsAnalyze.applySingleWordQuality();
        if ( this.positionsAnalyze.clusters.firstCluster().length() < 4 ) {
            log.add(BASE, "    [mark sequence] ");
            if ( this.weight.contains(VARIANT_CONTAINS_PATTERN) ) {
                float bonus = percentAsFloat(pattern.length(), variant.length()) / 10;
                this.weight.add(-bonus, SINGLE_WORD_VARIANT_CONTAINS_PATTERN);
            }
            this.positionsAnalyze.lookForDuplicatedCharsOfSingleCluster();
        } else {
            
        }        
    }
    
    private void doWhenManyClusters() {
//        this.positionsAnalyze.applySingleWordQuality();
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
            log.add(BASE, "  variant contains pattern: weight -%s", lengthRatio);
            this.weight.add(-lengthRatio, VARIANT_CONTAINS_PATTERN);
            this.variantContainsPattern = true;
        }
    }
    
    void findWordsAndPathAndTextSeparators() {
        WordInVariant wordInVariant = null;

        char c;
        int sLength = this.variantOriginal.length();
        String s = this.variantOriginal;

        int current;
        boolean currentIsUpper;
        boolean currentIsDigit;
        boolean currentIsSeparator;
        boolean previousIsUpper = false;
        boolean previousIsDigit = false;
        boolean previousIsSeparator = true;

        wordsInVariant.variantLength = sLength;

        for (int i = 0; i < sLength; i++) {
            current = i;
            c = s.charAt(current);
            currentIsSeparator = false;

            if ( isPathSeparator(c) ) {
                this.variantPathSeparators.add(current);
                currentIsSeparator = true;
            }

            if ( isTextSeparator(c) ) {
                this.variantTextSeparators.add(current);
                currentIsSeparator = true;
            }

            if ( currentIsSeparator ) {
                if ( ! previousIsSeparator ) {
                    if ( nonNull(wordInVariant) ) {
                        if ( wordInVariant.length > 0 ) {
                            wordInVariant.complete();
                            wordInVariant = wordsInVariant.next(INDEPENDENT);
                        }
                    }
                    else {
                        wordInVariant = wordsInVariant.next(INDEPENDENT);
                    }
                }
            }
            else {
                if ( current == 0 ) {
                    wordInVariant = wordsInVariant.next(INDEPENDENT);
                }

                currentIsUpper = Character.isUpperCase(c);

                if ( currentIsUpper ) {
                    currentIsDigit = false;
                } else {
                    currentIsDigit = Character.isDigit(c);
                }

                boolean isNewWordStart = false;

                if ( current > 0 ) {
                    if ( currentIsUpper ) {
                        if ( ! previousIsUpper ) {
                            isNewWordStart = true;
                        }
                    }
                    else {
                        if ( currentIsDigit ) {
                            if ( ! previousIsDigit ) {
                                isNewWordStart = true;
                            }
                        }
                    }
                }

                if ( isNewWordStart ) {
                    if ( isNull(wordInVariant) ) {
                        wordInVariant = wordsInVariant.next(INDEPENDENT);
                    }

                    if ( wordInVariant.length > 0 ) {
                        wordInVariant.complete();
                        wordInVariant = wordsInVariant.next(DEPENDENT);
                    }
                }

                if ( isNull(wordInVariant) ) {
                    throw new IllegalStateException();
                }

                wordInVariant.set(current, c);

                previousIsUpper = currentIsUpper;
                previousIsDigit = currentIsDigit;
            }

            previousIsSeparator = currentIsSeparator;
        }

        if ( nonNull(wordInVariant) && wordInVariant.length > 0 ) {
            wordInVariant.complete();
        }

        wordsInVariant.complete();

        if ( this.variantPathSeparators.isNotEmpty() ) {
            this.variantSeparators.addAll(this.variantPathSeparators);
        }

        if ( this.variantTextSeparators.isNotEmpty() ) {
            this.variantSeparators.addAll(this.variantTextSeparators);
        }
    }

    void setPatternCharsAndPositions() {
        this.patternChars.fillFrom(this.pattern);
        this.positionsAnalyze.positions.setSize(this.patternChars.size());
        this.positionsAnalyze.positions.fill(POS_UNINITIALIZED);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fillNulls(List list, int length) {
        for ( int i = 0; i < length; i++ ) {
            list.add(null);
        }
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
        String positionsS = data.positions
                .stream()
                .mapToObj(POSITION_INT_TO_STRING)
                .collect(joining(" "));
        log.add(BASE, "  positions before sorting: %s", positionsS);
    }

    boolean doesPatternContainsCharJustAfter(char c, int patternIndexExcl) {
        if ( patternIndexExcl == this.pattern.length() - 1 ) {
            return false;
        }

        return c == this.patternChars.i(patternIndexExcl + 1);
    }
}
