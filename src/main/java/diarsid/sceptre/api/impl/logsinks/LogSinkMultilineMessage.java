package diarsid.sceptre.api.impl.logsinks;

import java.util.function.Consumer;
import java.util.function.Supplier;

import diarsid.sceptre.api.LogSink;
import diarsid.support.strings.MultilineMessage;

public class LogSinkMultilineMessage implements LogSink {

    private final ThreadLocal<MultilineMessage> multilineMessagePerThread;
    private final Supplier<MultilineMessage> newMessageCreator;
    private final Consumer<MultilineMessage> messageConsumer;

    public LogSinkMultilineMessage() {
        this("[ANALYZE]");
    }

    public LogSinkMultilineMessage(String linePrefix) {
        this.multilineMessagePerThread = new ThreadLocal<>();

        this.newMessageCreator = () -> {
            return new MultilineMessage(linePrefix);
        };

        this.messageConsumer = (message) -> {
            String logLines = message.compose();
            System.out.println(logLines);
        };
    }

    public LogSinkMultilineMessage(String linePrefix, Consumer<String> logLinesConsumer) {
        this.multilineMessagePerThread = new ThreadLocal<>();

        this.newMessageCreator = () -> {
            return new MultilineMessage(linePrefix);
        };

        this.messageConsumer = (message) -> {
            String logLines = message.compose();
            logLinesConsumer.accept(logLines);
        };
    }

    public LogSinkMultilineMessage(
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
