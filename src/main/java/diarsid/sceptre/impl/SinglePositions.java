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

    private final List<Integer> filledPositions;
    private final List<Integer> allPositions;
    private final List<Integer> uninterruptedPositions;
    private int added;
    private int missed;
    private Event lastEvent;

    SinglePositions() {
        this.filledPositions = new ArrayList<>();
        this.allPositions = new ArrayList<>();
        this.uninterruptedPositions = new ArrayList<>();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = Event.UNINIT;
    }

    int lastBetween(int startIncl, int endIncl) {
        int position;
        for ( int i = this.filledPositions.size()-1; i > -1 ; i-- ) {
            position = this.filledPositions.get(i);
            if ( position > startIncl && position < endIncl ) {
                return position;
            }
        }

        return -1;
    }

    boolean contains(int position) {
        return this.allPositions.contains(position);
    }

    void clear() {
        this.filledPositions.clear();
        this.allPositions.clear();
        this.uninterruptedPositions.clear();
        this.added = 0;
        this.missed = 0;
        this.lastEvent = Event.UNINIT;
    }

    void add(int position) {
        if ( this.lastEvent.equals(Event.ADDED) ) {
            if ( this.uninterruptedPositions.isEmpty() ) {
                this.uninterruptedPositions.add(lastFrom(this.allPositions));
            }
            this.uninterruptedPositions.add(position);
        }
        this.filledPositions.add(position);
        this.allPositions.add(position);
        this.added++;
        this.lastEvent = Event.ADDED;
    }

    void miss() {
        this.allPositions.add(MISS);
        this.missed++;
        this.lastEvent = Event.MISSED;
    }
    
    void end() {
        
    }

    List<Integer> filled() {
        return this.filledPositions;
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
