package diarsid.sceptre.api;

import java.util.List;
import java.util.Optional;

import diarsid.sceptre.api.model.Input;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Outputs;
import diarsid.support.model.versioning.Version;

public interface Analyze {

    Version version();

    Outputs processStrings(String pattern, List<String> strings);

    Outputs processInputs(String pattern, List<Input> inputs);

    Outputs processStrings(String pattern, String noWorseThan, List<String> strings);

    Outputs processInputs(String pattern, String noWorseThan, List<Input> inputs);

    List<Output> processStringsToList(String pattern, List<String> strings);

    List<Output> processStringsToList(String pattern, String noWorseThan, List<String> strings);

    List<Output> processInputsToList(String pattern, List<Input> variants);

    List<Output> processInputsToList(String pattern, String noWorseThan, List<Input> strings);

    Optional<Output> process(String pattern, Input input);

    float process(String pattern, String string);

}
