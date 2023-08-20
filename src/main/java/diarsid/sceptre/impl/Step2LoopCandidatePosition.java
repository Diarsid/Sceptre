package diarsid.sceptre.impl;

import java.util.Comparator;
import java.util.Objects;

import diarsid.support.objects.PooledReusable;

public class Step2LoopCandidatePosition extends PooledReusable {

    public static final Comparator<Step2LoopCandidatePosition> FIRST_IS_LAST_IN_VARIANT = (o0, o1) -> {
        return Integer.compare(o1.variantPosition, o0.variantPosition);
    };

    public static final Comparator<Step2LoopCandidatePosition> FIRST_IS_LAST_IN_PATTERN = (o0, o1) -> {
        return Integer.compare(o1.patternPosition, o0.patternPosition);
    };
    
    char c;
    int patternPosition;
    int variantPosition;
    Boolean isFilledInVariant;
    Boolean isFilledInPattern;

    public Step2LoopCandidatePosition() {
        this.clearForReuse();
    }

    public void set(
            char c,
            int patternPosition,
            int variantPosition,
            boolean isFilledInVariant,
            boolean isFilledInPattern) {
        this.c = c;
        this.patternPosition = patternPosition;
        this.variantPosition = variantPosition;
        this.isFilledInVariant = isFilledInVariant;
        this.isFilledInPattern = isFilledInPattern;
    }

    @Override
    protected void clearForReuse() {
        this.c = '_';
        this.patternPosition = Integer.MIN_VALUE;
        this.variantPosition = Integer.MIN_VALUE;
        this.isFilledInVariant = null;
        this.isFilledInPattern = null;
    }

    public boolean is(Step2LoopCandidatePosition other) {
        return this.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step2LoopCandidatePosition)) return false;
        Step2LoopCandidatePosition that = (Step2LoopCandidatePosition) o;
        return c == that.c &&
                patternPosition == that.patternPosition &&
                variantPosition == that.variantPosition &&
                Objects.equals(isFilledInVariant, that.isFilledInVariant) &&
                Objects.equals(isFilledInPattern, that.isFilledInPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, patternPosition, variantPosition, isFilledInVariant, isFilledInPattern);
    }
}
