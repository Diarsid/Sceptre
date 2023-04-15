package diarsid.sceptre.impl.collections.impl;

public class PossibleInt {

    public static interface FunctionInt<T> {

        T map(int i);
    }

    private int i;

    public PossibleInt() {
        this.i = Integer.MIN_VALUE;
    }

    public boolean isPresent() {
        return this.i != Integer.MIN_VALUE;
    }

    public int resetTo(int c) {
        int old = this.i;
        this.i = c;
        return old;
    }

    public int resetTo(PossibleInt other) {
        int old = this.i;
        this.i = other.i;
        return old;
    }

    public int orThrow() {
        if ( this.isPresent() ) {
            return this.i;
        }

        throw new IllegalStateException();
    }

    public <T> T mapValueOr(FunctionInt<T> function, T t) {
        if ( this.isPresent() ) {
            return function.map(this.i);
        }
        else {
            return t;
        }
    }

    public int nullify() {
        int old = this.i;
        this.i = Integer.MIN_VALUE;
        return old;
    }
}
