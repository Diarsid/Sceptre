/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.sceptre.impl;

/**
 *
 * @author Diarsid
 */
public interface PositionIterableView extends PositionView {
    
    boolean hasNext();
    
    void goToNext();
    
    boolean isFilled();
    
    boolean isNotFilled();
}
