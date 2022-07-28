package diarsid.sceptre.impl.weight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.Float.MIN_VALUE;
import static java.util.Arrays.fill;

import static diarsid.sceptre.impl.logs.AnalyzeLogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.impl.WeightAnalyzeReal.logAnalyze;
import static diarsid.sceptre.impl.weight.WeightElement.WeightCalculationType.ADD_VALUE_TO_SUM;
import static diarsid.sceptre.impl.weight.WeightElement.WeightCalculationType.APPLY_PERCENT_TO_SUM;
import static diarsid.sceptre.impl.weight.WeightElement.WeightType.CALCULATED;
import static diarsid.sceptre.impl.weight.WeightElement.WeightType.PREDEFINED;

public class Weight {
    
    private final static int SIZE = 128;
    final static float WEIGHT_UNINITIALIZED = MIN_VALUE;
    
    private final List<WeightElement> elements;
    private final float[] weights;
    private float weightSum;
    private int nextFreeWeightIndex;

    public Weight() {
        this.elements = new ArrayList<>();
        this.weights = new float[SIZE];
        this.nextFreeWeightIndex = 0;
    }
    
    public void add(WeightElement weightElement) {
        weightElement.weightTypeMustBe(PREDEFINED);
        weightElement.weightCalculationTypeMustBe(ADD_VALUE_TO_SUM);
        this.addWeightAndElement(weightElement.predefinedWeight(), weightElement);
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "               [weight] %1$+.2f : %2$s", 
                weightElement.predefinedWeight(), weightElement.description());
    }
    
    public void add(float calculatedWeight, WeightElement element) {
        if ( calculatedWeight == 0.0 ) {
            return;
        }
        element.weightTypeMustBe(CALCULATED);
        element.weightCalculationTypeMustBe(ADD_VALUE_TO_SUM);
        this.addWeightAndElement(calculatedWeight, element);
        logAnalyze(
                POSITIONS_CLUSTERS, 
                "               [weight] %1$+.2f : %2$s", calculatedWeight, element.description());
    }
    
    public void applyPercent(int percent, WeightElement element) {
        element.weightTypeMustBe(CALCULATED);
        element.weightCalculationTypeMustBe(APPLY_PERCENT_TO_SUM);
        this.elements.add(element);
        this.weights[this.nextFreeWeightIndex] = percent;
        this.weightSum = this.weightSum * percent / 100;
        this.nextFreeWeightIndex++;
    }

    private void addWeightAndElement(float calculatedWeight, WeightElement element) {
        this.elements.add(element);
        this.weights[this.nextFreeWeightIndex] = calculatedWeight;
        this.weightSum = this.weightSum + calculatedWeight;
        this.nextFreeWeightIndex++;
    }
    
    public void add(Weight other) {
        for (int i = 0; i < other.nextFreeWeightIndex; i++) {
            this.addWeightAndElement(other.weights[i], other.elements.get(i));
        }
    }
    
    public boolean contains(WeightElement weightElement) {
        return this.elements.contains(weightElement);
    }
    
    public void clear() {
        this.elements.clear();
        fill(this.weights, WEIGHT_UNINITIALIZED);
        this.weightSum = 0;
        this.nextFreeWeightIndex = 0;
    }
    
    public float sum() {
        return this.weightSum;
    }
    
    int length() {
        return this.nextFreeWeightIndex;
    }
    
    public void observeAll(WeightConsumer weightConsumer) {
        for (int i = 0; i < this.nextFreeWeightIndex; i++) {
            weightConsumer.accept(i, this.weights[i], this.elements.get(i));
        }
    }
    
    void exclude(WeightElement element) {
        int i = this.elements.indexOf(element);
        if ( i > -1 ) {
            excludeByIndex(i);
        }
    }

    private void excludeByIndex(int i) {
        float excludedWeight = this.weights[i];
        this.weights[i] = WEIGHT_UNINITIALIZED;
        this.weightSum = this.weightSum - excludedWeight;
    }
    
    public void excludeIfAllPresent(WeightElement element1, WeightElement element2) {
        int i1 = this.elements.indexOf(element1);
        if ( i1 < 0 ) {
            return;
        } 
        
        int i2 = this.elements.indexOf(element2);
        if ( i2 < 0 ) {
            return;
        } 
        
        excludeByIndex(i1);
        excludeByIndex(i2);
    }

    @Override
    public String toString() {
        return String.valueOf(this.weightSum);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.elements);
        hash = 71 * hash + Arrays.hashCode(this.weights);
        hash =
                71 * hash + ( int ) (Float.floatToIntBits(this.weightSum) ^ (Float.floatToIntBits(this.weightSum) >>> 32));
        hash = 71 * hash + this.nextFreeWeightIndex;
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
        final Weight other = ( Weight ) obj;
        if ( Float.floatToIntBits(this.weightSum) != Float.floatToIntBits(other.weightSum) ) {
            return false;
        }
        if ( this.nextFreeWeightIndex != other.nextFreeWeightIndex ) {
            return false;
        }
        if ( !Objects.equals(this.elements, other.elements) ) {
            return false;
        }
        if ( !Arrays.equals(this.weights, other.weights) ) {
            return false;
        }
        return true;
    }    
    
}
