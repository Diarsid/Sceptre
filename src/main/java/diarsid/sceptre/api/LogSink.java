package diarsid.sceptre.api;

import java.util.function.Consumer;

public interface LogSink extends Consumer<String> {

    default void begins() {
        // signal that analyze is being started
    }

    default void finished() {
        // signal that analyze is done and that will be no more logs
    }
}
