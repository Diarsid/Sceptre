package diarsid.sceptre.api.model;

import java.io.Serializable;
import java.util.Objects;

import diarsid.sceptre.api.Sceptre;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import static diarsid.support.strings.StringUtils.lower;

public final class Variant implements Serializable, Reindexable, Weightable {
    
    private String value;
    private String name;
    private int index;
    
    private Boolean equalsToPattern;
    private float weight;
    
    public Variant(String value, int variantIndex) {
        this.value = value;
        this.name = "";
        this.index = variantIndex;
    }
    
    public Variant(String value, String name, int variantIndex) {
        this.value = value;
        this.name = name;
        this.index = variantIndex;
    }
    
    protected Variant(Variant other) {
        this.value = other.value;
        this.name = other.name;
        this.index = other.index;
    }
    
    public boolean doesHaveName() {
        return ! this.name.isEmpty();
    }

    public String nameOrValue() {
        return this.doesHaveName() ? this.name : this.value;
    }
    
    public String value() {
        return this.value;
    }

    public String name() {
        return this.name;
    }
    
    public Variant set(float weight, boolean equalsToPattern) {
        if ( nonNull(this.equalsToPattern) ) {
            throw new IllegalStateException();
        }
        
        this.weight = weight;
        this.equalsToPattern = equalsToPattern;
        
        return this;
    }
    
    public Variant retainInNameOnly(String part) {
        if ( this.name.contains(part) ) {
            this.name = part;
        } else {
            throw new IllegalArgumentException(format(
                    "Variant name '%s' does not contain '%s'", this.name, part));
        }
        return this;
    }
    
    public Variant retainInValueOnly(String part) {
        if ( this.value.contains(part) ) {
            this.value = part;
        } else {
            throw new IllegalArgumentException(format(
                    "Variant value '%s' does not contain '%s'", this.value, part));
        }
        return this;
    }
    
    public boolean hasEqualOrBetterWeightThan(Sceptre.Weight.Estimate otherEstimate) {
        return Sceptre.Weight.Estimate.of(this).isEqualOrBetterThan(otherEstimate);
    }

    @Override
    public boolean isBetterThan(Weightable other) {
        return this.weight < other.weight();
    }
    
    public boolean isEqualToPattern() {
        return this.equalsToPattern;
    }

    @Override
    public float weight() {
        return this.weight;
    }
    
    public boolean equalsByLowerValue(Variant variant) {
        return lower(this.value).equals(lower(variant.value));
    }
    
    public boolean equalsByLowerName(Variant variant) {
        return this.doesHaveName() && 
                lower(this.name).equals(lower(variant.name));
    }
    
    @Override
    public int index() {
        return this.index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.value);
        hash = 89 * hash + Objects.hashCode(this.name);
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
        final Variant other = ( Variant ) obj;
        if ( !Objects.equals(this.value, other.value) ) {
            return false;
        }
        if ( !Objects.equals(this.name, other.name) ) {
            return false;
        }
        return true;
    }
    
    public boolean doesNotEqual(Variant other) {
        return ! this.equals(other);
    }
    
    @Override
    public int compareTo(Weightable other) {
        float otherWeight = other.weight();

        if ( this.weight > otherWeight ) {
            return 1;
        } else if ( this.weight < otherWeight ) {
            return -1;
        } else {
            if ( this.index >  otherWeight ) {
                return 1;
            } else if ( this.index < otherWeight ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
