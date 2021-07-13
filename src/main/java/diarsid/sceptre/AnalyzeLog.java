/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre;

import java.util.function.Consumer;

/**
 *
 * @author Diarsid
 */
class AnalyzeLog {
    
    private final Consumer<String> logger;

    AnalyzeLog(Consumer<String> logger) {
        this.logger = logger;
    }
    
    
}
