package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Outputs;
import diarsid.sceptre.api.model.Variant;

import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.nonNull;

import static diarsid.support.misc.MathFunctions.absDiff;
import static diarsid.support.objects.collections.CollectionUtils.nonEmpty;
import static diarsid.support.objects.collections.Lists.lastFrom;
import static diarsid.support.strings.StringIgnoreCaseUtil.startsIgnoreCase;

public class OutputsImpl implements Outputs {
    
    private static final int FEED_ALGORITHM_VERSION = 2;
    
    private final List<Output> outputs;
    private final float bestWeight;
    private final float worstWeight;
    private final float weightDifference;
    private final float weightStep;
    private List<Output> currentSimilarVariants;
    private int currentVariantIndex;

    OutputsImpl(List<Output> outputs) {
        sort(outputs);
        this.outputs = unmodifiableList(outputs);
        if ( nonEmpty(this.outputs) ) {
            this.bestWeight = outputs.get(0).weight();
            this.worstWeight = lastFrom(outputs).weight();
            this.weightDifference = (float) absDiff(this.bestWeight, this.worstWeight);
            this.weightStep = this.weightDifference / this.outputs.size();
        }
        else {
            this.bestWeight = 0;
            this.worstWeight = 0;
            this.weightDifference = 0;
            this.weightStep = 0;
        }
        this.currentVariantIndex = -1;
        this.currentSimilarVariants = null;
    }
    
    public static Outputs unite(List<Output> variants) {
        return new OutputsImpl(variants);
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
        return this.outputs.isEmpty();
    }
    
    @Override
    public boolean isNotEmpty() {
        return ! this.outputs.isEmpty();
    }
    
    @Override
    public Output best() {
        return this.outputs.get(0);
    }
    
    @Override
    public Outputs removeHavingStart(String start) {
        Output current;
        List<Output> newOutputs = new ArrayList<>(this.outputs);
        for (int i = 0; i < newOutputs.size(); i++) {
            current = newOutputs.get(i);
            
            if ( startsIgnoreCase(current.input(), start) ) {
                newOutputs.remove(i);
                i--;
            }
        }

        return new OutputsImpl(newOutputs);
    }
    
    @Override
    public Outputs removeWorseThan(String input) {
        List<Output> newOutputs = new ArrayList<>(this.outputs);

        Output output;
        boolean needToRemove = false;
        for (int i = 0; i < newOutputs.size(); i++) {
            output = newOutputs.get(i);

            if ( needToRemove ) {
                newOutputs.remove(i);
                i--;
                continue;
            }

            if ( output.input().equalsIgnoreCase(input) ) {
                needToRemove = true;
            }
        }
        
        return new OutputsImpl(newOutputs);
    }
    
    @Override
    public Output get(int i) {
        return this.outputs.get(i);
    }
    
    @Override
    public int size() {
        return this.outputs.size();
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
        return this.outputs.size() == 1;
    }
    
    @Override
    public boolean hasMany() {
        return this.outputs.size() > 1;
    }
    
//    public Answer singleAnswer() {
//        return answerOfVariant(getOne(this.variants));
//    }
    
    @Override
    public boolean next() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
        if ( this.currentVariantIndex < this.outputs.size() - 1 ) {
            this.currentVariantIndex++;
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean isCurrentMuchBetterThanNext() {
        if ( this.currentVariantIndex < this.outputs.size() - 1 ) {
            if ( this.currentVariantIndex < 0 ) {
                throw new IllegalStateException(
                        "Unexpected behavior: call .next() before accessing variants!");
            }
            
            float currentWeight = this.current().weight();
            float nextWeight = this.outputs.get(this.currentVariantIndex + 1).weight();
            
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
            
        } else if ( this.currentVariantIndex == this.outputs.size() - 1 ) {
            return true;
        } else {
            throw new IllegalStateException("Unexpected behavior.");
        }
    }
    
    @Override
    public Output current() {
        return this.outputs.get(this.currentVariantIndex);
    }
    
    @Override
    public List<Output> nextSimilarSublist() {
        if ( nonNull(this.currentSimilarVariants) ) {
            this.currentSimilarVariants.clear();
        }
        else {
            this.currentSimilarVariants = new ArrayList<>();
        }

        boolean proceed = true;
        while ( this.currentVariantIndex < this.outputs.size() && proceed ) {
            if ( ! this.isCurrentMuchBetterThanNext() ) {
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
        for (int i = 0; i < this.outputs.size(); i++) {
            if ( this.outputs.get(i).input().equals(string) ) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(Output output) {
        return this.outputs.indexOf(output);
    }
}
