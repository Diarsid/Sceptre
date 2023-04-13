package diarsid.sceptre.impl.collections;

public interface SetInt extends Ints {

    boolean add(int element);

    boolean remove(int element);

    int lesserThan(int element);

    int lesserThanOrEqual(int element);

    int greaterThan(int element);

    int greaterThanOrEqual(int element);

    int first();

    int last();
}
