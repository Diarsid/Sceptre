package diarsid.sceptre.impl;

import diarsid.sceptre.api.model.Variant;

import static diarsid.support.misc.MathFunctions.cube;
import static diarsid.support.misc.MathFunctions.percentAsInt;
import static diarsid.support.misc.MathFunctions.ratio;
import static diarsid.support.misc.MathFunctions.square;

class AnalyzeUtil {     
    
    private static final float ADJUSTED_WEIGHT_TRESHOLD = 30;
    
    static int inconsistencyOf(Cluster orderDiff, int clusterLength) {
        int percent = percentAsInt(orderDiff.ordersDiffCount(), clusterLength) / 10;
        return (percent + orderDiff.ordersDiffSumAbs() + (orderDiff.ordersDiffSumAbs()/2) ) * orderDiff.ordersDiffSumAbs();
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

    static int cubeUpTo5AddSquareIfOver(int x) {
        if ( x < 5 ) {
            return cube(x) + square(x-2);
        }
        else if ( x == 5 ) {
            return cube(4) + square(x);
        }

        return cube(4) + square(x+1);
    }

    public static void main(String[] args) {
        System.out.println("2 : " + cubeUpTo5AddSquareIfOver(2));
        System.out.println("3 : " + cubeUpTo5AddSquareIfOver(3));
        System.out.println("4 : " + cubeUpTo5AddSquareIfOver(4));
        System.out.println("5 : " + cubeUpTo5AddSquareIfOver(5));
        System.out.println("6 : " + cubeUpTo5AddSquareIfOver(6));
        System.out.println("7 : " + cubeUpTo5AddSquareIfOver(7));
        System.out.println("8 : " + cubeUpTo5AddSquareIfOver(8));
    }
}
