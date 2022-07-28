package diarsid.sceptre.impl.logs;

import java.util.function.Consumer;

class AnalyzeLog {
    
    private final Consumer<String> logger;

    AnalyzeLog(Consumer<String> logger) {
        this.logger = logger;
    }
    
    
}
