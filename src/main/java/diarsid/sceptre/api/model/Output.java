package diarsid.sceptre.api.model;

import java.util.Map;

import static java.util.Objects.nonNull;

public interface Output extends Weighted {

    public static enum AdditionalData {
        WORDS
    }

    Input input();

    int index();

    Map<AdditionalData, Object> additionalData();

    default boolean hasAdditionalData() {
        return nonNull(this.additionalData());
    }

}
