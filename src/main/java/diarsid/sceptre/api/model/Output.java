package diarsid.sceptre.api.model;

import static java.util.Objects.nonNull;

public interface Output extends Weighted {

    String input();

    int index();

    int originalIndex();

    Object metadata();

    default boolean hasMetadata() {
        return nonNull(this.metadata());
    }
}
