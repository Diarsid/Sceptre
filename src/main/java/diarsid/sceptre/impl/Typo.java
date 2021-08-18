package diarsid.sceptre.impl;

import java.util.Objects;

import diarsid.support.objects.PooledReusable;
import diarsid.support.objects.references.Possible;

import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public class Typo extends PooledReusable {
    
    private final Possible<Integer> variantIndex;
    private final Possible<Integer> patternIndex;
    private final Possible<Character> character;

    Typo() {
        this.variantIndex = simplePossibleButEmpty();
        this.patternIndex = simplePossibleButEmpty();
        this.character = simplePossibleButEmpty();
    }
    
    public void set(int variantI, int patternI, char c) {
        this.variantIndex.resetTo(variantI);
        this.patternIndex.resetTo(patternI);
        this.character.resetTo(c);
    }

    public int variantIndex() {
        return this.variantIndex.orThrow();
    }

    public int patternIndex() {
        return this.patternIndex.orThrow();
    }

    public char character() {
        return this.character.orThrow();
    }

    public void copyFrom(Typo other) {
        this.variantIndex.resetTo(other.variantIndex);
        this.patternIndex.resetTo(other.patternIndex);
        this.character.resetTo(other.character);
    }

    @Override
    protected void clearForReuse() {
        this.variantIndex.nullify();
        this.patternIndex.nullify();
        this.character.nullify();
    }

    @Override
    public String toString() {
        return "Typo{" + "variant=" + variantIndex + ", pattern=" + 
               patternIndex + ", char=" + character + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.variantIndex);
        hash = 79 * hash + Objects.hashCode(this.patternIndex);
        hash = 79 * hash + Objects.hashCode(this.character);
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
        final Typo other = ( Typo ) obj;
        if ( !Objects.equals(this.variantIndex, other.variantIndex) ) {
            return false;
        }
        if ( !Objects.equals(this.patternIndex, other.patternIndex) ) {
            return false;
        }
        if ( !Objects.equals(this.character, other.character) ) {
            return false;
        }
        return true;
    }
    
}
