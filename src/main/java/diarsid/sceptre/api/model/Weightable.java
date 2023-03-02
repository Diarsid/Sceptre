package diarsid.sceptre.api.model;

public interface Weightable extends Comparable<Weightable> {

    float weight();

    boolean isBetterThan(Weightable other);

}
