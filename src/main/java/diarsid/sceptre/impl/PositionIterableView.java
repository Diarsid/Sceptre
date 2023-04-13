package diarsid.sceptre.impl;

public interface PositionIterableView extends PositionView {

    char character();

    String match();
    
    boolean hasNext();
    
    void goToNext();
    
    boolean isFilled();
    
    boolean isNotFilled();
}
