package diarsid.sceptre.api.util;

import java.util.function.Consumer;

import diarsid.sceptre.api.LogSink;

public class LineByLineLogSink implements LogSink {

    private final Consumer<String> lineConsumer;

    public LineByLineLogSink(Consumer<String> lineConsumer) {
        this.lineConsumer = lineConsumer;
    }

    @Override
    public void accept(String s) {
        this.lineConsumer.accept(s);
    }
}
