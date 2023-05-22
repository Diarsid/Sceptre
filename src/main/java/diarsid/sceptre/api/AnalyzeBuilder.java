package diarsid.sceptre.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import diarsid.sceptre.impl.AnalyzeImpl;
import diarsid.support.objects.Pools;

import static java.util.Objects.isNull;

import static diarsid.support.objects.Pools.pools;

public class AnalyzeBuilder {

    private Pools pools;
    private Consumer<String> logDelegate;
    private boolean logEnabled;
    private Map<LogType, Boolean> enabledByLogType;

    public AnalyzeBuilder() {
    }

    public AnalyzeBuilder withPools(Pools pools) {
        this.pools = pools;
        return this;
    }

    public AnalyzeBuilder withLogDelegate(Consumer<String> logDelegate) {
        this.logDelegate = logDelegate;
        return this;
    }

    public AnalyzeBuilder withLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
        return this;
    }

    public AnalyzeBuilder withEnabledByLogType(Map<LogType, Boolean> enabledByLogType) {
        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = enabledByLogType;
        }
        else {
            this.enabledByLogType.putAll(enabledByLogType);
        }

        return this;
    }

    public AnalyzeBuilder withLogTypeEnabled(LogType logType, boolean enabled) {
        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = new HashMap<>();
        }

        this.enabledByLogType.put(logType, enabled);

        return this;
    }

    public Analyze build() {
        if ( isNull(this.pools) ) {
            this.pools = pools();
        }

        if ( isNull(this.logDelegate) ) {
            this.logDelegate = System.out::println;
        }

        if ( isNull(this.enabledByLogType) ) {
            this.enabledByLogType = new HashMap<>();
        }

        for ( LogType logType : LogType.values() ) {
            if ( ! this.enabledByLogType.containsKey(logType) ) {
                this.enabledByLogType.put(logType, false);
            }
        }

        return new AnalyzeImpl(this);
    }

    public Pools getPools() {
        return this.pools;
    }

    public Consumer<String> getLogDelegate() {
        return this.logDelegate;
    }

    public boolean isLogEnabled() {
        return this.logEnabled;
    }

    public Map<LogType, Boolean> getEnabledByLogType() {
        return this.enabledByLogType;
    }
}
