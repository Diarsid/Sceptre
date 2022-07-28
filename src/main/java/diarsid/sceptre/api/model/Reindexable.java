package diarsid.sceptre.api.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface Reindexable extends Indexable {
    
    void setIndex(int index);
    
    static void reindex(List<? extends Reindexable> reindexables) {
        AtomicInteger index = new AtomicInteger(0);
        reindexables
                .stream()
                .sorted()
                .forEach(reindexable -> reindexable.setIndex(index.getAndIncrement()));
    }
}
