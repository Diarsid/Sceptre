package diarsid.sceptre.api.model;

public interface Weighted extends Comparable<Weighted> {

    float weight();

    boolean isBetterThan(Weighted other);

}
