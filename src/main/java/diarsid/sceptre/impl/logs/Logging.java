package diarsid.sceptre.impl.logs;

import java.util.Map;
import java.util.function.Consumer;

import diarsid.sceptre.api.AnalyzeBuilder;
import diarsid.sceptre.api.LogType;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Logging {

    private final Map<LogType, Boolean> enabledByLogType;
    private final Consumer<String> logger;
    private final boolean enabled;

    public Logging(AnalyzeBuilder builder) {
        this.logger = builder.getLogDelegate();
        this.enabledByLogType = builder.getEnabledByLogType();
        this.enabled = builder.isLogEnabled();
    }

    public Logging(Consumer<String> logger, Map<LogType, Boolean> enabledByLogType) {
        this.logger = logger;
        this.enabledByLogType = enabledByLogType;
        this.enabled = true;
    }

    public boolean isEnabled(LogType type) {
        if ( ! enabled ) {
            return false;
        }

        Boolean typeEnabled = this.enabledByLogType.get(type);

        return nonNull(typeEnabled) && typeEnabled;
    }

    public boolean isNotEnabled(LogType type) {
        if ( ! enabled ) {
            return true;
        }

        Boolean typeEnabled = this.enabledByLogType.get(type);

        return isNull(typeEnabled) || ( ! typeEnabled );
    }

    public void add(LogType type, String s) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(s);
    }

    public void add(LogType type, String s, Object arg0) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0));
    }

    public void add(LogType type, String s, Object arg0, Object arg1) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0, arg1));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0, arg1, arg2));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0, arg1, arg2, arg3));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0, arg1, arg2, arg3, arg4));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, arg0, arg1, arg2, arg3, arg4, arg5));
    }

    public void add(LogType type, String s, Object... args) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logger.accept(format(s, args));
    }
}
