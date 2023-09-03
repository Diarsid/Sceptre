package diarsid.sceptre.api.impl.logsinks;

import java.util.function.Consumer;

import diarsid.sceptre.api.LogSink;

public class LogSinkLineByLine implements LogSink {

    private final Consumer<String> lineConsumer;

    public LogSinkLineByLine(Consumer<String> lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    @Override
    public void accept(String s) {
        this.lineConsumer.accept(s);
    }
}
