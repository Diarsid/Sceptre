package diarsid.sceptre.api.model;

import java.io.Serializable;
import java.util.List;

public interface Outputs extends Serializable {

    void resetTraversing();

    void setTraversingToPositionBefore(int variantIndex);

    int currentTraverseIndex();

    boolean isEmpty();

    boolean isNotEmpty();

    Output best();

    Outputs removeHavingStart(String start);

    Outputs removeWorseThan(String variant);

    Output get(int i);

    int size();

    boolean isChoiceInSimilarVariantsNaturalRange(int number);

    boolean hasOne();

    boolean hasMany();

    boolean next();

    boolean isCurrentMuchBetterThanNext();

    Output current();

    List<Output> nextSimilarSublist();

    int indexOf(String string);

    int indexOf(Output output);
}
