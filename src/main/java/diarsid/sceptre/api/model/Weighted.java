package diarsid.sceptre.api.model;

import diarsid.sceptre.api.WeightEstimate;

public interface Weighted extends Comparable<Weighted> {

    float weight();

    boolean isBetterThan(Weighted other);

    default WeightEstimate estimate() {
        return WeightEstimate.of(this.weight());
    }

}
