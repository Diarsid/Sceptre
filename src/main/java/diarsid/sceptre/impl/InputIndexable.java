package diarsid.sceptre.impl;

public class InputIndexable extends Indexable {

    protected final String string;
    protected final Object metadata;

    public InputIndexable(String string) {
        this.string = string;
        this.metadata = null;
    }

    public InputIndexable(String string, Object metadata) {
        this.string = string;
        this.metadata = metadata;
    }

    public String string() {
        return this.string;
    }

    public Object metadata() {
        return this.metadata;
    }

}
