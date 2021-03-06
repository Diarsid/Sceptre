package diarsid.sceptre;

import static diarsid.support.configuration.Configuration.actualConfiguration;

enum AnalyzeLogType {
    
    BASE (
            actualConfiguration().asBoolean("log") &&
                    actualConfiguration().asBoolean("analyze.weight.base.log")),
    POSITIONS_SEARCH (
            BASE.isEnabled && actualConfiguration().asBoolean("analyze.weight.positions.search.log")),
    POSITIONS_CLUSTERS (
            BASE.isEnabled && actualConfiguration().asBoolean("analyze.weight.positions.clusters.log"));
    
    private final boolean isEnabled;
    
    private AnalyzeLogType(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    boolean isEnabled() {
        return this.isEnabled;
    }
    
    boolean isDisabled() {
        return ! this.isEnabled;
    }
}
