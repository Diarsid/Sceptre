package diarsid.sceptre.api.model;

import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

public interface Variants extends Serializable {

    IntStream indexes();

    void resetTraversing();

    void setTraversingToPositionBefore(int variantIndex);

    int currentTraverseIndex();

    boolean isEmpty();

    boolean isNotEmpty();

    Variant best();

    String stamp();

    void removeHavingSameStartAs(Variant variant);

    Variants removeWorseThan(String variantValue);

    String getVariantAt(int i);

    int size();

    boolean isChoiceInSimilarVariantsNaturalRange(int number);

    boolean hasOne();

    boolean hasMany();

    boolean next();

    boolean isCurrentMuchBetterThanNext();

    Variant current();

    List<Variant> nextSimilarVariants();

    int indexOf(String string);

    int indexOf(Variant variant);
}
