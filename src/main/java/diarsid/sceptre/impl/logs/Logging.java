package diarsid.sceptre.impl.logs;

import java.util.Map;

import diarsid.sceptre.impl.AnalyzeBuilder;
import diarsid.sceptre.api.LogSink;
import diarsid.sceptre.api.LogType;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Logging {

    private final Map<LogType, Boolean> enabledByLogType;
    private final LogSink logSink;
    private final boolean enabled;

    public Logging(AnalyzeBuilder builder) {
        this.logSink = builder.logSink();
        this.enabledByLogType = builder.enabledByLogType();
        this.enabled = builder.isLogEnabled() && nonNull(this.logSink);
    }

    public void begins() {
        if ( this.enabled ) {
            this.logSink.begins();
        }
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

        this.logSink.accept(s);
    }

    public void add(LogType type, String s, Object arg0) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0));
    }

    public void add(LogType type, String s, Object arg0, Object arg1) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0, arg1));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0, arg1, arg2));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0, arg1, arg2, arg3));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0, arg1, arg2, arg3, arg4));
    }

    public void add(LogType type, String s, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, arg0, arg1, arg2, arg3, arg4, arg5));
    }

    public void add(LogType type, String s, Object... args) {
        if ( isNotEnabled(type) ) {
            return;
        }

        this.logSink.accept(format(s, args));
    }

    public void finished() {
        if ( this.enabled ) {
            this.logSink.finished();
        }
    }
}
