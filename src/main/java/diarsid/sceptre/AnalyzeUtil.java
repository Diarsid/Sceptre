package diarsid.sceptre;

import java.util.List;

import diarsid.sceptre.api.model.Variant;

import static java.lang.Integer.MIN_VALUE;
import static java.util.stream.Collectors.joining;

import static diarsid.sceptre.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.misc.MathFunctions.meanSmartIngoringZeros;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.ratio;

class AnalyzeUtil {     
    
    private static final double ADJUSTED_WEIGHT_TRESHOLD = 30;
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
        if ( POSITIONS_CLUSTERS.isEnabled() ) {
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diffs         %s", 
                    ints.stream().map(i -> i.toString()).collect(joining(" ")));
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diffs mean    %s", mean);
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
                        logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] mutual +1-1 compensation for %s_vs_%s", current, next);
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
                        logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] mutual +2 0 -2 compensation for %s_vs_%s", previous, next);
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
                                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] compensation for %s_vs_(%s * %s)", lastBeforeRepeat, repeat, repeatQty);
                                diffSumAbs = diffSumAbs - (repeatAbsDiffSum * 2);
                                shifts = shifts + repeatQty;
                                haveCompensation = true;
                                lastBeforeRepeat = UNINITIALIZED;
                            } else if ( absDiff(next, mean) == repeatAbsDiffSum ) {
                                logAnalyze(POSITIONS_CLUSTERS, "              [O-diff] compensation for (%s * %s)_vs_%s", repeat, repeatQty, next);
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
        
        if ( POSITIONS_CLUSTERS.isEnabled() ) {
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order repeats       %s", cluster
                        .repeats()
                        .stream()
                        .map(repeating -> String.valueOf(repeating))
                        .collect(joining(",", "<", ">")));    
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order repeats qties %s", cluster
                        .repeatQties()
                        .stream()
                        .map(repeating -> String.valueOf(repeating))
                        .collect(joining(",", "<", ">")));    
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum real %s", diffSumReal);
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum abs  %s", diffSumAbs);
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diff count    %s", diffCount);
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diff compensation  %s", compensationSum);
        }
        if ( diffSumAbs == 0 && haveCompensation && clusterLength == 2 ) {            
            diffSumAbs = 1;
            logAnalyze(POSITIONS_CLUSTERS, "            [cluster stats] order diff sum fix  %s", diffSumAbs);
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
    
    static double missedRatio(double clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 19.0;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.0;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 12.0;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 10.0;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 8.0;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 6.0;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 4.0;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 2.0;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 1.0;
        } else {
            return 0.5;
        }
    }
    
    static double unsortedRatioDependingOn(double clustersImportance) {
        if ( clustersImportance < 0 ) {
            return 26.8;
        } else if ( clustersImportance >= 0.0 && clustersImportance < 10.0 ) {
            return 14.3;
        } else if ( clustersImportance >= 10.0 && clustersImportance < 20.0 ) {
            return 8.9;
        } else if ( clustersImportance >= 20.0 && clustersImportance < 30.0 ) {
            return 5.1;
        } else if ( clustersImportance >= 30.0 && clustersImportance < 40.0 ) {
            return 3.7;
        } else if ( clustersImportance >= 40.0 && clustersImportance < 60.0 ) {
            return 2.3;
        } else if ( clustersImportance >= 60.0 && clustersImportance < 80.0 ) {
            return 1.8;
        } else if ( clustersImportance >= 80.0 && clustersImportance < 100.0 ) {
            return 1.1;
        } else if ( clustersImportance >= 100.0 && clustersImportance < 130.0 ) {
            return 0.8;
        } else {
            return 0.2;
        }
    }

    static boolean isDiversitySufficient(double minWeight, double maxWeight) {
        return ((maxWeight - minWeight) > (minWeight * 0.25));
    }
    
    static final int CLUSTER_QTY_TRESHOLD = 4;
    static double clustersImportanceDependingOn(int clustersQty, int clustered, int nonClustered) {
        return clustersImportance_v2(clustersQty, nonClustered, clustered);
    }
    
    private static double clustersImportance_v2(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0 ;
        }
        if ( clustered < clustersQty * 2 ) {
            return 0;
        }
        
        double ci;
        int clusteredPercent = percentAsInt(clustered, clustered + nonClustered);
        if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
            ci = clusteredPercent * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty) / 10;
        } else {
            ci = clusteredPercent * clustered * 0.8 / 10;
        } 
        
        if ( nonClustered > 1 ) {      
            double ratio = 1.0 - (nonClustered * 0.15);
            if ( ratio < 0.2 ) {
                ratio = 0.2;
            }
            ci = ci * ratio;
        }
        
        return ci;
    }

    private static double clustersImportance_v1(int clustersQty, int nonClustered, int clustered) {
        if ( clustersQty == 0 ) {
            return CLUSTER_QTY_TRESHOLD * nonClustered * -1.0 ;
        }
        
        if ( nonClustered == 0 ) {
            if ( clustersQty < CLUSTER_QTY_TRESHOLD ) {
                return clustered * clustered * (CLUSTER_QTY_TRESHOLD - clustersQty);
            } else {
                return clustered * clustered * 0.8;
            }            
        }
        
        if ( clustersQty > CLUSTER_QTY_TRESHOLD ) {
            return ( clustersQty - CLUSTER_QTY_TRESHOLD ) * -8.34;
        }
        
        double result = 1.32 * ( ( CLUSTER_QTY_TRESHOLD - clustersQty ) * 1.0 ) *
                ( 1.0 + ( ( clustered * 1.0 ) / ( nonClustered * 1.0 ) ) ) * 
                ( ( ( clustered * 1.0 ) / ( clustersQty * 1.0 ) ) * 0.8 - 0.79 ) + ( ( clustered - 2 ) * 1.0 ) ;
        return result;
    }
    
    static double lengthImportanceRatio(int length) {
        int lengthSteps = length / 5;
        double ratio = 0.5 + (lengthSteps * 0.07);
        if ( ratio > 1.0 ) {
            ratio = ratio + lengthSteps * 0.05;
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

    static double missedImportanceDependingOn(
            int missed, double clustersImportance, int patternLength, int variantLength) {
        if ( missed == 0 ) {
            return 0;
        }
        
        double baseMissedImportance = ( missed - 0.25 ) * missedRatio(clustersImportance);
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
