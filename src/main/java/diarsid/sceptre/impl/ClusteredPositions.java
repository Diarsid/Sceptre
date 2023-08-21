package diarsid.sceptre.impl;

import diarsid.sceptre.impl.collections.ListInt;
import diarsid.sceptre.impl.collections.impl.ListIntImpl;
import diarsid.support.objects.StatefulClearable;

import static diarsid.sceptre.api.LogType.POSITIONS_CLUSTERS;
import static diarsid.sceptre.impl.collections.impl.Sort.STRAIGHT;

public class ClusteredPositions implements StatefulClearable {

    private final AnalyzeUnit data;
    private int adjustmentsSum;
    private final ListInt clustered;

    public ClusteredPositions(AnalyzeUnit data) {
        this.data = data;
        this.clustered = new ListIntImpl();
    }

    public int count() {
        if ( this.adjustmentsSum == 0 ) {
            return this.clustered.size();
        }
        else {
            return this.clustered.size() + this.adjustmentsSum;
        }
    }

    public void removeCluster(int clusterFirstPosition, int clusterLength) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] remove cluster %s -> %s ",
                clusterFirstPosition,
                clusterLength);

        int position;
        int countToRemove = clusterLength;
        for ( int i = 0; i < this.clustered.size(); i++ ) {
            if ( countToRemove == 0 ) {
                break;
            }

            position = this.clustered.get(i);
            if ( position >= clusterFirstPosition ) {
                this.clustered.remove(i);
                countToRemove--;
                i--;
            }
        }
    }

    public void removePosition(int position) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] remove %s", position);
        this.clustered.removeElement(position);
    }

    public void removePositions(ListInt positions) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] remove all %s", positions);
        this.clustered.removeAll(positions);
    }

    public void add(int position) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] add '%s' %s",
                this.data.variant.charAt(position),
                position);
        this.clustered.add(position);
        this.clustered.sort(STRAIGHT);
    }

    public void adjustTo(int size) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] adjust size");
        int sizeDiff = this.clustered.size() - size;
        this.adjustmentsSum = this.adjustmentsSum - sizeDiff;
    }

    public void teardown(int teardown) {
        this.data.log.add(POSITIONS_CLUSTERS, "    [clustered] teardown on %s", teardown);
        this.adjustmentsSum = this.adjustmentsSum - teardown;
    }

    @Override
    public void clear() {
        this.clustered.clear();
        this.adjustmentsSum = 0;
    }
}
