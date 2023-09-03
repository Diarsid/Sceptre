package diarsid.sceptre.api.model;

import diarsid.sceptre.impl.InputIndexable;

import static java.util.Objects.nonNull;

public class Input extends InputIndexable {

    public Input(String variant) {
        super(variant);
    }

    public Input(String variant, Object metadata) {
        super(variant, metadata);
    }

    @Override
    public String string() {
        return super.string;
    }

    @Override
    public Object metadata() {
        return super.metadata;
    }

    public boolean hasMetadata() {
        return nonNull(this.metadata());
    }
}
