package diarsid.sceptre.impl;

import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;

class SinglePositions {
    
    private static enum Event {
        ADDED,
        MISSED,
        UNINIT
    }
    
    private final static int MISS = -3;

    private final ListInt filledPositions;
    private final ListInt allPositions;
    private final ListInt uninterruptedPositions;
    private int added;
    private int missed;
    private Event lastEvent;

    SinglePositions() {
        this.filledPositions = new ListIntImpl();
        this.allPositions = new ListIntImpl();
        this.uninterruptedPositions = new ListIntImpl();
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
                this.uninterruptedPositions.add(this.allPositions.get(this.allPositions.size()-1));
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

    ListInt filled() {
        return this.filledPositions;
    }
    
    boolean doHaveUninterruptedRow() {
        return ! this.uninterruptedPositions.isEmpty();
    }
    
    ListInt uninterruptedRow() {
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
