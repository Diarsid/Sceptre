package diarsid.sceptre;

import java.util.ArrayList;
import java.util.List;

import diarsid.support.objects.GuardedPool;
import diarsid.support.objects.StatefulClearable;
import diarsid.support.objects.references.Possible;

import static diarsid.sceptre.AnalyzeLogType.POSITIONS_SEARCH;
import static diarsid.sceptre.Typos.Placing.BEFORE;
import static diarsid.sceptre.WeightAnalyzeReal.logAnalyze;
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
    private final List<Typo> typosBefore;
    private final List<Typo> typosAfter;

    Typos() {
        this.typosPool = pools().createPool(Typo.class, () -> new Typo());
        this.pattern = simplePossibleButEmpty();
        this.variant = simplePossibleButEmpty();
        this.typosBefore = new ArrayList<>();
        this.typosAfter = new ArrayList<>();
    }
    
    void set(String variant, String pattern) {
        this.variant.resetTo(variant);
        this.pattern.resetTo(pattern);
    }
    
    void findIn(
            Placing placing,
            int variantFromIncl, int variantToExcl,
            int patternFromIncl, int patternToExcl) {
        logAnalyze(POSITIONS_SEARCH, "          [info] typo searching:");
        logAnalyze(POSITIONS_SEARCH, "             in variant from incl. %s to excl %s - %s",
                                     variantFromIncl,
                                     variantToExcl,
                                     this.variant.orThrow().substring(variantFromIncl, variantToExcl));
        logAnalyze(POSITIONS_SEARCH, "             in pattern from incl. %s to excl %s - %s",
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
                        this.typosBefore.add(typo);
                    } else {
                        this.typosAfter.add(typo);
                    }
                }
            }
        }
    }
    
    int qtyBefore() {
        return this.typosBefore.size();
    }
    
    int qtyAfter() {
        return this.typosAfter.size();
    }
    
    int qtyTotal() {
        return this.qtyBefore() + this.qtyAfter();
    }
    
    boolean hasBefore() {
        return this.typosBefore.size() > 0;
    }
    
    boolean hasAfter() {
        return this.typosAfter.size() > 0;
    }
    
    boolean hasInBefore(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.typosBefore);
    }
    
    boolean hasInAfter(int variantIndex) {
        return hasIndexInTypos(variantIndex, this.typosAfter);
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
        return "Typos{" + "typosBefore=" + typosBefore + ", typosAfter=" + typosAfter + '}';
    }

    @Override
    public void clear() {
        this.pattern.nullify();
        this.variant.nullify();
        this.typosPool.takeBackAll(this.typosBefore);
        this.typosPool.takeBackAll(this.typosAfter);
    }

    public void copyFrom(Typos other) {
        this.pattern.resetTo(other.pattern);
        this.variant.resetTo(other.variant);

        this.typosPool.takeBackAll(this.typosBefore);
        this.typosPool.takeBackAll(this.typosAfter);

        copyTyposLists(this.typosBefore, other.typosBefore);
        copyTyposLists(this.typosAfter, other.typosAfter);
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
