package diarsid.sceptre.impl;

import java.util.Map;

import diarsid.sceptre.api.model.Input;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Weighted;

public class OutputImpl extends Indexable implements Output {

    private static final int INDEX_NOT_SET = -1;

    private final Input input;
    private final float weight;
    private final Map<AdditionalData, Object> additionalData;

    public OutputImpl(Input input, int index, float weight) {
        this.input = input;
        this.index = index;
        this.weight = weight;
        this.additionalData = null;
    }

    public OutputImpl(Input input, float weight) {
        this.input = input;
        this.index = INDEX_NOT_SET;
        this.weight = weight;
        this.additionalData = null;
    }

    public OutputImpl(Input input, float weight, Map<AdditionalData, Object> additionalData) {
        this.input = input;
        this.index = INDEX_NOT_SET;
        this.weight = weight;
        this.additionalData = additionalData;
    }

    @Override
    public Input input() {
        return this.input;
    }

    @Override
    public int index() {
        return this.index;
    }

    @Override
    void setIndex(int index) {
        this.index = index;
    }

    @Override
    public float weight() {
        return this.weight;
    }

    @Override
    public boolean isBetterThan(Weighted other) {
        return this.weight < other.weight();
    }

    @Override
    public Map<AdditionalData, Object> additionalData() {
        return this.additionalData;
    }

    @Override
    public int compareTo(Weighted other) {
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
