package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import diarsid.sceptre.api.Analyze;
import diarsid.sceptre.api.LogSink;
import diarsid.sceptre.api.LogType;
import diarsid.sceptre.api.impl.logsinks.LogSinkLineByLine;
import diarsid.sceptre.api.model.Output;
import diarsid.support.objects.Pools;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class AnalyzeBuilder implements Analyze.Builder {

    public Pools pools;
    public LogSink logSink;
    public boolean logEnabled;
    public Map<LogType, Boolean> enabledByLogType;
    public List<Output.AdditionalData> additionalData;

    public AnalyzeBuilder() {
    }

    public AnalyzeBuilder withPools(Pools pools) {
        this.pools = pools;
        return this;
    }

    @Override
    public AnalyzeBuilder withLogSink(LogSink logSink) {
        this.logSink = logSink;
        return this;
    }

    @Override
    public AnalyzeBuilder withLogSink(Consumer<String> lineByLineLogConsume) {
        this.logSink = new LogSinkLineByLine(lineByLineLogConsume);
        return this;
    }

    @Override
    public AnalyzeBuilder withLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
        return this;
    }

    @Override
    public AnalyzeBuilder withEnabledByLogType(Map<LogType, Boolean> enabledByLogType) {
        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = enabledByLogType;
        }
        else {
            this.enabledByLogType.putAll(enabledByLogType);
        }

        return this;
    }

    @Override
    public AnalyzeBuilder withLogTypeEnabled(LogType logType, boolean enabled) {
        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = new HashMap<>();
        }

        this.enabledByLogType.put(logType, enabled);

        return this;
    }

    @Override
    public Analyze.Builder withAdditionalDataInOutput(Output.AdditionalData additionalData) {
        if ( isNull(this.additionalData) ) {
            this.additionalData = new ArrayList<>();
        }

        this.additionalData.add(additionalData);

        return this;
    }

    @Override
    public Analyze build() {
        if ( isNull(this.pools) ) {
            this.pools = Pools.pools();
        }

        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = new HashMap<>();
        }

        for ( LogType logType : LogType.values() ) {
            if ( ! this.enabledByLogType.containsKey(logType) ) {
                this.enabledByLogType.put(logType, false);
            }
        }

        if ( isNull(this.logSink) ) {
            if ( this.logEnabled ) {
                this.logSink = System.out::println;
            }
        }

        return new AnalyzeImpl(this);
    }

    public boolean isDeclaringAdditionalData() {
        return nonNull(this.additionalData);
    }
}
