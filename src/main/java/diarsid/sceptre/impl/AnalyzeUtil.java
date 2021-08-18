package diarsid.sceptre.impl;

import java.util.List;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.impl.logs.AnalyzeLogType;

import static java.lang.Integer.MIN_VALUE;
import static java.util.stream.Collectors.joining;

import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.meanSmartIngoringZeros;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.ratio;

class AnalyzeUtil {     
    
    private static final float ADJUSTED_WEIGHT_TRESHOLD = 30;
    private static final int UNINITIALIZED = MIN_VALUE;
    
    static int inconsistencyOf(Cluster orderDiff, int clusterLength) {
        int percent = percentAsInt(orderDiff.ordersDiffCount(), clusterLength) / 10;
        return (percent + orderDiff.ordersDiffSumAbs() + (orderDiff.ordersDiffSumAbs()/2) ) * orderDiff.ordersDiffSumAbs();
    }
    
    static void processCluster(
            int patternLength,
            Cluster cluster, 
            List<Integer> ints, 
            int clusterFirstPosition, 
            int clusterLength) {
        int mean = meanSmartIngoringZeros(ints);
        if ( AnalyzeLogType.POSITIONS_CLUSTERS.isEnabled() ) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diffs         %s",
                    ints.stream().map(i -> i.toString()).collect(joining(" ")));
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diffs mean    %s", mean);
        }
        
        int limit = ints.size() - 1;
        
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
        
        // initial analize of first element
        int firstOrder = ints.get(0);
        int diffSumAbs = absDiff(firstOrder, mean);
        int diffSumReal = firstOrder;
        int diffCount = 0;        
        if ( firstOrder != mean ) {
            diffCount++;
        }   
        
        for (int i = 0; i < limit; i++) {            
            isLastPair = ( i + 1 == limit );
            
            current = ints.get(i);
            next = ints.get(i + 1);
            
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
                        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "              [O-diff] mutual +1-1 compensation for %s_vs_%s", current, next);
                        haveCompensation = true;
                        compensationSum = compensationSum + 2;
                        haveCompensationInCurrentStep = true;
                        diffSumAbs = diffSumAbs - 2;
                        lastBeforeRepeat = UNINITIALIZED;
                        if ( clusterLength == 2 || clusterLength == 3 ) {
                            shifts = 2;
                        } 
                    } else if ( absDiff(previous, next) == 4 && 
                                absDiff(previous, current) == 2 && 
                                absDiff(current, mean) == 0 ) {
                        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "              [O-diff] mutual +2 0 -2 compensation for %s_vs_%s", previous, next);
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
                
                if ( previousIsRepeat ) {
                    if ( ! haveCompensationInCurrentStep ) {
                        
                        repeatAbsDiffSum = absDiff(repeat * repeatQty, mean * repeatQty);
                        
                        if ( repeatAbsDiffSum > 0 ) {
                            if ( lastBeforeRepeat != UNINITIALIZED && absDiff(lastBeforeRepeat, mean) == repeatAbsDiffSum ) {
                                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "              [O-diff] compensation for %s_vs_(%s * %s)", lastBeforeRepeat, repeat, repeatQty);
                                diffSumAbs = diffSumAbs - (repeatAbsDiffSum * 2);
                                shifts = shifts + repeatQty;
                                haveCompensation = true;
                                lastBeforeRepeat = UNINITIALIZED;
                            } else if ( absDiff(next, mean) == repeatAbsDiffSum ) {
                                logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "              [O-diff] compensation for (%s * %s)_vs_%s", repeat, repeatQty, next);
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
        
        cluster.finish();
        
        if ( AnalyzeLogType.POSITIONS_CLUSTERS.isEnabled() ) {
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order repeats       %s", cluster
                        .repeats()
                        .stream()
                        .map(repeating -> String.valueOf(repeating))
                        .collect(joining(",", "<", ">")));    
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order repeats qties %s", cluster
                        .repeatQties()
                        .stream()
                        .map(repeating -> String.valueOf(repeating))
                        .collect(joining(",", "<", ">")));    
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diff sum real %s", diffSumReal);
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diff sum abs  %s", diffSumAbs);
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diff count    %s", diffCount);
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diff compensation  %s", compensationSum);
        }
        if ( diffSumAbs == 0 && haveCompensation && clusterLength == 2 ) {            
            diffSumAbs = 1;
            logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "            [cluster stats] order diff sum fix  %s", diffSumAbs);
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
                compensationSum);
    }
    
    static int lengthTolerance(int variantLength) {
        if ( variantLength < 20 ) {
            return 0;
        } else {
            return ( ( variantLength - 11 ) / 10 ) * 5;
        }
    }
    
    static float missedRatio(float clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 19.0f;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.0f;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 12.0f;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 10.0f;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 8.0f;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 6.0f;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 4.0f;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 2.0f;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 1.0f;
        } else {
            return 0.5f;
        }
    }
    
    static float unsortedRatioDependingOn(float clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 26.8f;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.3f;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 8.9f;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 5.1f;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 3.7f;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 2.3f;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 1.8f;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 1.1f;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 0.8f;
        } else {
            return 0.2f;
        }
    }

    static boolean isDiversitySufficient(float minWeight, float maxWeight) {
        return ((maxWeight - minWeight) > (minWeight * 0.25));
    }
    
    static final int CLUSTER_QTY_TRESHOLD = 4;
    static float clustersImportanceDependingOn(int clustersQty, int clustered, int nonClustered) {
        return clustersImportance_v2(clustersQty, nonClustered, clustered);
    }
    
    private static float clustersImportance_v2(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0f ;
        }
        if ( clustered < clustersQty * 2 ) {
            return 0;
        }
        
        float ci;
        int clusteredPercent = percentAsInt(clustered, clustered + nonClustered);
        if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
            ci = clusteredPercent * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty) / 10f;
        } else {
            ci = clusteredPercent * clustered * 0.8f / 10f;
        } 
        
        if ( nonClustered > 1 ) {      
            float ratio = 1.0f - (nonClustered * 0.15f);
            if ( ratio < 0.2 ) {
                ratio = 0.2f;
            }
            ci = ci * ratio;
        }
        
        return ci;
    }

    private static float clustersImportance_v1(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0f;
        }
        
        if ( nonClustered == 0 ) {
            if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
                return clustered * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty);
            } else {
                return clustered * clustered * 0.8f;
            }            
        }
        
        if ( clustersQty > CLUSTER_QTY_TRESHOLD ) {
            return ( clustersQty - CLUSTER_QTY_TRESHOLD ) * -8.34f;
        }
        
        float result = 1.32f * ( ( CLUSTER_QTY_TRESHOLD - clustersQty ) * 1.0f ) *
                ( 1.0f + ( ( clustered * 1.0f ) / ( nonClustered * 1.0f ) ) ) *
                ( ( ( clustered * 1.0f ) / ( clustersQty * 1.0f ) ) * 0.8f - 0.79f ) + ( ( clustered - 2 ) * 1.0f ) ;
        return result;
    }
    
    static float lengthImportanceRatio(int length) {
        int lengthSteps = length / 5;
        float ratio = 0.5f + (lengthSteps * 0.07f);
        if ( ratio > 1.0f ) {
            ratio = ratio + lengthSteps * 0.05f;
        }
        return ratio;
    }
    
    static int nonClusteredImportanceDependingOn(int nonClustered, int missed, int patternLength) {
        int importance = (patternLength - 3 + 5);
        return importance * nonClustered;
    }
    
    static boolean isVariantOkWhenAdjusted(Variant variant) {
        return variant.weight() <= 
                ADJUSTED_WEIGHT_TRESHOLD + lengthTolerance(variant.value().length());
    }
    
    static boolean missedTooMuch(int missed, int patterLength) {
        return ( ratio(missed, patterLength) >= 0.32 );
    }

    static float missedImportanceDependingOn(
            int missed, float clustersImportance, int patternLength, int variantLength) {
        if ( missed == 0 ) {
            return 0;
        }
        
        float baseMissedImportance = ( missed - 0.25f ) * missedRatio(clustersImportance);
        return baseMissedImportance;
//        if ( patternLength > variantLength ) {
//            return baseMissedImportance * ( absDiff(patternLength, variantLength) + 1.5 );
//        } else if ( patternLength == variantLength ) {
//            return baseMissedImportance * 1.5;
//        } else {
//            return baseMissedImportance;
//        }
    }
}
