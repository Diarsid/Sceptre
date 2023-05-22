package diarsid.sceptre.api.model;

import diarsid.sceptre.impl.InputIndexable;

public class Input extends InputIndexable {

    public Input(String variant) {
        super(variant);
    }

    public Input(String variant, Object metadata) {
        super(variant, metadata);
    }

    @Override
    public String string() {
        return super.variant;
    }

    @Override
    public Object metadata() {
        return super.metadata;
    }
}
