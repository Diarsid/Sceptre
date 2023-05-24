package diarsid.sceptre.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import diarsid.sceptre.api.Analyze;
import diarsid.sceptre.api.LogSink;
import diarsid.sceptre.api.LogType;
import diarsid.sceptre.api.util.LineByLineLogSink;
import diarsid.support.objects.Pools;

import static java.util.Objects.isNull;

public class AnalyzeBuilder implements Analyze.Builder {

    private Pools pools;
    private LogSink logSink;
    private boolean logEnabled;
    private Map<LogType, Boolean> enabledByLogType;

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
        this.logSink = new LineByLineLogSink(lineByLineLogConsume);
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

    public Pools pools() {
        return this.pools;
    }

    public LogSink logSink() {
        return this.logSink;
    }

    public boolean isLogEnabled() {
        return this.logEnabled;
    }

    public Map<LogType, Boolean> enabledByLogType() {
        return this.enabledByLogType;
    }
}
