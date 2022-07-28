package diarsid.sceptre.impl;

import java.util.ArrayList;
import java.util.List;

import static diarsid.support.objects.collections.Lists.lastFrom;

class SinglePositions {
    
    private static enum Event {
        ADDED,
        MISSED,
        UNINIT
    }
    
    private final static Integer MISS = -3;
    
    private final List<Integer> positions;
    private final List<Integer> uninterruptedPositions;
    private int added;
    private int missed;
    private Event lastEvent;

    SinglePositions() {
        this.positions = new ArrayList<>();
        this.uninterruptedPositions = new ArrayList<>();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = Event.UNINIT;
    }

    void clear() {
        this.positions.clear();
        this.uninterruptedPositions.clear();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = Event.UNINIT;
    }

    void add(int position) {
        if ( this.lastEvent.equals(Event.ADDED) ) {
            if ( this.uninterruptedPositions.isEmpty() ) {
                this.uninterruptedPositions.add(lastFrom(this.positions));
            }
            this.uninterruptedPositions.add(position);
        }
        this.positions.add(position);
        this.added++;
        this.lastEvent = Event.ADDED;
    }

    void miss() {
        this.positions.add(MISS);
        this.missed++;
        this.lastEvent = Event.MISSED;
    }
    
    void end() {
        
    }
    
    boolean doHaveUninterruptedRow() {
        return ! this.uninterruptedPositions.isEmpty();
    }
    
    List<Integer> uninterruptedRow() {
        return this.uninterruptedPositions;
    }

//    private void tryToProcessUninterruptedPositions() {
//        if ( this.lastEvent.equals(SinglePositions.Event.ADDED) ) {
//            if ( nonEmpty(this.uninterruptedPositions) ) {
//                this.uninterruptedPositionsProcessor.accept(this.uninterruptedPositions);
//                this.uninterruptedPositions.clear();
//            }
//        }
//    }
    
    int quantity() {
        return this.added;
    }
}
