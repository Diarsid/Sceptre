package diarsid.sceptre.api.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.sceptre.api.LogSink;
import diarsid.support.strings.MultilineMessage;

public class MultilineMessageLogSink implements LogSink {

    private final ThreadLocal<MultilineMessage> multilineMessagePerThread;
    private final Supplier<MultilineMessage> newMessageCreator;
    private final Consumer<MultilineMessage> messageConsumer;

    public MultilineMessageLogSink() {
        this("[ANALYZE]");
    }

    public MultilineMessageLogSink(String linePrefix) {
        this.multilineMessagePerThread = new ThreadLocal<>();

        this.newMessageCreator = () -> {
            return new MultilineMessage(linePrefix);
        };

        this.messageConsumer = (message) -> {
            String logLines = message.compose();
            System.out.println(logLines);
        };
    }

    public MultilineMessageLogSink(String linePrefix, Consumer<String> logLinesConsumer) {
        this.multilineMessagePerThread = new ThreadLocal<>();

        this.newMessageCreator = () -> {
            return new MultilineMessage(linePrefix);
        };

        this.messageConsumer = (message) -> {
            String logLines = message.compose();
            logLinesConsumer.accept(logLines);
        };
    }

    public MultilineMessageLogSink(
            Supplier<MultilineMessage> newMessageCreator,
            Consumer<MultilineMessage> messageConsumer) {
        this.multilineMessagePerThread = new ThreadLocal<>();
        this.newMessageCreator = newMessageCreator;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void accept(String s) {
        multilineMessagePerThread.get().newLine().add(s);
    }

    @Override
    public void begins() {
        multilineMessagePerThread.set(newMessageCreator.get());
    }

    @Override
    public void finished() {
        MultilineMessage message = multilineMessagePerThread.get();
        multilineMessagePerThread.remove();
        messageConsumer.accept(message);
    }
}
