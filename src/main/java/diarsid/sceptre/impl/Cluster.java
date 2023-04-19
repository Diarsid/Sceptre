/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre.impl;


import java.util.Objects;

import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.support.objects.PooledReusable;

import static java.lang.Math.abs;
import static java.lang.String.format;

import static diarsid.sceptre.impl.AnalyzeImpl.logAnalyze;
import static diarsid.sceptre.impl.collections.Ints.sumInts;

class Cluster 
        extends 
                PooledReusable 
        implements 
                Comparable<Cluster> {
    
    private final ListInt repeats;
    private final ListInt repeatQties;
    private int firstPosition;
    private int patternLength;
    private int length;
    private int ordersDiffMean;
    private int ordersDiffSumReal;
    private int ordersDiffSumAbs;
    private int ordersDiffCount;
    private int ordersDiffShifts;
    private boolean ordersDiffHaveCompensation;
    private boolean hasOnlyOneExccessCharBetween;
    private int compensationSum;
    private int teardown;
    private boolean rejected;

    Cluster() {
        super();
        this.repeats = new ListIntImpl();
        this.repeatQties = new ListIntImpl();
        this.firstPosition = PositionsAnalyze.POS_UNINITIALIZED;
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSumReal = 0;
        this.ordersDiffSumAbs = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
        this.hasOnlyOneExccessCharBetween = false;
        this.compensationSum = 0;
        this.teardown = 0;
        this.rejected = false;
    }
    
    void set(
            int firstPosition,
            int patternLength,
            int length, 
            int mean,  
            int diffSumReal,
            int diffSumAbs, 
            int diffCount, 
            int shifts, 
            boolean haveCompensation,
            int compensationSum,
            boolean rejected) {
        this.firstPosition = firstPosition;
        this.patternLength = patternLength;
        this.length = length;
        this.ordersDiffMean = mean;
        this.ordersDiffSumReal = diffSumReal;
        this.ordersDiffSumAbs = diffSumAbs;
        this.ordersDiffCount = diffCount;
        this.ordersDiffShifts = shifts;
        this.ordersDiffHaveCompensation = haveCompensation;
        this.compensationSum = compensationSum;
        this.rejected = rejected;
    }
    
    ListInt repeats() {
        return this.repeats;
    }
    
    ListInt repeatQties() {
        return this.repeatQties;
    }
    
    int firstPosition() {
        return this.firstPosition;
    }

    int position(int order) {
        if ( order < 0 ) {
            throw new IllegalArgumentException();
        }

        if ( order == 0 ) {
            return this.firstPosition;
        }

        if ( order > length-1 ) {
            throw new IllegalArgumentException();
        }

        return this.firstPosition + order;
    }

    boolean contains(int position) {
        return this.firstPosition <= position && position <= this.lastPosition();
    }
    
    int lastPosition() {
        return this.firstPosition + this.length - 1;
    }
    
    int length() {
        return this.length;
    }
    
    int positionsMean() {
        return (this.firstPosition * 2 + this.length) / 2;
    }
    
    int ordersDiffMean() {
        return this.ordersDiffMean;
    }

    int ordersDiffSumReal() {
        return this.ordersDiffSumReal;
    }

    int ordersDiffSumAbs() {
        return this.ordersDiffSumAbs;
    }
    
    int ordersDiffCount() {
        return this.ordersDiffCount;
    }

    int ordersDiffShifts() {
        return this.ordersDiffShifts;
    }
    
    boolean hasOrdersDiff() {
        return this.ordersDiffSumAbs > 0;
    }
    
    boolean hasOnlyOneExccessCharBetween() {
        return this.hasOnlyOneExccessCharBetween;
    }
    
    boolean hasOrdersDiffShifts() {
        return this.ordersDiffShifts > 0;
    }
    
    boolean haveOrdersDiffCompensations() {
        return this.ordersDiffHaveCompensation;
    }
    
    int compensationSum() {
        return this.compensationSum;
    }
    
    int teardown() {
        return this.teardown;
    }
    
    boolean hasTeardown() {
        return this.teardown > 0;
    }

    boolean isRejected() {
        return this.rejected;
    }

    boolean intersectsWith(WordInVariant word) {
        int lastPosition = this.lastPosition();

        boolean isCompletelyInWord =
                this.firstPosition >= word.startIndex &&
                lastPosition <= word.endIndex;

        if ( isCompletelyInWord ) {
            return true;
        }

        boolean wordIsCompletelyInCluster =
                word.startIndex >= this.firstPosition &&
                word.endIndex <= lastPosition;

        if ( wordIsCompletelyInCluster ) {
            return true;
        }

        boolean clusterTouchesWordStart =
                word.startIndex <= this.firstPosition &&
                word.endIndex <= lastPosition &&
                word.endIndex > this.firstPosition;

        if ( clusterTouchesWordStart ) {
            return true;
        }

        boolean clusterTouchesWordEnd =
                word.startIndex >= this.firstPosition &&
                word.startIndex <= lastPosition &&
                word.endIndex > lastPosition;

        if ( clusterTouchesWordEnd ) {
            return true;
        }

        return false;
    }
    
    void finish() {
        if ( this.length > 2 ) {
            boolean repeatsContainsZero = this.repeats.contains(0);
            boolean repeatsContainsAbsOne = this.repeats.contains(-1) || this.repeats.contains(1);
            boolean repeateOnlyTwo = this.repeatQties.size() == 2;

            this.hasOnlyOneExccessCharBetween =
                    repeatsContainsZero && repeatsContainsAbsOne && repeateOnlyTwo;
        }
        else {
            this.hasOnlyOneExccessCharBetween = this.ordersDiffSumReal == 1;
        }

    }
    
    boolean testOnTeardown() {
        if ( this.compensationSum > this.length ) {
            this.tearDownOn(this.length);
            return true;
        } else {
            if ( this.ordersDiffCount == 0 && this.ordersDiffSumAbs == 0 ) {
                return false;
            } else if ( this.ordersDiffCount > 0 && this.ordersDiffSumAbs == 0 ) {
                return this.tryToTearDownBasingOnDiffCountOnly();
            } else if ( this.ordersDiffSumAbs > 0 && this.ordersDiffCount == 0 ) {
                return this.tryToTearDownBasingOnDiffSumOnly();
            } else {
                return this.tryToTearDownBasingOnDiffSumAndCount();
            }
        }        
    }
    
    private boolean considerDiffCountCompensationWhen() {
        boolean tolerate = true;
        
        if ( this.length <= this.patternLength / 2 ) {
            return true;
        }
        
        if ( this.length < 4 ) {
            return false;
        }
        
        return tolerate;
    }
    
    private boolean tryToTearDownBasingOnDiffCountOnly() {
        boolean isToTeardown = false;
        
        if ( this.ordersDiffCount > this.length / 2 ) {
            if ( this.considerDiffCountCompensationWhen() ) {
                if ( this.compensationSum < this.ordersDiffCount ) {
                    this.tearDownOn(this.ordersDiffCount - this.compensationSum);
                    isToTeardown = true;
                }
            } else {
                this.tearDownOn(this.ordersDiffCount);
                isToTeardown = true;
            }            
        } else {
            if ( this.ordersDiffHaveCompensation ) {
                if ( this.compensationSum < this.ordersDiffCount ) {
                    this.tearDownOn(this.ordersDiffCount - this.compensationSum);
                    isToTeardown = true;
                }
            }
        }
        
        return isToTeardown;
    }
    
    private boolean tryToTearDownBasingOnDiffSumOnly() {
        this.tearDownOn(this.ordersDiffSumAbs);
        return true;
    }
    
    private boolean tryToTearDownBasingOnDiffSumAndCount() {  
        if ( this.hasOnlyOneExccessCharBetween ) {
            return false;
        }

        if ( this.length > 4 && this.ordersDiffCount == 1 ) {
            return false;
        }

        int tearDown = this.length - sumInts(this.repeatQties);
        
        if ( this.length > 2 && repeats.isEmpty() ) {
            tearDown = this.length;
        } 
        else if ( this.ordersDiffHaveCompensation ) {
            if ( this.considerDiffCountCompensationWhen() ) {
                tearDown = tearDown - this.compensationSum;
            }
        }       
        
        this.tearDownOn(tearDown);
        return true;
    }
    
    private void tearDownOn(int positionsQty) {
        positionsQty = abs(positionsQty);
        this.teardown = positionsQty;
        logAnalyze(AnalyzeLogType.POSITIONS_CLUSTERS, "               [TEARDOWN] cluster is to be teardown by %s", positionsQty);
    }

    @Override
    public void clearForReuse() {
        this.repeats.clear();
        this.repeatQties.clear();
        this.firstPosition = PositionsAnalyze.POS_UNINITIALIZED;
        this.patternLength = 0;
        this.length = 0;
        this.ordersDiffMean = 0;
        this.ordersDiffSumReal = 0;
        this.ordersDiffSumAbs = 0;
        this.ordersDiffCount = 0;
        this.ordersDiffShifts = 0;
        this.ordersDiffHaveCompensation = false;
        this.hasOnlyOneExccessCharBetween = false;
        this.compensationSum = 0;
        this.teardown = 0;
        this.rejected = false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.repeats);
        hash = 47 * hash + Objects.hashCode(this.repeatQties);
        hash = 47 * hash + this.firstPosition;
        hash = 47 * hash + this.patternLength;
        hash = 47 * hash + this.length;
        hash = 47 * hash + this.ordersDiffMean;
        hash = 47 * hash + this.ordersDiffSumReal;
        hash = 47 * hash + this.ordersDiffSumAbs;
        hash = 47 * hash + this.ordersDiffCount;
        hash = 47 * hash + this.ordersDiffShifts;
        hash = 47 * hash + (this.ordersDiffHaveCompensation ? 1 : 0);
        hash = 47 * hash + (this.hasOnlyOneExccessCharBetween ? 1 : 0);
        hash = 47 * hash + this.compensationSum;
        hash = 47 * hash + this.teardown;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final Cluster other = ( Cluster ) obj;
        if ( this.firstPosition != other.firstPosition ) {
            return false;
        }
        if ( this.patternLength != other.patternLength ) {
            return false;
        }
        if ( this.length != other.length ) {
            return false;
        }
        if ( this.ordersDiffMean != other.ordersDiffMean ) {
            return false;
        }
        if ( this.ordersDiffSumReal != other.ordersDiffSumReal ) {
            return false;
        }
        if ( this.ordersDiffSumAbs != other.ordersDiffSumAbs ) {
            return false;
        }
        if ( this.ordersDiffCount != other.ordersDiffCount ) {
            return false;
        }
        if ( this.ordersDiffShifts != other.ordersDiffShifts ) {
            return false;
        }
        if ( this.ordersDiffHaveCompensation != other.ordersDiffHaveCompensation ) {
            return false;
        }
        if ( this.hasOnlyOneExccessCharBetween != other.hasOnlyOneExccessCharBetween ) {
            return false;
        }
        if ( this.compensationSum != other.compensationSum ) {
            return false;
        }
        if ( this.teardown != other.teardown ) {
            return false;
        }
        if ( !Objects.equals(this.repeats, other.repeats) ) {
            return false;
        }
        if ( !Objects.equals(this.repeatQties, other.repeatQties) ) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Cluster other) {
        if ( this.firstPosition < other.firstPosition ) {
            return -1;
        } else if ( this.firstPosition > other.firstPosition ) {
            return 1;
        } else {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        switch ( this.length ) {
            case 0:
            case 1:
                throw new IllegalStateException("Cluster cannot have length lower than 2");
            case 2:
                return format("Cluster[%s,%s]", this.firstPosition, this.lastPosition());
            case 3:
                return format("Cluster[%s,%s,%s]", 
                              this.firstPosition, this.firstPosition + 1, this.lastPosition());
            case 4:
                return format("Cluster[%s,%s,%s,%s]", 
                          this.firstPosition, this.firstPosition + 1, this.firstPosition + 2, this.lastPosition());
            default:
                return format("Cluster[%s...%s]", this.firstPosition, this.lastPosition());
        }
    }
    
}
