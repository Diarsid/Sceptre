package diarsid.sceptre.impl;

public abstract class Indexable {

    private static final int INDEX_NOT_SET = -1;

    protected int index;

    public Indexable() {
        this.index = INDEX_NOT_SET;
    }

    int index() {
        return this.index;
    }

    void setIndex(int index) {
        this.index = index;
    }
}
