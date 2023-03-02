package diarsid.sceptre.impl;

public interface PositionIterableView extends PositionView {
    
    boolean hasNext();
    
    void goToNext();
    
    boolean isFilled();
    
    boolean isNotFilled();
}
