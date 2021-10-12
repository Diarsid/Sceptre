package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;

import diarsid.sceptre.impl.logs.AnalyzeLogType;
import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.StatefulClearable;
import diarsid.support.objects.references.Possible;

import static diarsid.sceptre.impl.Typos.Placing.BEFORE;
import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.support.objects.Pools.pools;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

class Typos implements StatefulClearable, AutoCloseable {
    
    static enum Placing {
        BEFORE,
        AFTER
    }
    
    private final GuardedPool<Typo> typosPool;
    private final Possible<String> pattern;
    private final Possible<String> variant;
    private final List<Typo> before;
    private final List<Typo> after;

    Typos() {
        this.typosPool = pools().createPool(Typo.class, () -> new Typo());
        this.pattern = simplePossibleButEmpty();
        this.variant = simplePossibleButEmpty();
        this.before = new ArrayList<>();
        this.after = new ArrayList<>();
    }
    
    void set(String variant, String pattern) {
        this.variant.resetTo(variant);
        this.pattern.resetTo(pattern);
    }
    
    void findIn(
            Placing placing,
            int variantFromIncl, int variantToExcl,
            int patternFromIncl, int patternToExcl) {
        logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "          [info] typo searching:");
        logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             in variant from incl. %s to excl %s - %s",
                                     variantFromIncl,
                                     variantToExcl,
                                     this.variant.orThrow().substring(variantFromIncl, variantToExcl));
        logAnalyze(AnalyzeLogType.POSITIONS_SEARCH, "             in pattern from incl. %s to excl %s - %s",
                                     patternFromIncl,
                                     patternToExcl,
                                     this.pattern.orThrow().substring(patternFromIncl, patternToExcl));
        if ( variantFromIncl < 0 ) {
            throw new IllegalArgumentException();
        }
        if ( patternFromIncl < 0 ) {
            throw new IllegalArgumentException();
        }
        if ( variantFromIncl >= variantToExcl ) {
            throw new IllegalArgumentException();
        }
        if ( patternFromIncl >= patternToExcl ) {
            throw new IllegalArgumentException();
        }

        String variantString = variant.orThrow();
        String patternString = pattern.orThrow();
        int variantLength = variantString.length();
        int patternLength = patternString.length();

        if ( variantToExcl > variantLength ) {
            throw new IllegalArgumentException();
        }
        if ( patternToExcl > patternLength ) {
            throw new IllegalArgumentException();
        }

        char charInVariant;
        char charInPattern;

        for (int vi = variantFromIncl; vi < variantToExcl && vi < variantLength; vi++) {
            charInVariant = variantString.charAt(vi);
            for (int pi = patternFromIncl; pi < patternToExcl && pi < patternLength; pi++) {
                charInPattern = patternString.charAt(pi);
                if ( charInVariant == charInPattern ) {
                    Typo typo = this.typosPool.give();
                    typo.set(vi, pi, charInVariant);
                    if (placing == BEFORE) {
                        this.before.add(typo);
                    } else {
                        this.after.add(typo);
                    }
                }
            }
        }
    }
    
    int qtyBefore() {
        return this.before.size();
    }
    
    int qtyAfter() {
        return this.after.size();
    }
    
    int qtyTotal() {
        return this.qtyBefore() + this.qtyAfter();
    }
    
    boolean hasBefore() {
        return this.before.size() > 0;
    }
    
    boolean hasAfter() {
        return this.after.size() > 0;
    }
    
    boolean hasInBefore(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.before);
    }
    
    boolean hasInAfter(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.after);
    }
    
    private static boolean hasIndexInTypos(int variantIndex, List<Typo> typos) {
        Typo typo;
        for (int i = 0; i < typos.size(); i++) {
            typo = typos.get(i);
            if ( typo.variantIndex() == variantIndex ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Typos{" + "before=" + before + ", after=" + after + '}';
    }

    @Override
    public void clear() {
        this.pattern.nullify();
        this.variant.nullify();
        this.typosPool.takeBackAll(this.before);
        this.typosPool.takeBackAll(this.after);
    }

    public void copyFrom(Typos other) {
        this.pattern.resetTo(other.pattern);
        this.variant.resetTo(other.variant);

        this.typosPool.takeBackAll(this.before);
        this.typosPool.takeBackAll(this.after);

        copyTyposLists(this.before, other.before);
        copyTyposLists(this.after, other.after);
    }

    private void copyTyposLists(List<Typo> target, List<Typo> src) {
        for ( Typo typoSrc : src ) {
            Typo typo = this.typosPool.give();
            typo.copyFrom(typoSrc);
            target.add(typo);
        }
    }

    @Override
    public void close() {
        this.clear();
    }
    
}
