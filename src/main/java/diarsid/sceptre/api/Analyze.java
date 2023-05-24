package diarsid.sceptre.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import diarsid.sceptre.api.model.Input;
import diarsid.sceptre.api.model.Output;
import diarsid.sceptre.api.model.Outputs;
import diarsid.sceptre.impl.AnalyzeBuilder;
import diarsid.support.model.versioning.Version;
import diarsid.support.objects.Pools;

public interface Analyze {

    interface Builder {

        public static Analyze.Builder newInstance() {
            return new AnalyzeBuilder();
        }

        Analyze.Builder withPools(Pools pools);

        Analyze.Builder withLogSink(LogSink logSink);

        Analyze.Builder withLogSink(Consumer<String> lineByLineLogSink);

        Analyze.Builder withLogEnabled(boolean logEnabled);

        Analyze.Builder withEnabledByLogType(Map<LogType, Boolean> enabledByLogType);

        Analyze.Builder withLogTypeEnabled(LogType logType, boolean enabled);

        Analyze build();
    }

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
