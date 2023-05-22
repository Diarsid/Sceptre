package diarsid.sceptre.impl;

import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Weighted;

public class RealOutput extends Indexable implements Output {

    private static final int INDEX_NOT_SET = -1;

    private final String input;
    private final float weight;
    private final int originalIndex;
    private final Object metadata;

    public RealOutput(String input, int index, int originalIndex, float weight, Object metadata) {
        this.input = input;
        this.index = index;
        this.originalIndex = originalIndex;
        this.weight = weight;
        this.metadata = metadata;
    }

    public RealOutput(InputIndexable input, int index, float weight) {
        this.input = input.string();
        this.index = index;
        this.originalIndex = input.index();
        this.weight = weight;
        this.metadata = input.metadata();
    }

    public RealOutput(InputIndexable input, float weight) {
        this.input = input.string();
        this.index = INDEX_NOT_SET;
        this.originalIndex = input.index();
        this.weight = weight;
        this.metadata = input.metadata();
    }

    @Override
    public String input() {
        return this.input;
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
    public int originalIndex() {
        return this.originalIndex;
    }

    @Override
    public Object metadata() {
        return this.metadata;
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
