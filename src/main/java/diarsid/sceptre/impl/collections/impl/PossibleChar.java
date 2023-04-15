package diarsid.sceptre.impl.collections.impl;

public class PossibleChar {

    public static interface FunctionChar<T> {

        T map(char c);
    }

    public static final char EMPTY_CHAR = '#';

    private char c;

    public PossibleChar() {
        this.c = EMPTY_CHAR;
    }

    public boolean isPresent() {
        return this.c != EMPTY_CHAR;
    }

    public char resetTo(char c) {
        char old = this.c;
        this.c = c;
        return old;
    }

    public char resetTo(PossibleChar other) {
        char old = this.c;
        this.c = other.c;
        return old;
    }

    public char orThrow() {
        if ( this.isPresent() ) {
            return this.c;
        }

        throw new IllegalStateException();
    }

    public <T> T mapValueOr(FunctionChar<T> function, T t) {
        if ( this.isPresent() ) {
            return function.map(this.c);
        }
        else {
            return t;
        }
    }

    public char nullify() {
        char old = this.c;
        this.c = EMPTY_CHAR;
        return old;
    }
}
