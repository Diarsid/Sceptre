package diarsid.sceptre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import diarsid.sceptre.api.model.Variant;
import diarsid.sceptre.api.model.Variants;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;
import static diarsid.support.objects.collections.Lists.lastFrom;
import static diarsid.support.strings.StringIgnoreCaseUtil.startsIgnoreCase;
import static diarsid.support.strings.StringUtils.lower;

public class VariantsImpl implements Variants {
    
    private static final int FEED_ALGORITHM_VERSION = 2;
    
    private final List<Variant> variants;
    private final double bestWeight;
    private final double worstWeight;
    private final double weightDifference;
    private final double weightStep;
    private List<Variant> currentSimilarVariants;
    private int currentVariantIndex;

    VariantsImpl(List<Variant> variants) {
        sort(variants);
        this.variants = unmodifiableList(variants);
        if ( nonEmpty(this.variants) ) {
            this.bestWeight = variants.get(0).weight();
            this.worstWeight = lastFrom(variants).weight();
            this.weightDifference = absDiff(this.bestWeight, this.worstWeight);
            this.weightStep = this.weightDifference / this.variants.size();
        } else {
            this.bestWeight = 0;
            this.worstWeight = 0;
            this.weightDifference = 0;
            this.weightStep = 0;
        }
        this.currentVariantIndex = -1;
        this.currentSimilarVariants = null;
    }
    
    public static Variants unite(List<Variant> variants) {
        return new VariantsImpl(variants);
    }
    
    public static Optional<Variant> findVariantEqualToPattern(
            List<Variant> variants) {
        return variants
                .stream()
                .filter(variant -> variant.isEqualToPattern())
                .findFirst();
    }
    
    @Override
    public IntStream indexes() {
        return this.variants.stream().mapToInt(variant -> variant.index());
    }
    
    @Override
    public void resetTraversing() {
        this.currentVariantIndex = -1;
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
    }
    
    @Override
    public void setTraversingToPositionBefore(int variantIndex) {
        if ( variantIndex > -1 ) {
            variantIndex--; // adjust for subsequent .next() call
        }
        this.currentVariantIndex = variantIndex;
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
    } 
    
    @Override
    public int currentTraverseIndex() {
        if ( nonEmpty(this.currentSimilarVariants) ) {
            // +1 is used in orded to balance subsequent index-- when .next() will be called.
            return this.currentVariantIndex - this.currentSimilarVariants.size() + 1;
        } else {
            return this.currentVariantIndex;
        }        
    }
    
    @Override
    public boolean isEmpty() {
        return this.variants.isEmpty();
    }
    
    @Override
    public boolean isNotEmpty() {
        return ! this.variants.isEmpty();
    }
    
    @Override
    public Variant best() {
        return this.variants.get(0);
    }
    
    @Override
    public String stamp() {
        return this.variants
                .stream()
                .map(variant -> lower(variant.value()))
                .collect(joining(";"));
    }
    
    @Override
    public void removeHavingSameStartAs(Variant variant) {
        Variant current;
        
        for (int i = 0; i < this.variants.size(); i++) {
            current = this.variants.get(i);
            if ( current.index() <= variant.index()) {
                continue;
            }
            
            if ( startsIgnoreCase(current.nameOrValue(), variant.nameOrValue()) ) {
                this.variants.remove(i);
                i--;
            }
        }
    }
    
    @Override
    public Variants removeWorseThan(String variantValue) {
        List<Variant> moidifiableVariants = new ArrayList<>(this.variants);
        boolean needToRemove = false;
        for (int i = 0; i < moidifiableVariants.size(); i++) {
            if ( needToRemove ) {
                moidifiableVariants.remove(i);
                i--;
            } else {
                if ( moidifiableVariants.get(i).value().equalsIgnoreCase(variantValue) ) {
                    needToRemove = true;
                }
            }
        }
        
        return new VariantsImpl(moidifiableVariants);
    }
    
    @Override
    public String getVariantAt(int i) {
        return this.variants.get(i).value();
    }
    
    @Override
    public int size() {
        return this.variants.size();
    }
    
    @Override
    public boolean isChoiceInSimilarVariantsNaturalRange(int number) {
        if ( nonNull(this.currentSimilarVariants) ) {
            return ( 0 < number ) && ( number < (this.currentSimilarVariants.size() + 1) );
        } else {
            return false;
        }
    }
    
//    public Answer answerWith(int choiceNumber) {
//        if ( this.isChoiceInSimilarVariantsNaturalRange(choiceNumber) ) {
//            return answerOfVariant(this.currentSimilarVariants.get(choiceNumber - 1));
//        } else {
//            return variantsDontContainSatisfiableAnswer();
//        }
//    }
//
//    public Answer ifPartOfAnySimilarVariant(String possibleFragment) {
//        List<Answer> matches = this.currentSimilarVariants
//                .stream()
//                .filter(variant -> {
//                    if ( variant.doesHaveName() ) {
//                        return containsIgnoreCase(variant.name(), possibleFragment);
//                    } else {
//                        return containsIgnoreCase(variant.value(), possibleFragment);
//                    }
//                })
//                .map(variant -> answerOfVariant(variant))
//                .collect(toList());
//        if ( CollectionsUtils.hasOne(matches) ) {
//            return CollectionsUtils.getOne(matches);
//        } else {
//            return variantsDontContainSatisfiableAnswer();
//        }
//    }
    
    @Override
    public boolean hasOne() {
        return ( this.variants.size() == 1 );
    }
    
    @Override
    public boolean hasMany() {
        return ( this.variants.size() > 1 );
    }
    
//    public Answer singleAnswer() {
//        return answerOfVariant(getOne(this.variants));
//    }
    
    @Override
    public boolean next() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
        if ( this.currentVariantIndex < this.variants.size() - 1 ) {
            this.currentVariantIndex++;
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean currentIsMuchBetterThanNext() {
        if ( this.currentVariantIndex < this.variants.size() - 1 ) {
            if ( this.currentVariantIndex < 0 ) {
                throw new IllegalStateException(
                        "Unexpected behavior: call .next() before accessing variants!");
            }
            
            double currentWeight = this.current().weight();
            double nextWeight = this.variants.get(this.currentVariantIndex + 1).weight();
            
            switch ( FEED_ALGORITHM_VERSION ) {
                case 1 : {
                    if ( this.currentVariantIndex == 0 ) {
                        return absDiff(currentWeight, nextWeight) >= 1.0;
                    } else if ( this.currentVariantIndex < 4 ) {
                        return absDiff(currentWeight, nextWeight) >= 2.0;
                    } else {
                        return ( currentWeight * 0.8 < nextWeight );
                    }
                }
                case 2 : {
                    if ( this.currentVariantIndex == 0 ) {
                        return absDiff(currentWeight, nextWeight) >= this.weightStep;
                    } else if ( this.currentVariantIndex < 4 ) {
                        return absDiff(currentWeight, nextWeight) >= this.weightDifference / 4 ;
                    } else {
                        return absDiff(currentWeight, nextWeight) >= this.weightDifference / 2 ;
                    }
                }
                default : {
                    throw new IllegalStateException(format(
                            "There is not feed algorithm with version %s", FEED_ALGORITHM_VERSION));
                }
            }
            
        } else if ( this.currentVariantIndex == this.variants.size() - 1 ) {
            return true;
        } else {
            throw new IllegalStateException("Unexpected behavior.");
        }
    }
    
    @Override
    public Variant current() {
        return this.variants.get(this.currentVariantIndex);
    }
    
    @Override
    public List<Variant> nextSimilarVariants() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        } else {
            this.currentSimilarVariants = new ArrayList<>();
        }        
        boolean proceed = true;
        while ( this.currentVariantIndex < this.variants.size() && proceed ) {
            if ( ! this.currentIsMuchBetterThanNext() ) {
                this.currentSimilarVariants.add(this.current());
                this.currentVariantIndex++;
            } else {
                this.currentSimilarVariants.add(this.current());
                proceed = false;
            }            
        }
        return this.currentSimilarVariants;
    }

    @Override
    public int indexOf(String string) {
        for (int i = 0; i < this.variants.size(); i++) {
            if ( this.variants.get(i).value().equals(string) ) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(Variant variant) {
        return this.variants.indexOf(variant);
    }
}
