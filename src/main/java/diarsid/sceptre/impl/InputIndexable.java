package diarsid.sceptre.impl;

public class InputIndexable extends Indexable {

    protected final String variant;
    protected final Object metadata;

    public InputIndexable(String variant) {
        this.variant = variant;
        this.metadata = null;
    }

    public InputIndexable(String variant, Object metadata) {
        this.variant = variant;
        this.metadata = metadata;
    }

    public String string() {
        return this.variant;
    }

    public Object metadata() {
        return this.metadata;
    }

}
