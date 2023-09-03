package diarsid.sceptre.api.model;

import java.io.Serializable;
import java.util.List;

import diarsid.sceptre.impl.OutputsImpl;

public interface Outputs extends Serializable {

    public interface Iteration {

        boolean next();

        Output current();

        int currentIndex();

        boolean isCurrentMuchBetterThanNext();

        void toPositionBefore(int outputIndex);

        List<Output> nextSimilarSublist();

        void reset();

    }

    public static Outputs of(List<Output> outputs) {
        return new OutputsImpl(outputs);
    }

    boolean isEmpty();

    boolean isNotEmpty();

    boolean hasOne();

    boolean hasMany();

    int size();

    int indexOf(String string);

    int indexOf(Output output);

    Output best();

    Output get(int i);

    Outputs removeHavingStart(String start);

    Outputs removeWorseThan(String input);

    boolean isChoiceInSimilarVariantsNaturalRange(int number);

    Outputs.Iteration iteration();

    List<Output> all();
}
