package diarsid.sceptre.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import diarsid.sceptre.api.model.Input;
import diarsid.sceptre.api.model.Output;
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

        Analyze.Builder withAdditionalDataInOutput(Output.AdditionalData additionalData);

        Analyze build();
    }

    Version version();

    List<Output> processStrings(String pattern, List<String> strings);

    List<Output> processStrings(String pattern, String noWorseThan, List<String> strings);

    List<Output> processInputs(String pattern, List<Input> variants);

    List<Output> processInputs(String pattern, String noWorseThan, List<Input> inputs);

    Optional<Output> process(String pattern, Input input);

    float process(String pattern, String string);

}
